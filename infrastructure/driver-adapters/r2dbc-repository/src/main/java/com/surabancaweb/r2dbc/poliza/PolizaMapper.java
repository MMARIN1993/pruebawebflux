package com.surabancaweb.r2dbc.poliza;



import com.surabancaweb.domain.model.poliza.PolizaDomain;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.FIELD,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PolizaMapper {

    PolizaDomain entityToDomain(PolizaEntity polizaEntity);

    PolizaEntity domainToEntity(PolizaDomain polizaDomain);
}
