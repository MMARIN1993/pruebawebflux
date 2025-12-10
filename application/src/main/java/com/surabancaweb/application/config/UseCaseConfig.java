package com.surabancaweb.application.config;

import com.surabancaweb.domain.model.poliza.gateway.PolizaRepository;
import com.surabancaweb.domain.usecase.poliza.MatricularPolizaUseCase;
import com.surabancaweb.domain.usecase.poliza.ObtenerPolizaUseCase;
import com.surabancaweb.domain.usecase.poliza.EliminarPolizaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class UseCaseConfig {

    @Bean
    public MatricularPolizaUseCase matricularPolizaUseCase(PolizaRepository polizaRepository) {
        return new MatricularPolizaUseCase(polizaRepository);
    }

    @Bean
    public ObtenerPolizaUseCase obtenerPolizaUseCase(PolizaRepository polizaRepository) {
        return new ObtenerPolizaUseCase(polizaRepository);
    }

    @Bean
    public EliminarPolizaUseCase eliminarPolizaUseCase(PolizaRepository polizaRepository) {
        return new EliminarPolizaUseCase(polizaRepository);
    }
}
