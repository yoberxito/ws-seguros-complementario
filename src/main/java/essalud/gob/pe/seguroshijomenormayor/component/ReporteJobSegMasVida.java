package essalud.gob.pe.seguroshijomenormayor.component;

import essalud.gob.pe.seguroshijomenormayor.service.ReporteServiceSeguroMasVida;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReporteJobSegMasVida {

    private final ReporteServiceSeguroMasVida
            reporteServiceSeguroMasVida;

    /*
     * MAPFRE
     *
     * Q1: 01 -> 15
     *     ejecución: día 16.
     *
     * Q2: 16 -> último día del mes
     *     ejecución: día 1 del mes siguiente.
     */
    @Scheduled(
            cron = "0 0 2 1,16 * *",
            zone = "America/Lima"
    )
    public void generarReporteQuincenalMapfre() {

        reporteServiceSeguroMasVida
                .generarReporteMafre();
    }

    /*
     * PERSONAL / PLANILLAS ESSALUD
     *
     * Q1: 04 -> 18
     *     ejecución: día 19.
     *
     * Q2: 19 -> 03 del mes siguiente
     *     ejecución: día 4.
     */
    @Scheduled(
            cron = "0 0 2 4,19 * *",
            zone = "America/Lima"
    )
    public void generarReporteQuincenalPersonal() {

        reporteServiceSeguroMasVida
                .generarReportePersonal();
    }
}