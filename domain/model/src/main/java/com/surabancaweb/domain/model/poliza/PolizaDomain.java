package com.surabancaweb.domain.model.poliza;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class PolizaDomain {
    private String uuid;
    private String policyId;
    private Tipo tipo;
    private LocalDate fechaInicio;
    private BigDecimal valor;

    public enum Tipo {
        AUTO, VIDA, HOGAR, SOAT
    }
}

