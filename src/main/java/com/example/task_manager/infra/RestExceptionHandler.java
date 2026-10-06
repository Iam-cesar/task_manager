package com.example.task_manager.infra;

import com.example.task_manager.exceptions.*;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    private @NonNull ResponseEntity<RestErrorMessage> UserNotFoundHandler(@NonNull UserNotFoundException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    private @NonNull ResponseEntity<RestErrorMessage> ProjectNotFoundHandler(@NonNull ProjectNotFoundException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
    }

    @ExceptionHandler(IdNotFoundException.class)
    private @NonNull ResponseEntity<RestErrorMessage> IdNotFoundHandler(@NonNull IdNotFoundException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    private @NonNull ResponseEntity<RestErrorMessage> UserAlreadyExistsHandler(@NonNull UserAlreadyExistsException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.CONFLICT, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(treatedMessage);
    }

    @ExceptionHandler(ProjectAlreadyExistsException.class)
    private @NonNull ResponseEntity<RestErrorMessage> ProjectAlreadyExistsHandler(@NonNull ProjectAlreadyExistsException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.CONFLICT, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(treatedMessage);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    private @NonNull ResponseEntity<RestErrorMessage> DataIntegrityViolationHandler(DataIntegrityViolationException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.CONFLICT, "Violação de integridade de dados");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(treatedMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private @NonNull ResponseEntity<RestErrorMessage> IllegalArgumentExceptionHandler(@NonNull IllegalArgumentException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(treatedMessage);
    }

	@ExceptionHandler(LabelNotFoundException.class)
	private @NonNull ResponseEntity<RestErrorMessage> LabelNotFoundExceptionHandler(@NonNull LabelNotFoundException e) {
		RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
	}

}
