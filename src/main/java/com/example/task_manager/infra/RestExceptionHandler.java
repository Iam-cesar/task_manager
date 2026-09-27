package com.example.task_manager.infra;

import com.example.task_manager.exceptions.IdNotFoundException;
import com.example.task_manager.exceptions.UserAlreadyExistsException;
import com.example.task_manager.exceptions.UserNotFoundException;
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
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
    };

    @ExceptionHandler(IdNotFoundException.class)
    private ResponseEntity<RestErrorMessage> IdNotFoundHandler(IdNotFoundException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, e.getMessage());
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
    };

    @ExceptionHandler(UserAlreadyExistsException.class)
    private ResponseEntity<RestErrorMessage> UserAlreadyExistsHandler(UserAlreadyExistsException e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.CONFLICT, e.getMessage());
        return  ResponseEntity.status(HttpStatus.CONFLICT).body(treatedMessage);
    };
}
