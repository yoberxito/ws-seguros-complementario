package essalud.gob.pe.wsseguroscomplementario.notificacion.dto;

import java.time.LocalDateTime;

public class RegistrarNotificacionResponse {

    private boolean notificacionRegistrada;
    private String mensajeRegistro;

    private String idNotificacion;
    private String registroInternoProceso;
    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;

    private String destinatario;
    private String canalNotificacion;
    private String tipoNotificacion;

    private String asunto;
    private String mensaje;
    private String enlaceConsulta;

    private LocalDateTime fechaHoraRegistro;
    private String estadoNotificacion;
    private String resultadoEnvio;

    public RegistrarNotificacionResponse() {
    }

    public boolean isNotificacionRegistrada() {
        return notificacionRegistrada;
    }

    public void setNotificacionRegistrada(boolean notificacionRegistrada) {
        this.notificacionRegistrada = notificacionRegistrada;
    }

    public String getMensajeRegistro() {
        return mensajeRegistro;
    }

    public void setMensajeRegistro(String mensajeRegistro) {
        this.mensajeRegistro = mensajeRegistro;
    }

    public String getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(String idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String registroInternoProceso) {
        this.registroInternoProceso = registroInternoProceso;
    }

    public String getTipoDocumentoTrabajador() {
        return tipoDocumentoTrabajador;
    }

    public void setTipoDocumentoTrabajador(String tipoDocumentoTrabajador) {
        this.tipoDocumentoTrabajador = tipoDocumentoTrabajador;
    }

    public String getNumeroDocumentoTrabajador() {
        return numeroDocumentoTrabajador;
    }

    public void setNumeroDocumentoTrabajador(String numeroDocumentoTrabajador) {
        this.numeroDocumentoTrabajador = numeroDocumentoTrabajador;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getCanalNotificacion() {
        return canalNotificacion;
    }

    public void setCanalNotificacion(String canalNotificacion) {
        this.canalNotificacion = canalNotificacion;
    }

    public String getTipoNotificacion() {
        return tipoNotificacion;
    }

    public void setTipoNotificacion(String tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEnlaceConsulta() {
        return enlaceConsulta;
    }

    public void setEnlaceConsulta(String enlaceConsulta) {
        this.enlaceConsulta = enlaceConsulta;
    }

    public LocalDateTime getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public void setFechaHoraRegistro(LocalDateTime fechaHoraRegistro) {
        this.fechaHoraRegistro = fechaHoraRegistro;
    }

    public String getEstadoNotificacion() {
        return estadoNotificacion;
    }

    public void setEstadoNotificacion(String estadoNotificacion) {
        this.estadoNotificacion = estadoNotificacion;
    }

    public String getResultadoEnvio() {
        return resultadoEnvio;
    }

    public void setResultadoEnvio(String resultadoEnvio) {
        this.resultadoEnvio = resultadoEnvio;
    }
}