package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.config.GoogleDriveProperties;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoCierreDrive;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CierreDriveLoteVidaService {

    private static final String DESTINO_MAPFRE =
            "MAPFRE";

    private static final String ID_TP_DOC_MAPFRE =
            "244";

    private static final String CARPETA_HISTORICAL_MAPFRE =
            "MAPFRE_HISTORICAL";

    private static final String MIME_TYPE_FOLDER =
            "application/vnd.google-apps.folder";

    private final GoogleDriveService googleDriveService;
    private final GoogleDriveProperties googleDriveProperties;

    /*
     * Busqueda no destructiva para reintentos MAPFRE.
     *
     * Nunca crea Historical ni Preparacion.
     */
    public File resolverCarpetaPeriodoTrabajo(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        validarMapfre(
                destinatario
        );

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        String nombrePeriodo =
                construirNombrePeriodo(
                        fechaInicio,
                        fechaFin
                );

        File historical =
                buscarHistoricalExistente();

        if (historical != null) {

            List<File> cerradas =
                    googleDriveService
                            .buscarCarpetas(
                                    nombrePeriodo,
                                    historical.getId()
                            );

            cerradas =
                    cerradas == null
                            ? Collections.emptyList()
                            : cerradas;

            if (cerradas.size() > 1) {
                throw new IllegalStateException(
                        "Existe mas de una carpeta MAPFRE Historical para el periodo "
                                + nombrePeriodo
                                + "."
                );
            }

            if (cerradas.size() == 1) {

                File carpetaFinal =
                        cerradas.get(0);

                validarCarpetaPeriodo(
                        carpetaFinal,
                        nombrePeriodo
                );

                return carpetaFinal;
            }
        }

        return googleDriveService
                .buscarCarpetaPeriodo(
                        ID_TP_DOC_MAPFRE,
                        fechaInicio,
                        fechaFin
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No existe lote MAPFRE para el periodo "
                                        + nombrePeriodo
                                        + "."
                        )
                );
    }

    /*
     * Post-acuse MAPFRE.
     *
     * Historical se crea solamente cuando existe un lote real
     * que ya fue descargado y confirmado por el destinatario.
     */
    public ResultadoCierreDrive cerrarCarpetaPeriodo(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String carpetaPeriodoId,
            int cantidadDocumentos
    ) throws IOException {

        validarMapfre(
                destinatario
        );

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        validarTexto(
                carpetaPeriodoId,
                "El ID de la carpeta MAPFRE es obligatorio."
        );

        if (cantidadDocumentos <= 0) {
            throw new IllegalArgumentException(
                    "MAPFRE no puede cerrar un lote vacio."
            );
        }

        String nombrePeriodo =
                construirNombrePeriodo(
                        fechaInicio,
                        fechaFin
                );

        File actual =
                googleDriveService
                        .obtenerInformacionArchivo(
                                carpetaPeriodoId.trim()
                        );

        validarCarpetaPeriodo(
                actual,
                nombrePeriodo
        );

        File historical =
                buscarHistoricalExistente();

        if (historical != null) {

            List<File> existentes =
                    googleDriveService
                            .buscarCarpetas(
                                    nombrePeriodo,
                                    historical.getId()
                            );

            existentes =
                    existentes == null
                            ? Collections.emptyList()
                            : existentes;

            if (existentes.size() > 1) {
                throw new IllegalStateException(
                        "Existe mas de una carpeta MAPFRE Historical para el mismo periodo."
                );
            }

            if (existentes.size() == 1) {

                File existente =
                        existentes.get(0);

                validarCarpetaPeriodo(
                        existente,
                        nombrePeriodo
                );

                if (!existente.getId().equals(actual.getId())) {
                    throw new IllegalStateException(
                            "Ya existe otra carpeta MAPFRE Historical para el mismo periodo."
                    );
                }

                return construirResultado(
                        asegurarWebViewLink(
                                existente
                        ),
                        cantidadDocumentos
                );
            }
        }

        String rootId =
                requerido(
                        googleDriveProperties.getFolderId(),
                        "google.drive.folder-id es obligatorio."
                );

        File historicalFinal =
                historical != null
                        ? historical
                        : googleDriveService
                                .obtenerOCrearCarpeta(
                                        CARPETA_HISTORICAL_MAPFRE,
                                        rootId
                                );

        validarCarpetaGenerica(
                historicalFinal,
                "No se pudo resolver MAPFRE_HISTORICAL."
        );

        File movida =
                googleDriveService
                        .moverArchivo(
                                actual.getId(),
                                historicalFinal.getId()
                        );

        validarCarpetaPeriodo(
                movida,
                nombrePeriodo
        );

        if (!actual.getId().equals(movida.getId())) {
            throw new IllegalStateException(
                    "El ID Drive del lote MAPFRE cambio durante el movimiento a Historical."
            );
        }

        return construirResultado(
                asegurarWebViewLink(
                        movida
                ),
                cantidadDocumentos
        );
    }

    private File buscarHistoricalExistente()
            throws IOException {

        String rootId =
                requerido(
                        googleDriveProperties.getFolderId(),
                        "google.drive.folder-id es obligatorio."
                );

        List<File> carpetas =
                googleDriveService
                        .buscarCarpetas(
                                CARPETA_HISTORICAL_MAPFRE,
                                rootId
                        );

        carpetas =
                carpetas == null
                        ? Collections.emptyList()
                        : carpetas;

        if (carpetas.isEmpty()) {
            return null;
        }

        if (carpetas.size() > 1) {
            throw new IllegalStateException(
                    "Existe mas de una carpeta MAPFRE_HISTORICAL bajo la raiz Drive."
            );
        }

        File carpeta =
                carpetas.get(0);

        validarCarpetaGenerica(
                carpeta,
                "La carpeta MAPFRE_HISTORICAL es invalida."
        );

        return carpeta;
    }

    private File asegurarWebViewLink(
            File carpeta
    ) throws IOException {

        validarCarpetaGenerica(
                carpeta,
                "La carpeta MAPFRE Historical es invalida."
        );

        if (!campoVacio(carpeta.getWebViewLink())) {
            return carpeta;
        }

        File completa =
                googleDriveService
                        .obtenerInformacionArchivo(
                                carpeta.getId()
                        );

        validarCarpetaGenerica(
                completa,
                "No se pudo recuperar la carpeta MAPFRE Historical."
        );

        if (campoVacio(completa.getWebViewLink())) {
            throw new IllegalStateException(
                    "La carpeta MAPFRE Historical no cuenta con webViewLink."
            );
        }

        return completa;
    }

    private ResultadoCierreDrive construirResultado(
            File carpeta,
            int cantidadDocumentos
    ) {

        return new ResultadoCierreDrive(
                carpeta.getId(),
                carpeta.getName(),
                carpeta.getWebViewLink(),
                cantidadDocumentos
        );
    }

    private void validarCarpetaPeriodo(
            File carpeta,
            String nombrePeriodo
    ) {

        validarCarpetaGenerica(
                carpeta,
                "La carpeta MAPFRE del periodo es invalida."
        );

        if (!nombrePeriodo.equals(carpeta.getName())) {
            throw new IllegalStateException(
                    "La carpeta Drive no corresponde al periodo MAPFRE esperado."
            );
        }
    }

    private void validarCarpetaGenerica(
            File carpeta,
            String mensaje
    ) {

        if (
                carpeta == null
                        || campoVacio(carpeta.getId())
                        || (
                                carpeta.getMimeType() != null
                                        && !MIME_TYPE_FOLDER.equals(
                                                carpeta.getMimeType()
                                        )
                        )
        ) {
            throw new IllegalStateException(
                    mensaje
            );
        }
    }

    private String construirNombrePeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        return fechaInicio
                + "_"
                + fechaFin;
    }

    private void validarMapfre(
            String destinatario
    ) {

        validarTexto(
                destinatario,
                "El destinatario es obligatorio."
        );

        if (!DESTINO_MAPFRE.equalsIgnoreCase(destinatario.trim())) {
            throw new IllegalArgumentException(
                    "CierreDriveLoteVidaService solo administra MAPFRE. PERSONAL usa Pending/Historical multidestino."
            );
        }
    }

    private void validarPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException(
                    "El periodo es obligatorio."
            );
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la final."
            );
        }
    }

    private String requerido(
            String valor,
            String mensaje
    ) {
        validarTexto(
                valor,
                mensaje
        );

        return valor.trim();
    }

    private void validarTexto(
            String valor,
            String mensaje
    ) {
        if (campoVacio(valor)) {
            throw new IllegalArgumentException(
                    mensaje
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
