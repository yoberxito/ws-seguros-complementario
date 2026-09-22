package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CalendarioLotesVidaService {

    public Periodo obtenerPeriodoMapfre(
            LocalDate fecha
    ) {

        validarFecha(
                fecha
        );

        int dia =
                fecha.getDayOfMonth();

        if (dia == 1) {

            LocalDate fin =
                    fecha.minusDays(1);

            return new Periodo(
                    fin.withDayOfMonth(16),
                    fin
            );
        }

        if (dia == 16) {
            return new Periodo(
                    fecha.withDayOfMonth(1),
                    fecha.withDayOfMonth(15)
            );
        }

        throw new IllegalStateException(
                "El proceso MAPFRE solamente puede ejecutarse los dias 1 y 16."
        );
    }

    public Periodo obtenerPeriodoPersonal(
            LocalDate fecha
    ) {

        validarFecha(
                fecha
        );

        int dia =
                fecha.getDayOfMonth();

        if (dia == 19) {
            return new Periodo(
                    fecha.withDayOfMonth(4),
                    fecha.withDayOfMonth(18)
            );
        }

        if (dia == 4) {
            return new Periodo(
                    fecha.minusMonths(1).withDayOfMonth(19),
                    fecha.withDayOfMonth(3)
            );
        }

        throw new IllegalStateException(
                "El proceso PERSONAL solamente puede ejecutarse los dias 4 y 19."
        );
    }

    private void validarFecha(
            LocalDate fecha
    ) {
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha es obligatoria."
            );
        }
    }
}
