package essalud.gob.pe.wsseguroscomplementario.otp.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.otp.dto.SolicitarOtpRequest;
import essalud.gob.pe.wsseguroscomplementario.otp.dto.SolicitarOtpResponse;
import essalud.gob.pe.wsseguroscomplementario.otp.dto.ValidarOtpRequest;
import essalud.gob.pe.wsseguroscomplementario.otp.service.OtpIntegracionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import essalud.gob.pe.wsseguroscomplementario.otp.dto.ValidarOtpExternoResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {

    private final OtpIntegracionService
            otpIntegracionService;

    public OtpController(
            OtpIntegracionService
                    otpIntegracionService
    ) {
        this.otpIntegracionService =
                otpIntegracionService;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<
            ApiResponse<SolicitarOtpResponse>
            > solicitarOtp(
            @RequestBody
            SolicitarOtpRequest request
    ) {

        try {

            if (request == null) {
                throw new IllegalArgumentException(
                        "La solicitud OTP es obligatoria."
                );
            }

            SolicitarOtpResponse response =
                    otpIntegracionService
                            .solicitarOtp(
                                    request
                                            .getCorreo()
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensaje(),
                            response
                    )
            );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
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
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }
    @PostMapping("/validar")
    public ResponseEntity<
            ApiResponse<ValidarOtpExternoResponse>
            > validarOtp(
            @RequestBody
            ValidarOtpRequest request
    ) {

        try {

            if (request == null) {

                throw new IllegalArgumentException(
                        "La solicitud de validacion OTP es obligatoria."
                );
            }

            ValidarOtpExternoResponse response =
                    otpIntegracionService
                            .validarOtp(
                                    request
                                            .getCorreo(),

                                    request
                                            .getCodigo()
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensaje(),
                            response
                    )
            );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
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
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }
}