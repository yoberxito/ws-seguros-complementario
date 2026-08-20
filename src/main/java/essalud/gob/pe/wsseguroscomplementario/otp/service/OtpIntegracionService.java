package essalud.gob.pe.wsseguroscomplementario.otp.service;

import essalud.gob.pe.wsseguroscomplementario.otp.dto.EnviarOtpCorreoExternoResponse;
import essalud.gob.pe.wsseguroscomplementario.otp.dto.GenerarOtpExternoResponse;
import essalud.gob.pe.wsseguroscomplementario.otp.dto.SolicitarOtpResponse;
import essalud.gob.pe.wsseguroscomplementario.otp.dto.ValidarOtpExternoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class OtpIntegracionService {

    private static final String
            CODIGO_GENERACION_EXITOSA =
            "0";

    private static final String
            CODIGO_CORREO_EXITOSO =
            "0";

    private final RestClient restClient;

    private final String generarUrl;
    private final String validarUrl;
    private final String emailUrl;
    private final String token;

    public OtpIntegracionService(
            @Value("${integraciones.otp.generar-url}")
            String generarUrl,

            @Value("${integraciones.otp.validar-url}")
            String validarUrl,

            @Value("${integraciones.otp.email-url}")
            String emailUrl,

            @Value("${integraciones.otp.token:}")
            String token
    ) {

        this.restClient =
                RestClient.create();

        this.generarUrl =
                generarUrl;

        this.validarUrl =
                validarUrl;

        this.emailUrl =
                emailUrl;

        this.token =
                token;
    }

    public SolicitarOtpResponse solicitarOtp(
            String correo
    ) {

        validarCorreo(
                correo
        );

        /*
         * Validamos el Bearer antes de generar.
         *
         * De esta forma no creamos un OTP
         * temporal que luego no podremos
         * enviar por correo.
         */
        validarTokenConfigurado();

        GenerarOtpExternoResponse generacion =
                generarOtp(
                        correo
                );

        if (generacion == null) {
            throw new IllegalStateException(
                    "El servicio de generacion OTP respondio sin contenido."
            );
        }

        SolicitarOtpResponse response =
                new SolicitarOtpResponse();

        response.setCodResultadoGeneracion(
                generacion
                        .getCodResultado()
        );

        response.setMensaje(
                generacion
                        .getMensaje()
        );

        /*
         * Contrato real comprobado:
         *
         * 0 = OTP nuevo generado.
         *
         * 1 = ya existe un OTP activo.
         *     codigoGenerado viene null.
         */
        if (
                !CODIGO_GENERACION_EXITOSA
                        .equals(
                                generacion
                                        .getCodResultado()
                        )
        ) {

            response.setOtpGenerado(
                    false
            );

            response.setCorreoEnviado(
                    false
            );

            return response;
        }

        if (
                campoVacio(
                        generacion
                                .getCodigoGenerado()
                )
        ) {

            throw new IllegalStateException(
                    "El servicio indico que genero un OTP, pero no devolvio codigoGenerado."
            );
        }

        response.setOtpGenerado(
                true
        );

        EnviarOtpCorreoExternoResponse envio =
                enviarOtpPorCorreo(
                        generacion
                                .getCodigoGenerado(),

                        correo
                );

        if (envio == null) {
            throw new IllegalStateException(
                    "El servicio de correo OTP respondio sin contenido."
            );
        }

        response.setCodigoResultadoCorreo(
                envio
                        .getCodigoResultado()
        );

        response.setMensajeCorreo(
                envio
                        .getMensaje()
        );

        boolean correoEnviado =
                CODIGO_CORREO_EXITOSO
                        .equals(
                                envio
                                        .getCodigoResultado()
                        );

        response.setCorreoEnviado(
                correoEnviado
        );

        if (!correoEnviado) {

            throw new IllegalStateException(
                    "El OTP fue generado, pero el servicio de correo no confirmo el envio. "
                            + "Resultado: "
                            + envio.getCodigoResultado()
                            + ". Mensaje: "
                            + envio.getMensaje()
            );
        }

        response.setMensaje(
                "Codigo OTP generado y enviado por correo correctamente."
        );

        return response;
    }

    public ValidarOtpExternoResponse validarOtp(
            String correo,
            String codigo
    ) {

        validarCorreo(
                correo
        );

        if (campoVacio(codigo)) {
            throw new IllegalArgumentException(
                    "El codigo OTP es obligatorio."
            );
        }

        validarTokenConfigurado();

        URI uri =
                construirUri(
                        validarUrl,
                        "correo",
                        correo.trim(),
                        "codigo",
                        codigo.trim()
                );

        try {

            ResponseEntity<ValidarOtpExternoResponse>
                    respuesta =
                    restClient
                            .get()
                            .uri(uri)
                            .headers(
                                    headers ->
                                            headers.setBearerAuth(
                                                    token.trim()
                                            )
                            )
                            .accept(
                                    MediaType.APPLICATION_JSON
                            )
                            .retrieve()
                            .toEntity(
                                    ValidarOtpExternoResponse.class
                            );

            if (
                    !respuesta
                            .getStatusCode()
                            .is2xxSuccessful()
            ) {

                throw new IllegalStateException(
                        "El servicio de validacion OTP respondio HTTP "
                                + respuesta
                                .getStatusCode()
                                .value()
                                + "."
                );
            }

            if (respuesta.getBody() == null) {

                throw new IllegalStateException(
                        "El servicio de validacion OTP respondio sin contenido."
                );
            }

            /*
             * valido=false NO es un error tecnico.
             *
             * El servicio institucional devuelve
             * HTTP 200 tanto para OTP valido como
             * para OTP no valido.
             */
            return respuesta.getBody();

        } catch (
                RestClientResponseException e
        ) {

            throw new IllegalStateException(
                    "El servicio de validacion OTP respondio HTTP "
                            + e.getStatusCode()
                            .value()
                            + ".",
                    e
            );

        } catch (
                RestClientException e
        ) {

            throw new IllegalStateException(
                    "No se pudo consumir el servicio institucional de validacion OTP.",
                    e
            );
        }
    }

    private GenerarOtpExternoResponse generarOtp(
            String correo
    ) {

        URI uri =
                construirUri(
                        generarUrl,
                        "correo",
                        correo.trim()
                );

        try {

            ResponseEntity<GenerarOtpExternoResponse>
                    respuesta =
                    restClient
                            .post()
                            .uri(uri)
                            .accept(
                                    MediaType.APPLICATION_JSON
                            )
                            .retrieve()
                            .toEntity(
                                    GenerarOtpExternoResponse.class
                            );

            if (
                    !respuesta
                            .getStatusCode()
                            .is2xxSuccessful()
            ) {

                throw new IllegalStateException(
                        "El servicio de generacion OTP respondio HTTP "
                                + respuesta
                                .getStatusCode()
                                .value()
                                + "."
                );
            }

            return respuesta.getBody();

        } catch (
                RestClientResponseException e
        ) {

            throw new IllegalStateException(
                    "El servicio de generacion OTP respondio HTTP "
                            + e.getStatusCode()
                            .value()
                            + ".",
                    e
            );

        } catch (
                RestClientException e
        ) {

            throw new IllegalStateException(
                    "No se pudo consumir el servicio institucional de generacion OTP.",
                    e
            );
        }
    }

    private EnviarOtpCorreoExternoResponse
    enviarOtpPorCorreo(
            String codigo,
            String correo
    ) {

        URI uri =
                construirUri(
                        emailUrl,
                        "codigo",
                        codigo.trim(),
                        "correoDestino",
                        correo.trim()
                );

        try {

            ResponseEntity<EnviarOtpCorreoExternoResponse>
                    respuesta =
                    restClient
                            .post()
                            .uri(uri)
                            .headers(
                                    headers ->
                                            headers.setBearerAuth(
                                                    token.trim()
                                            )
                            )
                            .accept(
                                    MediaType.APPLICATION_JSON
                            )
                            .retrieve()
                            .toEntity(
                                    EnviarOtpCorreoExternoResponse.class
                            );

            if (
                    !respuesta
                            .getStatusCode()
                            .is2xxSuccessful()
            ) {

                throw new IllegalStateException(
                        "El servicio de correo OTP respondio HTTP "
                                + respuesta
                                .getStatusCode()
                                .value()
                                + "."
                );
            }

            return respuesta.getBody();

        } catch (
                RestClientResponseException e
        ) {

            throw new IllegalStateException(
                    "El servicio de correo OTP respondio HTTP "
                            + e.getStatusCode()
                            .value()
                            + ".",
                    e
            );

        } catch (
                RestClientException e
        ) {

            throw new IllegalStateException(
                    "No se pudo consumir el servicio institucional de envio de correo OTP.",
                    e
            );
        }
    }

    private URI construirUri(
            String url,
            String... parametros
    ) {

        if (
                parametros == null
                        || parametros.length % 2 != 0
        ) {

            throw new IllegalArgumentException(
                    "Los parametros de la URL OTP son invalidos."
            );
        }

        UriComponentsBuilder builder =
                UriComponentsBuilder
                        .fromUriString(
                                url
                        );

        for (
                int i = 0;
                i < parametros.length;
                i += 2
        ) {

            builder.queryParam(
                    parametros[i],
                    parametros[i + 1]
            );
        }

        return builder
                .build()
                .encode()
                .toUri();
    }

    private void validarCorreo(
            String correo
    ) {

        if (campoVacio(correo)) {

            throw new IllegalArgumentException(
                    "El correo es obligatorio."
            );
        }

        String valor =
                correo.trim();

        if (
                !valor.contains("@")
                        || valor.startsWith("@")
                        || valor.endsWith("@")
        ) {

            throw new IllegalArgumentException(
                    "El correo no tiene un formato valido."
            );
        }
    }

    private void validarTokenConfigurado() {

        if (campoVacio(token)) {

            throw new IllegalStateException(
                    "No se ha configurado el token para consumir los servicios OTP protegidos."
            );
        }
    }

    private boolean campoVacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }
}