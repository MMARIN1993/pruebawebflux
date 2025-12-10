package com.surabancaweb.reactiveweb.api.ws.poliza;

import com.surabancaweb.domain.model.poliza.PolizaDomain;
import com.surabancaweb.domain.usecase.poliza.MatricularPolizaUseCase;
import com.surabancaweb.reactiveweb.api.ws.poliza.dto.PolizaRequestDTO;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatricularPolizaServiceTest {

    @Mock
    private MatricularPolizaUseCase useCase;

    @Mock
    private PolizaMapperService mapper;

    @InjectMocks
    private MatricularPolizaService controller;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(useCase, mapper);
    }

    private PolizaRequestDTO request() {
        PolizaRequestDTO dto = new PolizaRequestDTO();
        dto.setPolicyId("POL123");
        dto.setTipo("AUTO");
        dto.setFechaInicio(LocalDate.now().plusDays(1));
        dto.setValor(BigDecimal.valueOf(1000));
        return dto;
    }

    private PolizaDomain domain() {
        return PolizaDomain.builder()
                .policyId("POL123")
                .tipo(PolizaDomain.Tipo.AUTO)
                .fechaInicio(LocalDate.now().plusDays(1))
                .valor(BigDecimal.valueOf(1000))
                .build();
    }

    private PolizaResponseDTO responseDTO() {
        PolizaResponseDTO r = new PolizaResponseDTO();
        r.setPolicyId("POL123");
        r.setTipo("AUTO");
        r.setFechaInicio(LocalDate.now().plusDays(1));
        r.setValor(BigDecimal.valueOf(1000));
        return r;
    }

    @Test
    void matricularPoliza_devuelve200() {
        PolizaRequestDTO req = request();
        PolizaDomain d = domain();
        PolizaResponseDTO res = responseDTO();

        when(mapper.dtoToDomain(any(PolizaRequestDTO.class))).thenReturn(d);
        when(useCase.guardarPoliza(d)).thenReturn(Mono.just(d));
        when(mapper.domainToDTO(d)).thenReturn(res);

        Mono<ResponseEntity<ResponseDTO<PolizaResponseDTO>>> mono = controller.matricularPoliza(req);

        StepVerifier.create(mono)
                .expectNextMatches(entity -> entity.getStatusCode().is2xxSuccessful()
                                  && "Poliza guardada exitosamente".equals(entity.getBody().message()))
                .verifyComplete();

        verify(mapper).dtoToDomain(any(PolizaRequestDTO.class));
        verify(useCase).guardarPoliza(d);
        verify(mapper).domainToDTO(d);
    }

    @Test
    void matricularPoliza_errorDevuelve400() {
        PolizaRequestDTO req = request();
        PolizaDomain d = domain();

        when(mapper.dtoToDomain(any(PolizaRequestDTO.class))).thenReturn(d);
        when(useCase.guardarPoliza(d)).thenReturn(Mono.error(new RuntimeException("fallo")));

        Mono<ResponseEntity<ResponseDTO<PolizaResponseDTO>>> mono = controller.matricularPoliza(req);

        StepVerifier.create(mono)
                .expectNextMatches(entity -> entity.getStatusCode().is4xxClientError()

                        && entity.getBody().message().contains("Error al guardar la Poliza:"))
                .verifyComplete();

        verify(mapper).dtoToDomain(any(PolizaRequestDTO.class));
        verify(useCase).guardarPoliza(d);
    }
}
