package essalud.gob.pe.seguroshijomenormayor.service.impl;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import essalud.gob.pe.seguroshijomenormayor.config.GoogleDriveProperties;
import essalud.gob.pe.seguroshijomenormayor.dto.response.ArchivoUploadRes;
import essalud.gob.pe.seguroshijomenormayor.dto.response.CargaArchivoRes;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class GoogleDriveServiceImpl
        implements GoogleDriveService {

    private static final String MIME_TYPE_FOLDER =
            "application/vnd.google-apps.folder";

    private static final String MIME_TYPE_BINARIO =
            "application/octet-stream";

    private static final ZoneId ZONA_LIMA =
            ZoneId.of("America/Lima");

    private static final DateTimeFormatter
            FORMATO_FECHA_ARCHIVO =
            DateTimeFormatter.ofPattern("ddMMyyyy");

    private final Drive drive;
    private final GoogleDriveProperties properties;

    /*
     * ==========================================================
     * SUBIDA INDIVIDUAL EXISTENTE
     * ==========================================================
     *
     * Se conserva el contrato actual de Yober.
     *
     * Se añaden appProperties porque el Job necesita
     * identificar inequívocamente al trabajador sin
     * interpretar posiciones del nombre del archivo.
     */
    @Override
    public CargaArchivoRes guardarArchivo(
            MultipartFile archivo,
            String idTpDoc,
            String tpDocument,
            String numDocument,
            String nombreOriginal
    ) throws IOException {

        validarArchivo(
                archivo,
                idTpDoc,
                tpDocument,
                numDocument,
                nombreOriginal
        );

        String carpetaPreparacionId =
                obtenerCarpetaPreparacion(
                        idTpDoc
                );

        /*
         * La carpeta de preparación se determina según
         * el calendario del tipo documental:
         * 244 MAPFRE y 247 PERSONAL.
         */
        String nombreCarpetaQuincena =
                obtenerNombreCarpetaQuincenaActual(
                        idTpDoc
                );

        File carpetaQuincena =
                obtenerOCrearCarpeta(
                        nombreCarpetaQuincena,
                        carpetaPreparacionId
                );

        String nombreGenerado =
                generarNombreArchivo(
                        tpDocument,
                        numDocument,
                        nombreOriginal
                );

        File metadata =
                new File();

        metadata.setName(
                nombreGenerado
        );

        metadata.setParents(
                Collections.singletonList(
                        carpetaQuincena.getId()
                )
        );

        Map<String, String> appProperties =
                new HashMap<>();

        appProperties.put(
                "idTpDoc",
                idTpDoc.trim()
        );

        appProperties.put(
                "tpDocument",
                tpDocument.trim()
        );

        appProperties.put(
                "numDocument",
                numDocument.trim()
        );

        appProperties.put(
                "nombreOriginal",
                nombreOriginal.trim()
        );

        metadata.setAppProperties(
                appProperties
        );

        String contentType =
                archivo.getContentType();

        if (
                contentType == null
                        || contentType.trim().isEmpty()
        ) {
            contentType =
                    MIME_TYPE_BINARIO;
        }

        InputStreamContent mediaContent =
                new InputStreamContent(
                        contentType,
                        archivo.getInputStream()
                );

        mediaContent.setLength(
                archivo.getSize()
        );

        File archivoGuardado =
                drive.files()
                        .create(
                                metadata,
                                mediaContent
                        )
                        .setSupportsAllDrives(true)
                        .setFields(
                                "id,name,mimeType,size,"
                                        + "webViewLink,parents,"
                                        + "appProperties"
                        )
                        .execute();

        log.info(
                "Archivo +Vida guardado en Drive. "
                        + "id={}, nombre={}, idTpDoc={}, "
                        + "tipoDocumento={}, numeroDocumento={}",
                archivoGuardado.getId(),
                archivoGuardado.getName(),
                idTpDoc,
                tpDocument,
                numDocument
        );

        return CargaArchivoRes
                .builder()
                .flagResultado("0")
                .mensaje(
                        "Guardado Exitosamente"
                )
                .archivo(
                        new ArchivoUploadRes(
                                archivoGuardado
                                        .getName(),
                                archivoGuardado
                                        .getWebViewLink()
                        )
                )
                .build();
    }


    /*
     * ==========================================================
     * INFORMACIÓN / DESCARGA
     * ==========================================================
     */

    @Override
    public File obtenerInformacionArchivo(
            String fileId
    ) throws IOException {

        validarTexto(
                fileId,
                "El identificador del archivo es obligatorio."
        );

        return drive.files()
                .get(
                        fileId.trim()
                )
                .setSupportsAllDrives(true)
                .setFields(
                        "id,name,mimeType,size,"
                                + "webViewLink,parents,"
                                + "appProperties"
                )
                .execute();
    }


    @Override
    public byte[] descargarArchivo(
            String fileId
    ) throws IOException {

        validarTexto(
                fileId,
                "El identificador del archivo es obligatorio."
        );

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        drive.files()
                .get(
                        fileId.trim()
                )
                .setSupportsAllDrives(true)
                .executeMediaAndDownloadTo(
                        outputStream
                );

        return outputStream
                .toByteArray();
    }


    /*
     * ==========================================================
     * JOB: LOCALIZAR CARPETA EXACTA DEL PERÍODO
     * ==========================================================
     */

    @Override
    public File obtenerCarpetaPeriodo(
            String idTpDoc,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        validarIdTpDoc(
                idTpDoc
        );

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        String carpetaPreparacionId =
                obtenerCarpetaPreparacion(
                        idTpDoc
                );

        String nombrePeriodo =
                fechaInicio
                        + "_"
                        + fechaFin;

        List<File> carpetas =
                buscarCarpetas(
                        nombrePeriodo,
                        carpetaPreparacionId
                );

        if (carpetas.isEmpty()) {

            throw new IllegalStateException(
                    "No existe la carpeta Drive correspondiente "
                            + "al período "
                            + nombrePeriodo
                            + " para el tipo documental "
                            + idTpDoc
                            + "."
            );
        }

        if (carpetas.size() > 1) {

            throw new IllegalStateException(
                    "Existe más de una carpeta Drive con el "
                            + "mismo período "
                            + nombrePeriodo
                            + "."
            );
        }

        return carpetas.get(0);
    }


    /*
     * ==========================================================
     * JOB: LISTAR DOCUMENTOS DE UNA CARPETA
     * ==========================================================
     */

    @Override
    public List<File> listarArchivosCarpeta(
            String folderId
    ) throws IOException {

        validarTexto(
                folderId,
                "El identificador de la carpeta es obligatorio."
        );

        String query =
                "'"
                        + escaparTextoQuery(
                                folderId.trim()
                        )
                        + "' in parents "
                        + "and trashed = false "
                        + "and mimeType != '"
                        + MIME_TYPE_FOLDER
                        + "'";

        List<File> archivos =
                new ArrayList<>();

        String pageToken =
                null;

        do {

            FileList resultado =
                    drive.files()
                            .list()
                            .setQ(query)
                            .setSupportsAllDrives(true)
                            .setIncludeItemsFromAllDrives(true)
                            .setPageToken(
                                    pageToken
                            )
                            .setFields(
                                    "nextPageToken,"
                                            + "files("
                                            + "id,name,mimeType,size,"
                                            + "webViewLink,parents,"
                                            + "appProperties"
                                            + ")"
                            )
                            .execute();

            if (
                    resultado.getFiles()
                            != null
            ) {

                archivos.addAll(
                        resultado.getFiles()
                );
            }

            pageToken =
                    resultado.getNextPageToken();

        } while (
                pageToken != null
                        && !pageToken.trim().isEmpty()
        );

        return archivos;
    }


    /*
     * ==========================================================
     * JOB: SUBIR REPORTE SIN DUPLICARLO EN REEJECUCIONES
     * ==========================================================
     */

    @Override
    public File guardarOActualizarArchivoEnCarpeta(
            byte[] contenido,
            String nombreArchivo,
            String contentType,
            String folderId
    ) throws IOException {

        if (
                contenido == null
                        || contenido.length == 0
        ) {

            throw new IllegalArgumentException(
                    "El contenido del archivo es obligatorio."
            );
        }

        validarTexto(
                nombreArchivo,
                "El nombre del archivo es obligatorio."
        );

        validarTexto(
                contentType,
                "El tipo de contenido es obligatorio."
        );

        validarTexto(
                folderId,
                "El identificador de la carpeta es obligatorio."
        );

        List<File> existentes =
                buscarArchivosPorNombre(
                        nombreArchivo.trim(),
                        folderId.trim()
                );

        if (existentes.size() > 1) {

            throw new IllegalStateException(
                    "Existe más de un archivo llamado "
                            + nombreArchivo
                            + " dentro de la carpeta destino."
            );
        }

        ByteArrayContent mediaContent =
                new ByteArrayContent(
                        contentType.trim(),
                        contenido
                );

        if (existentes.size() == 1) {

            File existente =
                    existentes.get(0);

            File metadata =
                    new File();

            metadata.setName(
                    nombreArchivo.trim()
            );

            File actualizado =
                    drive.files()
                            .update(
                                    existente.getId(),
                                    metadata,
                                    mediaContent
                            )
                            .setSupportsAllDrives(true)
                            .setFields(
                                    "id,name,mimeType,size,"
                                            + "webViewLink,parents,"
                                            + "appProperties"
                            )
                            .execute();

            log.info(
                    "Archivo Drive actualizado por reejecución. "
                            + "id={}, nombre={}",
                    actualizado.getId(),
                    actualizado.getName()
            );

            return actualizado;
        }

        File metadata =
                new File();

        metadata.setName(
                nombreArchivo.trim()
        );

        metadata.setParents(
                Collections.singletonList(
                        folderId.trim()
                )
        );

        File creado =
                drive.files()
                        .create(
                                metadata,
                                mediaContent
                        )
                        .setSupportsAllDrives(true)
                        .setFields(
                                "id,name,mimeType,size,"
                                        + "webViewLink,parents,"
                                        + "appProperties"
                        )
                        .execute();

        log.info(
                "Archivo Drive creado. id={}, nombre={}",
                creado.getId(),
                creado.getName()
        );

        return creado;
    }


    /*
     * ==========================================================
     * JOB: CREAR / REUTILIZAR CARPETAS
     * ==========================================================
     */

    @Override
    public File obtenerOCrearCarpeta(
            String nombreCarpeta,
            String parentId
    ) throws IOException {

        validarTexto(
                nombreCarpeta,
                "El nombre de la carpeta es obligatorio."
        );

        validarTexto(
                parentId,
                "La carpeta padre es obligatoria."
        );

        List<File> existentes =
                buscarCarpetas(
                        nombreCarpeta.trim(),
                        parentId.trim()
                );

        if (existentes.size() > 1) {

            throw new IllegalStateException(
                    "Existe más de una carpeta llamada "
                            + nombreCarpeta
                            + " bajo el mismo padre Drive."
            );
        }

        if (existentes.size() == 1) {

            return existentes.get(0);
        }

        File carpeta =
                new File();

        carpeta.setName(
                nombreCarpeta.trim()
        );

        carpeta.setMimeType(
                MIME_TYPE_FOLDER
        );

        carpeta.setParents(
                Collections.singletonList(
                        parentId.trim()
                )
        );

        File creada =
                drive.files()
                        .create(
                                carpeta
                        )
                        .setSupportsAllDrives(true)
                        .setFields(
                                "id,name,mimeType,"
                                        + "webViewLink,parents"
                        )
                        .execute();

        log.info(
                "Carpeta Drive creada. id={}, nombre={}",
                creada.getId(),
                creada.getName()
        );

        return creada;
    }



    /*
     * ==========================================================
     * JOB: COPIA NO DESTRUCTIVA PARA DISTRIBUCION PERSONAL
     * ==========================================================
     *
     * Conserva el archivo fuente en Preparacion.
     *
     * Idempotencia:
     *
     * si ya existe un archivo con el mismo nombre bajo
     * el destino, solamente se reutiliza cuando las
     * propiedades documentales coinciden.
     */
    @Override
    public File copiarArchivoEnCarpeta(
            String fileId,
            String folderId
    ) throws IOException {

        validarTexto(
                fileId,
                "El identificador del archivo Drive es obligatorio."
        );

        validarTexto(
                folderId,
                "La carpeta destino Drive es obligatoria."
        );

        File origen =
                drive.files()
                        .get(
                                fileId.trim()
                        )
                        .setSupportsAllDrives(true)
                        .setFields(
                                "id,name,mimeType,size,"
                                        + "webViewLink,parents,"
                                        + "appProperties"
                        )
                        .execute();

        if (
                origen == null
                        || origen.getId() == null
                        || origen.getId()
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalStateException(
                    "No se pudo recuperar el archivo fuente desde Google Drive."
            );
        }

        if (
                MIME_TYPE_FOLDER.equals(
                        origen.getMimeType()
                )
        ) {

            throw new IllegalArgumentException(
                    "copiarArchivoEnCarpeta solamente admite archivos."
            );
        }

        validarTexto(
                origen.getName(),
                "El archivo fuente no cuenta con nombre."
        );

        List<File> existentes =
                buscarArchivosPorNombre(
                        origen.getName().trim(),
                        folderId.trim()
                );

        if (existentes.size() > 1) {

            throw new IllegalStateException(
                    "Existe mas de una copia Drive con el nombre "
                            + origen.getName()
                            + " bajo la misma carpeta destino."
            );
        }

        if (existentes.size() == 1) {

            File existente =
                    existentes.get(0);

            validarMismaIdentidadDocumento(
                    origen,
                    existente
            );

            return existente;
        }

        File metadatosCopia =
                new File();

        metadatosCopia.setName(
                origen.getName().trim()
        );

        metadatosCopia.setParents(
                Collections.singletonList(
                        folderId.trim()
                )
        );

        if (origen.getAppProperties() != null) {

            metadatosCopia.setAppProperties(
                    new HashMap<>(
                            origen.getAppProperties()
                    )
            );
        }

        File copia =
                drive.files()
                        .copy(
                                origen.getId(),
                                metadatosCopia
                        )
                        .setSupportsAllDrives(true)
                        .setFields(
                                "id,name,mimeType,size,"
                                        + "webViewLink,parents,"
                                        + "appProperties"
                        )
                        .execute();

        if (
                copia == null
                        || copia.getId() == null
                        || copia.getId()
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalStateException(
                    "Google Drive no confirmo la copia del archivo."
            );
        }

        validarMismaIdentidadDocumento(
                origen,
                copia
        );

        log.info(
                "Archivo Drive copiado. origen={}, copia={}, destino={}",
                origen.getId(),
                copia.getId(),
                folderId
        );

        return copia;
    }


    /*
     * Las propiedades institucionales del documento permiten
     * evitar reutilizar silenciosamente un archivo que tenga
     * el mismo nombre pero corresponda a otro trabajador.
     */
    private void validarMismaIdentidadDocumento(
            File origen,
            File candidato
    ) {

        if (candidato == null) {

            throw new IllegalStateException(
                    "La copia Drive recuperada es invalida."
            );
        }

        Map<String, String> propiedadesOrigen =
                origen.getAppProperties();

        Map<String, String> propiedadesCandidato =
                candidato.getAppProperties();

        if (
                propiedadesOrigen == null
                        || propiedadesCandidato == null
        ) {

            throw new IllegalStateException(
                    "No es posible comprobar la identidad documental de la copia Drive."
            );
        }

        boolean mismoTipoLogico =
                java.util.Objects.equals(
                        propiedadesOrigen.get(
                                "idTpDoc"
                        ),
                        propiedadesCandidato.get(
                                "idTpDoc"
                        )
                );

        boolean mismoTipoDocumento =
                java.util.Objects.equals(
                        propiedadesOrigen.get(
                                "tpDocument"
                        ),
                        propiedadesCandidato.get(
                                "tpDocument"
                        )
                );

        boolean mismoNumeroDocumento =
                java.util.Objects.equals(
                        propiedadesOrigen.get(
                                "numDocument"
                        ),
                        propiedadesCandidato.get(
                                "numDocument"
                        )
                );

        if (
                !mismoTipoLogico
                        || !mismoTipoDocumento
                        || !mismoNumeroDocumento
        ) {

            throw new IllegalStateException(
                    "Existe un archivo Drive con el mismo nombre, "
                            + "pero con otra identidad documental."
            );
        }
    }

    /*
     * ==========================================================
     * JOB: MOVER ARCHIVO O CARPETA
     * ==========================================================
     *
     * Google Drive trata archivos y carpetas mediante
     * el mismo recurso File.
     *
     * La operación es idempotente:
     * si ya se encuentra bajo el nuevo padre, no se mueve.
     */

    @Override
    public File moverArchivo(
            String fileId,
            String nuevoParentId
    ) throws IOException {

        validarTexto(
                fileId,
                "El identificador del elemento Drive es obligatorio."
        );

        validarTexto(
                nuevoParentId,
                "La carpeta destino es obligatoria."
        );

        File actual =
                drive.files()
                        .get(
                                fileId.trim()
                        )
                        .setSupportsAllDrives(true)
                        .setFields(
                                "id,name,mimeType,"
                                        + "webViewLink,parents,"
                                        + "appProperties"
                        )
                        .execute();

        List<String> parents =
                actual.getParents();

        if (
                parents != null
                        && parents.contains(
                                nuevoParentId.trim()
                        )
        ) {

            return actual;
        }

        String padresEliminar =
                parents == null
                        ? null
                        : String.join(
                                ",",
                                parents
                        );

        Drive.Files.Update update =
                drive.files()
                        .update(
                                fileId.trim(),
                                null
                        )
                        .setSupportsAllDrives(true)
                        .setAddParents(
                                nuevoParentId.trim()
                        )
                        .setFields(
                                "id,name,mimeType,"
                                        + "webViewLink,parents,"
                                        + "appProperties"
                        );

        if (
                padresEliminar != null
                        && !padresEliminar
                                .trim()
                                .isEmpty()
        ) {

            update.setRemoveParents(
                    padresEliminar
            );
        }

        File movido =
                update.execute();

        log.info(
                "Elemento Drive movido. id={}, nombre={}, nuevoPadre={}",
                movido.getId(),
                movido.getName(),
                nuevoParentId
        );

        return movido;
    }


    /*
     * ==========================================================
     * BÚSQUEDAS INTERNAS
     * ==========================================================
     */

    @Override
    public List<File> buscarCarpetas(
            String nombreCarpeta,
            String parentId
    ) throws IOException {

        String query =
                "name = '"
                        + escaparTextoQuery(
                                nombreCarpeta
                        )
                        + "' "
                        + "and mimeType = '"
                        + MIME_TYPE_FOLDER
                        + "' "
                        + "and '"
                        + escaparTextoQuery(
                                parentId
                        )
                        + "' in parents "
                        + "and trashed = false";

        FileList resultado =
                drive.files()
                        .list()
                        .setQ(
                                query
                        )
                        .setSupportsAllDrives(true)
                        .setIncludeItemsFromAllDrives(true)
                        .setFields(
                                "files("
                                        + "id,name,mimeType,"
                                        + "webViewLink,parents"
                                        + ")"
                        )
                        .execute();

        if (
                resultado.getFiles()
                        == null
        ) {
            return Collections.emptyList();
        }

        return resultado.getFiles();
    }


    private List<File> buscarArchivosPorNombre(
            String nombreArchivo,
            String parentId
    ) throws IOException {

        String query =
                "name = '"
                        + escaparTextoQuery(
                                nombreArchivo
                        )
                        + "' "
                        + "and '"
                        + escaparTextoQuery(
                                parentId
                        )
                        + "' in parents "
                        + "and trashed = false "
                        + "and mimeType != '"
                        + MIME_TYPE_FOLDER
                        + "'";

        FileList resultado =
                drive.files()
                        .list()
                        .setQ(
                                query
                        )
                        .setSupportsAllDrives(true)
                        .setIncludeItemsFromAllDrives(true)
                        .setFields(
                                "files("
                                        + "id,name,mimeType,size,"
                                        + "webViewLink,parents,"
                                        + "appProperties"
                                        + ")"
                        )
                        .execute();

        if (
                resultado.getFiles()
                        == null
        ) {
            return Collections.emptyList();
        }

        return resultado.getFiles();
    }


    /*
     * ==========================================================
     * ESTRUCTURA EXISTENTE DE PREPARACIÓN
     * ==========================================================
     */

    private String obtenerCarpetaPreparacion(
            String idTpDoc
    ) throws IOException {

        validarIdTpDoc(
                idTpDoc
        );

        String nombreCarpeta;

        if (
                "244".equals(
                        idTpDoc.trim()
                )
        ) {

            nombreCarpeta =
                    "Preparacion_6012";

        } else {

            nombreCarpeta =
                    "Preparacion_Declaracion_Descuento";
        }

        File carpeta =
                obtenerOCrearCarpeta(
                        nombreCarpeta,
                        properties.getFolderId()
                );

        return carpeta.getId();
    }


    /*
     * ==========================================================
     * PERÍODO DE PREPARACIÓN SEGÚN TIPO DOCUMENTAL
     * ==========================================================
     *
     * MAPFRE - 244:
     * 01-15 / 16-fin de mes
     *
     * PERSONAL - 247:
     * 04-18 / 19-03 del mes siguiente
     *
     * La carpeta de preparación debe utilizar exactamente
     * el mismo período que posteriormente consumirá el job.
     */

    private String obtenerNombreCarpetaQuincenaActual(
            String idTpDoc
    ) {

        validarIdTpDoc(
                idTpDoc
        );

        LocalDate fecha =
                LocalDate.now(
                        ZONA_LIMA
                );

        String tipoDocumento =
                idTpDoc.trim();

        LocalDate inicio;
        LocalDate fin;

        /*
         * MAPFRE - Formulario 6012
         *
         * 01 -> 15
         * 16 -> fin de mes
         */
        if ("244".equals(tipoDocumento)) {

            YearMonth yearMonth =
                    YearMonth.from(
                            fecha
                    );

            if (
                    fecha.getDayOfMonth()
                            <= 15
            ) {

                inicio =
                        yearMonth.atDay(1);

                fin =
                        yearMonth.atDay(15);

            } else {

                inicio =
                        yearMonth.atDay(16);

                fin =
                        yearMonth.atEndOfMonth();
            }

            /*
             * PERSONAL - Autorizacion 247
             *
             * 04 -> 18
             * 19 -> 03 del mes siguiente
             */
        } else {

            int dia =
                    fecha.getDayOfMonth();

            if (
                    dia >= 4
                            && dia <= 18
            ) {

                inicio =
                        fecha.withDayOfMonth(4);

                fin =
                        fecha.withDayOfMonth(18);

            } else if (dia >= 19) {

                inicio =
                        fecha.withDayOfMonth(19);

                fin =
                        fecha
                                .plusMonths(1)
                                .withDayOfMonth(3);

            } else {

                inicio =
                        fecha
                                .minusMonths(1)
                                .withDayOfMonth(19);

                fin =
                        fecha.withDayOfMonth(3);
            }
        }

        return inicio
                + "_"
                + fin;
    }


    private String generarNombreArchivo(
            String codTipoDocumento,
            String numeroDocumento,
            String nombreOriginal
    ) {

        return LocalDate
                .now(
                        ZONA_LIMA
                )
                .format(
                        FORMATO_FECHA_ARCHIVO
                )
                + codTipoDocumento.trim()
                + numeroDocumento.trim()
                + nombreOriginal
                        .trim()
                        .replaceAll(
                                "\\s+",
                                ""
                        );
    }


    private String escaparTextoQuery(
            String texto
    ) {

        return texto
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "'",
                        "\\'"
                );
    }


    /*
     * ==========================================================
     * VALIDACIONES
     * ==========================================================
     */

    private void validarArchivo(
            MultipartFile archivo,
            String idTpDoc,
            String tpDocument,
            String numDocument,
            String nombreOriginal
    ) {

        if (
                archivo == null
                        || archivo.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El archivo es obligatorio."
            );
        }

        validarIdTpDoc(
                idTpDoc
        );

        validarTexto(
                tpDocument,
                "El tipo de documento del titular es obligatorio."
        );

        validarTexto(
                numDocument,
                "El número de documento del titular es obligatorio."
        );

        validarTexto(
                nombreOriginal,
                "El nombre original del archivo es obligatorio."
        );
    }


    private void validarIdTpDoc(
            String idTpDoc
    ) {

        validarTexto(
                idTpDoc,
                "El tipo documental institucional es obligatorio."
        );

        String valor =
                idTpDoc.trim();

        if (
                !"244".equals(valor)
                        && !"247".equals(valor)
        ) {

            throw new IllegalArgumentException(
                    "El tipo documental solamente puede ser 244 o 247."
            );
        }
    }


    private void validarPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        if (
                fechaInicio == null
                        || fechaFin == null
        ) {

            throw new IllegalArgumentException(
                    "El período Drive es obligatorio."
            );
        }

        if (
                fechaInicio.isAfter(
                        fechaFin
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }
    }


    private void validarTexto(
            String valor,
            String mensaje
    ) {

        if (
                valor == null
                        || valor.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }
}