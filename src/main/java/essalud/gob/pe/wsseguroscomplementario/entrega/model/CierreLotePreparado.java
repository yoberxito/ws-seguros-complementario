package essalud.gob.pe.wsseguroscomplementario.entrega.model;

/*
 * Resultado del cierre operativo de un lote +Vida.
 *
 * Contiene:
 * - el lote persistido/publicado;
 * - la entrega preparada;
 * - el token público únicamente en memoria.
 *
 * El token público NO se persiste en Oracle.
 */
public class CierreLotePreparado {

    private final LoteDistribucion lote;
    private final PreparacionEntregaLote preparacionEntrega;

    public CierreLotePreparado(
            LoteDistribucion lote,
            PreparacionEntregaLote preparacionEntrega
    ) {
        this.lote = lote;
        this.preparacionEntrega = preparacionEntrega;
    }

    public LoteDistribucion getLote() {
        return lote;
    }

    public PreparacionEntregaLote getPreparacionEntrega() {
        return preparacionEntrega;
    }

    public String getTokenPublico() {

        return preparacionEntrega == null
                ? null
                : preparacionEntrega.getTokenPublico();
    }

    public boolean isTokenGenerado() {

        return preparacionEntrega != null
                && preparacionEntrega.isTokenGenerado();
    }
}