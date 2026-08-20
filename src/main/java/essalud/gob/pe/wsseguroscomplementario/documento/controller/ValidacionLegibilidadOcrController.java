package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarLegibilidadOcrResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.ValidacionLegibilidadOcrService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(
        "/api/v1/documentos/validacion/legibilidad"
)
public class ValidacionLegibilidadOcrController {

    private final ValidacionLegibilidadOcrService
            validacionLegibilidadOcrService;

    public ValidacionLegibilidadOcrController(
            ValidacionLegibilidadOcrService
                    validacionLegibilidadOcrService
    ) {
        this.validacionLegibilidadOcrService =
                validacionLegibilidadOcrService;
    }

    @PostMapping
    public ResponseEntity<
            ApiResponse<ValidarLegibilidadOcrResponse>>
    validarLegibilidad(
            @RequestParam("archivo")
            MultipartFile archivo,

            @RequestParam("tipoDocumento")
            String tipoDocumento
    ) {

        try {

            ValidarLegibilidadOcrResponse response =
                    validacionLegibilidadOcrService
                            .validarLegibilidad(
                                    archivo,
                                    tipoDocumento
                            );

            if (
                    !response
                            .isLegibilidadValida()
            ) {

                return ResponseEntity.ok(
                        ApiResponse.error(
                                response
                                        .getMensajeValidacion(),
                                response
                        )
                );
            }

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response
                                    .getMensajeValidacion(),
                            response
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            ApiResponse.error(
                                    e.getMessage(),
                                    null
                            )
                    );

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            ApiResponse.error(
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }
}