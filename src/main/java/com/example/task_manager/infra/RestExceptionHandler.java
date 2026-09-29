package com.example.task_manager.infra;

import com.example.task_manager.exceptions.IdNotFoundException;
import com.example.task_manager.exceptions.ProjectAlreadyExistsException;
import com.example.task_manager.exceptions.ProjectNotFoundException;
import com.example.task_manager.exceptions.UserAlreadyExistsException;
import com.example.task_manager.exceptions.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<RestErrorMessage> UserNotFoundHandler(UserNotFoundException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    private ResponseEntity<RestErrorMessage> ProjectNotFoundHandler(ProjectNotFoundException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
    }

    @ExceptionHandler(IdNotFoundException.class)
    private ResponseEntity<RestErrorMessage> IdNotFoundHandler(IdNotFoundException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    private ResponseEntity<RestErrorMessage> UserAlreadyExistsHandler(UserAlreadyExistsException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.CONFLICT, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(treatedMessage);
    }

    @ExceptionHandler(ProjectAlreadyExistsException.class)
    private ResponseEntity<RestErrorMessage> ProjectAlreadyExistsHandler(ProjectAlreadyExistsException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.CONFLICT, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(treatedMessage);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<RestErrorMessage> DataIntegrityViolationHandler(DataIntegrityViolationException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.CONFLICT, "Violação de integridade de dados");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(treatedMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<RestErrorMessage> IllegalArgumentExceptionHandler(IllegalArgumentException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(treatedMessage);
    }
}
