package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarFirmaTrabajadorResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.ValidacionFirmaTrabajadorService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos/validacion")
public class ValidacionFirmaTrabajadorController {

    private final ValidacionFirmaTrabajadorService validacionFirmaTrabajadorService;

    public ValidacionFirmaTrabajadorController(
            ValidacionFirmaTrabajadorService validacionFirmaTrabajadorService
    ) {
        this.validacionFirmaTrabajadorService = validacionFirmaTrabajadorService;
    }

    @PostMapping(
            value = "/firma-trabajador",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ValidarFirmaTrabajadorResponse>> validarFirmaTrabajador(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoDocumento") String tipoDocumento
    ) {
        try {
            ValidarFirmaTrabajadorResponse response =
                    validacionFirmaTrabajadorService.validarFirmaTrabajador(
                            archivo,
                            tipoDocumento
                    );

            if (!response.isFirmaValida()) {
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