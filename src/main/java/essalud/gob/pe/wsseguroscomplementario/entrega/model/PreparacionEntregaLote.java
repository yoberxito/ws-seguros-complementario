package essalud.gob.pe.wsseguroscomplementario.entrega.model;

public class PreparacionEntregaLote {

    private final EntregaLote entrega;
    private final String tokenPublico;
    private final boolean entregaNueva;
    private final boolean tokenGenerado;

    public PreparacionEntregaLote(
            EntregaLote entrega,
            String tokenPublico,
            boolean entregaNueva,
            boolean tokenGenerado
    ) {
        this.entrega = entrega;
        this.tokenPublico = tokenPublico;
        this.entregaNueva = entregaNueva;
        this.tokenGenerado = tokenGenerado;
    }

    public EntregaLote getEntrega() {
        return entrega;
    }

    public String getTokenPublico() {
        return tokenPublico;
    }

    public boolean isEntregaNueva() {
        return entregaNueva;
    }

    public boolean isTokenGenerado() {
        return tokenGenerado;
    }
}