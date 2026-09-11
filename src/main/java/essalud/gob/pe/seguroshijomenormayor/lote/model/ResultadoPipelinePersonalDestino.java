package essalud.gob.pe.seguroshijomenormayor.lote.model;

import essalud.gob.pe.seguroshijomenormayor.entrega.model.CierreLotePreparado;

/*
 * Resultado completo del pipeline PERSONAL
 * para un unico destino institucional.
 */
public class ResultadoPipelinePersonalDestino {

    private final DestinoPersonalVida destino;

    private final ResultadoReportePersonalDestino reporte;

    private final CierreLotePreparado cierre;

    public ResultadoPipelinePersonalDestino(
            DestinoPersonalVida destino,
            ResultadoReportePersonalDestino reporte,
            CierreLotePreparado cierre
    ) {

        if (destino == null) {
            throw new IllegalArgumentException(
                    "El destino PERSONAL es obligatorio."
            );
        }

        if (reporte == null) {
            throw new IllegalArgumentException(
                    "El reporte PERSONAL es obligatorio."
            );
        }

        if (
                cierre == null
                        || cierre.getLote() == null
                        || cierre.getLote()
                                .getIdLote() == null
                        || cierre.getPreparacionEntrega() == null
                        || cierre.getPreparacionEntrega()
                                .getEntrega() == null
                        || cierre.getPreparacionEntrega()
                                .getEntrega()
                                .getIdEntrega() == null
        ) {

            throw new IllegalArgumentException(
                    "El cierre persistente PERSONAL es incompleto."
            );
        }

        this.destino =
                destino;

        this.reporte =
                reporte;

        this.cierre =
                cierre;
    }

    public DestinoPersonalVida getDestino() {
        return destino;
    }

    public ResultadoReportePersonalDestino getReporte() {
        return reporte;
    }

    public CierreLotePreparado getCierre() {
        return cierre;
    }

    public Long getIdLote() {
        return cierre
                .getLote()
                .getIdLote();
    }

    public Long getIdEntrega() {
        return cierre
                .getPreparacionEntrega()
                .getEntrega()
                .getIdEntrega();
    }

    /*
     * Puede ser null en un rerun posterior a una
     * notificacion ya enviada o a un acuse ya registrado.
     *
     * El servicio existente deliberadamente no regenera
     * tokens en esos estados.
     */
    public String getTokenPublico() {
        return cierre.getTokenPublico();
    }

    public boolean isTokenGenerado() {
        return cierre.isTokenGenerado();
    }
}