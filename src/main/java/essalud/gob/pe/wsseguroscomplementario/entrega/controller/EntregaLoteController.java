package essalud.gob.pe.wsseguroscomplementario.entrega.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.dto.ConfirmarAcuseEntregaResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.dto.ConsultarEntregaPublicaResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.exception.EstadoEntregaException;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.EntregaLoteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
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

    public EntregaLoteController(
            EntregaLoteService entregaLoteService
    ) {
        this.entregaLoteService =
                entregaLoteService;
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
     * El OTP NO pertenece a este controller.
     *
     * Generación y validación se ejecutan desde Angular
     * contra los servicios institucionales existentes.
     */
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
                                    ? "La recepción ya se encontraba registrada."
                                    : "Recepción registrada correctamente.",
                            response
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ApiResponse.error(
                                    "La entrega solicitada no existe o el enlace no es válido.",
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
                                    "No fue posible registrar la recepción del lote.",
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
                                "La entrega solicitada no existe o el enlace no es válido.",
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