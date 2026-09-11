package essalud.gob.pe.wsseguroscomplementario.entrega.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ConsultarEntregaPublicaResponse {

    private String destinatario;

    private LocalDate fechaInicioPeriodo;
    private LocalDate fechaFinPeriodo;
    private LocalDateTime fechaPublicacion;

    private int cantidadDocumentos;
    private LocalDateTime fechaAcuse;

    private String correoEnmascarado;
    private String estadoEntrega;

    private boolean acuseRegistrado;
    private boolean accesoDisponible;

    private String urlAcceso;
    private String textoAcuse;
    private String versionTextoAcuse;

    public ConsultarEntregaPublicaResponse() {
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(
            String destinatario
    ) {
        this.destinatario = destinatario;
    }

    public LocalDate getFechaInicioPeriodo() {
        return fechaInicioPeriodo;
    }

    public void setFechaInicioPeriodo(
            LocalDate fechaInicioPeriodo
    ) {
        this.fechaInicioPeriodo = fechaInicioPeriodo;
    }

    public LocalDate getFechaFinPeriodo() {
        return fechaFinPeriodo;
    }

    public void setFechaFinPeriodo(
            LocalDate fechaFinPeriodo
    ) {
        this.fechaFinPeriodo = fechaFinPeriodo;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(
            LocalDateTime fechaPublicacion
    ) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public int getCantidadDocumentos() {
        return cantidadDocumentos;
    }

    public void setCantidadDocumentos(
            int cantidadDocumentos
    ) {
        this.cantidadDocumentos = cantidadDocumentos;
    }

    public LocalDateTime getFechaAcuse() {
        return fechaAcuse;
    }

    public void setFechaAcuse(
            LocalDateTime fechaAcuse
    ) {
        this.fechaAcuse = fechaAcuse;
    }

    public String getCorreoEnmascarado() {
        return correoEnmascarado;
    }

    public void setCorreoEnmascarado(
            String correoEnmascarado
    ) {
        this.correoEnmascarado = correoEnmascarado;
    }

    public String getEstadoEntrega() {
        return estadoEntrega;
    }

    public void setEstadoEntrega(
            String estadoEntrega
    ) {
        this.estadoEntrega = estadoEntrega;
    }

    public boolean isAcuseRegistrado() {
        return acuseRegistrado;
    }

    public void setAcuseRegistrado(
            boolean acuseRegistrado
    ) {
        this.acuseRegistrado = acuseRegistrado;
    }

    public boolean isAccesoDisponible() {
        return accesoDisponible;
    }

    public void setAccesoDisponible(
            boolean accesoDisponible
    ) {
        this.accesoDisponible = accesoDisponible;
    }

    public String getUrlAcceso() {
        return urlAcceso;
    }

    public void setUrlAcceso(
            String urlAcceso
    ) {
        this.urlAcceso = urlAcceso;
    }

    public String getTextoAcuse() {
        return textoAcuse;
    }

    public void setTextoAcuse(
            String textoAcuse
    ) {
        this.textoAcuse = textoAcuse;
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