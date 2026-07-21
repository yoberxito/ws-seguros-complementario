package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarFormulario6012Request;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarFormulario6012Response;
import essalud.gob.pe.wsseguroscomplementario.documento.service.GeneracionFormulario6012Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documentos/formulario-6012")
public class GeneracionFormulario6012Controller {

    private final GeneracionFormulario6012Service generacionFormulario6012Service;

    public GeneracionFormulario6012Controller(
            GeneracionFormulario6012Service generacionFormulario6012Service
    ) {
        this.generacionFormulario6012Service = generacionFormulario6012Service;
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
}