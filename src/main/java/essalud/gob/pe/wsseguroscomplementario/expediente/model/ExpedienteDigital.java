package essalud.gob.pe.wsseguroscomplementario.expediente.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpedienteDigital {

    private String registroInternoProceso;

    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String nombresApellidosTrabajador;

    private String canalAcceso;
    private String estadoActual;

    private LocalDateTime fechaHoraCreacion;
    private LocalDateTime fechaHoraUltimaActualizacion;

    private String usuarioAutenticado;
    private String ipOrigen;
    private String datosSesionDispositivo;

    private List<String> documentosGenerados = new ArrayList<>();
    private List<String> documentosCargados = new ArrayList<>();
    private List<String> documentosSellados = new ArrayList<>();
    private List<String> documentosPublicados = new ArrayList<>();
    private List<String> rechazosDocumentales = new ArrayList<>();

    private List<EventoExpedienteDigital> eventos = new ArrayList<>();

    public ExpedienteDigital() {
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

    public String getNombresApellidosTrabajador() {
        return nombresApellidosTrabajador;
    }

    public void setNombresApellidosTrabajador(String nombresApellidosTrabajador) {
        this.nombresApellidosTrabajador = nombresApellidosTrabajador;
    }

    public String getCanalAcceso() {
        return canalAcceso;
    }

    public void setCanalAcceso(String canalAcceso) {
        this.canalAcceso = canalAcceso;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public LocalDateTime getFechaHoraCreacion() {
        return fechaHoraCreacion;
    }

    public void setFechaHoraCreacion(LocalDateTime fechaHoraCreacion) {
        this.fechaHoraCreacion = fechaHoraCreacion;
    }

    public LocalDateTime getFechaHoraUltimaActualizacion() {
        return fechaHoraUltimaActualizacion;
    }

    public void setFechaHoraUltimaActualizacion(LocalDateTime fechaHoraUltimaActualizacion) {
        this.fechaHoraUltimaActualizacion = fechaHoraUltimaActualizacion;
    }

    public String getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public void setUsuarioAutenticado(String usuarioAutenticado) {
        this.usuarioAutenticado = usuarioAutenticado;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public String getDatosSesionDispositivo() {
        return datosSesionDispositivo;
    }

    public void setDatosSesionDispositivo(String datosSesionDispositivo) {
        this.datosSesionDispositivo = datosSesionDispositivo;
    }

    public List<String> getDocumentosGenerados() {
        return documentosGenerados;
    }

    public void setDocumentosGenerados(List<String> documentosGenerados) {
        this.documentosGenerados = documentosGenerados;
    }

    public List<String> getDocumentosCargados() {
        return documentosCargados;
    }

    public void setDocumentosCargados(List<String> documentosCargados) {
        this.documentosCargados = documentosCargados;
    }

    public List<String> getDocumentosSellados() {
        return documentosSellados;
    }

    public void setDocumentosSellados(List<String> documentosSellados) {
        this.documentosSellados = documentosSellados;
    }

    public List<String> getDocumentosPublicados() {
        return documentosPublicados;
    }

    public void setDocumentosPublicados(List<String> documentosPublicados) {
        this.documentosPublicados = documentosPublicados;
    }

    public List<String> getRechazosDocumentales() {
        return rechazosDocumentales;
    }

    public void setRechazosDocumentales(List<String> rechazosDocumentales) {
        this.rechazosDocumentales = rechazosDocumentales;
    }

    public List<EventoExpedienteDigital> getEventos() {
        return eventos;
    }

    public void setEventos(List<EventoExpedienteDigital> eventos) {
        this.eventos = eventos;
    }
}