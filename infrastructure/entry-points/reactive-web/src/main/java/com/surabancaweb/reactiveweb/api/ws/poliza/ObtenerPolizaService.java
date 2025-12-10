package com.surabancaweb.reactiveweb.api.ws.poliza;

import com.surabancaweb.domain.usecase.poliza.ObtenerPolizaUseCase;
import com.surabancaweb.reactiveweb.api.ws.poliza.dto.PolizaResponseDTO;
import com.surabancaweb.reactiveweb.api.ws.poliza.mapper.PolizaMapperService;
import com.surabancaweb.reactiveweb.api.ws.util.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
@Tag(name = "Consulta de Pólizas",
        description = "Servicio para la consulta de pólizas de seguros.")
public class ObtenerPolizaService {


    private final ObtenerPolizaUseCase obtenerPolizaUseCase;
    private final PolizaMapperService polizaMapperService;

    @Operation(summary = "Servicio para consultar Poliza por id", responses = {
            @ApiResponse(description = "Successful Operation", responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping(path = "/polizas/{polizaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseDTO<PolizaResponseDTO>>> buscarPolizaPorId(
            @Valid @NotBlank(message = "polizaId no debe estar vacío")
            @Size(max = 10, message = "polizaId debe tener máximo 10 caracteres")
            @PathVariable String polizaId) {
        return obtenerPolizaUseCase.buscarPolizaPorPolizaId(polizaId)
                .map(domain -> ResponseEntity.ok(
                        new ResponseDTO<>(200, "Poliza encontrada exitosamente",
                                polizaMapperService.domainToDTO(domain), 1)
                ))
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(new ResponseDTO<>(400, "Error al consultar la Poliza: " + ex.getMessage(), null, 0))));
    }


    @Operation(summary = "Servicio para consultar todas las polizas", responses = {
            @ApiResponse(description = "Successful Operation", responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping(path = "/polizas", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseDTO<List<PolizaResponseDTO>>>> buscarPolizas() {
        return obtenerPolizaUseCase.obtenerPolizas()
                .map(domain ->polizaMapperService.domainToDTO(domain))
                .collectList()
                .map(lista->
                     ResponseEntity.ok(
                            new ResponseDTO<>(200, "Polizas encontradas exitosamente",
                                    lista, lista.size()))

                )
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDTO<>(400, "Error al listar polizas: " + error.getMessage(), null, 0))));
    }

}
