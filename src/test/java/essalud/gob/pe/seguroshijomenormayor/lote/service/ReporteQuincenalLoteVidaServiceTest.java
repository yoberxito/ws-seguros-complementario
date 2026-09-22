package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteExcelLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteLoteVidaItem;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoReporteLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.repository.ReporteLoteVidaRepository;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReporteQuincenalLoteVidaServiceTest {

    @Test
    void generaMapfreSinMoverNiCrearCarpetasFinales()
            throws Exception {

        GoogleDriveService drive =
                mock(GoogleDriveService.class);
        ExtractorDocumentosDriveLoteVidaService extractor =
                mock(ExtractorDocumentosDriveLoteVidaService.class);
        ReporteLoteVidaRepository repository =
                mock(ReporteLoteVidaRepository.class);
        GeneradorExcelLoteVidaService generador =
                mock(GeneradorExcelLoteVidaService.class);

        ReporteQuincenalLoteVidaService service =
                new ReporteQuincenalLoteVidaService(
                        drive,
                        extractor,
                        repository,
                        generador
                );

        LocalDate inicio =
                LocalDate.of(2026, 9, 1);
        LocalDate fin =
                LocalDate.of(2026, 9, 15);

        File carpeta = new File();
        carpeta.setId("PERIODO-MAPFRE");
        carpeta.setName("2026-09-01_2026-09-15");
        carpeta.setMimeType(
                "application/vnd.google-apps.folder"
        );
        carpeta.setWebViewLink(
                "https://drive/periodo-mapfre"
        );

        when(
                drive.buscarCarpetaPeriodo(
                        "244",
                        inicio,
                        fin
                )
        ).thenReturn(Optional.of(carpeta));

        File rawPdf = new File();
        rawPdf.setId("PDF-1");
        rawPdf.setName("6012.pdf");

        when(
                drive.listarArchivosCarpeta(
                        "PERIODO-MAPFRE"
                )
        ).thenReturn(List.of(rawPdf));

        DocumentoDriveLoteVida documento =
                new DocumentoDriveLoteVida(
                        "PDF-1",
                        "6012.pdf",
                        "https://drive/pdf-1",
                        "244",
                        "01",
                        "12345678"
                );

        when(
                extractor.extraer(
                        List.of(rawPdf),
                        "244"
                )
        ).thenReturn(List.of(documento));

        ReporteLoteVidaItem item =
                new ReporteLoteVidaItem();
        item.setNumeroDocumentoTitular("12345678");
        item.setPrimerNombreTitular("JUAN");
        item.setApellidoPaternoTitular("PEREZ");
        item.setRegistroInternoProceso("VIDA-TEST");
        item.setCantidadBeneficiarios(2);
        item.setFechaAfiliacion(
                LocalDateTime.of(
                        2026,
                        9,
                        10,
                        12,
                        0
                )
        );

        when(
                repository.buscarDocumentoPublicado(
                        "01",
                        "12345678",
                        "FORMULARIO_6012",
                        inicio,
                        fin
                )
        ).thenReturn(Optional.of(item));

        ReporteExcelLoteVida excel =
                new ReporteExcelLoteVida(
                        "Reporte_MAPFRE_0109_1509.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        new byte[]{1, 2, 3}
                );

        when(
                generador.generar(
                        eq("MAPFRE"),
                        eq(inicio),
                        eq(fin),
                        any()
                )
        ).thenReturn(excel);

        File excelDrive = new File();
        excelDrive.setId("EXCEL-1");
        excelDrive.setName(
                "Reporte_MAPFRE_0109_1509.xlsx"
        );
        excelDrive.setWebViewLink(
                "https://drive/excel-1"
        );

        when(
                drive.guardarOActualizarArchivoEnCarpeta(
                        any(byte[].class),
                        eq("Reporte_MAPFRE_0109_1509.xlsx"),
                        eq("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                        eq("PERIODO-MAPFRE")
                )
        ).thenReturn(excelDrive);

        when(
                drive.obtenerInformacionArchivo(
                        "PERIODO-MAPFRE"
                )
        ).thenReturn(carpeta);

        Optional<ResultadoReporteLoteVida> resultadoOpt =
                service.generarMapfre(
                        inicio,
                        fin
                );

        assertTrue(resultadoOpt.isPresent());
        assertEquals(
                1,
                resultadoOpt.get().getCantidadDocumentos()
        );
        assertEquals(
                "PERIODO-MAPFRE",
                resultadoOpt.get().getCarpetaDriveId()
        );

        verify(drive, never())
                .obtenerOCrearCarpeta(
                        any(),
                        any()
                );
        verify(drive, never())
                .moverArchivo(
                        any(),
                        any()
                );
    }
}
