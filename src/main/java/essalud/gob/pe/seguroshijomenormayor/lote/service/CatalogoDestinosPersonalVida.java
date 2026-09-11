package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DestinoPersonalVida;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogo institucional de destinos PERSONAL +Vida.
 *
 * codigoDestino es la identidad funcional.
 *
 * Este catalogo define los destinos disponibles.
 * NO resuelve DNI -> destino.
 */
public final class CatalogoDestinosPersonalVida {

    private static final List<DestinoPersonalVida> DESTINOS =
            List.of(
                    new DestinoPersonalVida("001", "RED ASISTENCIAL AMAZONAS"),
                    new DestinoPersonalVida("002", "RED ASISTENCIAL ANCASH"),
                    new DestinoPersonalVida("003", "RED ASISTENCIAL APURIMAC"),
                    new DestinoPersonalVida("004", "RED ASISTENCIAL AREQUIPA"),
                    new DestinoPersonalVida("005", "RED ASISTENCIAL AYACUCHO"),
                    new DestinoPersonalVida("006", "RED ASISTENCIAL CAJAMARCA"),
                    new DestinoPersonalVida("007", "RED ASISTENCIAL CUSCO"),
                    new DestinoPersonalVida("008", "RED ASISTENCIAL HUANCAVELICA"),
                    new DestinoPersonalVida("009", "RED ASISTENCIAL HUÁNUCO"),
                    new DestinoPersonalVida("010", "RED ASISTENCIAL HUARAZ"),
                    new DestinoPersonalVida("011", "RED ASISTENCIAL ICA"),
                    new DestinoPersonalVida("012", "RED ASISTENCIAL JAEN"),
                    new DestinoPersonalVida("013", "RED ASISTENCIAL JULIACA"),
                    new DestinoPersonalVida("014", "RED ASISTENCIAL JUNIN"),
                    new DestinoPersonalVida("015", "RED ASISTENCIAL LA LIBERTAD"),
                    new DestinoPersonalVida("016", "RED ASISTENCIAL LORETO"),
                    new DestinoPersonalVida("017", "RED ASISTENCIAL MOQUEGUA"),
                    new DestinoPersonalVida("018", "RED ASISTENCIAL MOYOBAMBA"),
                    new DestinoPersonalVida("019", "RED ASISTENCIAL PASCO"),
                    new DestinoPersonalVida("020", "RED ASISTENCIAL PIURA"),
                    new DestinoPersonalVida("021", "RED ASISTENCIAL PUNO"),
                    new DestinoPersonalVida("022", "RED ASISTENCIAL TACNA"),
                    new DestinoPersonalVida("023", "RED ASISTENCIAL TARAPOTO"),
                    new DestinoPersonalVida("024", "RED ASISTENCIAL TUMBES"),
                    new DestinoPersonalVida("025", "RED ASISTENCIAL UCAYALI"),
                    new DestinoPersonalVida("026", "RED PRESTACIONAL ALMENARA"),
                    new DestinoPersonalVida("027", "RED PRESTACIONAL LAMBAYEQUE"),
                    new DestinoPersonalVida("028", "RED PRESTACIONAL REBAGLIATI"),
                    new DestinoPersonalVida("029", "RED PRESTACIONAL SABOGAL"),
                    new DestinoPersonalVida("030", "INCOR"),
                    new DestinoPersonalVida("031", "CENTRO NACIONAL DE SALUD RENAL"),
                    new DestinoPersonalVida("032", "SEDE CENTRAL")
            );

    private static final Map<String, DestinoPersonalVida>
            POR_CODIGO =
            construirIndice();

    private CatalogoDestinosPersonalVida() {
    }

    public static List<DestinoPersonalVida> listar() {

        return DESTINOS;
    }

    public static DestinoPersonalVida buscarPorCodigo(
            String codigoDestino
    ) {

        if (
                codigoDestino == null
                        || codigoDestino.trim().isEmpty()
        ) {

            return null;
        }

        return POR_CODIGO.get(
                codigoDestino.trim()
        );
    }

    public static DestinoPersonalVida requerirPorCodigo(
            String codigoDestino
    ) {

        DestinoPersonalVida destino =
                buscarPorCodigo(
                        codigoDestino
                );

        if (destino == null) {

            throw new IllegalArgumentException(
                    "El codigo de destino PERSONAL "
                            + codigoDestino
                            + " no pertenece al catalogo institucional."
            );
        }

        return destino;
    }

    private static Map<String, DestinoPersonalVida>
    construirIndice() {

        Map<String, DestinoPersonalVida> indice =
                new LinkedHashMap<>();

        for (
                DestinoPersonalVida destino
                : DESTINOS
        ) {

            DestinoPersonalVida anterior =
                    indice.put(
                            destino.codigoDestino(),
                            destino
                    );

            if (anterior != null) {

                throw new IllegalStateException(
                        "Codigo PERSONAL duplicado: "
                                + destino.codigoDestino()
                );
            }
        }

        return Map.copyOf(
                indice
        );
    }
}