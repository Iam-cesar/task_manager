package com.example.task_manager.infra;

import com.example.task_manager.exceptions.IdNotFoundException;
import com.example.task_manager.exceptions.InvalidTaskTransitionException;
import com.example.task_manager.exceptions.LabelNotFoundException;
import com.example.task_manager.exceptions.ProjectAlreadyExistsException;
import com.example.task_manager.exceptions.ProjectNotFoundException;
import com.example.task_manager.exceptions.TaskDueDateInPastException;
import com.example.task_manager.exceptions.TaskNotEditableException;
import com.example.task_manager.exceptions.TaskNotFoundException;
import com.example.task_manager.exceptions.UserAlreadyExistsException;
import com.example.task_manager.exceptions.UserHasTasksException;
import com.example.task_manager.exceptions.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler({
        UserNotFoundException.class,
        ProjectNotFoundException.class,
        LabelNotFoundException.class,
        TaskNotFoundException.class,
        IdNotFoundException.class
    })
    private @NonNull ResponseEntity<RestErrorMessage> handleNotFound(@NonNull RuntimeException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({
        UserAlreadyExistsException.class,
        ProjectAlreadyExistsException.class,
        UserHasTasksException.class,
        TaskNotEditableException.class,
        InvalidTaskTransitionException.class,
        DataIntegrityViolationException.class
    })
    private @NonNull ResponseEntity<RestErrorMessage> handleConflict(RuntimeException exception) {
        String message = exception instanceof DataIntegrityViolationException
            ? "Violação de integridade de dados"
            : exception.getMessage();
        return error(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private @NonNull ResponseEntity<RestErrorMessage> handleIllegalArgument(@NonNull IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(TaskDueDateInPastException.class)
    private @NonNull ResponseEntity<RestErrorMessage> handlePastDueDate(
        @NonNull TaskDueDateInPastException exception
    ) {
        return error(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            List.of(new RestErrorMessage.FieldError("due_date", exception.getMessage()))
        );
    }

    @ExceptionHandler(Exception.class)
    private @NonNull ResponseEntity<RestErrorMessage> handleUnexpected(Exception exception) {
        logger.error("Unexpected API error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    private @NonNull ResponseEntity<RestErrorMessage> handleConstraintViolation(@NonNull ConstraintViolationException exception) {
        var errors = exception.getConstraintViolations().stream()
            .map(violation -> new RestErrorMessage.FieldError(
                violation.getPropertyPath().toString(),
                violation.getMessage()
            ))
            .sorted(fieldErrorComparator())
            .toList();
        return error(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
	    @NonNull MethodArgumentNotValidException exception,
	    HttpHeaders headers,
	    HttpStatusCode status,
	    WebRequest request
    ) {
        var errors = exception.getBindingResult().getAllErrors().stream()
            .map(error -> error instanceof FieldError fieldError
                ? toFieldError(fieldError)
                : new RestErrorMessage.FieldError(
                    "request",
                    Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value")
                ))
            .sorted(fieldErrorComparator())
            .toList();
        return validationError(headers, request, errors);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
	    @NonNull HandlerMethodValidationException exception,
	    HttpHeaders headers,
	    HttpStatusCode status,
	    WebRequest request
    ) {
        var errors = Stream.concat(
            exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                    .map(validationError -> new RestErrorMessage.FieldError(
                        Objects.requireNonNullElse(
                            result.getMethodParameter().getParameterName(),
                            "request"
                        ),
                        Objects.requireNonNullElse(validationError.getDefaultMessage(), "Invalid value")
                    ))),
            exception.getCrossParameterValidationResults().stream()
                .map(validationError -> new RestErrorMessage.FieldError(
                    "request",
                    Objects.requireNonNullElse(validationError.getDefaultMessage(), "Invalid value")
                ))
        )
            .sorted(fieldErrorComparator())
            .toList();
        return validationError(headers, request, errors);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return validationError(headers, request, List.of(
            new RestErrorMessage.FieldError("body", "Request body is missing or malformed")
        ));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
	    @NonNull MissingServletRequestParameterException exception,
	    HttpHeaders headers,
	    HttpStatusCode status,
	    WebRequest request
    ) {
        return validationError(headers, request, List.of(
            new RestErrorMessage.FieldError(exception.getParameterName(), "Parameter is required")
        ));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
        TypeMismatchException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        String field = exception instanceof MethodArgumentTypeMismatchException mismatch
            ? mismatch.getName()
            : Objects.requireNonNullElse(exception.getPropertyName(), "request");
        return validationError(headers, request, List.of(
            new RestErrorMessage.FieldError(field, "Value has an invalid type")
        ));
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
        ServletRequestBindingException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return validationError(headers, request, List.of(
            new RestErrorMessage.FieldError("request", "A required request value is missing or invalid")
        ));
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return errorResponse(headers, request, HttpStatus.NOT_FOUND, "Resource not found");
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
        NoResourceFoundException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return errorResponse(headers, request, HttpStatus.NOT_FOUND, "Resource not found");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception exception,
        @Nullable Object body,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        if (body instanceof RestErrorMessage) {
            return super.handleExceptionInternal(exception, body, headers, status, request);
        }
        return errorResponse(headers, request, status, messageFor(status));
    }

    private RestErrorMessage.@NonNull FieldError toFieldError(@NonNull FieldError error) {
        return new RestErrorMessage.FieldError(
            error.getField(),
            Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value")
        );
    }

    private @NonNull Comparator<RestErrorMessage.FieldError> fieldErrorComparator() {
        return Comparator.comparing(RestErrorMessage.FieldError::field)
            .thenComparing(RestErrorMessage.FieldError::message);
    }

    private @NonNull ResponseEntity<Object> validationError(
        HttpHeaders headers,
        WebRequest request,
        List<RestErrorMessage.FieldError> errors
    ) {
        return errorResponse(headers, request, HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    private @NonNull ResponseEntity<Object> errorResponse(
        HttpHeaders headers,
        WebRequest request,
        HttpStatusCode status,
        String message
    ) {
        return errorResponse(headers, request, status, message, List.of());
    }

    private @NonNull ResponseEntity<Object> errorResponse(
	    HttpHeaders headers,
	    WebRequest request,
	    @NonNull HttpStatusCode status,
	    String message,
	    List<RestErrorMessage.FieldError> errors
    ) {
        HttpStatus httpStatus = HttpStatus.valueOf(status.value());
        return new ResponseEntity<>(
            new RestErrorMessage(httpStatus, message, errors),
            headers,
            status
        );
    }

    private @NonNull ResponseEntity<RestErrorMessage> error(HttpStatus status, String message) {
        return error(status, Objects.requireNonNullElse(message, status.getReasonPhrase()), List.of());
    }

    private @NonNull ResponseEntity<RestErrorMessage> error(
        HttpStatus status,
        String message,
        List<RestErrorMessage.FieldError> errors
    ) {
        return ResponseEntity.status(status).body(new RestErrorMessage(status, message, errors));
    }

    private @NonNull String messageFor(@NonNull HttpStatusCode status) {
        if (status.is4xxClientError()) {
            return switch (status.value()) {
                case 400 -> "Invalid request";
                case 404 -> "Resource not found";
                case 405 -> "Method not allowed";
                case 406 -> "Not acceptable";
                case 409 -> "Request conflicts with existing data";
                case 415 -> "Unsupported media type";
                default -> "Request could not be processed";
            };
        }
        return "An unexpected error occurred";
    }
}
