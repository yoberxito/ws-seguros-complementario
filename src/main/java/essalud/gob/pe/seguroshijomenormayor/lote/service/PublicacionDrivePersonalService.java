package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;

import essalud.gob.pe.seguroshijomenormayor.config.GoogleDriveProperties;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/*
 * Publica una entrega PERSONAL despues del acuse.
 *
 * Estructura esperada antes:
 *
 * PENDING_ROOT
 *   -> codigo - nombre
 *      -> fechaInicio_fechaFin
 *
 * Estructura despues:
 *
 * HISTORICAL_ROOT
 *   -> codigo - nombre
 *      -> fechaInicio_fechaFin
 *
 * Se mueve la carpeta de periodo completa.
 * Google Drive conserva su ID y webViewLink.
 *
 * Esta clase NO modifica permisos.
 * Pending e Historical deben ser roots ya configurados
 * con permisos distintos fuera de esta aplicacion.
 */
@Service
public class PublicacionDrivePersonalService {

    private static final String MIME_TYPE_FOLDER =
            "application/vnd.google-apps.folder";

    private static final String DRIVE_PREFIX =
            "https://drive.google.com/";

    private static final String FOLDERS_SEGMENT =
            "/folders/";

    private final GoogleDriveService googleDriveService;
    private final GoogleDriveProperties googleDriveProperties;

    public PublicacionDrivePersonalService(
            GoogleDriveService googleDriveService,
            GoogleDriveProperties googleDriveProperties
    ) {
        this.googleDriveService =
                googleDriveService;

        this.googleDriveProperties =
                googleDriveProperties;
    }

