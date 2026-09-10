package essalud.gob.pe.seguroshijomenormayor.component;

import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import essalud.gob.pe.seguroshijomenormayor.lote.service.ReporteQuincenalLoteVidaService;
import essalud.gob.pe.seguroshijomenormayor.service.impl.ReporteServiceSeguroMasVidaImpl;

import org.junit.jupiter.api.Test;

import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReporteSchedulingVidaTest {

    @Test
    void calendarioMapfreEsCorrecto() {

        ReporteServiceSeguroMasVidaImpl service =
                crearService();

        Periodo q1 =
                service.obtenerPeriodoMapfre(
                        LocalDate.of(
                                2026,
                                9,
                                16
                        )
                );

        assertEquals(
                LocalDate.of(2026, 9, 1),
                q1.inicio()
        );

        assertEquals(
                LocalDate.of(2026, 9, 15),
                q1.fin()
        );

        Periodo q2 =
                service.obtenerPeriodoMapfre(
                        LocalDate.of(
                                2026,
                                10,
                                1
                        )
                );

        assertEquals(
                LocalDate.of(2026, 9, 16),
                q2.inicio()
        );

        assertEquals(
                LocalDate.of(2026, 9, 30),
                q2.fin()
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.obtenerPeriodoMapfre(
                                LocalDate.of(
                                        2026,
                                        9,
                                        9
                                )
                        )
        );
    }


    @Test
    void calendarioPersonalEsCorrecto() {

        ReporteServiceSeguroMasVidaImpl service =
                crearService();

        Periodo q1 =
                service.obtenerPeriodoPersonal(
                        LocalDate.of(
                                2026,
                                9,
                                19
                        )
                );

        assertEquals(
                LocalDate.of(2026, 9, 4),
                q1.inicio()
        );

        assertEquals(
                LocalDate.of(2026, 9, 18),
                q1.fin()
        );

        Periodo q2 =
                service.obtenerPeriodoPersonal(
                        LocalDate.of(
                                2026,
                                10,
                                4
                        )
                );

        assertEquals(
                LocalDate.of(2026, 9, 19),
                q2.inicio()
        );

        assertEquals(
                LocalDate.of(2026, 10, 3),
                q2.fin()
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.obtenerPeriodoPersonal(
                                LocalDate.of(
                                        2026,
                                        9,
                                        9
                                )
                        )
        );
    }


    @Test
    void cronMapfreEsElConfirmado()
            throws Exception {

        Method metodo =
                ReporteJobSegMasVida.class
                        .getMethod(
                                "generarReporteQuincenalMapfre"
                        );

        Scheduled scheduled =
                metodo.getAnnotation(
                        Scheduled.class
                );

        assertNotNull(
                scheduled
        );

        assertEquals(
                "0 0 2 1,16 * *",
                scheduled.cron()
        );

        assertEquals(
                "America/Lima",
                scheduled.zone()
        );
    }


    @Test
    void cronPersonalEsElConfirmado()
            throws Exception {

        Method metodo =
                ReporteJobSegMasVida.class
                        .getMethod(
                                "generarReporteQuincenalPersonal"
                        );

        Scheduled scheduled =
                metodo.getAnnotation(
                        Scheduled.class
                );

        assertNotNull(
                scheduled
        );

        assertEquals(
                "0 0 2 4,19 * *",
                scheduled.cron()
        );

        assertEquals(
                "America/Lima",
                scheduled.zone()
        );
    }

    private ReporteServiceSeguroMasVidaImpl crearService() {

        return new ReporteServiceSeguroMasVidaImpl(
                mock(
                        ReporteQuincenalLoteVidaService.class
                ),
                mock(
                        essalud.gob.pe.seguroshijomenormayor.entrega.service.CierreLoteDistribucionService.class
                )
        );
    }
}
