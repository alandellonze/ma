package it.ade.ma.api.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.context.request.WebRequest.SCOPE_REQUEST;
import static org.springframework.web.util.WebUtils.ERROR_EXCEPTION_ATTRIBUTE;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public final ResponseEntity<ApiError> handlePasswordNotMatchException(Exception e, WebRequest request) {
        HttpStatus status = INTERNAL_SERVER_ERROR;
        request.setAttribute(ERROR_EXCEPTION_ATTRIBUTE, e, SCOPE_REQUEST);
        log.error("an exception will be thrown: " + e.getMessage(), e);
        return new ResponseEntity<>(new ApiError(status, e.getMessage()), status);
    }

}
