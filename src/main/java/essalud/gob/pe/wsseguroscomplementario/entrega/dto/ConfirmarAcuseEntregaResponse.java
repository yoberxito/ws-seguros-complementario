package essalud.gob.pe.wsseguroscomplementario.entrega.dto;

import java.time.LocalDateTime;

public class ConfirmarAcuseEntregaResponse {

    private boolean acuseRegistrado;
    private boolean yaRegistrado;

    private LocalDateTime fechaAcuse;

    private String textoAcuse;
    private String versionTextoAcuse;

    private boolean accesoDisponible;
    private String urlAcceso;

    public ConfirmarAcuseEntregaResponse() {
    }

    public boolean isAcuseRegistrado() {
        return acuseRegistrado;
    }

    public void setAcuseRegistrado(
            boolean acuseRegistrado
    ) {
        this.acuseRegistrado =
                acuseRegistrado;
    }

    public boolean isYaRegistrado() {
        return yaRegistrado;
    }

    public void setYaRegistrado(
            boolean yaRegistrado
    ) {
        this.yaRegistrado =
                yaRegistrado;
    }

    public LocalDateTime getFechaAcuse() {
        return fechaAcuse;
    }

    public void setFechaAcuse(
            LocalDateTime fechaAcuse
    ) {
        this.fechaAcuse =
                fechaAcuse;
    }

    public String getTextoAcuse() {
        return textoAcuse;
    }

    public void setTextoAcuse(
            String textoAcuse
    ) {
        this.textoAcuse =
                textoAcuse;
    }

    public String getVersionTextoAcuse() {
        return versionTextoAcuse;
    }

    public void setVersionTextoAcuse(
            String versionTextoAcuse
    ) {
        this.versionTextoAcuse =
                versionTextoAcuse;
    }

    public boolean isAccesoDisponible() {
        return accesoDisponible;
    }

    public void setAccesoDisponible(
            boolean accesoDisponible
    ) {
        this.accesoDisponible =
                accesoDisponible;
    }

    public String getUrlAcceso() {
        return urlAcceso;
    }

    public void setUrlAcceso(
            String urlAcceso
    ) {
        this.urlAcceso =
                urlAcceso;
    }
}