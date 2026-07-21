package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.PublicarDocumentoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.PublicarDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;
import essalud.gob.pe.wsseguroscomplementario.documento.service.PublicacionDocumentoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos/publicacion")
public class PublicacionDocumentoController {

    private final PublicacionDocumentoService publicacionDocumentoService;

    public PublicacionDocumentoController(
            PublicacionDocumentoService publicacionDocumentoService
    ) {
        this.publicacionDocumentoService = publicacionDocumentoService;
    }

    @PostMapping("/publicar")
    public ResponseEntity<ApiResponse<PublicarDocumentoResponse>> publicarDocumento(
            @RequestBody PublicarDocumentoRequest request
    ) {
        try {
            PublicarDocumentoResponse response =
                    publicacionDocumentoService.publicarDocumento(request);

            if (!response.isPublicado()) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error(response.getMensajePublicacion(), response)
                );
            }

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajePublicacion(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @GetMapping("/{idDocumentoPublicado}/archivo")
    public ResponseEntity<byte[]> visualizarDocumentoPublicado(
            @PathVariable String idDocumentoPublicado
    ) {
        try {
            DocumentoPublicado documentoPublicado =
                    publicacionDocumentoService.obtenerDocumentoPublicado(idDocumentoPublicado);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + documentoPublicado.getNombreArchivo() + "\""
                    )
                    .body(documentoPublicado.getContenidoArchivo());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage().getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/proceso/{registroInternoProceso}/trabajador/{numeroDocumentoTrabajador}")
    public ResponseEntity<ApiResponse<List<PublicarDocumentoResponse>>> listarDocumentosPublicados(
            @PathVariable String registroInternoProceso,
            @PathVariable String numeroDocumentoTrabajador
    ) {
        List<PublicarDocumentoResponse> documentos =
                publicacionDocumentoService.listarDocumentosPublicadosPorProcesoYTrabajador(
                        registroInternoProceso,
                        numeroDocumentoTrabajador
                );

        return ResponseEntity.ok(
                ApiResponse.exito("Documentos publicados obtenidos correctamente.", documentos)
        );
    }
}