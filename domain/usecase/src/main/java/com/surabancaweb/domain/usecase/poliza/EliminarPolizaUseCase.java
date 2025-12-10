package com.surabancaweb.domain.usecase.poliza;

import com.surabancaweb.domain.model.exceptions.BusinessException;
import com.surabancaweb.domain.model.poliza.gateway.PolizaRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class EliminarPolizaUseCase {

    private final PolizaRepository repository;

    public Mono<Void> eliminarPorPolicyId(String policyId) {
        return repository.buscarPorId(policyId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.Type.POLIZA_NO_EXISTE_POLICYID)))
                .flatMap(poliza -> repository.eliminar(policyId));
    }
}
