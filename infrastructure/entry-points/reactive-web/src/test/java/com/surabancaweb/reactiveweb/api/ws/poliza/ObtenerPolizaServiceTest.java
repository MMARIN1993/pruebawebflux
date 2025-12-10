package com.surabancaweb.reactiveweb.api.ws.poliza;

import com.surabancaweb.domain.model.poliza.PolizaDomain;
import com.surabancaweb.domain.usecase.poliza.ObtenerPolizaUseCase;
import com.surabancaweb.reactiveweb.api.ws.poliza.dto.PolizaResponseDTO;
import com.surabancaweb.reactiveweb.api.ws.poliza.mapper.PolizaMapperService;
import com.surabancaweb.reactiveweb.api.ws.util.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObtenerPolizaServiceTest {

    @Mock
    private ObtenerPolizaUseCase useCase;

    @Mock
    private PolizaMapperService mapper;

    @InjectMocks
    private ObtenerPolizaService controller;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(useCase, mapper);
    }

    private PolizaDomain domain(String id, PolizaDomain.Tipo tipo, LocalDate fecha, BigDecimal valor) {
        return PolizaDomain.builder()
                .policyId(id)
                .tipo(tipo)
                .fechaInicio(fecha)
                .valor(valor)
                .build();
    }

    private PolizaResponseDTO dto(String id, String tipo, LocalDate fecha, BigDecimal valor) {
        PolizaResponseDTO r = new PolizaResponseDTO();
        r.setPolicyId(id);
        r.setTipo(tipo);
        r.setFechaInicio(fecha);
        r.setValor(valor);
        return r;
    }

    @Test
    void buscarPolizaPorId_devuelve200() {
        String id = "POL123";
        PolizaDomain d = domain(id, PolizaDomain.Tipo.AUTO, LocalDate.now().plusDays(1), BigDecimal.valueOf(1000));
        PolizaResponseDTO r = dto(id, "AUTO", d.getFechaInicio(), d.getValor());

        when(useCase.buscarPolizaPorPolizaId(eq(id))).thenReturn(Mono.just(d));
        when(mapper.domainToDTO(d)).thenReturn(r);

        Mono<ResponseEntity<ResponseDTO<PolizaResponseDTO>>> respuesta = controller.buscarPolizaPorId(id);

        StepVerifier.create(respuesta)
                .expectNextMatches(entity -> entity.getStatusCode().is2xxSuccessful()
                            && "Poliza encontrada exitosamente".equals(entity.getBody().message()))
                .verifyComplete();

        verify(useCase).buscarPolizaPorPolizaId(id);
        verify(mapper).domainToDTO(d);
    }

    @Test
    void buscarPolizaPorId_error_devuelve400() {
        String id = "POL404";
        when(useCase.buscarPolizaPorPolizaId(eq(id))).thenReturn(Mono.error(new RuntimeException("fallo")));

        Mono<ResponseEntity<ResponseDTO<PolizaResponseDTO>>> mono = controller.buscarPolizaPorId(id);

        StepVerifier.create(mono)
                .expectNextMatches(entity -> entity.getStatusCode().is4xxClientError()
                                      && entity.getBody().message().contains("Error al consultar la Poliza:"))
                .verifyComplete();

        verify(useCase).buscarPolizaPorPolizaId(id);
        verify(mapper, never()).domainToDTO(any());
    }

    @Test
    void buscarPolizas_devuelve200_listaDTO() {
        PolizaDomain d1 = domain("POL1", PolizaDomain.Tipo.AUTO, LocalDate.now().plusDays(1), BigDecimal.valueOf(1000));
        PolizaDomain d2 = domain("POL2", PolizaDomain.Tipo.HOGAR, LocalDate.now().plusDays(2), BigDecimal.valueOf(2000));
        PolizaResponseDTO r1 = dto("POL1", "AUTO", d1.getFechaInicio(), d1.getValor());
        PolizaResponseDTO r2 = dto("POL2", "HOGAR", d2.getFechaInicio(), d2.getValor());

        when(useCase.obtenerPolizas()).thenReturn(Flux.just(d1, d2));
        when(mapper.domainToDTO(d1)).thenReturn(r1);
        when(mapper.domainToDTO(d2)).thenReturn(r2);

        Mono<ResponseEntity<ResponseDTO<java.util.List<PolizaResponseDTO>>>> mono = controller.buscarPolizas();

        StepVerifier.create(mono)
                .expectNextMatches(entity -> entity.getStatusCode().is2xxSuccessful()
                        && entity.getBody() != null
                        && "Polizas encontradas exitosamente".equals(entity.getBody().message()))
                .verifyComplete();

        verify(useCase).obtenerPolizas();
        verify(mapper).domainToDTO(d1);
        verify(mapper).domainToDTO(d2);
    }

    @Test
    void buscarPolizas_error_devuelve400() {
        when(useCase.obtenerPolizas()).thenReturn(Flux.error(new RuntimeException("fallo")));

        Mono<ResponseEntity<ResponseDTO<java.util.List<PolizaResponseDTO>>>> mono = controller.buscarPolizas();

        StepVerifier.create(mono)
                .expectNextMatches(entity -> entity.getStatusCode().is4xxClientError()
                        && entity.getBody().message().contains("Error al listar polizas:"))
                .verifyComplete();

        verify(useCase).obtenerPolizas();
        verify(mapper, never()).domainToDTO(any());
    }
}
