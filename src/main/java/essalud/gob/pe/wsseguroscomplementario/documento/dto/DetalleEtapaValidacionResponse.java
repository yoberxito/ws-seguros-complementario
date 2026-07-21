package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class DetalleEtapaValidacionResponse {

    private String codigoEtapa;
    private String nombreEtapa;
    private boolean etapaAprobada;
    private String estadoEtapa;
    private String mensajeEtapa;
    private List<String> observaciones = new ArrayList<>();

    public DetalleEtapaValidacionResponse() {
    }

    public String getCodigoEtapa() {
        return codigoEtapa;
    }

    public void setCodigoEtapa(String codigoEtapa) {
        this.codigoEtapa = codigoEtapa;
    }

    public String getNombreEtapa() {
        return nombreEtapa;
    }

    public void setNombreEtapa(String nombreEtapa) {
        this.nombreEtapa = nombreEtapa;
    }

    public boolean isEtapaAprobada() {
        return etapaAprobada;
    }

    public void setEtapaAprobada(boolean etapaAprobada) {
        this.etapaAprobada = etapaAprobada;
    }

    public String getEstadoEtapa() {
        return estadoEtapa;
    }

    public void setEstadoEtapa(String estadoEtapa) {
        this.estadoEtapa = estadoEtapa;
    }

    public String getMensajeEtapa() {
        return mensajeEtapa;
    }

    public void setMensajeEtapa(String mensajeEtapa) {
        this.mensajeEtapa = mensajeEtapa;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
}