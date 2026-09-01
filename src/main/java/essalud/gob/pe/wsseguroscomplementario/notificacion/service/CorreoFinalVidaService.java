package essalud.gob.pe.wsseguroscomplementario.notificacion.service;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;
import essalud.gob.pe.wsseguroscomplementario.notificacion.dto.EnviarCorreoFinalExternoResponse;
import essalud.gob.pe.wsseguroscomplementario.notificacion.dto.EnviarCorreoFinalRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class CorreoFinalVidaService {

    private static final String
            CODIGO_EXITO =
            "0";

    private final RestClient restClient;

    private final String url;
    private final String token;

    public CorreoFinalVidaService(
            @Value("${integraciones.correo-final.url}")
            String url,

            @Value("${integraciones.correo-final.token:}")
            String token
    ) {

        this.restClient =
                RestClient.create();

        this.url = url;
        this.token = token;
    }

    public EnviarCorreoFinalExternoResponse
    enviarCorreoFinal(
            EnviarCorreoFinalRequest request,
            List<DocumentoPublicado> documentos
    ) {

        validarRequest(
                request,
                documentos
        );

        validarTokenConfigurado();

        MultiValueMap<String, Object>
                partes =
                new LinkedMultiValueMap<>();

        /*
         * Postman confirmó que "req" se envía
         * como parte de texto dentro del
         * multipart/form-data.
         */
        HttpHeaders headersReq =
                new HttpHeaders();

        headersReq.setContentType(
                MediaType.TEXT_PLAIN
        );

        HttpEntity<String> parteReq =
                new HttpEntity<>(
                        construirJsonReq(
                                request
                        ),
                        headersReq
                );

        partes.add(
                "req",
                parteReq
        );

        /*
         * El servicio permite varios archivos
         * utilizando la misma clave
         * "archivosAdjuntos".
         */
        for (
                DocumentoPublicado documento :
                documentos
        ) {

            partes.add(
                    "archivosAdjuntos",
                    construirParteArchivo(
                            documento
                    )
            );
        }

        try {

            ResponseEntity<
                    EnviarCorreoFinalExternoResponse
                    > respuesta =
                    restClient
                            .post()
                            .uri(url)

                            .headers(
                                    headers ->
                                            headers
                                                    .setBearerAuth(
                                                            token.trim()
                                                    )
                            )

                            .contentType(
                                    MediaType
                                            .MULTIPART_FORM_DATA
                            )

                            .accept(
                                    MediaType
                                            .APPLICATION_JSON
                            )

                            .body(partes)

                            .retrieve()

                            .toEntity(
                                    EnviarCorreoFinalExternoResponse.class
                            );

            if (
                    !respuesta
                            .getStatusCode()
                            .is2xxSuccessful()
            ) {

                throw new IllegalStateException(
                        "El servicio de correo final respondio HTTP "
                                + respuesta
                                .getStatusCode()
                                .value()
                                + "."
                );
            }

            EnviarCorreoFinalExternoResponse
                    resultado =
                    respuesta.getBody();

            if (resultado == null) {

                throw new IllegalStateException(
                        "El servicio de correo final respondio sin contenido."
                );
            }

            /*
             * Contrato real comprobado en QA:
             *
             * codigoResultado = "0"
             * mensaje = "Correo enviado."
             */
            if (
                    !CODIGO_EXITO.equals(
                            resultado
                                    .getCodigoResultado()
                    )
            ) {

                throw new IllegalStateException(
                        "El servicio de correo final no confirmo el envio. "
                                + "Codigo: "
                                + resultado
                                .getCodigoResultado()
                                + ". Mensaje: "
                                + resultado
                                .getMensaje()
                );
            }

            return resultado;

        } catch (
                RestClientResponseException e
        ) {

            throw new IllegalStateException(
                    "El servicio de correo final respondio HTTP "
                            + e.getStatusCode()
                            .value()
                            + ".",
                    e
            );

        } catch (
                RestClientException e
        ) {

            throw new IllegalStateException(
                    "No se pudo consumir el servicio institucional de correo final.",
                    e
            );
        }
    }

    private HttpEntity<ByteArrayResource>
    construirParteArchivo(
            DocumentoPublicado documento
    ) {

        byte[] contenido =
                documento
                        .getContenidoArchivo();

        String nombre =
                documento
                        .getNombreArchivo()
                        .trim();

        ByteArrayResource recurso =
                new ByteArrayResource(
                        contenido
                ) {
                    @Override
                    public String getFilename() {
                        return nombre;
                    }
                };

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDispositionFormData(
                "archivosAdjuntos",
                nombre
        );

        return new HttpEntity<>(
                recurso,
                headers
        );
    }

    private String construirJsonReq(
            EnviarCorreoFinalRequest request
    ) {

        return """
                {
                  "correo": "%s",
                  "descripcionSeguro": "%s",
                  "nombreSeguro": "%s",
                  "usuario": "%s",
                  "nombreCompleto": "%s",
                  "montoVida": %d,
                  "enviaFormulario": %s
                }
                """.formatted(
                escaparJson(
                        request.getCorreo()
                ),

                escaparJson(
                        request.getDescripcionSeguro()
                ),

                escaparJson(
                        request.getNombreSeguro()
                ),

                escaparJson(
                        request.getUsuario()
                ),

                escaparJson(
                        request.getNombreCompleto()
                ),

                request.getMontoVida(),

                Boolean.toString(
                        request
                                .isEnviaFormulario()
                )
        );
    }

    private void validarRequest(
            EnviarCorreoFinalRequest request,
            List<DocumentoPublicado> documentos
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La solicitud del correo final es obligatoria."
            );
        }

        if (campoVacio(request.getCorreo())) {

            throw new IllegalArgumentException(
                    "El correo del titular es obligatorio."
            );
        }

        if (
                !request
                        .getCorreo()
                        .contains("@")
        ) {

            throw new IllegalArgumentException(
                    "El correo del titular no tiene un formato valido."
            );
        }

        if (
                campoVacio(
                        request
                                .getDescripcionSeguro()
                )
        ) {

            throw new IllegalArgumentException(
                    "La descripcion del seguro es obligatoria."
            );
        }

        if (
                campoVacio(
                        request
                                .getNombreSeguro()
                )
        ) {

            throw new IllegalArgumentException(
                    "El nombre del seguro es obligatorio."
            );
        }

        if (campoVacio(request.getUsuario())) {

            throw new IllegalArgumentException(
                    "El usuario del correo final es obligatorio."
            );
        }

        if (
                campoVacio(
                        request
                                .getNombreCompleto()
                )
        ) {

            throw new IllegalArgumentException(
                    "El nombre completo del titular es obligatorio."
            );
        }

        if (
                documentos == null
                        || documentos.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El correo final debe contener al menos un documento adjunto."
            );
        }

        /*
         * Nuestros ciclos +Vida requieren
         * uno o dos documentos.
         */
        if (documentos.size() > 2) {

            throw new IllegalArgumentException(
                    "El correo final +Vida no admite mas de dos documentos para este flujo."
            );
        }

        for (
                DocumentoPublicado documento :
                documentos
        ) {

            if (
                    documento == null
                            || documento
                            .getContenidoArchivo()
                            == null
                            || documento
                            .getContenidoArchivo()
                            .length == 0
            ) {

                throw new IllegalArgumentException(
                        "Existe un documento publicado sin contenido para adjuntar."
                );
            }

            if (
                    campoVacio(
                            documento
                                    .getNombreArchivo()
                    )
            ) {

                throw new IllegalArgumentException(
                        "Existe un documento publicado sin nombre de archivo."
                );
            }
        }
    }

    private void validarTokenConfigurado() {

        if (campoVacio(token)) {

            throw new IllegalStateException(
                    "No se ha configurado el token para consumir el servicio de correo final."
            );
        }
    }

    private String escaparJson(
            String valor
    ) {

        if (valor == null) {
            return "";
        }

        return valor
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                .replace(
                        "\r",
                        "\\r"
                )
                .replace(
                        "\n",
                        "\\n"
                )
                .replace(
                        "\t",
                        "\\t"
                );
    }

    private boolean campoVacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }
}