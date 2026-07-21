package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarAutorizacionDescuentoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarAutorizacionDescuentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.GeneracionAutorizacionDescuentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documentos/autorizacion-descuento")
public class GeneracionAutorizacionDescuentoController {

    private final GeneracionAutorizacionDescuentoService generacionAutorizacionDescuentoService;

    public GeneracionAutorizacionDescuentoController(
            GeneracionAutorizacionDescuentoService generacionAutorizacionDescuentoService
    ) {
        this.generacionAutorizacionDescuentoService = generacionAutorizacionDescuentoService;
    }

    @PostMapping("/generar")
    public ResponseEntity<ApiResponse<GenerarAutorizacionDescuentoResponse>> generarAutorizacionDescuento(
            @RequestBody GenerarAutorizacionDescuentoRequest request
    ) {
        try {
            GenerarAutorizacionDescuentoResponse response =
                    generacionAutorizacionDescuentoService.generarAutorizacionDescuento(request);

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