package essalud.gob.pe.wsseguroscomplementario.entrega.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.ConsultarEntregaPublicaResponse;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.SolicitarOtpEntregaResponse;
import essalud.gob.pe.wsseguroscomplementario.entrega.exception.EstadoEntregaException;
import essalud.gob.pe.wsseguroscomplementario.entrega.service.EntregaLoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.ValidarOtpEntregaRequest;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.ValidarOtpEntregaResponse;
import org.springframework.web.bind.annotation.RequestBody;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.ConfirmarAcuseEntregaResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(
        "/api/v1/entregas/publicas"
)
public class EntregaLoteController {

    private final EntregaLoteService
            entregaLoteService;
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
                        userAgent
                                .trim()
                                .isEmpty()
        ) {

            return null;
        }

        return userAgent.trim();
    }
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
                            .consultarPorToken(
                                    token
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            "Entrega obtenida correctamente.",
                            response
                    )
            );

        } catch (
                IllegalArgumentException e
        ) {

            return respuestaEntregaNoEncontrada();
        }
    }

    @PostMapping(
            "/{token}/otp/solicitar"
    )
    public ResponseEntity<
            ApiResponse<SolicitarOtpEntregaResponse>>
    solicitarOtp(
            @PathVariable String token
    ) {

        try {

            SolicitarOtpEntregaResponse response =
                    entregaLoteService
                            .solicitarOtpPorToken(
                                    token
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            "Solicitud OTP procesada correctamente.",
                            response
                    )
            );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    "La entrega solicitada no existe o el enlace no es válido.",
                                    null
                            )
                    );

        } catch (
                EstadoEntregaException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.CONFLICT
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage(),
                                    null
                            )
                    );

        } catch (
                IllegalStateException e
        ) {

            /*
             * No exponemos hacia Internet los detalles
             * internos de la integración institucional.
             */

            return ResponseEntity
                    .status(
                            HttpStatus.BAD_GATEWAY
                    )
                    .body(
                            ApiResponse.error(
                                    "No fue posible procesar el envío del código OTP.",
                                    null
                            )
                    );
        }
    }

    @PostMapping(
            "/{token}/otp/validar"
    )
    public ResponseEntity<
            ApiResponse<ValidarOtpEntregaResponse>>
    validarOtp(
            @PathVariable String token,
            @RequestBody
            ValidarOtpEntregaRequest request,
            HttpServletRequest httpServletRequest
    ) {

        /*
         * El body contiene solamente el código.
         *
         * Nunca aceptamos el correo desde Internet.
         */
        if (
                request == null
                        ||
                        request.getCodigo() == null
                        ||
                        request.getCodigo()
                                .trim()
                                .isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            ApiResponse.error(
                                    "El código OTP es obligatorio.",
                                    null
                            )
                    );
        }

        try {


            String ipOrigen =
                    obtenerIpOrigen(
                            httpServletRequest
                    );

            String datosSesionDispositivo =
                    obtenerDatosSesionDispositivo(
                            httpServletRequest
                    );

            ValidarOtpEntregaResponse response =
                    entregaLoteService
                            .validarOtpPorToken(
                                    token,
                                    request,
                                    ipOrigen,
                                    datosSesionDispositivo
                            );

            /*
             * OTP inválido sigue siendo HTTP 200.
             *
             * Es un resultado funcional,
             * no una falla técnica.
             */
            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.isOtpValidado()
                                    ? "Validación OTP procesada correctamente."
                                    : "El código OTP no pudo ser validado.",
                            response
                    )
            );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    "La entrega solicitada no existe o el enlace no es válido.",
                                    null
                            )
                    );

        } catch (
                EstadoEntregaException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.CONFLICT
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage(),
                                    null
                            )
                    );

        } catch (
                IllegalStateException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.BAD_GATEWAY
                    )
                    .body(
                            ApiResponse.error(
                                    "No fue posible procesar la validación del código OTP.",
                                    null
                            )
                    );
        }
    }

    @PostMapping(
            "/{token}/confirmar"
    )
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

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    "La entrega solicitada no existe o el enlace no es válido.",
                                    null
                            )
                    );

        } catch (
                EstadoEntregaException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.CONFLICT
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage(),
                                    null
                            )
                    );

        } catch (
                IllegalStateException e
        ) {

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
                .status(
                        HttpStatus.NOT_FOUND
                )
                .body(
                        ApiResponse.error(
                                "La entrega solicitada no existe o el enlace no es válido.",
                                null
                        )
                );
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