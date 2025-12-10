package com.surabancaweb.reactiveweb.api.ws.poliza;

import com.surabancaweb.domain.usecase.poliza.EliminarPolizaUseCase;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Eliminación de Pólizas de Seguros",
        description = "Servicio para la eliminación de pólizas de seguros por su ID.")
public class EliminarPolizaService {

    private final EliminarPolizaUseCase eliminarPolizaUseCase;

    @Operation(summary = "Servicio para eliminar Poliza por id", responses = {
            @ApiResponse(description = "Successful Operation", responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(hidden = true))),})
    @DeleteMapping(path = "/polizas/{polizaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseDTO<Object>>> eliminarPorId(
            @Valid @NotBlank(message = "polizaId no debe estar vacío")
            @Size(max = 10, message = "polizaId debe tener máximo 10 caracteres")
            @PathVariable String polizaId) {
        return eliminarPolizaUseCase.eliminarPorPolicyId(polizaId)
                .thenReturn(ResponseEntity.ok(new ResponseDTO<Object>(200, "Poliza eliminada exitosamente", null, 0)))
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDTO<Object>(400, "Error al eliminar la Poliza: " + ex.getMessage(), null, 0))));
    }
}
