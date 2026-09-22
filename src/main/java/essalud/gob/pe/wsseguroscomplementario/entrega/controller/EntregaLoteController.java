package essalud.gob.pe.wsseguroscomplementario.entrega.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.dto.ConfirmarAcuseEntregaResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.dto.ConsultarEntregaPublicaResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.exception.EstadoEntregaException;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.DescargaLotePreparada;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.DescargaLoteService;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.EntregaLoteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/entregas/publicas"
)
public class EntregaLoteController {

    private final EntregaLoteService
            entregaLoteService;

    private final DescargaLoteService
            descargaLoteService;

    public EntregaLoteController(
            EntregaLoteService entregaLoteService,
            DescargaLoteService descargaLoteService
    ) {
        this.entregaLoteService =
                entregaLoteService;

        this.descargaLoteService =
                descargaLoteService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<
            ApiResponse<ConsultarEntregaPublicaResponse>>
    consultarEntrega(
            @PathVariable String token
    ) {

        try {

            ConsultarEntregaPublicaResponse response =
                    entregaLoteService
                            .consultarPorToken(token);

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            "Entrega obtenida correctamente.",
                            response
                    )
            );

        } catch (IllegalArgumentException e) {

            return respuestaEntregaNoEncontrada();
        }
    }

    /*
     * Primera entrega controlada del lote PERSONAL.
     *
     * Este GET solamente transfiere el ZIP.
     * NO registra DESCARGA_LOTE_COMPLETADA.
     *
     * La evidencia se registra en un POST independiente
     * despues de que Angular reciba completamente el Blob.
     */
    @GetMapping("/{token}/lote")
    public ResponseEntity<byte[]> descargarLote(
            @PathVariable String token
    ) {

        try {

            DescargaLotePreparada descarga =
                    descargaLoteService
                            .prepararDescargaPorToken(
                                    token
                            );

            byte[] contenido =
                    descarga.getContenido();

            return ResponseEntity
                    .ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + descarga.getNombreArchivo()
                                    + "\""
                    )
                    .contentType(
                            MediaType.parseMediaType(
                                    "application/zip"
                            )
                    )
                    .contentLength(
                            contenido.length
                    )
                    .body(
                            contenido
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .build();

        } catch (EstadoEntregaException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.CONFLICT
                    )
                    .build();

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .build();
        }
    }

    /*
     * Angular llama a este endpoint solamente despues de
     * recibir completamente el Blob del ZIP.
     *
     * Secuencia vigente:
     * correo -> link -> descarga ZIP -> confirmacion -> Historical.
     */
    @PostMapping("/{token}/descarga-completada")
    public ResponseEntity<
            ApiResponse<Boolean>>
    registrarDescargaCompletada(
            @PathVariable String token,
            HttpServletRequest httpServletRequest
    ) {

        try {

            String ipOrigen =
                    obtenerIpOrigen(
                            httpServletRequest
                    );

            String datosSesionDispositivo =
                    obtenerDatosSesionDispositivo(
                            httpServletRequest
                    );

            entregaLoteService
                    .registrarDescargaCompletadaPorToken(
                            token,
                            ipOrigen,
                            datosSesionDispositivo
                    );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            "Descarga completa del lote registrada correctamente.",
                            Boolean.TRUE
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ApiResponse.error(
                                    "La entrega solicitada no existe o el enlace no es valido.",
                                    null
                            )
                    );

        } catch (EstadoEntregaException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.error(
                                    e.getMessage(),
                                    null
                            )
                    );

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            ApiResponse.error(
                                    "No fue posible registrar la descarga del lote.",
                                    null
                            )
                    );
        }
    }

    @PostMapping("/{token}/confirmar")
    public ResponseEntity<
            ApiResponse<ConfirmarAcuseEntregaResponse>>
    confirmarAcuse(
            @PathVariable String token,
            HttpServletRequest httpServletRequest
    ) {

        try {

            String ipOrigen =
                    obtenerIpOrigen(
                            httpServletRequest
                    );

            String datosSesionDispositivo =
                    obtenerDatosSesionDispositivo(
                            httpServletRequest
                    );

            ConfirmarAcuseEntregaResponse response =
                    entregaLoteService
                            .confirmarAcusePorToken(
                                    token,
                                    ipOrigen,
                                    datosSesionDispositivo
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.isYaRegistrado()
                                    ? "La recepciÃ³n ya se encontraba registrada."
                                    : "RecepciÃ³n registrada correctamente.",
                            response
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ApiResponse.error(
                                    "La entrega solicitada no existe o el enlace no es vÃ¡lido.",
                                    null
                            )
                    );

        } catch (EstadoEntregaException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.error(
                                    e.getMessage(),
                                    null
                            )
                    );

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            ApiResponse.error(
                                    "No fue posible registrar la recepciÃ³n del lote.",
                                    null
                            )
                    );
        }
    }

    private ResponseEntity<
            ApiResponse<ConsultarEntregaPublicaResponse>>
    respuestaEntregaNoEncontrada() {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.error(
                                "La entrega solicitada no existe o el enlace no es vÃ¡lido.",
                                null
                        )
                );
    }

    private String obtenerDatosSesionDispositivo(
            HttpServletRequest request
    ) {

        String userAgent =
                request.getHeader(
                        "User-Agent"
                );

        if (
                userAgent == null
                        ||
                        userAgent.trim().isEmpty()
        ) {
            return null;
        }

        return userAgent.trim();
    }

    private String obtenerIpOrigen(
            HttpServletRequest request
    ) {

        String forwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (
                forwardedFor != null
                        &&
                        !forwardedFor
                                .trim()
                                .isEmpty()
        ) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request.getRemoteAddr();
    }
}
