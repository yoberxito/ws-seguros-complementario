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
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CierreDriveLoteVidaService {

    private static final String DESTINO_MAPFRE =
            "MAPFRE";

    private static final String DESTINO_PERSONAL =
            "PERSONAL";

    private static final String ID_TP_DOC_MAPFRE =
            "244";

    private static final String ID_TP_DOC_PERSONAL =
            "247";

    private static final String CARPETA_FINAL_MAPFRE =
            "Seguro +Vida - Afiliaciones MAPFRE";

    private static final String CARPETA_FINAL_PERSONAL =
            "Seguro +Vida - Autorizaciones de Descuento - Personal EsSalud";

    private static final String MIME_TYPE_FOLDER =
            "application/vnd.google-apps.folder";

    private final GoogleDriveService googleDriveService;
    private final GoogleDriveProperties googleDriveProperties;

    /*
     * Resuelve la carpeta sobre la cual debe trabajar
     * el reporte.
     *
     * Primera ejecucion:
     *   usa la carpeta que todavia esta en Preparacion.
     *
     * Reejecucion:
     *   si la carpeta ya fue cerrada/movida, utiliza
     *   directamente la carpeta final.
     */
    public File resolverCarpetaPeriodoTrabajo(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        String destino =
                normalizarDestinatario(
                        destinatario
                );

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        File carpetaDestino =
                obtenerCarpetaDestinoFinal(
                        destino
                );

        String nombrePeriodo =
                construirNombrePeriodo(
                        fechaInicio,
                        fechaFin
                );

        List<File> cerradas =
                googleDriveService
                        .buscarCarpetas(
                                nombrePeriodo,
                                carpetaDestino.getId()
                        );

        cerradas =
                cerradas == null
                        ? Collections.emptyList()
                        : cerradas;

        if (cerradas.size() > 1) {

            throw new IllegalStateException(
                    "Existe mas de una carpeta final para el periodo "
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

        File carpetaPreparacion =
                googleDriveService
                        .obtenerCarpetaPeriodo(
                                resolverIdTpDoc(
                                        destino
                                ),
                                fechaInicio,
                                fechaFin
                        );

        validarCarpetaPeriodo(
                carpetaPreparacion,
                nombrePeriodo
        );

        return carpetaPreparacion;
    }

    /*
     * Cierra el periodo moviendo la carpeta completa
     * hacia la carpeta oficial del destinatario.
     *
     * Es seguro ante reejecuciones:
     * si el periodo ya esta en el destino final,
     * no crea otra carpeta ni vuelve a moverlo.
     */
    public ResultadoCierreDrive cerrarCarpetaPeriodo(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String carpetaPeriodoId,
            int cantidadDocumentos
    ) throws IOException {

        String destino =
                normalizarDestinatario(
                        destinatario
                );

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        validarTexto(
                carpetaPeriodoId,
                "El ID de la carpeta del periodo es obligatorio."
        );

        File carpetaDestino =
                obtenerCarpetaDestinoFinal(
                        destino
                );

        String nombrePeriodo =
                construirNombrePeriodo(
                        fechaInicio,
                        fechaFin
                );

        List<File> existentesFinales =
                googleDriveService
                        .buscarCarpetas(
                                nombrePeriodo,
                                carpetaDestino.getId()
                        );

        existentesFinales =
                existentesFinales == null
                        ? Collections.emptyList()
                        : existentesFinales;

        if (existentesFinales.size() > 1) {

            throw new IllegalStateException(
                    "Existe mas de una carpeta final para el periodo "
                            + nombrePeriodo
                            + "."
            );
        }

        if (existentesFinales.size() == 1) {

            File existente =
                    existentesFinales.get(0);

            validarCarpetaPeriodo(
                    existente,
                    nombrePeriodo
            );

            if (
                    !existente.getId()
                            .equals(
                                    carpetaPeriodoId.trim()
                            )
            ) {

                throw new IllegalStateException(
                        "Ya existe otra carpeta final para el mismo periodo."
                );
            }

            existente =
                    asegurarWebViewLink(
                            existente
                    );

            return construirResultado(
                    existente,
                    cantidadDocumentos
            );
        }

        File actual =
                googleDriveService
                        .obtenerInformacionArchivo(
                                carpetaPeriodoId.trim()
                        );

        validarCarpetaPeriodo(
                actual,
                nombrePeriodo
        );

        File movida =
                googleDriveService
                        .moverArchivo(
                                actual.getId(),
                                carpetaDestino.getId()
                        );

        validarCarpetaPeriodo(
                movida,
                nombrePeriodo
        );

        movida =
                asegurarWebViewLink(
                        movida
                );

        return construirResultado(
                movida,
                cantidadDocumentos
        );
    }

    private File obtenerCarpetaDestinoFinal(
            String destino
    ) throws IOException {

        String rootId =
                requerido(
                        googleDriveProperties
                                .getFolderId(),
                        "google.drive.folder-id es obligatorio."
                );

        File destinoFinal =
                googleDriveService
                        .obtenerOCrearCarpeta(
                                resolverNombreCarpetaFinal(
                                        destino
                                ),
                                rootId
                        );

        validarElementoDrive(
                destinoFinal,
                "La carpeta final del destinatario no pudo resolverse."
        );

        if (
                destinoFinal.getMimeType() != null
                        &&
                        !MIME_TYPE_FOLDER.equals(
                                destinoFinal.getMimeType()
                        )
        ) {

            throw new IllegalStateException(
                    "El destino final resuelto en Drive no es una carpeta."
            );
        }

        return destinoFinal;
    }

    private File asegurarWebViewLink(
            File carpeta
    ) throws IOException {

        validarElementoDrive(
                carpeta,
                "La carpeta final del periodo es invalida."
        );

        if (!campoVacio(carpeta.getWebViewLink())) {
            return carpeta;
        }

        File completa =
                googleDriveService
                        .obtenerInformacionArchivo(
                                carpeta.getId()
                        );

        validarElementoDrive(
                completa,
                "No se pudo recuperar la carpeta final desde Drive."
        );

        if (campoVacio(completa.getWebViewLink())) {

            throw new IllegalStateException(
                    "La carpeta final no cuenta con webViewLink."
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

        validarElementoDrive(
                carpeta,
                "La carpeta del periodo no pudo resolverse."
        );

        if (
                !nombrePeriodo.equals(
                        carpeta.getName()
                )
        ) {

            throw new IllegalStateException(
                    "La carpeta Drive no corresponde al periodo esperado."
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
                    "El elemento Drive del periodo no es una carpeta."
            );
        }
    }

    private void validarElementoDrive(
            File archivo,
            String mensaje
    ) {

        if (
                archivo == null
                        || campoVacio(
                                archivo.getId()
                        )
        ) {

            throw new IllegalStateException(
                    mensaje
            );
        }
    }

    private String resolverNombreCarpetaFinal(
            String destino
    ) {

        if (DESTINO_MAPFRE.equals(destino)) {
            return CARPETA_FINAL_MAPFRE;
        }

        return CARPETA_FINAL_PERSONAL;
    }

    private String resolverIdTpDoc(
            String destino
    ) {

        if (DESTINO_MAPFRE.equals(destino)) {
            return ID_TP_DOC_MAPFRE;
        }

        return ID_TP_DOC_PERSONAL;
    }

    private String construirNombrePeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        return fechaInicio
                + "_"
                + fechaFin;
    }

    private String normalizarDestinatario(
            String destinatario
    ) {

        validarTexto(
                destinatario,
                "El destinatario es obligatorio."
        );

        String destino =
                destinatario
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                !DESTINO_MAPFRE.equals(destino)
                        &&
                        !DESTINO_PERSONAL.equals(destino)
        ) {

            throw new IllegalArgumentException(
                    "El destinatario debe ser MAPFRE o PERSONAL."
            );
        }

        return destino;
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