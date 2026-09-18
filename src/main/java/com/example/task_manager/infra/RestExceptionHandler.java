package com.example.task_manager.infra;

import com.example.task_manager.exceptions.UserNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFound.class)
    private ResponseEntity<RestErrorMessage> UserNotFoundHandler(UserNotFound e) {
        RestErrorMessage treatedMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, e.getMessage());
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(treatedMessage);
    };
}
