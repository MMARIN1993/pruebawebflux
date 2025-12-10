package com.surabancaweb.reactiveweb.api.ws.poliza.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class PolizaRequestDTO {

    @NotBlank(message = "policyId no debe estar vacío")
    @Size(max = 10, message = "policyId debe tener máximo 10 caracteres")
    private String policyId;

    @NotBlank(message = "tipo no debe estar vacío")
    @Pattern(regexp = "AUTO|VIDA|HOGAR|SOAT", message = "tipo debe ser uno de: AUTO, VIDA, HOGAR, SOAT")
    private String tipo;

    @NotNull(message = "fechaInicio no debe ser nula")
    private LocalDate fechaInicio;

    @NotNull(message = "valor no debe ser nulo")
    private BigDecimal valor;

}
