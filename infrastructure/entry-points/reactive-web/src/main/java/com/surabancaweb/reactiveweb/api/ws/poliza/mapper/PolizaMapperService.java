package com.surabancaweb.reactiveweb.api.ws.poliza.mapper;

import com.surabancaweb.domain.model.poliza.PolizaDomain;
import com.surabancaweb.reactiveweb.api.ws.poliza.dto.PolizaRequestDTO;
import com.surabancaweb.reactiveweb.api.ws.poliza.dto.PolizaResponseDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.FIELD,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PolizaMapperService {

    PolizaDomain dtoToDomain(PolizaRequestDTO polizaDTO);

    PolizaResponseDTO domainToDTO(PolizaDomain polizaDomain);

}
