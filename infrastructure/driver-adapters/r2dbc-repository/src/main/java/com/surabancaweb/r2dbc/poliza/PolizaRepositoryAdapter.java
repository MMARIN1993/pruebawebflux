package com.surabancaweb.r2dbc.poliza;

import com.surabancaweb.domain.model.poliza.PolizaDomain;
import com.surabancaweb.domain.model.poliza.gateway.PolizaRepository;
import com.surabancaweb.domain.model.poliza.PolizaDomain.Tipo;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.surabancaweb.r2dbc.utils.SqlStatement;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class PolizaRepositoryAdapter implements PolizaRepository {

    private final DatabaseClient client;
    private final PolizaMapper mapper;

    @SqlStatement(namespace = "poliza", value = "crear")
    private static String sqlCrear;

    @SqlStatement(namespace = "poliza", value = "listarTodo")
    private static String sqlListarTodo;

    @SqlStatement(namespace = "poliza", value = "buscarPorId")
    private static String sqlBuscarPorId;

    @SqlStatement(namespace = "poliza", value = "actualizar")
    private static String sqlActualizar;

    @SqlStatement(namespace = "poliza", value = "eliminar")
    private static String sqlEliminar;

    @Override
    public Mono<PolizaDomain> crear(PolizaDomain poliza) {
        PolizaEntity entity = mapper.domainToEntity(poliza);
        DatabaseClient.GenericExecuteSpec spec = client.sql(sqlCrear);
        spec = spec.bind("uuid", entity.getUuid())
                .bind("policy_id", entity.getPolicyId())
                .bind("tipo", entity.getTipo().name())
                .bind("fecha_inicio", entity.getFechaInicio())
                .bind("valor", entity.getValor());
        return spec.fetch()
                .rowsUpdated()
                .flatMap(rows -> rows > 0 ?
                        Mono.just(mapper.entityToDomain(entity)) :
                        Mono.error(new RuntimeException("No se pudo insertar la poliza")));
    }

    @Override
    public Flux<PolizaDomain> listarTodo() {
        return client.sql(sqlListarTodo)
                .map((row, meta) -> {
                    PolizaEntity e = PolizaEntity.builder()
                            .uuid(row.get("uuid", String.class))
                            .policyId(row.get("policy_id", String.class))
                            .tipo(row.get("tipo", String.class) != null ? Tipo.valueOf(row.get("tipo", String.class)) : null)
                            .fechaInicio(row.get("fecha_inicio", LocalDate.class))
                            .valor(row.get("valor", java.math.BigDecimal.class))
                            .build();
                    return mapper.entityToDomain(e);
                })
                .all();
    }

    @Override
    public Mono<PolizaDomain> buscarPorId(String policyId) {
        return client.sql(sqlBuscarPorId)
                .bind("policy_id", policyId)
                .map((row, meta) -> {
                    PolizaEntity e = PolizaEntity.builder()
                            .uuid(row.get("uuid", String.class))
                            .policyId(row.get("policy_id", String.class))
                            .tipo(row.get("tipo", String.class) != null ? Tipo.valueOf(row.get("tipo", String.class)) : null)
                            .fechaInicio(row.get("fecha_inicio", LocalDate.class))
                            .valor(row.get("valor", BigDecimal.class))
                            .build();
                    return mapper.entityToDomain(e);
                })
                .one();
    }

    @Override
    public Mono<PolizaDomain> actualizar(PolizaDomain poliza) {
        PolizaEntity entity = mapper.domainToEntity(poliza);
        DatabaseClient.GenericExecuteSpec spec = client.sql(sqlActualizar)
                .bind("tipo", entity.getTipo() != null ? entity.getTipo().name() : null)
                .bind("fecha_inicio", entity.getFechaInicio())
                .bind("valor", entity.getValor())
                .bind("policy_id", entity.getPolicyId());

        return spec.fetch()
                .rowsUpdated()
                .flatMap(rows -> rows > 0 ? Mono.just(mapper.entityToDomain(entity))
                        : Mono.error(new org.springframework.dao.TransientDataAccessResourceException(
                        "Failed to update table [poliza]; Row with Id [" + entity.getPolicyId() + "] does not exist")));
    }

    @Override
    public Mono<Void> eliminar(String policyId) {
        return client.sql(sqlEliminar)
                .bind("policy_id", policyId)
                .fetch()
                .rowsUpdated()
                .flatMap(rows -> rows > 0 ? Mono.empty()
                        : Mono.error(new org.springframework.dao.TransientDataAccessResourceException(
                        "Failed to delete from table [poliza]; Row with Id [" + policyId + "] does not exist")));
    }

}
