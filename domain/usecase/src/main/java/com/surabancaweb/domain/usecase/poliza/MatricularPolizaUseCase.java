package com.surabancaweb.domain.usecase.poliza;


import lombok.RequiredArgsConstructor;
import com.surabancaweb.domain.model.poliza.gateway.PolizaRepository;
import com.surabancaweb.domain.model.poliza.PolizaDomain;
import com.surabancaweb.domain.model.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
public class MatricularPolizaUseCase {

    private final PolizaRepository repository;

    public Mono<PolizaDomain> guardarPoliza(PolizaDomain poliza) {
        return validarPoliza(poliza).then(Mono.defer(() -> {
            PolizaDomain nuevo = poliza.toBuilder()
                    .uuid(UUID.randomUUID().toString())
                    .build();
            return repository.buscarPorId(nuevo.getPolicyId())
                    .flatMap(existente -> repository.actualizar(existente.toBuilder()
                            .tipo(nuevo.getTipo())
                            .fechaInicio(nuevo.getFechaInicio())
                            .valor(nuevo.getValor())
                            .build()))
                    .switchIfEmpty(Mono.defer(() -> repository.crear(nuevo)))
                    .onErrorResume(throwable -> Mono.error(new RuntimeException(throwable.getMessage(), throwable)));
        }));
    }

    private Mono<Void> validarPoliza(PolizaDomain poliza) {
        return Mono.defer(() -> {
            LocalDate hoy = LocalDate.now();
            if (poliza.getFechaInicio() == null || poliza.getFechaInicio().isBefore(hoy)) {
                return Mono.error(new BusinessException(BusinessException.Type.FECHA_MAYOR));
            }
            if (poliza.getValor() == null || poliza.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                return Mono.error(new BusinessException(BusinessException.Type.VALOR_MAYOR));
            }
            return Mono.empty();
        });
    }

}
