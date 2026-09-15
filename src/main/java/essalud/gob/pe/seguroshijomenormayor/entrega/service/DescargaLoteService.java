package essalud.gob.pe.seguroshijomenormayor.entrega.service;

import com.google.api.services.drive.model.File;

import essalud.gob.pe.seguroshijomenormayor.common.util.HashUtil;
import essalud.gob.pe.seguroshijomenormayor.entrega.exception.EstadoEntregaException;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.DescargaLotePreparada;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.EntregaLote;
import essalud.gob.pe.seguroshijomenormayor.entrega.repository.EntregaLoteRepository;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class DescargaLoteService {

    private static final String DESTINO_PERSONAL =
            "PERSONAL";

    private static final String DESTINO_MAPFRE =
            "MAPFRE";

    private static final String ID_TP_DOC_PERSONAL =
            "247";

    private static final String ID_TP_DOC_MAPFRE =
            "244";

    private static final String MIME_TYPE_FOLDER =
            "application/vnd.google-apps.folder";

    private static final String DRIVE_HOST =
            "drive.google.com";

    private final EntregaLoteRepository
            entregaLoteRepository;

    private final GoogleDriveService
            googleDriveService;

    public DescargaLoteService(
            EntregaLoteRepository entregaLoteRepository,
            GoogleDriveService googleDriveService
    ) {

        this.entregaLoteRepository =
                entregaLoteRepository;

        this.googleDriveService =
                googleDriveService;
    }

    public DescargaLotePreparada prepararDescargaPorToken(
            String token
    ) {

        validarToken(token);

        String tokenHash =
                HashUtil.calcularSha256(
                        token
                                .trim()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        EntregaLote entrega =
                entregaLoteRepository
                        .buscarPorTokenHash(
                                tokenHash
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La entrega solicitada no existe."
                                        )
                        );

        validarEntregaDescargable(
                entrega
        );

        String destino =
                normalizarDestinatario(
                        entrega.getTipoDestinatario()
                );

        String idTpDocEsperado =
                resolverIdTpDoc(
                        destino
                );

        try {

            String folderId =
                    extraerFolderId(
                            entrega.getUrlAcceso()
                    );

            File carpeta =
                    googleDriveService
                            .obtenerInformacionArchivo(
                                    folderId
                            );

            validarCarpeta(
                    carpeta,
                    folderId
            );

            List<File> archivos =
                    googleDriveService
                            .listarArchivosCarpeta(
                                    folderId
                            );

            if (
                    archivos == null
                            || archivos.isEmpty()
            ) {
                throw new IllegalStateException(
                        "La carpeta Drive del lote no contiene archivos."
                );
            }

            List<File> documentosLote =
                    new ArrayList<>();

            List<File> candidatosReporte =
                    new ArrayList<>();

            for (File archivo : archivos) {

                validarMetadataArchivo(
                        archivo
                );

                String idTpDoc =
                        obtenerIdTpDoc(
                                archivo
                        );

                if (
                        idTpDocEsperado.equals(idTpDoc)
                ) {
                    documentosLote.add(
                            archivo
                    );
                    continue;
                }

                if (
                        idTpDoc != null
                                && !idTpDoc.isEmpty()
                ) {
                    throw new IllegalStateException(
                            "La carpeta " + destino + " contiene un documento "
                                    + "institucional de tipo inesperado: "
                                    + idTpDoc
                                    + "."
                    );
                }

                candidatosReporte.add(
                        archivo
                );
            }

            if (
                    entrega.getCantidadDocumentos()
                            <= 0
            ) {
                throw new IllegalStateException(
                        "La entrega no registra una cantidad valida de documentos."
                );
            }

            if (
                    documentosLote.size()
                            != entrega
                                    .getCantidadDocumentos()
            ) {
                throw new IllegalStateException(
                        "La cantidad de documentos " + idTpDocEsperado + " en Drive no coincide "
                                + "con CANTIDAD_DOCUMENTOS de la entrega."
                );
            }

            /*
             * Fail closed:
             *
             * la carpeta de un destino/periodo PERSONAL debe contener
             * exactamente:
             *
             * - los PDF 247 esperados;
             * - un unico reporte Excel.
             *
             * No se incorpora silenciosamente ningun archivo adicional.
             */
            if (candidatosReporte.size() != 1) {
                throw new IllegalStateException(
                        "No fue posible identificar de forma inequivoca "
                                + "el reporte Excel del lote."
                );
            }

            File reporte =
                    candidatosReporte.get(0);

            byte[] contenidoReporte =
                    googleDriveService
                            .descargarArchivo(
                                    reporte.getId()
                            );

            validarXlsxReal(
                    contenidoReporte
            );

            documentosLote.sort(
                    Comparator.comparing(
                            File::getName,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            byte[] zip =
                    construirZip(
                            reporte,
                            contenidoReporte,
                            documentosLote
                    );

            String nombreZip =
                    construirNombreZip(
                            entrega
                    );

            return new DescargaLotePreparada(
                    nombreZip,
                    zip
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No fue posible recuperar el lote desde Google Drive.",
                    e
            );
        }
    }

    private byte[] construirZip(
            File reporte,
            byte[] contenidoReporte,
            List<File> documentosLote
    ) throws IOException {

        Set<String> nombresUsados =
                new HashSet<>();

        try (
                ByteArrayOutputStream salida =
                        new ByteArrayOutputStream();

                ZipOutputStream zip =
                        new ZipOutputStream(
                                salida
                        )
        ) {

            agregarEntrada(
                    zip,
                    reporte.getName(),
                    contenidoReporte,
                    nombresUsados
            );

            for (File documento : documentosLote) {

                byte[] contenido =
                        googleDriveService
                                .descargarArchivo(
                                        documento.getId()
                                );

                validarPdfReal(
                        documento.getName(),
                        contenido
                );

                agregarEntrada(
                        zip,
                        documento.getName(),
                        contenido,
                        nombresUsados
                );
            }

            zip.finish();

            byte[] resultado =
                    salida.toByteArray();

            if (resultado.length == 0) {
                throw new IllegalStateException(
                        "El ZIP generado se encuentra vacio."
                );
            }

            return resultado;
        }
    }

    private void agregarEntrada(
            ZipOutputStream zip,
            String nombreOriginal,
            byte[] contenido,
            Set<String> nombresUsados
    ) throws IOException {

        if (
                contenido == null
                        || contenido.length == 0
        ) {
            throw new IllegalStateException(
                    "Existe un archivo vacio dentro del lote."
            );
        }

        String nombre =
                normalizarNombreEntrada(
                        nombreOriginal
                );

        String clave =
                nombre.toLowerCase(
                        Locale.ROOT
                );

        if (!nombresUsados.add(clave)) {
            throw new IllegalStateException(
                    "El lote contiene nombres de archivo duplicados: "
                            + nombre
                            + "."
            );
        }

        ZipEntry entry =
                new ZipEntry(
                        nombre
                );

        /*
         * Evita que el ZIP dependa del reloj del servidor
         * para el timestamp interno de cada entrada.
         */
        entry.setTime(0L);

        zip.putNextEntry(
                entry
        );

        zip.write(
                contenido
        );

        zip.closeEntry();
    }

    private void validarPdfReal(
            String nombre,
            byte[] contenido
    ) {

        if (
                contenido == null
                        || contenido.length < 5
        ) {
            throw new IllegalStateException(
                    "El documento "
                            + nombre
                            + " no contiene un PDF valido."
            );
        }

        String firma =
                new String(
                        contenido,
                        0,
                        5,
                        StandardCharsets.US_ASCII
                );

        if (!"%PDF-".equals(firma)) {
            throw new IllegalStateException(
                    "El documento "
                            + nombre
                            + " no corresponde a un PDF valido."
            );
        }
    }

    private void validarXlsxReal(
            byte[] contenido
    ) {

        if (
                contenido == null
                        || contenido.length == 0
        ) {
            throw new IllegalStateException(
                    "El reporte Excel se encuentra vacio."
            );
        }

        boolean contentTypes =
                false;

        boolean workbook =
                false;

        try (
                ZipInputStream zip =
                        new ZipInputStream(
                                new ByteArrayInputStream(
                                        contenido
                                )
                        )
        ) {

            ZipEntry entry;

            while (
                    (entry = zip.getNextEntry())
                            != null
            ) {

                String nombre =
                        entry.getName();

                if (
                        "[Content_Types].xml"
                                .equals(nombre)
                ) {
                    contentTypes =
                            true;
                }

                if (
                        "xl/workbook.xml"
                                .equals(nombre)
                ) {
                    workbook =
                            true;
                }

                if (
                        contentTypes
                                && workbook
                ) {
                    break;
                }
            }

        } catch (IOException e) {

            throw new IllegalStateException(
                    "El reporte del lote no corresponde a un XLSX valido.",
                    e
            );
        }

        if (
                !contentTypes
                        || !workbook
        ) {
            throw new IllegalStateException(
                    "El archivo identificado como reporte "
                            + "no corresponde a un XLSX valido."
            );
        }
    }

    private String obtenerIdTpDoc(
            File archivo
    ) {

        Map<String, String> properties =
                archivo.getAppProperties();

        if (properties == null) {
            return null;
        }

        String valor =
                properties.get(
                        "idTpDoc"
                );

        return campoVacio(valor)
                ? null
                : valor.trim();
    }

    private void validarMetadataArchivo(
            File archivo
    ) {

        if (
                archivo == null
                        || campoVacio(
                                archivo.getId()
                        )
                        || campoVacio(
                                archivo.getName()
                        )
        ) {
            throw new IllegalStateException(
                    "Google Drive devolvio un archivo "
                            + "con metadata incompleta."
            );
        }
    }

    private void validarCarpeta(
            File carpeta,
            String folderId
    ) {

        if (
                carpeta == null
                        || campoVacio(
                                carpeta.getId()
                        )
                        || !folderId.equals(
                                carpeta
                                        .getId()
                                        .trim()
                        )
                        || !MIME_TYPE_FOLDER.equals(
                                carpeta.getMimeType()
                        )
        ) {
            throw new IllegalStateException(
                    "URL_ACCESO no corresponde a la carpeta Drive "
                            + "esperada para el lote."
            );
        }
    }

    private String extraerFolderId(
            String urlAcceso
    ) {

        if (campoVacio(urlAcceso)) {
            throw new IllegalStateException(
                    "La entrega no cuenta con URL_ACCESO."
            );
        }

        final URI uri;

        try {
            uri =
                    URI.create(
                            urlAcceso.trim()
                    );
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "URL_ACCESO no contiene una URL Drive valida.",
                    e
            );
        }

        String host =
                uri.getHost();

        if (
                host == null
                        || !DRIVE_HOST.equalsIgnoreCase(
                                host
                        )
        ) {
            throw new IllegalStateException(
                    "URL_ACCESO no pertenece a Google Drive."
            );
        }

        String path =
                uri.getPath();

        if (campoVacio(path)) {
            throw new IllegalStateException(
                    "URL_ACCESO no contiene una ruta de carpeta Drive."
            );
        }

        String[] segmentos =
                path.split("/");

        for (
                int i = 0;
                i < segmentos.length - 1;
                i++
        ) {

            if (
                    "folders".equals(
                            segmentos[i]
                    )
            ) {

                String folderId =
                        segmentos[i + 1];

                if (
                        folderId != null
                                && folderId.matches(
                                        "[A-Za-z0-9_-]+"
                                )
                ) {
                    return folderId;
                }
            }
        }

        throw new IllegalStateException(
                "No fue posible obtener el identificador "
                        + "de carpeta desde URL_ACCESO."
        );
    }

    private void validarEntregaDescargable(
            EntregaLote entrega
    ) {

        if (entrega == null) {
            throw new IllegalArgumentException(
                    "La entrega solicitada no existe."
            );
        }

        if (
                entrega.getFechaPublicacion()
                        == null
        ) {
            throw new EstadoEntregaException(
                    "El lote todavia no se encuentra publicado."
            );
        }

        if (
                entrega.getFechaAcuse()
                        != null
        ) {
            throw new EstadoEntregaException(
                    "La recepcion del lote ya se encuentra registrada."
            );
        }

        if (
                !DESTINO_PERSONAL.equalsIgnoreCase(
                        entrega.getTipoDestinatario()
                )
                        &&
                !DESTINO_MAPFRE.equalsIgnoreCase(
                        entrega.getTipoDestinatario()
                )
        ) {
            throw new EstadoEntregaException(
                    "La descarga ZIP controlada solamente aplica a PERSONAL o MAPFRE."
            );
        }

        if (
                entrega.getFechaInicioPeriodo()
                        == null
                        || entrega.getFechaFinPeriodo()
                        == null
        ) {
            throw new IllegalStateException(
                    "La entrega no cuenta con un periodo valido."
            );
        }

        if (campoVacio(entrega.getUrlAcceso())) {
            throw new IllegalStateException(
                    "La entrega no cuenta con la carpeta Drive del lote."
            );
        }
    }

    private String resolverIdTpDoc(
            String destino
    ) {

        if (DESTINO_MAPFRE.equals(destino)) {
            return ID_TP_DOC_MAPFRE;
        }

        return ID_TP_DOC_PERSONAL;
    }


    private String normalizarDestinatario(
            String destinatario
    ) {

        if (campoVacio(destinatario)) {
            throw new EstadoEntregaException(
                    "La entrega no cuenta con destinatario."
            );
        }

        String destino =
                destinatario
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                !DESTINO_PERSONAL.equals(destino)
                        &&
                !DESTINO_MAPFRE.equals(destino)
        ) {
            throw new EstadoEntregaException(
                    "El destinatario de la entrega debe ser PERSONAL o MAPFRE."
            );
        }

        return destino;
    }

    private String construirNombreZip(
            EntregaLote entrega
    ) {

        String destino =
                normalizarDestinatario(
                        entrega.getTipoDestinatario()
                );

        return "Lote_Mas_Vida_"
                + destino
                + "_"
                + entrega.getFechaInicioPeriodo()
                + "_"
                + entrega.getFechaFinPeriodo()
                + ".zip";
    }

    private String normalizarNombreEntrada(
            String nombre
    ) {

        if (campoVacio(nombre)) {
            throw new IllegalStateException(
                    "Existe un archivo sin nombre dentro del lote."
            );
        }

        String resultado =
                nombre
                        .trim()
                        .replace('\\', '_')
                        .replace('/', '_')
                        .replace('\r', '_')
                        .replace('\n', '_');

        if (resultado.isEmpty()) {
            throw new IllegalStateException(
                    "Existe un nombre de archivo invalido dentro del lote."
            );
        }

        return resultado;
    }

    private void validarToken(
            String token
    ) {

        if (campoVacio(token)) {
            throw new IllegalArgumentException(
                    "El token de entrega es obligatorio."
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