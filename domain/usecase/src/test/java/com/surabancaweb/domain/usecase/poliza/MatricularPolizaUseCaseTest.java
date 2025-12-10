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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatricularPolizaUseCaseTest {

    @Mock
    private PolizaRepository repository;

    @InjectMocks
    private MatricularPolizaUseCase useCase;

    private PolizaDomain buildDomain(String policyId, Tipo tipo, LocalDate fechaInicio, BigDecimal valor) {
        return PolizaDomain.builder()
                .policyId(policyId)
                .tipo(tipo)
                .fechaInicio(fechaInicio)
                .valor(valor)
                .build();
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(repository);
    }

    @Test
    void guardarPoliza_creaNueva_cuandoNoExiste() {
        PolizaDomain entrada = buildDomain("POL123", Tipo.AUTO, LocalDate.now().plusDays(1), BigDecimal.valueOf(1000));
        when(repository.buscarPorId(eq("POL123"))).thenReturn(Mono.empty());
        when(repository.crear(any(PolizaDomain.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0, PolizaDomain.class)));

        StepVerifier.create(useCase.guardarPoliza(entrada))
                .expectNextMatches(result -> result.getPolicyId().equals("POL123")
                        && result.getTipo() == Tipo.AUTO
                        && result.getValor() != null
                        && result.getValor().compareTo(BigDecimal.valueOf(1000)) == 0)
                .verifyComplete();

        verify(repository).buscarPorId("POL123");
        verify(repository).crear(any(PolizaDomain.class));
        verify(repository, never()).actualizar(any());
    }

    @Test
    void guardarPoliza_actualiza_cuandoExiste() {
        PolizaDomain existente = buildDomain("POL123", Tipo.HOGAR, LocalDate.now().plusDays(5), BigDecimal.valueOf(500));
        PolizaDomain entrada = buildDomain("POL123", Tipo.AUTO, LocalDate.now().plusDays(10), BigDecimal.valueOf(1500));
        when(repository.buscarPorId(eq("POL123"))).thenReturn(Mono.just(existente));
        when(repository.actualizar(any(PolizaDomain.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0, PolizaDomain.class)));

        StepVerifier.create(useCase.guardarPoliza(entrada))
                .expectNextMatches(result -> result.getPolicyId().equals("POL123")
                        && result.getTipo() == Tipo.AUTO
                        && result.getValor() != null
                        && result.getValor().compareTo(BigDecimal.valueOf(1500)) == 0)
                .verifyComplete();

        verify(repository).buscarPorId("POL123");
        verify(repository).actualizar(any(PolizaDomain.class));
        verify(repository, never()).crear(any());
    }

    @Test
    void guardarPoliza_falla_porFechaInvalida_FECHA_MAYOR() {
        PolizaDomain entrada = buildDomain("POL123", Tipo.AUTO, LocalDate.now().minusDays(1), BigDecimal.valueOf(1000));

        StepVerifier.create(useCase.guardarPoliza(entrada))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getType() == BusinessException.Type.FECHA_MAYOR)
                .verify();

        verify(repository, never()).buscarPorId(any());
        verify(repository, never()).crear(any());
        verify(repository, never()).actualizar(any());
    }

    @Test
    void guardarPoliza_falla_porValorInvalido_VALOR_MAYOR() {
        PolizaDomain entrada = buildDomain("POL123", Tipo.AUTO, LocalDate.now().plusDays(1), BigDecimal.ZERO);

        StepVerifier.create(useCase.guardarPoliza(entrada))
            .expectErrorMatches(throwable -> throwable instanceof BusinessException
                    && ((BusinessException) throwable).getType() == BusinessException.Type.VALOR_MAYOR)
            .verify();

        verify(repository, never()).buscarPorId(any());
        verify(repository, never()).crear(any());
        verify(repository, never()).actualizar(any());
    }

    @Test
    void guardarPoliza_falla_porFechaNula_FECHA_MAYOR() {
        PolizaDomain entrada = buildDomain("POL123", Tipo.AUTO, null, BigDecimal.valueOf(1000));

        StepVerifier.create(useCase.guardarPoliza(entrada))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getType() == BusinessException.Type.FECHA_MAYOR)
                .verify();

        verify(repository, never()).buscarPorId(any());
        verify(repository, never()).crear(any());
        verify(repository, never()).actualizar(any());
    }

    @Test
    void guardarPoliza_falla_porValorNulo_VALOR_MAYOR() {
        PolizaDomain entrada = buildDomain("POL123", Tipo.AUTO, LocalDate.now().plusDays(1), null);

        StepVerifier.create(useCase.guardarPoliza(entrada))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getType() == BusinessException.Type.VALOR_MAYOR)
                .verify();

        verify(repository, never()).buscarPorId(any());
        verify(repository, never()).crear(any());
        verify(repository, never()).actualizar(any());
    }

    @Test
    void guardarPoliza_onErrorResume_envuelveEnRuntimeException() {
        // Entrada válida para que no falle la validación de negocio
        PolizaDomain entrada = buildDomain("POL_ERR", Tipo.AUTO, LocalDate.now().plusDays(1), BigDecimal.valueOf(999));

        // Simular error técnico en el repositorio al buscar por id
        when(repository.buscarPorId(eq("POL_ERR"))).thenReturn(Mono.error(new IllegalStateException("Fallo técnico")));

        StepVerifier.create(useCase.guardarPoliza(entrada))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getCause() instanceof IllegalStateException
                        && "Fallo técnico".equals(throwable.getCause().getMessage()))
                .verify();

        verify(repository).buscarPorId("POL_ERR");
        // No debe llamar crear/actualizar por el error temprano
        verify(repository, never()).crear(any());
        verify(repository, never()).actualizar(any());
    }

}
