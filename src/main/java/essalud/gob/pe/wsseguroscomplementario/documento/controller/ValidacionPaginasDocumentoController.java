package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarPaginasDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.ValidacionPaginasDocumentoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos/validacion")
public class ValidacionPaginasDocumentoController {

    private final ValidacionPaginasDocumentoService validacionPaginasDocumentoService;

    public ValidacionPaginasDocumentoController(
            ValidacionPaginasDocumentoService validacionPaginasDocumentoService
    ) {
        this.validacionPaginasDocumentoService = validacionPaginasDocumentoService;
    }

    @PostMapping(
            value = "/paginas",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ValidarPaginasDocumentoResponse>> validarPaginas(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("registroInternoProceso") String registroInternoProceso,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("numeroDocumentoTrabajador") String numeroDocumentoTrabajador
    ) {
        try {
            ValidarPaginasDocumentoResponse response =
                    validacionPaginasDocumentoService.validarPaginas(
                            archivo,
                            registroInternoProceso,
                            tipoDocumento,
                            numeroDocumentoTrabajador
                    );

            if (!response.isPaginasValidas()) {
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