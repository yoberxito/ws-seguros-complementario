package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.lote.repository.ReporteLoteVidaRepository;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReporteQuincenalLoteVidaPreparacionA4Test {

    @Test
    void periodoSinDocumentosEsNoOpYSinEfectosDrive()
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
                LocalDate.of(2026, 9, 16);
        LocalDate fin =
                LocalDate.of(2026, 9, 30);

        when(
                drive.buscarCarpetaPeriodo(
                        "244",
                        inicio,
                        fin
                )
        ).thenReturn(Optional.empty());

        assertTrue(
                service.generarMapfre(
                        inicio,
                        fin
                ).isEmpty()
        );

        verify(drive, never())
                .listarArchivosCarpeta(
                        anyString()
                );
        verify(drive, never())
                .obtenerOCrearCarpeta(
                        anyString(),
                        anyString()
                );
    }
}
