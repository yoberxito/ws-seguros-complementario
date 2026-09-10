package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.config.GoogleDriveProperties;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoCierreDrive;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CierreDriveLoteVidaServiceTest {

    private static final String ROOT_ID =
            "ROOT";

    private static final String FINAL_MAPFRE_ID =
            "FINAL_MAPFRE";

    private static final LocalDate INICIO =
            LocalDate.of(2026, 9, 1);

    private static final LocalDate FIN =
            LocalDate.of(2026, 9, 15);

    private static final String NOMBRE_PERIODO =
            "2026-09-01_2026-09-15";

    private GoogleDriveService googleDriveService;
    private GoogleDriveProperties properties;

    private CierreDriveLoteVidaService service;

    @BeforeEach
    void setUp() {

        googleDriveService =
                mock(
                        GoogleDriveService.class
                );

        properties =
                mock(
                        GoogleDriveProperties.class
                );

        when(
                properties.getFolderId()
        ).thenReturn(
                ROOT_ID
        );

        service =
                new CierreDriveLoteVidaService(
                        googleDriveService,
                        properties
                );
    }

    @Test
    void resolverUsaPreparacionEnPrimeraEjecucion()
            throws Exception {

        File destinoFinal =
                carpeta(
                        FINAL_MAPFRE_ID,
                        "Seguro +Vida - Afiliaciones MAPFRE",
                        "https://drive/final-mapfre"
                );

        File preparacion =
                carpeta(
                        "PERIODO_PREP",
                        NOMBRE_PERIODO,
                        "https://drive/preparacion"
                );

        when(
                googleDriveService
                        .obtenerOCrearCarpeta(
                                "Seguro +Vida - Afiliaciones MAPFRE",
                                ROOT_ID
                        )
        ).thenReturn(
                destinoFinal
        );

        when(
                googleDriveService
                        .buscarCarpetas(
                                NOMBRE_PERIODO,
                                FINAL_MAPFRE_ID
                        )
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                googleDriveService
                        .obtenerCarpetaPeriodo(
                                "244",
                                INICIO,
                                FIN
                        )
        ).thenReturn(
                preparacion
        );

        File resultado =
                service
                        .resolverCarpetaPeriodoTrabajo(
                                "MAPFRE",
                                INICIO,
                                FIN
                        );

        assertEquals(
                "PERIODO_PREP",
                resultado.getId()
        );
    }

    @Test
    void resolverUsaCarpetaFinalEnReejecucion()
            throws Exception {

        File destinoFinal =
                carpeta(
                        FINAL_MAPFRE_ID,
                        "Seguro +Vida - Afiliaciones MAPFRE",
                        "https://drive/final-mapfre"
                );

        File periodoFinal =
                carpeta(
                        "PERIODO_FINAL",
                        NOMBRE_PERIODO,
                        "https://drive/periodo-final"
                );

        when(
                googleDriveService
                        .obtenerOCrearCarpeta(
                                "Seguro +Vida - Afiliaciones MAPFRE",
                                ROOT_ID
                        )
        ).thenReturn(
                destinoFinal
        );

        when(
                googleDriveService
                        .buscarCarpetas(
                                NOMBRE_PERIODO,
                                FINAL_MAPFRE_ID
                        )
        ).thenReturn(
                List.of(
                        periodoFinal
                )
        );

        File resultado =
                service
                        .resolverCarpetaPeriodoTrabajo(
                                "MAPFRE",
                                INICIO,
                                FIN
                        );

        assertEquals(
                "PERIODO_FINAL",
                resultado.getId()
        );

        verify(
                googleDriveService,
                never()
        ).obtenerCarpetaPeriodo(
                anyString(),
                any(),
                any()
        );
    }

    @Test
    void cerrarMueveCarpetaHaciaDestinoFinal()
            throws Exception {

        File destinoFinal =
                carpeta(
                        FINAL_MAPFRE_ID,
                        "Seguro +Vida - Afiliaciones MAPFRE",
                        "https://drive/final-mapfre"
                );

        File preparacion =
                carpeta(
                        "PERIODO_PREP",
                        NOMBRE_PERIODO,
                        "https://drive/preparacion"
                );

        File movida =
                carpeta(
                        "PERIODO_PREP",
                        NOMBRE_PERIODO,
                        "https://drive/periodo-final"
                );

        when(
                googleDriveService
                        .obtenerOCrearCarpeta(
                                "Seguro +Vida - Afiliaciones MAPFRE",
                                ROOT_ID
                        )
        ).thenReturn(
                destinoFinal
        );

        when(
                googleDriveService
                        .buscarCarpetas(
                                NOMBRE_PERIODO,
                                FINAL_MAPFRE_ID
                        )
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                googleDriveService
                        .obtenerInformacionArchivo(
                                "PERIODO_PREP"
                        )
        ).thenReturn(
                preparacion
        );

        when(
                googleDriveService
                        .moverArchivo(
                                "PERIODO_PREP",
                                FINAL_MAPFRE_ID
                        )
        ).thenReturn(
                movida
        );

        ResultadoCierreDrive resultado =
                service
                        .cerrarCarpetaPeriodo(
                                "MAPFRE",
                                INICIO,
                                FIN,
                                "PERIODO_PREP",
                                3
                        );

        assertEquals(
                "PERIODO_PREP",
                resultado.getIdCarpetaFinal()
        );

        assertEquals(
                NOMBRE_PERIODO,
                resultado.getNombreCarpetaFinal()
        );

        assertEquals(
                "https://drive/periodo-final",
                resultado.getUrlCarpetaFinal()
        );

        assertEquals(
                3,
                resultado.getCantidadDocumentos()
        );
    }

    @Test
    void cerrarEsIdempotenteSiPeriodoYaEstaCerrado()
            throws Exception {

        File destinoFinal =
                carpeta(
                        FINAL_MAPFRE_ID,
                        "Seguro +Vida - Afiliaciones MAPFRE",
                        "https://drive/final-mapfre"
                );

        File periodoFinal =
                carpeta(
                        "PERIODO_FINAL",
                        NOMBRE_PERIODO,
                        "https://drive/periodo-final"
                );

        when(
                googleDriveService
                        .obtenerOCrearCarpeta(
                                "Seguro +Vida - Afiliaciones MAPFRE",
                                ROOT_ID
                        )
        ).thenReturn(
                destinoFinal
        );

        when(
                googleDriveService
                        .buscarCarpetas(
                                NOMBRE_PERIODO,
                                FINAL_MAPFRE_ID
                        )
        ).thenReturn(
                List.of(
                        periodoFinal
                )
        );

        ResultadoCierreDrive resultado =
                service
                        .cerrarCarpetaPeriodo(
                                "MAPFRE",
                                INICIO,
                                FIN,
                                "PERIODO_FINAL",
                                3
                        );

        assertEquals(
                "PERIODO_FINAL",
                resultado.getIdCarpetaFinal()
        );

        verify(
                googleDriveService,
                never()
        ).moverArchivo(
                anyString(),
                anyString()
        );
    }

    @Test
    void cerrarRechazaOtraCarpetaFinalDelMismoPeriodo()
            throws Exception {

        File destinoFinal =
                carpeta(
                        FINAL_MAPFRE_ID,
                        "Seguro +Vida - Afiliaciones MAPFRE",
                        "https://drive/final-mapfre"
                );

        File periodoFinal =
                carpeta(
                        "OTRO_ID",
                        NOMBRE_PERIODO,
                        "https://drive/otro"
                );

        when(
                googleDriveService
                        .obtenerOCrearCarpeta(
                                "Seguro +Vida - Afiliaciones MAPFRE",
                                ROOT_ID
                        )
        ).thenReturn(
                destinoFinal
        );

        when(
                googleDriveService
                        .buscarCarpetas(
                                NOMBRE_PERIODO,
                                FINAL_MAPFRE_ID
                        )
        ).thenReturn(
                List.of(
                        periodoFinal
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service
                                .cerrarCarpetaPeriodo(
                                        "MAPFRE",
                                        INICIO,
                                        FIN,
                                        "PERIODO_PREP",
                                        3
                                )
        );
    }

    @Test
    void personalCreaYUsaSuCarpetaInstitucional()
            throws Exception {

        File destinoPersonal =
                carpeta(
                        "FINAL_PERSONAL",
                        "Seguro +Vida - Autorizaciones de Descuento - Personal EsSalud",
                        "https://drive/final-personal"
                );

        File preparacion =
                carpeta(
                        "PERIODO_PERSONAL",
                        NOMBRE_PERIODO,
                        "https://drive/preparacion-personal"
                );

        when(
                googleDriveService
                        .obtenerOCrearCarpeta(
                                "Seguro +Vida - Autorizaciones de Descuento - Personal EsSalud",
                                ROOT_ID
                        )
        ).thenReturn(
                destinoPersonal
        );

        when(
                googleDriveService
                        .buscarCarpetas(
                                NOMBRE_PERIODO,
                                "FINAL_PERSONAL"
                        )
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                googleDriveService
                        .obtenerCarpetaPeriodo(
                                "247",
                                INICIO,
                                FIN
                        )
        ).thenReturn(
                preparacion
        );

        File resultado =
                service
                        .resolverCarpetaPeriodoTrabajo(
                                "PERSONAL",
                                INICIO,
                                FIN
                        );

        assertEquals(
                "PERIODO_PERSONAL",
                resultado.getId()
        );

        verify(
                googleDriveService
        ).obtenerOCrearCarpeta(
                "Seguro +Vida - Autorizaciones de Descuento - Personal EsSalud",
                ROOT_ID
        );
    }

    private File carpeta(
            String id,
            String nombre,
            String webViewLink
    ) {

        return new File()
                .setId(id)
                .setName(nombre)
                .setMimeType(
                        "application/vnd.google-apps.folder"
                )
                .setWebViewLink(
                        webViewLink
                );
    }
}