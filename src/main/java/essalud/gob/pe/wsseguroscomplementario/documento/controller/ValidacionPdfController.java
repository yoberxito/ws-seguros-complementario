package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionPdfResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.ValidacionPdfService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos/pdf")
public class ValidacionPdfController {

    private final ValidacionPdfService validacionPdfService;

    public ValidacionPdfController(ValidacionPdfService validacionPdfService) {
        this.validacionPdfService = validacionPdfService;
    }

    @PostMapping(
            value = "/validar-estructura",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ValidacionPdfResponse>> validarEstructuraPdf(
            @RequestParam("archivo") MultipartFile archivo
    ) {
        ValidacionPdfResponse resultado = validacionPdfService.validarEstructuraTecnica(archivo);

        ApiResponse<ValidacionPdfResponse> response = resultado.isValido()
                ? ApiResponse.exito(resultado.getMensajeValidacion(), resultado)
                : ApiResponse.error(resultado.getMensajeValidacion(), resultado);

        return ResponseEntity.ok(response);
    }
}