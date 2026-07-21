package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarCorrespondenciaDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.ValidacionCorrespondenciaDocumentoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos/validacion")
public class ValidacionCorrespondenciaDocumentoController {

    private final ValidacionCorrespondenciaDocumentoService validacionCorrespondenciaDocumentoService;

    public ValidacionCorrespondenciaDocumentoController(
            ValidacionCorrespondenciaDocumentoService validacionCorrespondenciaDocumentoService
    ) {
        this.validacionCorrespondenciaDocumentoService = validacionCorrespondenciaDocumentoService;
    }

    @PostMapping(
            value = "/correspondencia",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ValidarCorrespondenciaDocumentoResponse>> validarCorrespondencia(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("registroInternoProceso") String registroInternoProceso,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("numeroDocumentoTrabajador") String numeroDocumentoTrabajador
    ) {
        try {
            ValidarCorrespondenciaDocumentoResponse response =
                    validacionCorrespondenciaDocumentoService.validarCorrespondencia(
                            archivo,
                            registroInternoProceso,
                            tipoDocumento,
                            numeroDocumentoTrabajador
                    );

            if (!response.isCorrespondenciaValida()) {
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