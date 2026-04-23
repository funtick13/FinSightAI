package com.finsightai.web.exception;

import com.finsightai.web.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler({
            InvalidCredentialsException.class,
            EmailNotConfirmedException.class,
            UserNotFoundException.class,
            TokenNotFoundException.class,
            TokenExpiredException.class,
            TokenAlreadyUsedException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handleUnauthorized(AuthException exception) {
        return new MessageResponse(false, exception.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public MessageResponse handleConflict(AuthException exception) {
        return new MessageResponse(false, exception.getMessage());
    }

    @ExceptionHandler(JwtKeyException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageResponse handleServerError(AuthException exception) {
        return new MessageResponse(false, exception.getMessage());
    }
}
