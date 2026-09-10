package essalud.gob.pe.seguroshijomenormayor.dto;

import java.time.LocalDate;

public record Periodo(
        LocalDate inicio,
        LocalDate fin
) {

    public Periodo {

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(
                    "Las fechas del período son obligatorias."
            );
        }

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la final."
            );
        }
    }
}