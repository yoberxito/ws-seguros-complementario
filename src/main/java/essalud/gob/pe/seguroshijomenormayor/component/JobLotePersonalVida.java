package essalud.gob.pe.seguroshijomenormayor.component;

import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import essalud.gob.pe.seguroshijomenormayor.lote.service.CalendarioLotesVidaService;
import essalud.gob.pe.seguroshijomenormayor.lote.service.ProcesoLotePersonalVidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@ConditionalOnProperty(
        name = "integraciones.lotes-vida.personal.job-habilitado",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class JobLotePersonalVida {

    private static final ZoneId ZONA_LIMA =
            ZoneId.of("America/Lima");

    private final CalendarioLotesVidaService calendario;
    private final ProcesoLotePersonalVidaService proceso;

    @Scheduled(
            cron = "20 15 13 22 9 *",
            zone = "America/Lima"
    )
    public void ejecutar() {

        Periodo periodo =
                calendario
                        .obtenerPeriodoPersonal(
                                LocalDate.of(2026, 10, 4)
                        );

        proceso.procesar(
                periodo
        );
    }
}
