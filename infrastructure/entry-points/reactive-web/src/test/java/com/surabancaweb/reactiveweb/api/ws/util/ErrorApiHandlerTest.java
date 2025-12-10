package com.surabancaweb.reactiveweb.api.ws.util;

import com.surabancaweb.domain.model.exceptions.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import reactor.test.StepVerifier;

import java.util.Set;

class ErrorApiHandlerTest {

    private final ErrorApiHandler handler = new ErrorApiHandler();

    @Test
    void handleBusinessExceptions_validation_badRequest() {
        BusinessException ex = new BusinessException(BusinessException.Type.FECHA_MAYOR);
        StepVerifier.create(handler.handleBusinessExceptions(ex))
                .expectNextMatches(entity -> entity.getStatusCode().is4xxClientError()
                                        && entity.getBody().message().equals(BusinessException.Type.FECHA_MAYOR.getDefaultMessage()))
                .verifyComplete();
    }

    @Test
    void handleBusinessExceptions_notFound() {
        BusinessException ex = new BusinessException(BusinessException.Type.NOT_FOUND, "No encontrado");
        StepVerifier.create(handler.handleBusinessExceptions(ex))
                .expectNextMatches(entity -> entity.getStatusCode().value() == 404
                                              && entity.getBody().message().equals("No encontrado"))
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_returnsDefaultMessages() {
        // Simular WebExchangeBindException con errores de campo y defaultMessage
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "polizaRequestDTO");
        bindingResult.addError(new FieldError("polizaRequestDTO", "policyId", "policyId debe tener máximo 10 caracteres"));
        bindingResult.addError(new FieldError("polizaRequestDTO", "tipo", "tipo debe ser uno de: AUTO, VIDA, HOGAR, SOAT"));
        WebExchangeBindException webEx = new WebExchangeBindException(null, bindingResult);
        ServerWebInputException inputEx = webEx;

        StepVerifier.create(handler.handleAllExceptions(inputEx))
                .expectNextMatches(entity -> entity.getStatusCode().value() == 400

                        && entity.getBody().message().contains("policyId debe tener máximo 10 caracteres")
                        && entity.getBody().message().contains("tipo debe ser uno de: AUTO, VIDA, HOGAR, SOAT"))
                .verifyComplete();
    }

    @Test
    void handleConstraintViolationException_concatenatesMessages() {
        ConstraintViolation<?> v1 = new DummyViolation("Error A");
        ConstraintViolation<?> v2 = new DummyViolation("Error B");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(v1, v2));

        StepVerifier.create(handler.handleConstraintViolationException(ex))
                .expectNextMatches(entity -> entity.getStatusCode().value() == 400

                        && entity.getBody().message().contains("Error A")
                        && entity.getBody().message().contains("Error B"))
                .verifyComplete();
    }

    @Test
    void handleAllExceptions_genericException_returns500CodeInBodyWith400StatusWrapper() {
        Exception ex = new Exception("algo falló");
        StepVerifier.create(handler.handleAllExceptions(ex))
                .expectNextMatches(entity -> entity.getStatusCode().is4xxClientError()

                        && entity.getBody().message().contains("Error interno del servidor"))
                .verifyComplete();
    }

    // Dummy ConstraintViolation para pruebas
    static class DummyViolation implements ConstraintViolation<Object> {
        private final String message;
        DummyViolation(String message) { this.message = message; }
        @Override public String getMessage() { return message; }
        @Override public String getMessageTemplate() { return null; }
        @Override public Object getRootBean() { return null; }
        @Override public Class<Object> getRootBeanClass() { return Object.class; }
        @Override public Object getLeafBean() { return null; }
        @Override public Object[] getExecutableParameters() { return new Object[0]; }
        @Override public Object getExecutableReturnValue() { return null; }
        @Override public jakarta.validation.Path getPropertyPath() { return null; }
        @Override public Object getInvalidValue() { return null; }
        @Override public jakarta.validation.metadata.ConstraintDescriptor<?> getConstraintDescriptor() { return null; }
        @Override public <U> U unwrap(Class<U> type) { return null; }
    }
}

