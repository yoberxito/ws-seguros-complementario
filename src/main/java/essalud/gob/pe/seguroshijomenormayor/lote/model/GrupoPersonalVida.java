package essalud.gob.pe.seguroshijomenormayor.lote.model;

import java.util.List;

/*
 * Conjunto de trabajadores PERSONAL pertenecientes
 * al mismo destino institucional.
 */
public class GrupoPersonalVida {

    private final DestinoPersonalVida destino;

    private final List<ReporteLoteVidaItem> items;

    public GrupoPersonalVida(
            DestinoPersonalVida destino,
            List<ReporteLoteVidaItem> items
    ) {

        if (destino == null) {
            throw new IllegalArgumentException(
                    "El destino PERSONAL es obligatorio."
            );
        }

        if (items == null) {
            throw new IllegalArgumentException(
                    "Los items PERSONAL son obligatorios."
            );
        }

        this.destino =
                destino;

        this.items =
                List.copyOf(
                        items
                );
    }

    public DestinoPersonalVida getDestino() {
        return destino;
    }

    public List<ReporteLoteVidaItem> getItems() {
        return items;
    }

    public int getCantidadDocumentos() {
        return items.size();
    }
}