package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.FilaReporteLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteExcelLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteLoteVidaItem;
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
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReporteQuincenalLoteVidaService {

    private static final String DESTINO_MAPFRE =
            "MAPFRE";

    private static final String ID_TP_DOC_MAPFRE =
            "244";

    private final GoogleDriveService googleDriveService;
    private final ExtractorDocumentosDriveLoteVidaService extractorDrive;
    private final ReporteLoteVidaRepository reporteRepository;
    private final GeneradorExcelLoteVidaService generadorExcel;

    /*
     * Reporte productivo MAPFRE.
     *
     * La busqueda del periodo es NO destructiva:
     * si no existe una carpeta de preparacion para la quincena,
     * el job termina sin crear carpetas, Excel, lotes ni entregas.
     */
    public Optional<ResultadoReporteLoteVida> generarMapfre(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        Optional<File> carpetaEncontrada =
                googleDriveService
                        .buscarCarpetaPeriodo(
                                ID_TP_DOC_MAPFRE,
                                fechaInicio,
                                fechaFin
                        );

        if (carpetaEncontrada.isEmpty()) {

            log.info(
                    "MAPFRE sin documentos para el periodo {} a {}. No se genera lote.",
                    fechaInicio,
                    fechaFin
            );

            return Optional.empty();
        }

        File carpetaPeriodo =
                carpetaEncontrada.get();

        validarCarpetaPeriodo(
                carpetaPeriodo,
                fechaInicio,
                fechaFin
        );

        List<File> archivosDrive =
                googleDriveService
                        .listarArchivosCarpeta(
                                carpetaPeriodo.getId()
                        );

        List<DocumentoDriveLoteVida> documentosDrive =
                extractorDrive.extraer(
                        archivosDrive,
                        ID_TP_DOC_MAPFRE
                );

        /*
         * Si la carpeta existe, debe provenir de al menos una
         * publicacion documental real. Una carpeta vacia es una
         * inconsistencia y no se trata como una quincena vacia.
         */
        if (documentosDrive.isEmpty()) {
            throw new IllegalStateException(
                    "La carpeta MAPFRE del periodo "
                            + fechaInicio
                            + " a "
                            + fechaFin
                            + " existe, pero no contiene PDFs 244 validos."
            );
        }

        List<FilaReporteLoteVida> filas =
                new ArrayList<>();

        for (DocumentoDriveLoteVida documento : documentosDrive) {

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
                                    () -> new IllegalStateException(
                                            "No se encontro en Oracle el documento publicado correspondiente al PDF Drive "
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
                    item.getFechaAfiliacion().toLocalDate()
            );
            fila.setCantidadBeneficiarios(
                    item.getCantidadBeneficiarios()
            );
            fila.setRegistroInternoProceso(
                    item.getRegistroInternoProceso()
            );
            fila.setUrlPdf(
                    documento.getWebViewLink()
            );

            filas.add(
                    fila
            );
        }

        filas.sort(
                Comparator.comparing(
                        FilaReporteLoteVida::getNumeroDocumentoTitular
                )
        );

        ReporteExcelLoteVida reporte =
                generadorExcel.generar(
                        DESTINO_MAPFRE,
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
                        || campoVacio(reporteDrive.getId())
        ) {
            throw new IllegalStateException(
                    "Google Drive no confirmo el almacenamiento del Excel MAPFRE."
            );
        }

        File carpetaEntrega =
                googleDriveService
                        .obtenerInformacionArchivo(
                                carpetaPeriodo.getId()
                        );

        if (
                carpetaEntrega == null
                        || campoVacio(carpetaEntrega.getId())
                        || !carpetaPeriodo.getId().equals(
                                carpetaEntrega.getId().trim()
                        )
        ) {
            throw new IllegalStateException(
                    "Google Drive devolvio una carpeta MAPFRE distinta a la esperada."
            );
        }

        String urlCarpeta =
                !campoVacio(carpetaEntrega.getWebViewLink())
                        ? carpetaEntrega.getWebViewLink().trim()
                        : "https://drive.google.com/drive/folders/"
                                + carpetaPeriodo.getId();

        ResultadoReporteLoteVida resultado =
                new ResultadoReporteLoteVida(
                        DESTINO_MAPFRE,
                        fechaInicio,
                        fechaFin,
                        filas.size(),
                        carpetaPeriodo.getId(),
                        urlCarpeta,
                        reporteDrive.getId(),
                        reporteDrive.getName(),
                        reporteDrive.getWebViewLink()
                );

        log.info(
                "Reporte MAPFRE generado. periodo={} a {}, documentos={}, carpetaDrive={}, reporteDrive={}",
                fechaInicio,
                fechaFin,
                filas.size(),
                carpetaPeriodo.getId(),
                reporteDrive.getId()
        );

        return Optional.of(
                resultado
        );
    }

    /*
     * Compatibilidad temporal para pruebas antiguas.
     * No existe ruta productiva PERSONAL por este metodo.
     */
    @Deprecated
    public ResultadoReporteLoteVida generar(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        if (
                destinatario == null
                        || !DESTINO_MAPFRE.equalsIgnoreCase(
                                destinatario.trim()
                        )
        ) {
            throw new IllegalArgumentException(
                    "ReporteQuincenalLoteVidaService solo procesa MAPFRE. PERSONAL usa el pipeline multidestino."
            );
        }

        return generarMapfre(
                fechaInicio,
                fechaFin
        ).orElseThrow(
                () -> new IllegalStateException(
                        "No existe lote MAPFRE para el periodo solicitado."
                )
        );
    }

    private void validarCarpetaPeriodo(
            File carpeta,
            LocalDate inicio,
            LocalDate fin
    ) {

        if (
                carpeta == null
                        || campoVacio(carpeta.getId())
                        || campoVacio(carpeta.getName())
        ) {
            throw new IllegalStateException(
                    "Google Drive devolvio una carpeta MAPFRE invalida."
            );
        }

        String esperado =
                inicio
                        + "_"
                        + fin;

        if (!esperado.equals(carpeta.getName())) {
            throw new IllegalStateException(
                    "La carpeta MAPFRE no corresponde al periodo esperado."
            );
        }
    }

    private void validarPeriodo(
            LocalDate inicio,
            LocalDate fin
    ) {

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(
                    "El periodo MAPFRE es obligatorio."
            );
        }

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException(
                    "El inicio del periodo MAPFRE no puede ser posterior al fin."
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
