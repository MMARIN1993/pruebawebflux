package com.surabancaweb.reactiveweb.api.ws.util;

import com.surabancaweb.domain.model.exceptions.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ErrorApiHandler {


    @ExceptionHandler(Exception.class)
    public final Mono<ResponseEntity<ResponseDTO<?>>> handleAllExceptions(Exception exception) {

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(500, "Error interno del servidor: " + exception.getMessage(), null, 0)));
    }


    @ExceptionHandler(BusinessException.class)
    public final Mono<ResponseEntity<ResponseDTO<?>>> handleBusinessExceptions(BusinessException exception) {
        HttpStatus status;
        switch (exception.getType()) {
            case VALIDATION, FECHA_MAYOR, VALOR_MAYOR -> status = HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> status = HttpStatus.NOT_FOUND;
            case CONFLICT -> status = HttpStatus.CONFLICT;
            default -> status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return Mono.just(ResponseEntity.status(status)
                .body(new ResponseDTO<>(status.value(), exception.getMessage(), null, 0)));
    }

    private String getMessagesErrorAnotations(WebExchangeBindException webExchangeException) {
        StringBuilder sb = new StringBuilder();
        for (FieldError error : webExchangeException.getFieldErrors()) {
            // Solo el defaultMessage de cada error de validación
            sb.append(error.getDefaultMessage()).append(' ');
        }
        return sb.toString().trim();
    }

    @ExceptionHandler(ServerWebInputException.class)
    public final Mono<ResponseEntity<ResponseDTO<?>>> handleAllExceptions(ServerWebInputException exception) {
        if (exception instanceof WebExchangeBindException webExchangeException) {
            if (webExchangeException.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String messageError = getMessagesErrorAnotations(webExchangeException);
                if (messageError.isEmpty()) {
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ResponseDTO<>(400, "Dato de entrada incorrecto.", null, 0)));
                } else {
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ResponseDTO<>(400, messageError, null, 0)));
                }
            }
        }
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(400, "Dato de entrada incorrecto.", null, 0)));

    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final Mono<ResponseEntity<ResponseDTO<?>>> handleConstraintViolationException(ConstraintViolationException exception) {
        String errorMessage = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .reduce((msg1, msg2) -> msg1 + " " + msg2)
                .orElse("Dato de entrada incorrecto.");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(400, errorMessage, null, 0)));

    }



}
