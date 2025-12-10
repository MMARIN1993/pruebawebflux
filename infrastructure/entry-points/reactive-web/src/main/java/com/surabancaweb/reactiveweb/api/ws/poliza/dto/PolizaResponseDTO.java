package com.surabancaweb.reactiveweb.api.ws.poliza.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PolizaResponseDTO {
    private String uuid;
    private String policyId;
    private String tipo;
    private LocalDate fechaInicio;
    private BigDecimal valor;
}