    public File publicarPeriodoPersonal(
            String urlAcceso,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        try {

            return publicarInterno(
                    urlAcceso,
                    fechaInicio,
                    fechaFin
            );

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "No fue posible publicar el lote PERSONAL en Drive.",
                    ex
            );
        }
    }

    private File publicarInterno(
            String urlAcceso,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        String pendingRootId =
                requerir(
                        googleDriveProperties
                                .getPersonalPendingFolderId(),
                        "google.drive.personal-pending-folder-id es obligatorio."
                );

        String historicalRootId =
                requerir(
                        googleDriveProperties
                                .getPersonalHistoricalFolderId(),
                        "google.drive.personal-historical-folder-id es obligatorio."
                );

        if (
                pendingRootId.equals(
                        historicalRootId
                )
        ) {
            throw new IllegalStateException(
                    "Las raices Pending e Historical de PERSONAL deben ser distintas."
            );
        }

        String carpetaPeriodoId =
                extraerFolderId(
                        urlAcceso
                );

        File carpetaPeriodo =
                googleDriveService
                        .obtenerInformacionArchivo(
                                carpetaPeriodoId
                        );

        validarCarpeta(
                carpetaPeriodo,
                "No se pudo resolver la carpeta de periodo PERSONAL."
        );

        String nombrePeriodo =
                fechaInicio
                        + "_"
                        + fechaFin;

        if (
                !nombrePeriodo.equals(
                        carpetaPeriodo.getName()
                )
        ) {
            throw new IllegalStateException(
                    "La carpeta Drive no corresponde al periodo PERSONAL esperado."
            );
        }

        String destinoActualId =
                obtenerPadreUnico(
                        carpetaPeriodo,
                        "La carpeta de periodo PERSONAL debe tener un unico padre."
                );

        File destinoActual =
                googleDriveService
                        .obtenerInformacionArchivo(
                                destinoActualId
                        );

        validarCarpeta(
                destinoActual,
                "No se pudo resolver el destino PERSONAL del periodo."
        );

        List<String> padresDestino =
                destinoActual.getParents();

        /*
         * Reintento posterior a un move exitoso.
         *
         * Si el destino ya cuelga de Historical, la carpeta
         * de periodo ya esta publicada y no se vuelve a mover.
         */
        if (
                padresDestino != null
                        &&
                padresDestino.contains(
                        historicalRootId
                )
        ) {
            return carpetaPeriodo;
        }

        if (
                padresDestino == null
                        ||
                !padresDestino.contains(
                        pendingRootId
                )
        ) {
            throw new IllegalStateException(
                    "La carpeta PERSONAL no pertenece a Pending ni a Historical."
            );
        }

        File destinoHistorical =
                googleDriveService
                        .obtenerOCrearCarpeta(
                                requerir(
                                        destinoActual.getName(),
                                        "El nombre del destino PERSONAL es obligatorio."
                                ),
                                historicalRootId
                        );

        validarCarpeta(
                destinoHistorical,
                "No se pudo resolver el destino PERSONAL historico."
        );

        /*
         * Antes de mover, protegemos la identidad del periodo.
         *
         * Drive permite carpetas hermanas con el mismo nombre.
         * Por eso no basta con mover ciegamente.
         */
        List<File> periodosHistoricos =
                googleDriveService
                        .buscarCarpetas(
                                nombrePeriodo,
                                destinoHistorical.getId()
                        );

        if (
                periodosHistoricos != null
                        &&
                !periodosHistoricos.isEmpty()
        ) {

            File mismoPeriodo =
                    null;

            for (
                    File existente
                    : periodosHistoricos
            ) {

                validarCarpeta(
                        existente,
                        "Existe una carpeta Historical PERSONAL invalida."
                );

                if (
                        carpetaPeriodo.getId()
                                .equals(
                                        existente.getId()
                                )
                ) {

                    mismoPeriodo =
                            existente;

                    continue;
                }

                throw new IllegalStateException(
                        "Ya existe otra carpeta Historical para el mismo periodo PERSONAL."
                );
            }

            /*
             * Caso idempotente:
             * Historical ya contiene exactamente el mismo
             * recurso Drive.
             */
            if (mismoPeriodo != null) {

                return googleDriveService
                        .obtenerInformacionArchivo(
                                mismoPeriodo.getId()
                        );
            }
        }

        File movida =
                googleDriveService
                        .moverArchivo(
                                carpetaPeriodo.getId(),
                                destinoHistorical.getId()
                        );

        validarCarpeta(
                movida,
                "Drive no confirmo el movimiento del periodo PERSONAL."
        );

        if (
                !carpetaPeriodo.getId()
                        .equals(
                                movida.getId()
                        )
        ) {
            throw new IllegalStateException(
                    "El ID Drive del periodo cambio durante la publicacion."
            );
        }

        File confirmada =
                googleDriveService
                        .obtenerInformacionArchivo(
                                movida.getId()
                        );

        validarCarpeta(
                confirmada,
                "No fue posible verificar la publicacion PERSONAL."
        );

        List<String> padresFinales =
                confirmada.getParents();

        if (
                padresFinales == null
                        ||
                !padresFinales.contains(
                        destinoHistorical.getId()
                )
        ) {
            throw new IllegalStateException(
                    "Drive no confirmo el nuevo padre Historical del periodo."
            );
        }

        return confirmada;
    }

    private String extraerFolderId(
            String urlAcceso
    ) {

        String url =
                requerir(
                        urlAcceso,
                        "La URL Drive de la entrega PERSONAL es obligatoria."
                );

        if (!url.startsWith(DRIVE_PREFIX)) {
            throw new IllegalArgumentException(
                    "La URL de la entrega no pertenece a Google Drive."
            );
        }

        int indice =
                url.indexOf(
                        FOLDERS_SEGMENT
                );

        if (indice < 0) {
            throw new IllegalArgumentException(
                    "La URL Drive no contiene una carpeta valida."
            );
        }

        String restante =
                url.substring(
                        indice
                                + FOLDERS_SEGMENT.length()
                );

        int fin =
                restante.length();

        int pregunta =
                restante.indexOf('?');

        if (
                pregunta >= 0
                        &&
                pregunta < fin
        ) {
            fin = pregunta;
        }

        int slash =
                restante.indexOf('/');

        if (
                slash >= 0
                        &&
                slash < fin
        ) {
            fin = slash;
        }

        int hash =
                restante.indexOf('#');

        if (
                hash >= 0
                        &&
                hash < fin
        ) {
            fin = hash;
        }

        String folderId =
                restante
                        .substring(
                                0,
                                fin
                        )
                        .trim();

        if (folderId.isEmpty()) {
            throw new IllegalArgumentException(
                    "La URL Drive no contiene un folderId."
            );
        }

        return folderId;
    }

    private String obtenerPadreUnico(
            File carpeta,
            String mensaje
    ) {

        List<String> parents =
                carpeta.getParents();

        if (
                parents == null
                        ||
                parents.size() != 1
                        ||
                parents.get(0) == null
                        ||
                parents.get(0).trim().isEmpty()
        ) {
            throw new IllegalStateException(
                    mensaje
            );
        }

        return parents
                .get(0)
                .trim();
    }

    private void validarCarpeta(
            File carpeta,
            String mensaje
    ) {

        if (
                carpeta == null
                        ||
                carpeta.getId() == null
                        ||
                carpeta.getId().trim().isEmpty()
        ) {
            throw new IllegalStateException(
                    mensaje
            );
        }

        if (
                carpeta.getMimeType() != null
                        &&
                !MIME_TYPE_FOLDER.equals(
                        carpeta.getMimeType()
                )
        ) {
            throw new IllegalStateException(
                    mensaje
            );
        }
    }

    private void validarPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        if (
                fechaInicio == null
                        ||
                fechaFin == null
                        ||
                fechaInicio.isAfter(
                        fechaFin
                )
        ) {
            throw new IllegalArgumentException(
                    "El periodo PERSONAL es invalido."
            );
        }
    }

    private String requerir(
            String valor,
            String mensaje
    ) {

        if (
                valor == null
                        ||
                valor.trim().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    mensaje
            );
        }

        return valor.trim();
    }
}