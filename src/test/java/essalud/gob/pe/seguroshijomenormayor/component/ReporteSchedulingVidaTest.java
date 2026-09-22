package essalud.gob.pe.seguroshijomenormayor.component;

import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import essalud.gob.pe.seguroshijomenormayor.lote.service.CalendarioLotesVidaService;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReporteSchedulingVidaTest {

    private final CalendarioLotesVidaService calendario =
            new CalendarioLotesVidaService();

    @Test
    void calendarioMapfreEsCorrecto() {

        Periodo primeraMitad =
                calendario.obtenerPeriodoMapfre(
                        LocalDate.of(2026, 9, 16)
                );

        assertEquals(
                LocalDate.of(2026, 9, 1),
                primeraMitad.inicio()
        );
        assertEquals(
                LocalDate.of(2026, 9, 15),
                primeraMitad.fin()
        );

        Periodo segundaMitad =
                calendario.obtenerPeriodoMapfre(
                        LocalDate.of(2026, 10, 1)
                );

        assertEquals(
                LocalDate.of(2026, 9, 16),
                segundaMitad.inicio()
        );
        assertEquals(
                LocalDate.of(2026, 9, 30),
                segundaMitad.fin()
        );
    }

    @Test
    void calendarioPersonalEsCorrecto() {

        Periodo cuatroDieciocho =
                calendario.obtenerPeriodoPersonal(
                        LocalDate.of(2026, 9, 19)
                );

        assertEquals(
                LocalDate.of(2026, 9, 4),
                cuatroDieciocho.inicio()
        );
        assertEquals(
                LocalDate.of(2026, 9, 18),
                cuatroDieciocho.fin()
        );

        Periodo diecinueveTres =
                calendario.obtenerPeriodoPersonal(
                        LocalDate.of(2026, 10, 4)
                );

        assertEquals(
                LocalDate.of(2026, 9, 19),
                diecinueveTres.inicio()
        );
        assertEquals(
                LocalDate.of(2026, 10, 3),
                diecinueveTres.fin()
        );
    }

    @Test
    void cronMapfreEsElConfirmado() throws Exception {

        Scheduled scheduled =
                scheduled(
                        JobLoteMapfreVida.class
                );

        assertEquals(
                "0 15 13 22 9 *",
                scheduled.cron()
        );
        assertEquals(
                "America/Lima",
                scheduled.zone()
        );
    }

    @Test
    void cronPersonalEsElConfirmado() throws Exception {

        Scheduled scheduled =
                scheduled(
                        JobLotePersonalVida.class
                );

        assertEquals(
                "20 15 13 22 9 *",
                scheduled.cron()
        );
        assertEquals(
                "America/Lima",
                scheduled.zone()
        );
    }

    private Scheduled scheduled(
            Class<?> jobClass
    ) throws Exception {

        Method metodo =
                jobClass.getMethod(
                        "ejecutar"
                );

        return metodo.getAnnotation(
                Scheduled.class
        );
    }
}
