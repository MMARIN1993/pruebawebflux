package com.surabancaweb.domain.usecase.poliza;


import com.surabancaweb.domain.model.exceptions.BusinessException;
import com.surabancaweb.domain.model.poliza.PolizaDomain;
import com.surabancaweb.domain.model.poliza.gateway.PolizaRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ObtenerPolizaUseCase {

    private final PolizaRepository repository;

    public Mono<PolizaDomain> buscarPolizaPorPolizaId(String polizaId) {
        return repository.buscarPorId(polizaId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.Type.POLIZA_NO_EXISTE_POLICYID)));
    }

    public Flux<PolizaDomain> obtenerPolizas() {
        return repository.listarTodo().switchIfEmpty(
                Flux.error(new BusinessException(BusinessException.Type.POLIZAS_NO_REGISTRADAS)));
    }

}
