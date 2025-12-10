package com.surabancaweb.reactiveweb.api.ws.poliza;

import com.surabancaweb.domain.usecase.poliza.EliminarPolizaUseCase;
import com.surabancaweb.reactiveweb.api.ws.util.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EliminarPolizaServiceTest {

    @Mock
    private EliminarPolizaUseCase useCase;

    @InjectMocks
    private EliminarPolizaService controller;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(useCase);
    }

    @Test
    void eliminarPorId_happyPath_devuelve200() {
        String id = "POL123";
        when(useCase.eliminarPorPolicyId(eq(id))).thenReturn(Mono.empty());

        Mono<ResponseEntity<ResponseDTO<Object>>> mono = controller.eliminarPorId(id);

        StepVerifier.create(mono)
                .expectNextMatches(entity -> entity.getStatusCode().is2xxSuccessful()
                                          && "Poliza eliminada exitosamente".equals(entity.getBody().message()))
                .verifyComplete();

        verify(useCase).eliminarPorPolicyId(id);
    }

    @Test
    void eliminarPorId_errorDevuelve400() {
        String id = "POL123";
        when(useCase.eliminarPorPolicyId(eq(id))).thenReturn(Mono.error(new RuntimeException("fallo")));

        Mono<ResponseEntity<ResponseDTO<Object>>> mono = controller.eliminarPorId(id);

        StepVerifier.create(mono)
                .expectNextMatches(entity -> entity.getStatusCode().is4xxClientError()
                                    && entity.getBody().message().contains("Error al eliminar la Poliza:"))
                .verifyComplete();

        verify(useCase).eliminarPorPolicyId(id);
    }
}

