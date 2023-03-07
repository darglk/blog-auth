package com.darglk.blogauth.exception;

import com.darglk.blogcommons.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(value = { CustomException.class })
    public ResponseEntity<?> handleCustomException(CustomException ex) {
        return CustomException.handleCustomException(ex);
    }
}
