package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

import java.time.LocalDateTime;

public class IniciarProcesoVidaResponse {

    private boolean procesoCreado;
    private String mensajeOperacion;

    private Long idSecomasvida;
    private String registroInternoProceso;

    private String tipoDocumentoTitular;
    private String numeroDocumentoTitular;

    private String codigoEstadoProceso;
    private String rutaFrontend;
    private String estadoOperativo;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    public IniciarProcesoVidaResponse() {
    }

    public boolean isProcesoCreado() {
        return procesoCreado;
    }

    public void setProcesoCreado(boolean procesoCreado) {
        this.procesoCreado = procesoCreado;
    }

    public String getMensajeOperacion() {
        return mensajeOperacion;
    }

    public void setMensajeOperacion(String mensajeOperacion) {
        this.mensajeOperacion = mensajeOperacion;
    }

    public Long getIdSecomasvida() {
        return idSecomasvida;
    }

    public void setIdSecomasvida(Long idSecomasvida) {
        this.idSecomasvida = idSecomasvida;
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String registroInternoProceso) {
        this.registroInternoProceso = registroInternoProceso;
    }

    public String getTipoDocumentoTitular() {
        return tipoDocumentoTitular;
    }

    public void setTipoDocumentoTitular(String tipoDocumentoTitular) {
        this.tipoDocumentoTitular = tipoDocumentoTitular;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public void setNumeroDocumentoTitular(String numeroDocumentoTitular) {
        this.numeroDocumentoTitular = numeroDocumentoTitular;
    }

    public String getCodigoEstadoProceso() {
        return codigoEstadoProceso;
    }

    public void setCodigoEstadoProceso(String codigoEstadoProceso) {
        this.codigoEstadoProceso = codigoEstadoProceso;
    }

    public String getRutaFrontend() {
        return rutaFrontend;
    }

    public void setRutaFrontend(String rutaFrontend) {
        this.rutaFrontend = rutaFrontend;
    }

    public String getEstadoOperativo() {
        return estadoOperativo;
    }

    public void setEstadoOperativo(String estadoOperativo) {
        this.estadoOperativo = estadoOperativo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}