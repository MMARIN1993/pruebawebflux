package com.surabancaweb.domain.usecase.poliza;

import com.surabancaweb.domain.model.exceptions.BusinessException;
import com.surabancaweb.domain.model.poliza.PolizaDomain;
import com.surabancaweb.domain.model.poliza.PolizaDomain.Tipo;
import com.surabancaweb.domain.model.poliza.gateway.PolizaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EliminarPolizaUseCaseTest {

    @Mock
    private PolizaRepository repository;

    @InjectMocks
    private EliminarPolizaUseCase useCase;

    private PolizaDomain buildDomain(String policyId) {
        return PolizaDomain.builder()
                .policyId(policyId)
                .tipo(Tipo.AUTO)
                .fechaInicio(LocalDate.now().plusDays(1))
                .valor(BigDecimal.valueOf(1000))
                .build();
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(repository);
    }

    @Test
    void eliminarPorPolicyId_exitoso_cuandoExiste() {
        String policyId = "POL123";
        when(repository.buscarPorId(policyId)).thenReturn(Mono.just(buildDomain(policyId)));
        when(repository.eliminar(policyId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.eliminarPorPolicyId(policyId))
                .verifyComplete();

        verify(repository).buscarPorId(policyId);
        verify(repository).eliminar(policyId);
    }

    @Test
    void eliminarPorPolicyId_falla_cuandoNoExiste() {
        String policyId = "POL404";
        when(repository.buscarPorId(policyId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.eliminarPorPolicyId(policyId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getType() == BusinessException.Type.POLIZA_NO_EXISTE_POLICYID)
                .verify();

        verify(repository).buscarPorId(policyId);
        verify(repository, never()).eliminar(anyString());
    }

}

