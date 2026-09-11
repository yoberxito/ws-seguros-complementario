package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.FilaReporteLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteExcelLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteLoteVidaItem;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoDistribucionPersonalDrive;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoReportePersonalDestino;
import essalud.gob.pe.seguroshijomenormayor.lote.repository.ReporteLoteVidaRepository;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * Genera un Excel PERSONAL independiente para cada
 * destino institucional previamente distribuido en Drive.
 *
 * El reporte utiliza los enlaces de las COPIAS finales
 * de cada destino.
 *
 * No redistribuye PDFs y no crea lotes Oracle.
 */
@Service
public class GeneradorReportesPersonalPorDestinoService {

    private static final String DESTINO_PERSONAL =
            GeneradorExcelLoteVidaService.DESTINO_PERSONAL;

    private final ReporteLoteVidaRepository
            reporteRepository;

    private final GeneradorExcelLoteVidaService
            generadorExcel;

    private final GoogleDriveService
            googleDriveService;

    public GeneradorReportesPersonalPorDestinoService(
            ReporteLoteVidaRepository reporteRepository,
            GeneradorExcelLoteVidaService generadorExcel,
            GoogleDriveService googleDriveService
    ) {

        this.reporteRepository =
                reporteRepository;

        this.generadorExcel =
                generadorExcel;

        this.googleDriveService =
                googleDriveService;
    }

    public List<ResultadoReportePersonalDestino> generar(
            List<ResultadoDistribucionPersonalDrive> distribuciones,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        if (distribuciones == null) {
            throw new IllegalArgumentException(
                    "Las distribuciones PERSONAL son obligatorias."
            );
        }

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        if (distribuciones.isEmpty()) {
            return List.of();
        }

        Set<String> destinosProcesados =
                new HashSet<>();

        List<ResultadoReportePersonalDestino> resultados =
                new ArrayList<>();

        for (
                ResultadoDistribucionPersonalDrive distribucion
                : distribuciones
        ) {

            if (distribucion == null) {
                throw new IllegalStateException(
                        "Existe una distribucion PERSONAL nula."
                );
            }

            String codigoDestino =
                    distribucion
                            .getDestino()
                            .codigoDestino();

            if (!destinosProcesados.add(codigoDestino)) {

                throw new IllegalStateException(
                        "El destino PERSONAL "
                                + codigoDestino
                                + " aparece mas de una vez en la distribucion."
                );
            }

            List<DocumentoDriveLoteVida> documentos =
                    distribucion
                            .getDocumentosFinales();

            if (documentos.isEmpty()) {

                throw new IllegalStateException(
                        "El destino PERSONAL "
                                + codigoDestino
                                + " no contiene documentos."
                );
            }

            List<FilaReporteLoteVida> filas =
                    new ArrayList<>();

            for (
                    DocumentoDriveLoteVida documento
                    : documentos
            ) {

                if (documento == null) {
                    throw new IllegalStateException(
                            "Existe un documento PERSONAL final nulo."
                    );
                }

                ReporteLoteVidaItem item =
                        reporteRepository
                                .buscarDocumentoPublicado(
                                        documento.getTipoDocumentoTitular(),
                                        documento.getNumeroDocumentoTitular(),
                                        documento.getTipoDocumentoLogico(),
                                        fechaInicio,
                                        fechaFin
                                )
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "No se encontro en Oracle el documento publicado "
                                                                + "correspondiente al PDF final "
                                                                + documento.getNombreArchivo()
                                                                + "."
                                                )
                                );

                if (item.getFechaAfiliacion() == null) {

                    throw new IllegalStateException(
                            "El proceso "
                                    + item.getRegistroInternoProceso()
                                    + " no cuenta con fecha de afiliacion."
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
                        item.getFechaAfiliacion()
                                .toLocalDate()
                );

                fila.setRegistroInternoProceso(
                        item.getRegistroInternoProceso()
                );

                /*
                 * El Excel debe enlazar a la COPIA final
                 * de la Red, nunca al PDF de Preparacion.
                 */
                fila.setUrlPdf(
                        documento.getWebViewLink()
                );

                filas.add(
                        fila
                );
            }

            filas.sort(
                    Comparator.comparing(
                            FilaReporteLoteVida::
                                    getNumeroDocumentoTitular
                    )
            );

            ReporteExcelLoteVida reporte =
                    generadorExcel.generar(
                            DESTINO_PERSONAL,
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
                                    distribucion.getCarpetaPeriodoId()
                            );

            validarReporteDrive(
                    reporteDrive
            );

            resultados.add(
                    new ResultadoReportePersonalDestino(
                            distribucion.getDestino(),
                            distribucion.getCarpetaPeriodoId(),
                            distribucion.getUrlCarpetaPeriodo(),
                            filas.size(),
                            reporteDrive.getId(),
                            reporteDrive.getName(),
                            reporteDrive.getWebViewLink()
                    )
            );
        }

        return List.copyOf(
                resultados
        );
    }

    private void validarReporteDrive(
            File reporte
    ) {

        if (
                reporte == null
                        || campoVacio(
                                reporte.getId()
                        )
                        || campoVacio(
                                reporte.getName()
                        )
                        || campoVacio(
                                reporte.getWebViewLink()
                        )
        ) {

            throw new IllegalStateException(
                    "Google Drive no confirmo correctamente el Excel PERSONAL."
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
                    "El periodo PERSONAL es obligatorio."
            );
        }

        if (fechaInicio.isAfter(fechaFin)) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la final."
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