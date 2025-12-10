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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObtenerPolizaUseCaseTest {

    @Mock
    private PolizaRepository repository;

    @InjectMocks
    private ObtenerPolizaUseCase useCase;

    private PolizaDomain poliza(String id, Tipo tipo, LocalDate fecha, BigDecimal valor) {
        return PolizaDomain.builder()
                .policyId(id)
                .tipo(tipo)
                .fechaInicio(fecha)
                .valor(valor)
                .build();
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(repository);
    }

    @Test
    void buscarPorId_exitoso_cuandoExiste() {
        String id = "POL123";
        when(repository.buscarPorId(id)).thenReturn(Mono.just(poliza(id, Tipo.AUTO, LocalDate.now().plusDays(1), BigDecimal.valueOf(1000))));

        StepVerifier.create(useCase.buscarPolizaPorPolizaId(id))
                .expectNextMatches(p -> p.getPolicyId().equals(id) && p.getTipo() == Tipo.AUTO)
                .verifyComplete();

        verify(repository).buscarPorId(id);
    }

    @Test
    void buscarPorId_falla_cuandoNoExiste() {
        String id = "POL404";
        when(repository.buscarPorId(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.buscarPolizaPorPolizaId(id))
                .expectErrorMatches(t -> t instanceof BusinessException
                        && ((BusinessException) t).getType() == BusinessException.Type.POLIZA_NO_EXISTE_POLICYID)
                .verify();

        verify(repository).buscarPorId(id);
    }

    @Test
    void obtenerPolizas_lista_conElementos() {
        when(repository.listarTodo()).thenReturn(Flux.just(
                poliza("POL1", Tipo.AUTO, LocalDate.now().plusDays(1), BigDecimal.valueOf(1000)),
                poliza("POL2", Tipo.HOGAR, LocalDate.now().plusDays(2), BigDecimal.valueOf(2000))
        ));

        StepVerifier.create(useCase.obtenerPolizas())
                .expectNextMatches(p -> p.getPolicyId().equals("POL1"))
                .expectNextMatches(p -> p.getPolicyId().equals("POL2"))
                .verifyComplete();

        verify(repository).listarTodo();
    }

    @Test
    void obtenerPolizas_falla_cuandoNoHayRegistros() {
        when(repository.listarTodo()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.obtenerPolizas())
                .expectErrorMatches(t -> t instanceof BusinessException
                        && ((BusinessException) t).getType() == BusinessException.Type.POLIZAS_NO_REGISTRADAS)
                .verify();

        verify(repository).listarTodo();
    }

}
