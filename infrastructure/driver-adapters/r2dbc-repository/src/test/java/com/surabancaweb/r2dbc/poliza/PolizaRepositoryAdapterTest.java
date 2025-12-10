package com.surabancaweb.r2dbc.poliza;

import com.surabancaweb.domain.model.poliza.PolizaDomain;
import com.surabancaweb.domain.model.poliza.PolizaDomain.Tipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Answers;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class PolizaRepositoryAdapterTest {

    @Mock
    private DatabaseClient client;

    @Mock
    private PolizaMapper mapper;

    @InjectMocks
    private PolizaRepositoryAdapter adapter;

    // Usar deep stubs para encadenar fetch().rowsUpdated()
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseClient.GenericExecuteSpec genericSpec;

    private PolizaDomain domainSample() {
        return PolizaDomain.builder()
                .uuid("UUID-1")
                .policyId("POL123")
                .tipo(Tipo.AUTO)
                .fechaInicio(LocalDate.now().plusDays(1))
                .valor(BigDecimal.valueOf(1000))
                .build();
    }

    private PolizaEntity entitySample() {
        return PolizaEntity.builder()
                .uuid("UUID-1")
                .policyId("POL123")
                .tipo(Tipo.AUTO)
                .fechaInicio(LocalDate.now().plusDays(1))
                .valor(BigDecimal.valueOf(1000))
                .build();
    }

    @BeforeEach
    void setupMapper() {
        // Inicializar SQLs estáticos para evitar client.sql(null)
        ReflectionTestUtils.setField(PolizaRepositoryAdapter.class, "sqlCrear", "INSERT INTO poliza (...) VALUES (...)");
        ReflectionTestUtils.setField(PolizaRepositoryAdapter.class, "sqlActualizar", "UPDATE poliza SET ... WHERE policy_id = :policy_id");
        ReflectionTestUtils.setField(PolizaRepositoryAdapter.class, "sqlEliminar", "DELETE FROM poliza WHERE policy_id = :policy_id");
        ReflectionTestUtils.setField(PolizaRepositoryAdapter.class, "sqlBuscarPorId", "SELECT * FROM poliza WHERE policy_id = :policy_id");
        ReflectionTestUtils.setField(PolizaRepositoryAdapter.class, "sqlListarTodo", "SELECT * FROM poliza");
   // Usar stubbing lenient para evitar UnnecessaryStubbing cuando un test no usa el mapper
        Mockito.lenient().when(mapper.domainToEntity(any(PolizaDomain.class))).thenAnswer(inv -> {
            PolizaDomain d = inv.getArgument(0);
            return PolizaEntity.builder()
                    .uuid(d.getUuid())
                    .policyId(d.getPolicyId())
                    .tipo(d.getTipo())
                    .fechaInicio(d.getFechaInicio())
                    .valor(d.getValor())
                    .build();
        });
        Mockito.lenient().when(mapper.entityToDomain(any(PolizaEntity.class))).thenAnswer(inv -> {
            PolizaEntity e = inv.getArgument(0);
            return PolizaDomain.builder()
                    .uuid(e.getUuid())
                    .policyId(e.getPolicyId())
                    .tipo(e.getTipo())
                    .fechaInicio(e.getFechaInicio())
                    .valor(e.getValor())
                    .build();
        });
    }

    @Test
    void crear_inserta_exitosamente() {
        PolizaDomain input = domainSample();
        PolizaEntity entity = entitySample();
        when(mapper.domainToEntity(eq(input))).thenReturn(entity);

        when(client.sql(any(String.class))).thenReturn(genericSpec);
        when(genericSpec.bind(eq("uuid"), any())).thenReturn(genericSpec);
        when(genericSpec.bind(eq("policy_id"), any())).thenReturn(genericSpec);
        when(genericSpec.bind(eq("tipo"), any())).thenReturn(genericSpec);
        when(genericSpec.bind(eq("fecha_inicio"), any())).thenReturn(genericSpec);
        when(genericSpec.bind(eq("valor"), any())).thenReturn(genericSpec);
        when(genericSpec.fetch().rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(adapter.crear(input))
                .expectNextMatches(res -> res.getPolicyId().equals("POL123") && res.getValor().compareTo(BigDecimal.valueOf(1000)) == 0)
                .verifyComplete();
    }

    @Test
    void crear_falla_cuando_noInsertado() {
        PolizaDomain input = domainSample();
        when(client.sql(any(String.class))).thenReturn(genericSpec);
        when(genericSpec.bind(any(String.class), any())).thenReturn(genericSpec);
        when(genericSpec.fetch().rowsUpdated()).thenReturn(Mono.just(0L));

        StepVerifier.create(adapter.crear(input))
                .expectErrorMatches(t -> t instanceof RuntimeException && t.getMessage().contains("No se pudo insertar"))
                .verify();
    }

    @Test
    void actualizar_actualiza_exitosamente() {
        PolizaDomain input = domainSample();
        PolizaEntity entity = entitySample();
        when(mapper.domainToEntity(eq(input))).thenReturn(entity);

        when(client.sql(any(String.class))).thenReturn(genericSpec);
        when(genericSpec.bind(eq("tipo"), any())).thenReturn(genericSpec);
        when(genericSpec.bind(eq("fecha_inicio"), any())).thenReturn(genericSpec);
        when(genericSpec.bind(eq("valor"), any())).thenReturn(genericSpec);
        when(genericSpec.bind(eq("policy_id"), any())).thenReturn(genericSpec);
        when(genericSpec.fetch().rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(adapter.actualizar(input))
                .expectNextMatches(res -> res.getPolicyId().equals("POL123"))
                .verifyComplete();
    }

    @Test
    void actualizar_falla_cuando_noExiste() {
        PolizaDomain input = domainSample();
        when(client.sql(any(String.class))).thenReturn(genericSpec);
        when(genericSpec.bind(any(String.class), any())).thenReturn(genericSpec);
        when(genericSpec.fetch().rowsUpdated()).thenReturn(Mono.just(0L));

        StepVerifier.create(adapter.actualizar(input))
                .expectErrorMatches(t -> t instanceof org.springframework.dao.TransientDataAccessResourceException)
                .verify();
    }

    @Test
    void eliminar_elimina_exitosamente() {
        when(client.sql(any(String.class))).thenReturn(genericSpec);
        when(genericSpec.bind(eq("policy_id"), any())).thenReturn(genericSpec);
        when(genericSpec.fetch().rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(adapter.eliminar("POL123"))
                .verifyComplete();
    }

    @Test
    void eliminar_falla_cuando_noExiste() {
        when(client.sql(any(String.class))).thenReturn(genericSpec);
        when(genericSpec.bind(eq("policy_id"), any())).thenReturn(genericSpec);
        when(genericSpec.fetch().rowsUpdated()).thenReturn(Mono.just(0L));

        StepVerifier.create(adapter.eliminar("POL123"))
                .expectErrorMatches(t -> t instanceof org.springframework.dao.TransientDataAccessResourceException)
                .verify();
    }

}
