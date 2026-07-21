package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarSelloEssaludResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.ValidacionSelloEssaludService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos/validacion")
public class ValidacionSelloEssaludController {

    private final ValidacionSelloEssaludService validacionSelloEssaludService;

    public ValidacionSelloEssaludController(
            ValidacionSelloEssaludService validacionSelloEssaludService
    ) {
        this.validacionSelloEssaludService = validacionSelloEssaludService;
    }

    @PostMapping(
            value = "/sello-essalud",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ValidarSelloEssaludResponse>> validarSelloEssalud(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoDocumento") String tipoDocumento
    ) {
        try {
            ValidarSelloEssaludResponse response =
                    validacionSelloEssaludService.validarSelloEssalud(
                            archivo,
                            tipoDocumento
                    );

            if (!response.isSelloValido()) {
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