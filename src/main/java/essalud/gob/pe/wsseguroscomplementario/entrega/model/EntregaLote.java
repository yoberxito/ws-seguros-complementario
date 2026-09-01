package essalud.gob.pe.wsseguroscomplementario.entrega.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EntregaLote {

    private String tipoDestinatario;
    private String correoDestinatario;
    private int cantidadDocumentos;
    private String urlAcceso;

    private LocalDateTime fechaOtpValidado;
    private LocalDateTime fechaAcuse;
    private String textoAcuse;
    private String versionTextoAcuse;
    private LocalDate fechaInicioPeriodo;
    private LocalDate fechaFinPeriodo;
    private LocalDateTime fechaPublicacion;

    public EntregaLote() {
    }

    public String getTipoDestinatario() {
        return tipoDestinatario;
    }

    public void setTipoDestinatario(
            String tipoDestinatario
    ) {
        this.tipoDestinatario =
                tipoDestinatario;
    }

    public String getCorreoDestinatario() {
        return correoDestinatario;
    }

    public void setCorreoDestinatario(
            String correoDestinatario
    ) {
        this.correoDestinatario =
                correoDestinatario;
    }

    public int getCantidadDocumentos() {
        return cantidadDocumentos;
    }

    public void setCantidadDocumentos(
            int cantidadDocumentos
    ) {
        this.cantidadDocumentos =
                cantidadDocumentos;
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

    public LocalDateTime getFechaOtpValidado() {
        return fechaOtpValidado;
    }

    public void setFechaOtpValidado(
            LocalDateTime fechaOtpValidado
    ) {
        this.fechaOtpValidado =
                fechaOtpValidado;
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

    public LocalDate getFechaInicioPeriodo() {
        return fechaInicioPeriodo;
    }

    public void setFechaInicioPeriodo(
            LocalDate fechaInicioPeriodo
    ) {
        this.fechaInicioPeriodo =
                fechaInicioPeriodo;
    }

    public LocalDate getFechaFinPeriodo() {
        return fechaFinPeriodo;
    }

    public void setFechaFinPeriodo(
            LocalDate fechaFinPeriodo
    ) {
        this.fechaFinPeriodo =
                fechaFinPeriodo;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(
            LocalDateTime fechaPublicacion
    ) {
        this.fechaPublicacion =
                fechaPublicacion;
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
}