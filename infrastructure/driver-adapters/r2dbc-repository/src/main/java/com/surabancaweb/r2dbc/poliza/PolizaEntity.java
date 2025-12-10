package com.surabancaweb.r2dbc.poliza;

import com.surabancaweb.domain.model.poliza.PolizaDomain;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;




@Data
@Builder(toBuilder = true)
public class PolizaEntity {
    private String uuid;
    private String policyId;
    private PolizaDomain.Tipo tipo;
    private LocalDate fechaInicio;
    private BigDecimal valor;
}
