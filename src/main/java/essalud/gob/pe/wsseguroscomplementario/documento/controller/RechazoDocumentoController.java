package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RechazoDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.RechazoDocumentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos/rechazos")
public class RechazoDocumentoController {

    private final RechazoDocumentoService rechazoDocumentoService;

    public RechazoDocumentoController(RechazoDocumentoService rechazoDocumentoService) {
        this.rechazoDocumentoService = rechazoDocumentoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RechazoDocumentoResponse>>> listarRechazos() {
        List<RechazoDocumentoResponse> response = rechazoDocumentoService.listarRechazos();

        return ResponseEntity.ok(
                ApiResponse.exito("Consulta de rechazos documentales realizada correctamente.", response)
        );
    }

    @GetMapping("/{idRechazoDocumental}")
    public ResponseEntity<ApiResponse<RechazoDocumentoResponse>> buscarRechazoPorId(
            @PathVariable String idRechazoDocumental
    ) {
        try {
            RechazoDocumentoResponse response = rechazoDocumentoService.buscarPorId(idRechazoDocumental);

            return ResponseEntity.ok(
                    ApiResponse.exito("Rechazo documental encontrado.", response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }
}