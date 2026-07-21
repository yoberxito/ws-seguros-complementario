package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CierreDocumentalCompletoResponse {

    private boolean cierreEjecutado;
    private boolean cierreCompletado;
    private boolean requiereIntervencionInterna;
    private boolean expedienteActualizado;

    private String mensajeCierre;
    private String estadoCierreDocumental;

    private String registroInternoProceso;
    private String tipoDocumento;
    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String nombresApellidosTrabajador;

    private String idDocumentoSellado;
    private String idDocumentoPublicado;
    private String canalPublicacion;
    private String urlVisualizacionSimulada;

    private LocalDateTime fechaHoraCierre;

    private int totalEtapasEjecutadas;
    private int totalEtapasAprobadas;
    private int totalEtapasRechazadas;

    private List<DetalleEtapaCierreDocumentalResponse> etapas = new ArrayList<>();
    private List<String> observaciones = new ArrayList<>();

    public CierreDocumentalCompletoResponse() {
    }

    public boolean isCierreEjecutado() {
        return cierreEjecutado;
    }

    public void setCierreEjecutado(boolean cierreEjecutado) {
        this.cierreEjecutado = cierreEjecutado;
    }

    public boolean isCierreCompletado() {
        return cierreCompletado;
    }

    public void setCierreCompletado(boolean cierreCompletado) {
        this.cierreCompletado = cierreCompletado;
    }

    public boolean isRequiereIntervencionInterna() {
        return requiereIntervencionInterna;
    }

    public void setRequiereIntervencionInterna(boolean requiereIntervencionInterna) {
        this.requiereIntervencionInterna = requiereIntervencionInterna;
    }

    public boolean isExpedienteActualizado() {
        return expedienteActualizado;
    }

    public void setExpedienteActualizado(boolean expedienteActualizado) {
        this.expedienteActualizado = expedienteActualizado;
    }

    public String getMensajeCierre() {
        return mensajeCierre;
    }

    public void setMensajeCierre(String mensajeCierre) {
        this.mensajeCierre = mensajeCierre;
    }

    public String getEstadoCierreDocumental() {
        return estadoCierreDocumental;
    }

    public void setEstadoCierreDocumental(String estadoCierreDocumental) {
        this.estadoCierreDocumental = estadoCierreDocumental;
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String registroInternoProceso) {
        this.registroInternoProceso = registroInternoProceso;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
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

    public String getIdDocumentoSellado() {
        return idDocumentoSellado;
    }

    public void setIdDocumentoSellado(String idDocumentoSellado) {
        this.idDocumentoSellado = idDocumentoSellado;
    }

    public String getIdDocumentoPublicado() {
        return idDocumentoPublicado;
    }

    public void setIdDocumentoPublicado(String idDocumentoPublicado) {
        this.idDocumentoPublicado = idDocumentoPublicado;
    }

    public String getCanalPublicacion() {
        return canalPublicacion;
    }

    public void setCanalPublicacion(String canalPublicacion) {
        this.canalPublicacion = canalPublicacion;
    }

    public String getUrlVisualizacionSimulada() {
        return urlVisualizacionSimulada;
    }

    public void setUrlVisualizacionSimulada(String urlVisualizacionSimulada) {
        this.urlVisualizacionSimulada = urlVisualizacionSimulada;
    }

    public LocalDateTime getFechaHoraCierre() {
        return fechaHoraCierre;
    }

    public void setFechaHoraCierre(LocalDateTime fechaHoraCierre) {
        this.fechaHoraCierre = fechaHoraCierre;
    }

    public int getTotalEtapasEjecutadas() {
        return totalEtapasEjecutadas;
    }

    public void setTotalEtapasEjecutadas(int totalEtapasEjecutadas) {
        this.totalEtapasEjecutadas = totalEtapasEjecutadas;
    }

    public int getTotalEtapasAprobadas() {
        return totalEtapasAprobadas;
    }

    public void setTotalEtapasAprobadas(int totalEtapasAprobadas) {
        this.totalEtapasAprobadas = totalEtapasAprobadas;
    }

    public int getTotalEtapasRechazadas() {
        return totalEtapasRechazadas;
    }

    public String getNombresApellidosTrabajador() {
        return nombresApellidosTrabajador;
    }

    public void setNombresApellidosTrabajador(String nombresApellidosTrabajador) {
        this.nombresApellidosTrabajador = nombresApellidosTrabajador;
    }

    public void setTotalEtapasRechazadas(int totalEtapasRechazadas) {
        this.totalEtapasRechazadas = totalEtapasRechazadas;
    }

    public List<DetalleEtapaCierreDocumentalResponse> getEtapas() {
        return etapas;
    }

    public void setEtapas(List<DetalleEtapaCierreDocumentalResponse> etapas) {
        this.etapas = etapas;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
}