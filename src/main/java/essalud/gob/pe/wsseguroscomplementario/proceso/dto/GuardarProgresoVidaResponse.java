package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

import java.time.LocalDateTime;

public class GuardarProgresoVidaResponse {

    private String registroInternoProceso;
    private String codigoEstadoProceso;
    private String rutaFrontend;
    private String estadoOperativo;
    private LocalDateTime fechaActualizacion;
    private String mensajeOperacion;
    private String codigoEstadoNavegacion;
    private String rutaFrontendNavegacion;
    public GuardarProgresoVidaResponse() {
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String registroInternoProceso) {
        this.registroInternoProceso = registroInternoProceso;
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

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getMensajeOperacion() {
        return mensajeOperacion;
    }

    public void setMensajeOperacion(String mensajeOperacion) {
        this.mensajeOperacion = mensajeOperacion;
    }

    public String getCodigoEstadoNavegacion() {
        return codigoEstadoNavegacion;
    }

    public void setCodigoEstadoNavegacion(
            String codigoEstadoNavegacion
    ) {
        this.codigoEstadoNavegacion =
                codigoEstadoNavegacion;
    }

    public String getRutaFrontendNavegacion() {
        return rutaFrontendNavegacion;
    }

    public void setRutaFrontendNavegacion(
            String rutaFrontendNavegacion
    ) {
        this.rutaFrontendNavegacion =
                rutaFrontendNavegacion;
    }
}