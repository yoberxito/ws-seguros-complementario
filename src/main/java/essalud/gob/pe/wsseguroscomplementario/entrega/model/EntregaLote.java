package essalud.gob.pe.wsseguroscomplementario.entrega.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EntregaLote {

    private Long idEntrega;
    private String codEntrega;
    private Long idLote;

    private String tipoDestinatario;
    private String correoDestinatario;
    private String tokenHash;

    private int cantidadDocumentos;

    private String estadoNotificacion;
    private LocalDateTime fechaNotificacion;

    private String urlAcceso;

    private LocalDateTime fechaAcuse;
    private String textoAcuse;
    private String versionTextoAcuse;
    private String ipAcuse;
    private String datosSesionDispositivo;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    /*
     * Datos del lote obtenidos mediante JOIN.
     */
    private LocalDate fechaInicioPeriodo;
    private LocalDate fechaFinPeriodo;
    private LocalDateTime fechaPublicacion;

    public EntregaLote() {
    }

    public Long getIdEntrega() {
        return idEntrega;
    }

    public void setIdEntrega(Long idEntrega) {
        this.idEntrega = idEntrega;
    }

    public String getCodEntrega() {
        return codEntrega;
    }

    public void setCodEntrega(String codEntrega) {
        this.codEntrega = codEntrega;
    }

    public Long getIdLote() {
        return idLote;
    }

    public void setIdLote(Long idLote) {
        this.idLote = idLote;
    }

    public String getTipoDestinatario() {
        return tipoDestinatario;
    }

    public void setTipoDestinatario(
            String tipoDestinatario
    ) {
        this.tipoDestinatario = tipoDestinatario;
    }

    public String getCorreoDestinatario() {
        return correoDestinatario;
    }

    public void setCorreoDestinatario(
            String correoDestinatario
    ) {
        this.correoDestinatario = correoDestinatario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public int getCantidadDocumentos() {
        return cantidadDocumentos;
    }

    public void setCantidadDocumentos(
            int cantidadDocumentos
    ) {
        this.cantidadDocumentos = cantidadDocumentos;
    }

    public String getEstadoNotificacion() {
        return estadoNotificacion;
    }

    public void setEstadoNotificacion(
            String estadoNotificacion
    ) {
        this.estadoNotificacion = estadoNotificacion;
    }

    public LocalDateTime getFechaNotificacion() {
        return fechaNotificacion;
    }

    public void setFechaNotificacion(
            LocalDateTime fechaNotificacion
    ) {
        this.fechaNotificacion = fechaNotificacion;
    }

    public String getUrlAcceso() {
        return urlAcceso;
    }

    public void setUrlAcceso(String urlAcceso) {
        this.urlAcceso = urlAcceso;
    }

    public LocalDateTime getFechaAcuse() {
        return fechaAcuse;
    }

    public void setFechaAcuse(
            LocalDateTime fechaAcuse
    ) {
        this.fechaAcuse = fechaAcuse;
    }

    public String getTextoAcuse() {
        return textoAcuse;
    }

    public void setTextoAcuse(String textoAcuse) {
        this.textoAcuse = textoAcuse;
    }

    public String getVersionTextoAcuse() {
        return versionTextoAcuse;
    }

    public void setVersionTextoAcuse(
            String versionTextoAcuse
    ) {
        this.versionTextoAcuse = versionTextoAcuse;
    }

    public String getIpAcuse() {
        return ipAcuse;
    }

    public void setIpAcuse(String ipAcuse) {
        this.ipAcuse = ipAcuse;
    }

    public String getDatosSesionDispositivo() {
        return datosSesionDispositivo;
    }

    public void setDatosSesionDispositivo(
            String datosSesionDispositivo
    ) {
        this.datosSesionDispositivo =
                datosSesionDispositivo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDateTime fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            LocalDateTime fechaActualizacion
    ) {
        this.fechaActualizacion = fechaActualizacion;
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
}