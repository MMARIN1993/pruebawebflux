package com.surabancaweb.reactiveweb.api.ws.poliza;

import com.surabancaweb.domain.usecase.poliza.MatricularPolizaUseCase;
import com.surabancaweb.reactiveweb.api.ws.poliza.mapper.PolizaMapperService;
import com.surabancaweb.reactiveweb.api.ws.util.ResponseDTO;
import com.surabancaweb.reactiveweb.api.ws.poliza.dto.PolizaRequestDTO;
import com.surabancaweb.reactiveweb.api.ws.poliza.dto.PolizaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Creacion y Edición de Pólizas",
        description = "Servicio para la creación y edición de pólizas de seguros.")
public class MatricularPolizaService {


    private final MatricularPolizaUseCase matricularPolizaUseCase;
    private final PolizaMapperService polizaMapperService;


    @Operation(summary = "Servicio para guardar polizas", responses = {
            @ApiResponse(description = "Successful Operation", responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Authentication Failure", content = @Content(schema = @Schema(hidden = true))) })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/polizas", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseDTO<PolizaResponseDTO>>>  matricularPoliza(@Valid @RequestBody PolizaRequestDTO polizaDTO) {
        return matricularPolizaUseCase.guardarPoliza(polizaMapperService.dtoToDomain(polizaDTO))
                .map(domain -> ResponseEntity.ok(
                        new ResponseDTO<>(200, "Poliza guardada exitosamente",
                                polizaMapperService.domainToDTO(domain), 1)
                ))
                .onErrorResume(ex -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ResponseDTO<>(400, "Error al guardar la Poliza: " + ex.getMessage(), null, 0)));
                });
    }


}
