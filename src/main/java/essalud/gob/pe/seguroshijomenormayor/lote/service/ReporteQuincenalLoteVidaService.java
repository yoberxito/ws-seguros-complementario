package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.FilaReporteLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteExcelLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteLoteVidaItem;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoCierreDrive;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoReporteLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.repository.ReporteLoteVidaRepository;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReporteQuincenalLoteVidaService {

    private static final String
            DESTINO_MAPFRE =
            "MAPFRE";

    private static final String
            DESTINO_PERSONAL =
            "PERSONAL";

    private static final String
            ID_TP_DOC_MAPFRE =
            "244";

    private static final String
            ID_TP_DOC_PERSONAL =
            "247";

    private final GoogleDriveService
            googleDriveService;

    private final ExtractorDocumentosDriveLoteVidaService
            extractorDrive;

    private final ReporteLoteVidaRepository
            reporteRepository;

    private final GeneradorExcelLoteVidaService
            generadorExcel;

    private final CierreDriveLoteVidaService
            cierreDrive;

    public ResultadoReporteLoteVida generar(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        String destino =
                normalizarDestino(
                        destinatario
                );

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        String idTpDoc =
                resolverIdTpDoc(
                        destino
                );

        /*
         * Primera ejecucion:
         * obtiene el periodo desde Preparacion.
         *
         * Reejecucion:
         * si el periodo ya fue cerrado, trabaja
         * directamente sobre la carpeta final.
         */
        File carpetaPeriodo =
                cierreDrive
                        .resolverCarpetaPeriodoTrabajo(
                                destino,
                                fechaInicio,
                                fechaFin
                        );

        if (
                carpetaPeriodo == null
                        || campoVacio(
                                carpetaPeriodo.getId()
                        )
        ) {

            throw new IllegalStateException(
                    "Google Drive no devolvió "
                            + "la carpeta del período."
            );
        }

        List<File> archivosDrive =
                googleDriveService
                        .listarArchivosCarpeta(
                                carpetaPeriodo.getId()
                        );

        List<DocumentoDriveLoteVida>
                documentosDrive =
                extractorDrive.extraer(
                        archivosDrive,
                        idTpDoc
                );

        List<FilaReporteLoteVida> filas =
                new ArrayList<>();

        for (
                DocumentoDriveLoteVida documento
                : documentosDrive
        ) {

            ReporteLoteVidaItem item =
                    reporteRepository
                            .buscarDocumentoPublicado(
                                    documento
                                            .getTipoDocumentoTitular(),

                                    documento
                                            .getNumeroDocumentoTitular(),

                                    documento
                                            .getTipoDocumentoLogico(),

                                    fechaInicio,
                                    fechaFin
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "No se encontró en Oracle "
                                                            + "el documento publicado "
                                                            + "correspondiente al PDF "
                                                            + "de Drive "
                                                            + documento
                                                            .getNombreArchivo()
                                                            + "."
                                            )
                            );

            if (
                    item.getFechaAfiliacion()
                            == null
            ) {

                throw new IllegalStateException(
                        "El proceso "
                                + item
                                .getRegistroInternoProceso()
                                + " no cuenta con una "
                                + "Autorización de Descuento "
                                + "publicada para determinar "
                                + "la fecha de afiliación."
                );
            }

            FilaReporteLoteVida fila =
                    new FilaReporteLoteVida();

            fila.setNumeroDocumentoTitular(
                    item.getNumeroDocumentoTitular()
            );

            fila.setNombresApellidosTitular(
                    item.getNombresApellidosTitular()
            );

            fila.setFechaAfiliacion(
                    item
                            .getFechaAfiliacion()
                            .toLocalDate()
            );

            /*
             * Solamente MAPFRE presenta la cantidad
             * de beneficiarios en su reporte.
             */
            if (
                    DESTINO_MAPFRE.equals(
                            destino
                    )
            ) {

                fila.setCantidadBeneficiarios(
                        item.getCantidadBeneficiarios()
                );
            }

            fila.setRegistroInternoProceso(
                    item.getRegistroInternoProceso()
            );

            /*
             * El enlace visible del Excel apunta
             * al PDF de Google Drive del lote,
             * no a RUTA_ARCHIVO de SFTP.
             */
            fila.setUrlPdf(
                    documento.getWebViewLink()
            );

            filas.add(
                    fila
            );
        }

        /*
         * Orden determinístico.
         * No altera ninguna regla de negocio.
         */
        filas.sort(
                Comparator.comparing(
                        FilaReporteLoteVida::
                                getNumeroDocumentoTitular
                )
        );

        ReporteExcelLoteVida reporte =
                generadorExcel.generar(
                        destino,
                        fechaInicio,
                        fechaFin,
                        filas
                );

        File reporteDrive =
                googleDriveService
                        .guardarOActualizarArchivoEnCarpeta(
                                reporte.getContenido(),
                                reporte.getNombreArchivo(),
                                reporte.getContentType(),
                                carpetaPeriodo.getId()
                        );

        if (
                reporteDrive == null
                        || campoVacio(
                                reporteDrive.getId()
                        )
        ) {

            throw new IllegalStateException(
                    "Google Drive no confirmó "
                            + "el almacenamiento del reporte."
            );
        }

        /*
         * El periodo se cierra solamente cuando:
         *
         * - los PDFs fueron procesados;
         * - Oracle fue validado;
         * - el Excel fue generado;
         * - Drive confirmó el almacenamiento del Excel.
         */
        ResultadoCierreDrive cierreResultado =
                cierreDrive
                        .cerrarCarpetaPeriodo(
                                destino,
                                fechaInicio,
                                fechaFin,
                                carpetaPeriodo.getId(),
                                filas.size()
                        );

        log.info(
                "Reporte quincenal +Vida generado. "
                        + "destinatario={}, periodo={} a {}, "
                        + "documentos={}, carpetaDrive={}, "
                        + "reporteDrive={}",
                destino,
                fechaInicio,
                fechaFin,
                filas.size(),
                cierreResultado.getIdCarpetaFinal(),
                reporteDrive.getId()
        );

        return new ResultadoReporteLoteVida(
                destino,
                fechaInicio,
                fechaFin,
                filas.size(),
                cierreResultado.getIdCarpetaFinal(),
                cierreResultado.getUrlCarpetaFinal(),
                reporteDrive.getId(),
                reporteDrive.getName(),
                reporteDrive.getWebViewLink()
        );
    }

    private String resolverIdTpDoc(
            String destinatario
    ) {

        if (
                DESTINO_MAPFRE.equals(
                        destinatario
                )
        ) {
            return ID_TP_DOC_MAPFRE;
        }

        return ID_TP_DOC_PERSONAL;
    }

    private String normalizarDestino(
            String destinatario
    ) {

        if (campoVacio(destinatario)) {

            throw new IllegalArgumentException(
                    "El destinatario del lote "
                            + "es obligatorio."
            );
        }

        String valor =
                destinatario
                        .trim()
                        .toUpperCase();

        if (
                !DESTINO_MAPFRE.equals(valor)
                        && !DESTINO_PERSONAL.equals(valor)
        ) {

            throw new IllegalArgumentException(
                    "El destinatario solamente "
                            + "puede ser MAPFRE o PERSONAL."
            );
        }

        return valor;
    }

    private void validarPeriodo(
            LocalDate inicio,
            LocalDate fin
    ) {

        if (
                inicio == null
                        || fin == null
        ) {

            throw new IllegalArgumentException(
                    "El período del lote "
                            + "es obligatorio."
            );
        }

        if (inicio.isAfter(fin)) {

            throw new IllegalArgumentException(
                    "El inicio del período no puede "
                            + "ser posterior al fin."
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