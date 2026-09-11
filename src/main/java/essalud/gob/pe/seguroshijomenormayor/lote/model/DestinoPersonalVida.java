package essalud.gob.pe.seguroshijomenormayor.lote.model;

import java.util.Locale;

/*
 * Destino institucional de una entrega PERSONAL +Vida.
 *
 * El motor no presupone que la dimension institucional
 * definitiva tenga que llamarse siempre "Red".
 *
 * La integracion futura podra resolver por DNI el codigo
 * y nombre de la unidad responsable que corresponda.
 */
public record DestinoPersonalVida(
        String codigoDestino,
        String nombreDestino
) {

    public DestinoPersonalVida {

        codigoDestino =
                requerir(
                        codigoDestino,
                        "El codigo del destino PERSONAL es obligatorio."
                )
                        .toUpperCase(
                                Locale.ROOT
                        );

        nombreDestino =
                requerir(
                        nombreDestino,
                        "El nombre del destino PERSONAL es obligatorio."
                );
    }

    private static String requerir(
            String valor,
            String mensaje
    ) {

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    mensaje
            );
        }

        return valor.trim();
    }
}