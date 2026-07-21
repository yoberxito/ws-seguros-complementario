package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidacionDocumentalCompletaResponse {

    private boolean validacionCompletaEjecutada;
    private boolean documentoAprobado;
    private boolean permiteNuevaCargaTrabajador;

    private String mensajeValidacion;
    private String estadoValidacionDocumental;

    private String registroInternoProceso;
    private String tipoDocumento;
    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String idDocumentoCargado;

    private LocalDateTime fechaHoraValidacion;

    private int totalEtapasEjecutadas;
    private int totalEtapasAprobadas;
    private int totalEtapasRechazadas;

    private boolean expedienteActualizado;

    private List<DetalleEtapaValidacionResponse> etapas = new ArrayList<>();
    private List<String> observaciones = new ArrayList<>();

    public ValidacionDocumentalCompletaResponse() {
    }

    public boolean isValidacionCompletaEjecutada() {
        return validacionCompletaEjecutada;
    }

    public void setValidacionCompletaEjecutada(boolean validacionCompletaEjecutada) {
        this.validacionCompletaEjecutada = validacionCompletaEjecutada;
    }

    public boolean isDocumentoAprobado() {
        return documentoAprobado;
    }

    public void setDocumentoAprobado(boolean documentoAprobado) {
        this.documentoAprobado = documentoAprobado;
    }

    public boolean isPermiteNuevaCargaTrabajador() {
        return permiteNuevaCargaTrabajador;
    }

    public void setPermiteNuevaCargaTrabajador(boolean permiteNuevaCargaTrabajador) {
        this.permiteNuevaCargaTrabajador = permiteNuevaCargaTrabajador;
    }

    public String getMensajeValidacion() {
        return mensajeValidacion;
    }

    public void setMensajeValidacion(String mensajeValidacion) {
        this.mensajeValidacion = mensajeValidacion;
    }

    public String getEstadoValidacionDocumental() {
        return estadoValidacionDocumental;
    }

    public void setEstadoValidacionDocumental(String estadoValidacionDocumental) {
        this.estadoValidacionDocumental = estadoValidacionDocumental;
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

    public String getIdDocumentoCargado() {
        return idDocumentoCargado;
    }

    public void setIdDocumentoCargado(String idDocumentoCargado) {
        this.idDocumentoCargado = idDocumentoCargado;
    }

    public LocalDateTime getFechaHoraValidacion() {
        return fechaHoraValidacion;
    }

    public void setFechaHoraValidacion(LocalDateTime fechaHoraValidacion) {
        this.fechaHoraValidacion = fechaHoraValidacion;
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

    public void setTotalEtapasRechazadas(int totalEtapasRechazadas) {
        this.totalEtapasRechazadas = totalEtapasRechazadas;
    }

    public boolean isExpedienteActualizado() {
        return expedienteActualizado;
    }

    public void setExpedienteActualizado(boolean expedienteActualizado) {
        this.expedienteActualizado = expedienteActualizado;
    }

    public List<DetalleEtapaValidacionResponse> getEtapas() {
        return etapas;
    }

    public void setEtapas(List<DetalleEtapaValidacionResponse> etapas) {
        this.etapas = etapas;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
}