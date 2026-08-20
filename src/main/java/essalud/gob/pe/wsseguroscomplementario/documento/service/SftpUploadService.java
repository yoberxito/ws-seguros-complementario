package essalud.gob.pe.wsseguroscomplementario.documento.service;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.SftpUploadResponse;
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

@Service
public class SftpUploadService {

    private final RestClient restClient;
    private final String uploadUrl;

    public SftpUploadService(
            @Value("${integraciones.sftp.upload-url}") String uploadUrl
    ) {
        this.restClient = RestClient.create();
        this.uploadUrl = uploadUrl;
    }

    public SftpUploadResponse subirDocumentoSellado(
            byte[] contenidoArchivo,
            String nombreArchivo,
            String tipoDocumentoTrabajador,
            String numeroDocumentoTrabajador
    ) {
        validarParametros(
                contenidoArchivo,
                nombreArchivo,
                tipoDocumentoTrabajador,
                numeroDocumentoTrabajador
        );

        ByteArrayResource recursoArchivo =
                new ByteArrayResource(contenidoArchivo) {
                    @Override
                    public String getFilename() {
                        return nombreArchivo.trim();
                    }
                };

        HttpHeaders cabecerasArchivo =
                new HttpHeaders();

        cabecerasArchivo.setContentType(
                MediaType.APPLICATION_PDF
        );

        cabecerasArchivo.setContentDispositionFormData(
                "archivo",
                nombreArchivo.trim()
        );

        HttpEntity<ByteArrayResource> parteArchivo =
                new HttpEntity<>(
                        recursoArchivo,
                        cabecerasArchivo
                );

        MultiValueMap<String, Object> partes =
                new LinkedMultiValueMap<>();

        partes.add(
                "archivo",
                parteArchivo
        );

        partes.add(
                "tpDocument",
                tipoDocumentoTrabajador.trim()
        );

        partes.add(
                "numDocument",
                numeroDocumentoTrabajador.trim()
        );

        try {
            ResponseEntity<SftpUploadResponse> respuesta =
                    restClient
                            .post()
                            .uri(uploadUrl)
                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA
                            )
                            .body(partes)
                            .retrieve()
                            .toEntity(
                                    SftpUploadResponse.class
                            );

            if (!respuesta.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException(
                        "El servicio SFTP respondió con estado HTTP "
                                + respuesta.getStatusCode().value()
                                + "."
                );
            }

            SftpUploadResponse resultado =
                    respuesta.getBody();

            validarRespuestaSftp(
                    resultado
            );

            return resultado;

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "No se pudo subir el documento sellado al servicio SFTP.",
                    e
            );
        }
    }

    private void validarParametros(
            byte[] contenidoArchivo,
            String nombreArchivo,
            String tipoDocumentoTrabajador,
            String numeroDocumentoTrabajador
    ) {
        if (
                contenidoArchivo == null
                        || contenidoArchivo.length == 0
        ) {
            throw new IllegalArgumentException(
                    "El contenido del documento sellado es obligatorio."
            );
        }

        if (campoVacio(nombreArchivo)) {
            throw new IllegalArgumentException(
                    "El nombre del documento sellado es obligatorio."
            );
        }

        if (campoVacio(tipoDocumentoTrabajador)) {
            throw new IllegalArgumentException(
                    "El tipo de documento del trabajador es obligatorio para el SFTP."
            );
        }

        if (campoVacio(numeroDocumentoTrabajador)) {
            throw new IllegalArgumentException(
                    "El número de documento del trabajador es obligatorio para el SFTP."
            );
        }
    }

    private void validarRespuestaSftp(
            SftpUploadResponse respuesta
    ) {
        if (respuesta == null) {
            throw new IllegalStateException(
                    "El servicio SFTP respondió sin contenido."
            );
        }

        if (respuesta.getArchivo() == null) {
            throw new IllegalStateException(
                    "El servicio SFTP no devolvió información del archivo almacenado."
            );
        }

        if (
                campoVacio(
                        respuesta
                                .getArchivo()
                                .getNombreArchivo()
                )
        ) {
            throw new IllegalStateException(
                    "El servicio SFTP no devolvió el nombre del archivo almacenado."
            );
        }

        if (
                campoVacio(
                        respuesta
                                .getArchivo()
                                .getRutaArchivo()
                )
        ) {
            throw new IllegalStateException(
                    "El servicio SFTP no devolvió la ruta del archivo almacenado."
            );
        }
    }

    private boolean campoVacio(String valor) {
        return valor == null
                || valor.trim().isEmpty();
    }
}