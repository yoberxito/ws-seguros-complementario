package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarFormulario6012Request;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarFormulario6012Response;
import essalud.gob.pe.wsseguroscomplementario.documento.service.GeneracionFormulario6012Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import essalud.gob.pe.wsseguroscomplementario.documento.service.RespaldoFormulario6012Service;
@RestController
@RequestMapping("/api/v1/documentos/formulario-6012")
public class GeneracionFormulario6012Controller {

    private final GeneracionFormulario6012Service generacionFormulario6012Service;
    private final RespaldoFormulario6012Service
            respaldoFormulario6012Service;
    public GeneracionFormulario6012Controller(
            GeneracionFormulario6012Service generacionFormulario6012Service,
            RespaldoFormulario6012Service respaldoFormulario6012Service
    ) {
        this.generacionFormulario6012Service =
                generacionFormulario6012Service;

        this.respaldoFormulario6012Service =
                respaldoFormulario6012Service;
    }

    @PostMapping("/generar")
    public ResponseEntity<ApiResponse<GenerarFormulario6012Response>> generarFormulario6012(
            @RequestBody GenerarFormulario6012Request request
    ) {
        try {
            GenerarFormulario6012Response response =
                    generacionFormulario6012Service.generarFormulario6012(request);

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeGeneracion(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }
    @GetMapping(
            "/proceso/{registroInternoProceso}/respaldo"
    )
    public ResponseEntity<byte[]> descargarRespaldoFormulario6012(
            @PathVariable String registroInternoProceso
    ) {

        try {

            String contenido =
                    respaldoFormulario6012Service
                            .obtenerRespaldo(
                                    registroInternoProceso
                            );

            byte[] archivo =
                    contenido.getBytes(
                            StandardCharsets.UTF_8
                    );

            return ResponseEntity
                    .ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    "text/plain;charset=UTF-8"
                            )
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"Respaldo-Formulario-6012-"
                                    + registroInternoProceso
                                    + ".txt\""
                    )
                    .body(
                            archivo
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .contentType(
                            MediaType.TEXT_PLAIN
                    )
                    .body(
                            e.getMessage()
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );
        }
    }
}