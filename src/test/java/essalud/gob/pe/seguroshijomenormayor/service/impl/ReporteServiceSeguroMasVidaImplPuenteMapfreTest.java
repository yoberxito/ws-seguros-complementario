package essalud.gob.pe.seguroshijomenormayor.service.impl;

import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.CierreLotePreparado;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.EntregaLote;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.LoteDistribucion;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.PreparacionEntregaLote;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.CierreLoteDistribucionService;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoReporteLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.service.ReporteQuincenalLoteVidaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceSeguroMasVidaImplPuenteMapfreTest {

    @Mock
    private ReporteQuincenalLoteVidaService reporteService;

    @Mock
    private CierreLoteDistribucionService cierreService;

    private ReporteServiceSeguroMasVidaImpl servicio;

    @BeforeEach
    void setUp() {

        servicio =
                new ReporteServiceSeguroMasVidaImpl(
                        reporteService,
                        cierreService
                );
    }

    @Test
    void conectaCarpetaFinalConLoteEntregaYTokenMapfre()
            throws Exception {

        LocalDate inicio =
                LocalDate.of(2096, 9, 1);

        LocalDate fin =
                LocalDate.of(2096, 9, 15);

        Periodo periodo =
                new Periodo(
                        inicio,
                        fin
                );

        ReflectionTestUtils.setField(
                servicio,
                "correoDestinatarioMapfre",
                "mapfre.qa@example.test"
        );

        ResultadoReporteLoteVida resultado =
                mock(ResultadoReporteLoteVida.class);

        when(resultado.getDestinatario())
                .thenReturn("MAPFRE");

        when(resultado.getFechaInicio())
                .thenReturn(inicio);

        when(resultado.getFechaFin())
                .thenReturn(fin);

        when(resultado.getCantidadDocumentos())
                .thenReturn(3);

        when(resultado.getUrlCarpetaFinal())
                .thenReturn(
                        "https://drive.google.com/drive/folders/MAPFRE_FINAL"
                );

        when(
                reporteService.generar(
                        "MAPFRE",
                        inicio,
                        fin
                )
        ).thenReturn(
                resultado
        );

        LoteDistribucion lote =
                mock(LoteDistribucion.class);

        when(lote.getIdLote())
                .thenReturn(101L);

        EntregaLote entrega =
                mock(EntregaLote.class);

        when(entrega.getIdEntrega())
                .thenReturn(202L);

        PreparacionEntregaLote preparacion =
                new PreparacionEntregaLote(
                        entrega,
                        "TOKEN_SOLO_MEMORIA_TEST",
                        true,
                        true
                );

        CierreLotePreparado cierre =
                new CierreLotePreparado(
                        lote,
                        preparacion
                );

        when(
                cierreService.prepararLotePublicado(
                        "MAPFRE",
                        inicio,
                        fin,
                        "mapfre.qa@example.test",
                        3,
                        "https://drive.google.com/drive/folders/MAPFRE_FINAL"
                )
        ).thenReturn(
                cierre
        );

        ReflectionTestUtils.invokeMethod(
                servicio,
                "ejecutarReporte",
                "MAPFRE",
                periodo
        );

        verify(
                reporteService,
                times(1)
        ).generar(
                "MAPFRE",
                inicio,
                fin
        );

        verify(
                cierreService,
                times(1)
        ).prepararLotePublicado(
                "MAPFRE",
                inicio,
                fin,
                "mapfre.qa@example.test",
                3,
                "https://drive.google.com/drive/folders/MAPFRE_FINAL"
        );
    }

    @Test
    void noTocaDriveSiCorreoMapfreNoEstaConfigurado() {

        LocalDate inicio =
                LocalDate.of(2096, 9, 1);

        LocalDate fin =
                LocalDate.of(2096, 9, 15);

        Periodo periodo =
                new Periodo(
                        inicio,
                        fin
                );

        ReflectionTestUtils.setField(
                servicio,
                "correoDestinatarioMapfre",
                ""
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        ReflectionTestUtils.invokeMethod(
                                servicio,
                                "ejecutarReporte",
                                "MAPFRE",
                                periodo
                        )
        );

        verifyNoInteractions(
                reporteService,
                cierreService
        );
    }
}