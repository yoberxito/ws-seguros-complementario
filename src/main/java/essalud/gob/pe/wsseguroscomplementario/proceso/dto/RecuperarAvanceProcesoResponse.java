package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecuperarAvanceProcesoResponse {

    private boolean expedienteEncontrado;
    private String mensajeConsulta;
    private boolean procesoEncontrado;

    private String codigoEstadoProceso;
    private String rutaFrontend;
    private String estadoOperativo;
    private String tipoFlujo;
    private String codigoEstadoNavegacion;
    private String rutaFrontendNavegacion;
    private LocalDateTime fechaRegistroProceso;
    private LocalDateTime fechaActualizacionProceso;

    private FormularioVidaRecuperadoResponse
            formularioVida;
    private String registroInternoProceso;
    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String nombresApellidosTrabajador;

    private String canalAcceso;
    private String estadoActual;

    private String accionPendiente;
    private String accionFrontendSugerida;
    private String mensajeUsuario;


    private boolean puedeContinuarEnModulo;
    private boolean permiteNuevaCargaTrabajador;
    private boolean requiereIntervencionInterna;
    private boolean documentosDisponiblesParaConsulta;

    private LocalDateTime fechaHoraCreacion;
    private LocalDateTime fechaHoraUltimaActualizacion;

    private int cantidadEventos;

    private List<String> documentosGenerados = new ArrayList<>();
    private List<String> documentosCargados = new ArrayList<>();
    private List<String> documentosSellados = new ArrayList<>();
    private List<String> documentosPublicados = new ArrayList<>();
    private List<String> rechazosDocumentales = new ArrayList<>();

    private List<String> urlsDocumentosPublicados = new ArrayList<>();

    public RecuperarAvanceProcesoResponse() {
    }

    public boolean isExpedienteEncontrado() {
        return expedienteEncontrado;
    }

    public void setExpedienteEncontrado(boolean expedienteEncontrado) {
        this.expedienteEncontrado = expedienteEncontrado;
    }

    public String getMensajeConsulta() {
        return mensajeConsulta;
    }

    public void setMensajeConsulta(String mensajeConsulta) {
        this.mensajeConsulta = mensajeConsulta;
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

    public String getAccionPendiente() {
        return accionPendiente;
    }

    public void setAccionPendiente(String accionPendiente) {
        this.accionPendiente = accionPendiente;
    }

    public String getAccionFrontendSugerida() {
        return accionFrontendSugerida;
    }

    public void setAccionFrontendSugerida(String accionFrontendSugerida) {
        this.accionFrontendSugerida = accionFrontendSugerida;
    }

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }

    public void setMensajeUsuario(String mensajeUsuario) {
        this.mensajeUsuario = mensajeUsuario;
    }

    public boolean isPuedeContinuarEnModulo() {
        return puedeContinuarEnModulo;
    }

    public void setPuedeContinuarEnModulo(boolean puedeContinuarEnModulo) {
        this.puedeContinuarEnModulo = puedeContinuarEnModulo;
    }

    public boolean isPermiteNuevaCargaTrabajador() {
        return permiteNuevaCargaTrabajador;
    }

    public void setPermiteNuevaCargaTrabajador(boolean permiteNuevaCargaTrabajador) {
        this.permiteNuevaCargaTrabajador = permiteNuevaCargaTrabajador;
    }

    public boolean isRequiereIntervencionInterna() {
        return requiereIntervencionInterna;
    }

    public void setRequiereIntervencionInterna(boolean requiereIntervencionInterna) {
        this.requiereIntervencionInterna = requiereIntervencionInterna;
    }

    public boolean isDocumentosDisponiblesParaConsulta() {
        return documentosDisponiblesParaConsulta;
    }

    public void setDocumentosDisponiblesParaConsulta(boolean documentosDisponiblesParaConsulta) {
        this.documentosDisponiblesParaConsulta = documentosDisponiblesParaConsulta;
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

    public int getCantidadEventos() {
        return cantidadEventos;
    }

    public void setCantidadEventos(int cantidadEventos) {
        this.cantidadEventos = cantidadEventos;
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

    public List<String> getUrlsDocumentosPublicados() {
        return urlsDocumentosPublicados;
    }

    public void setUrlsDocumentosPublicados(List<String> urlsDocumentosPublicados) {
        this.urlsDocumentosPublicados = urlsDocumentosPublicados;
    }
    public boolean isProcesoEncontrado() {
        return procesoEncontrado;
    }

    public void setProcesoEncontrado(
            boolean procesoEncontrado
    ) {
        this.procesoEncontrado =
                procesoEncontrado;
    }

    public String getCodigoEstadoProceso() {
        return codigoEstadoProceso;
    }

    public void setCodigoEstadoProceso(
            String codigoEstadoProceso
    ) {
        this.codigoEstadoProceso =
                codigoEstadoProceso;
    }

    public String getRutaFrontend() {
        return rutaFrontend;
    }

    public void setRutaFrontend(
            String rutaFrontend
    ) {
        this.rutaFrontend =
                rutaFrontend;
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

    public String getEstadoOperativo() {
        return estadoOperativo;
    }

    public void setEstadoOperativo(
            String estadoOperativo
    ) {
        this.estadoOperativo =
                estadoOperativo;
    }

    public String getTipoFlujo() {
        return tipoFlujo;
    }

    public void setTipoFlujo(
            String tipoFlujo
    ) {
        this.tipoFlujo =
                tipoFlujo;
    }

    public LocalDateTime getFechaRegistroProceso() {
        return fechaRegistroProceso;
    }

    public void setFechaRegistroProceso(
            LocalDateTime fechaRegistroProceso
    ) {
        this.fechaRegistroProceso =
                fechaRegistroProceso;
    }

    public LocalDateTime
    getFechaActualizacionProceso() {
        return fechaActualizacionProceso;
    }

    public void setFechaActualizacionProceso(
            LocalDateTime fechaActualizacionProceso
    ) {
        this.fechaActualizacionProceso =
                fechaActualizacionProceso;
    }

    public FormularioVidaRecuperadoResponse
    getFormularioVida() {
        return formularioVida;
    }

    public void setFormularioVida(
            FormularioVidaRecuperadoResponse
                    formularioVida
    ) {
        this.formularioVida =
                formularioVida;
    }


}