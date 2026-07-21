package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarElementosVisualesResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.ValidacionElementosVisualesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos/validacion/elementos-visuales")
public class ValidacionElementosVisualesController {

    private final ValidacionElementosVisualesService validacionElementosVisualesService;

    public ValidacionElementosVisualesController(
            ValidacionElementosVisualesService validacionElementosVisualesService
    ) {
        this.validacionElementosVisualesService = validacionElementosVisualesService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ValidarElementosVisualesResponse>> validarElementosVisuales(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoDocumento") String tipoDocumento
    ) {
        try {
            ValidarElementosVisualesResponse response =
                    validacionElementosVisualesService.validarElementosVisuales(
                            archivo,
                            tipoDocumento
                    );

            if (!response.isElementosVisualesValidos()) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error(response.getMensajeValidacion(), response)
                );
            }

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeValidacion(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }
}