package com.surabancaweb.r2dbc.poliza;

import com.surabancaweb.domain.model.poliza.PolizaDomain;
import com.surabancaweb.domain.model.poliza.PolizaDomain.Tipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;

import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@ExtendWith(MockitoExtension.class)
class PolizaRepositoryAdapterQueryTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseClient client;

    @Mock
    private PolizaMapper mapper;

    @InjectMocks
    private PolizaRepositoryAdapter adapter;

    private PolizaDomain domainSample() {
        return PolizaDomain.builder()
                .uuid("UUID-1")
                .policyId("POL123")
                .tipo(Tipo.AUTO)
                .fechaInicio(LocalDate.now().plusDays(1))
                .valor(BigDecimal.valueOf(1000))
                .build();
    }

    private PolizaEntity entityFromRow() {
        return PolizaEntity.builder()
                .uuid("UUID-1")
                .policyId("POL123")
                .tipo(Tipo.AUTO)
                .fechaInicio(LocalDate.now().plusDays(1))
                .valor(BigDecimal.valueOf(1000))
                .build();
    }

    @BeforeEach
    void setup() {
        // Inicializar SQLs estáticos para evitar client.sql(null)
        ReflectionTestUtils.setField(PolizaRepositoryAdapter.class, "sqlListarTodo", "SELECT * FROM poliza");
        ReflectionTestUtils.setField(PolizaRepositoryAdapter.class, "sqlBuscarPorId", "SELECT * FROM poliza WHERE policy_id = :policy_id");

        // Mapper: convertir de entidad a dominio
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
    void listarTodo_devuelveDosElementos() {
        when(client.sql(anyString()).map(any(BiFunction.class)).all())
                .thenReturn(Flux.just(domainSample(), domainSample()));

        StepVerifier.create(adapter.listarTodo())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void listarTodo_devuelveVacio() {
        when(client.sql(anyString()).map(any(BiFunction.class)).all())
                .thenReturn(Flux.empty());

        StepVerifier.create(adapter.listarTodo())
                .verifyComplete();
    }

    @Test
    void buscarPorId_devuelveUno() {
        when(client.sql(anyString()).bind(eq("policy_id"), any()).map(any(BiFunction.class)).one())
                .thenReturn(Mono.just(domainSample()));

        StepVerifier.create(adapter.buscarPorId("POL123"))
                .expectNextMatches(res -> res.getPolicyId().equals("POL123"))
                .verifyComplete();
    }

    @Test
    void buscarPorId_devuelveEmpty() {
        when(client.sql(anyString()).bind(eq("policy_id"), any()).map(any(BiFunction.class)).one())
                .thenReturn(Mono.empty());

        StepVerifier.create(adapter.buscarPorId("NOEXISTE"))
                .verifyComplete();
    }

    @Test
    void listarTodo_devuelveDosElementos_cubreMapeo() {
        // Capturar la BiFunction en map(...) y devolver un FetchSpec que ejecuta la función
        when(client.sql(anyString()).map(any(BiFunction.class)))
                .thenAnswer(inv -> {
                    BiFunction<Row, RowMetadata, PolizaDomain> mapperFn = inv.getArgument(0);
                    @SuppressWarnings("unchecked")
                    FetchSpec<PolizaDomain> rowsSpec = mock(FetchSpec.class);

                    Row row1 = mock(Row.class);
                    Row row2 = mock(Row.class);
                    RowMetadata meta = mock(RowMetadata.class);
                    // Stubs de columnas
                    when(row1.get(eq("uuid"), eq(String.class))).thenReturn("UUID-1");
                    when(row1.get(eq("policy_id"), eq(String.class))).thenReturn("POL123");
                    when(row1.get(eq("tipo"), eq(String.class))).thenReturn("AUTO");
                    when(row1.get(eq("fecha_inicio"), eq(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
                    when(row1.get(eq("valor"), eq(BigDecimal.class))).thenReturn(BigDecimal.valueOf(1000));

                    when(row2.get(eq("uuid"), eq(String.class))).thenReturn("UUID-2");
                    when(row2.get(eq("policy_id"), eq(String.class))).thenReturn("POL456");
                    when(row2.get(eq("tipo"), eq(String.class))).thenReturn("VIDA");
                    when(row2.get(eq("fecha_inicio"), eq(LocalDate.class))).thenReturn(LocalDate.now().plusDays(2));
                    when(row2.get(eq("valor"), eq(BigDecimal.class))).thenReturn(BigDecimal.valueOf(2000));

                    PolizaDomain d1 = mapperFn.apply(row1, meta);
                    PolizaDomain d2 = mapperFn.apply(row2, meta);
                    when(rowsSpec.all()).thenReturn(Flux.just(d1, d2));
                    return rowsSpec;
                });

        StepVerifier.create(adapter.listarTodo())
                .expectNextMatches(res -> res.getPolicyId().equals("POL123") && res.getTipo() == Tipo.AUTO)
                .expectNextMatches(res -> res.getPolicyId().equals("POL456") && res.getTipo() == Tipo.VIDA)
                .verifyComplete();

        verify(mapper, atLeastOnce()).entityToDomain(any(PolizaEntity.class));
    }

    @Test
    void buscarPorId_devuelveUno_cubreMapeo() {
        // Capturar la BiFunction en map(...) y devolver un FetchSpec cuyo one() aplica la función
        when(client.sql(anyString()).bind(eq("policy_id"), any()).map(any(BiFunction.class)))
                .thenAnswer(inv -> {
                    BiFunction<Row, RowMetadata, PolizaDomain> mapperFn = inv.getArgument(0);
                    @SuppressWarnings("unchecked")
                    FetchSpec<PolizaDomain> rowsSpec = mock(FetchSpec.class);

                    Row row = mock(Row.class);
                    RowMetadata meta = mock(RowMetadata.class);
                    when(row.get(eq("uuid"), eq(String.class))).thenReturn("UUID-1");
                    when(row.get(eq("policy_id"), eq(String.class))).thenReturn("POL123");
                    when(row.get(eq("tipo"), eq(String.class))).thenReturn("AUTO");
                    when(row.get(eq("fecha_inicio"), eq(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
                    when(row.get(eq("valor"), eq(BigDecimal.class))).thenReturn(BigDecimal.valueOf(1000));
                    PolizaDomain d = mapperFn.apply(row, meta);
                    when(rowsSpec.one()).thenReturn(Mono.just(d));
                    return rowsSpec;
                });

        StepVerifier.create(adapter.buscarPorId("POL123"))
                .expectNextMatches(res -> res.getPolicyId().equals("POL123") && res.getTipo() == Tipo.AUTO)
                .verifyComplete();

        verify(mapper, atLeastOnce()).entityToDomain(any(PolizaEntity.class));
    }
}
