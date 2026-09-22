package essalud.gob.pe.seguroshijomenormayor.component;

import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import essalud.gob.pe.seguroshijomenormayor.lote.service.CalendarioLotesVidaService;
import essalud.gob.pe.seguroshijomenormayor.lote.service.ProcesoLoteMapfreVidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class JobLoteMapfreVida {

    private static final ZoneId ZONA_LIMA =
            ZoneId.of("America/Lima");

    private final CalendarioLotesVidaService calendario;
    private final ProcesoLoteMapfreVidaService proceso;

    @Scheduled(
            cron = "0 15 13 22 9 *",
            zone = "America/Lima"
    )
    public void ejecutar() {

        Periodo periodo =
                calendario
                        .obtenerPeriodoMapfre(
                                LocalDate.of(2026, 10, 1)
                        );

        proceso.procesar(
                periodo
        );
    }
}
