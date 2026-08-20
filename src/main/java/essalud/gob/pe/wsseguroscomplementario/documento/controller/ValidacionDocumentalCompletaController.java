package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionDocumentalCompletaResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.ValidacionDocumentalCompletaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos/validacion/completa")
public class ValidacionDocumentalCompletaController {

    private final ValidacionDocumentalCompletaService validacionDocumentalCompletaService;

    public ValidacionDocumentalCompletaController(
            ValidacionDocumentalCompletaService validacionDocumentalCompletaService
    ) {
        this.validacionDocumentalCompletaService = validacionDocumentalCompletaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ValidacionDocumentalCompletaResponse>> validarDocumentoCompleto(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("registroInternoProceso") String registroInternoProceso,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam(value = "tipoDocumentoTrabajador", required = false) String tipoDocumentoTrabajador,
            @RequestParam("numeroDocumentoTrabajador") String numeroDocumentoTrabajador,
            @RequestParam(value = "idDocumentoCargado", required = false) String idDocumentoCargado,
            @RequestParam(value = "usuarioAutenticado", required = false) String usuarioAutenticado,
            @RequestParam(value = "ipOrigen", required = false) String ipOrigen,
            @RequestParam(value = "datosSesionDispositivo", required = false) String datosSesionDispositivo
    ) {
        try {
            ValidacionDocumentalCompletaResponse response =
                    validacionDocumentalCompletaService.validarDocumentoCompleto(
                            archivo,
                            registroInternoProceso,
                            tipoDocumento,
                            tipoDocumentoTrabajador,
                            numeroDocumentoTrabajador,
                            idDocumentoCargado,
                            usuarioAutenticado,
                            ipOrigen,
                            datosSesionDispositivo
                    );

            if (!response.isDocumentoAprobado()) {
                return ResponseEntity.ok(
                        ApiResponse.error(
                                response.getMensajeValidacion(),
                                response
                        )
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