package com.surabancaweb.domain.model.poliza.gateway;

import com.surabancaweb.domain.model.poliza.PolizaDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolizaRepository {
    Flux<PolizaDomain> listarTodo();
    Mono<PolizaDomain> buscarPorId(String policyId);
    Mono<PolizaDomain> crear(PolizaDomain poliza);
    Mono<PolizaDomain> actualizar(PolizaDomain poliza);
    Mono<Void> eliminar(String policyId);
}
