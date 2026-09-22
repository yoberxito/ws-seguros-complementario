package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.config.GoogleDriveProperties;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoCierreDrive;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CierreDriveLoteVidaServiceTest {

    private static final LocalDate INICIO =
            LocalDate.of(2026, 9, 1);

    private static final LocalDate FIN =
            LocalDate.of(2026, 9, 15);

    private GoogleDriveService drive;
    private GoogleDriveProperties properties;
    private CierreDriveLoteVidaService service;

    @BeforeEach
    void setUp() {

        drive = mock(GoogleDriveService.class);
        properties = mock(GoogleDriveProperties.class);

        when(properties.getFolderId())
                .thenReturn("ROOT");

        service =
                new CierreDriveLoteVidaService(
                        drive,
                        properties
                );
    }

    @Test
    void resolverPeriodoNoCreaCarpetas() throws Exception {

        File preparacion =
                carpeta(
                        "PERIODO",
                        "2026-09-01_2026-09-15",
                        "https://drive/periodo"
                );

        when(
                drive.buscarCarpetas(
                        "MAPFRE_HISTORICAL",
                        "ROOT"
                )
        ).thenReturn(Collections.emptyList());

        when(
                drive.buscarCarpetaPeriodo(
                        "244",
                        INICIO,
                        FIN
                )
        ).thenReturn(Optional.of(preparacion));

        File resultado =
                service.resolverCarpetaPeriodoTrabajo(
                        "MAPFRE",
                        INICIO,
                        FIN
                );

        assertEquals(
                "PERIODO",
                resultado.getId()
        );

        verify(drive, never())
                .obtenerOCrearCarpeta(
                        anyString(),
                        anyString()
                );
    }

    @Test
    void cerrarCreaHistoricalSoloCuandoExisteLoteReal()
            throws Exception {

        File periodo =
                carpeta(
                        "PERIODO",
                        "2026-09-01_2026-09-15",
                        "https://drive/preparacion"
                );

        File historical =
                carpeta(
                        "HIST",
                        "MAPFRE_HISTORICAL",
                        "https://drive/historical"
                );

        File movida =
                carpeta(
                        "PERIODO",
                        "2026-09-01_2026-09-15",
                        "https://drive/historical/periodo"
                );

        when(drive.obtenerInformacionArchivo("PERIODO"))
                .thenReturn(periodo);

        when(
                drive.buscarCarpetas(
                        "MAPFRE_HISTORICAL",
                        "ROOT"
                )
        ).thenReturn(Collections.emptyList());

        when(
                drive.obtenerOCrearCarpeta(
                        "MAPFRE_HISTORICAL",
                        "ROOT"
                )
        ).thenReturn(historical);

        when(
                drive.moverArchivo(
                        "PERIODO",
                        "HIST"
                )
        ).thenReturn(movida);

        ResultadoCierreDrive resultado =
                service.cerrarCarpetaPeriodo(
                        "MAPFRE",
                        INICIO,
                        FIN,
                        "PERIODO",
                        3
                );

        assertEquals(
                "PERIODO",
                resultado.getIdCarpetaFinal()
        );
        assertEquals(
                3,
                resultado.getCantidadDocumentos()
        );
    }

    @Test
    void personalNoPuedeUsarCierreLegacyMapfre() throws java.io.IOException {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.resolverCarpetaPeriodoTrabajo(
                        "PERSONAL",
                        INICIO,
                        FIN
                )
        );

        verify(drive, never())
                .buscarCarpetas(
                        anyString(),
                        anyString()
                );
    }

    private File carpeta(
            String id,
            String nombre,
            String webViewLink
    ) {

        File file = new File();
        file.setId(id);
        file.setName(nombre);
        file.setMimeType(
                "application/vnd.google-apps.folder"
        );
        file.setWebViewLink(webViewLink);

        return file;
    }
}
