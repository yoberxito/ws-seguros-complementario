package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarDocumentoSelladoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoSellado;
import essalud.gob.pe.wsseguroscomplementario.documento.service.GeneracionDocumentoSelladoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/documentos/sellado")
public class GeneracionDocumentoSelladoController {

    private final GeneracionDocumentoSelladoService generacionDocumentoSelladoService;

    public GeneracionDocumentoSelladoController(
            GeneracionDocumentoSelladoService generacionDocumentoSelladoService
    ) {
        this.generacionDocumentoSelladoService = generacionDocumentoSelladoService;
    }

    @PostMapping(
            value = "/generar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<GenerarDocumentoSelladoResponse>> generarDocumentoSellado(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("registroInternoProceso") String registroInternoProceso,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("numeroDocumentoTrabajador") String numeroDocumentoTrabajador
    ) {
        try {
            GenerarDocumentoSelladoResponse response =
                    generacionDocumentoSelladoService.generarDocumentoSellado(
                            archivo,
                            registroInternoProceso,
                            tipoDocumento,
                            numeroDocumentoTrabajador
                    );

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeSellado(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @GetMapping("/{idDocumentoSellado}/archivo")
    public ResponseEntity<byte[]> visualizarDocumentoSellado(
            @PathVariable String idDocumentoSellado
    ) {
        try {
            DocumentoSellado documentoSellado =
                    generacionDocumentoSelladoService.obtenerDocumentoSellado(idDocumentoSellado);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + documentoSellado.getNombreArchivo() + "\""
                    )
                    .body(documentoSellado.getContenidoArchivo());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage().getBytes(StandardCharsets.UTF_8));
        }
    }
}