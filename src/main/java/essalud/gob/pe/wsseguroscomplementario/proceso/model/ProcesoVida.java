package essalud.gob.pe.wsseguroscomplementario.proceso.model;

import java.time.LocalDateTime;

public class ProcesoVida {

    private Long idSecomasvida;
    private String registroInternoProceso;

    private Long idEstado;
    private String codigoEstadoProceso;
    private String rutaFrontend;

    private Long idEstadoNavegacion;
    private String codigoEstadoNavegacion;
    private String rutaFrontendNavegacion;

    private String estadoOperativo;
    private String tipoFlujo;

    private String codigoDocumentoTitular;
    private String descripcionOtroDocumentoTitular;
    private String numeroDocumentoTitular;

    private String apellidoPaternoTitular;
    private String apellidoMaternoTitular;
    private String primerNombreTitular;
    private String segundoNombreTitular;
    private boolean beneficiarioBorradorAbierto;
    private String correo;
    private String numeroTelefono;
    private String notificacionesCorreo;

    private String tipoAsegurado;
    private String codigoPlanilla;
    private String decretoLegislativo;
    private String convenioCgbvp;

    private String rucEmpleador;
    private String razonSocialEntidad;

    private String codigoDocumentoConyuge;
    private String descripcionOtroDocumentoConyuge;
    private String numeroDocumentoConyuge;

    private String apellidoPaternoConyuge;
    private String apellidoMaternoConyuge;
    private String primerNombreConyuge;
    private String segundoNombreConyuge;

    private String tipoRelacion;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    public ProcesoVida() {
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

    public Long getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(Long idEstado) {
        this.idEstado = idEstado;
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

    public String getTipoFlujo() {
        return tipoFlujo;
    }

    public void setTipoFlujo(String tipoFlujo) {
        this.tipoFlujo = tipoFlujo;
    }

    public String getCodigoDocumentoTitular() {
        return codigoDocumentoTitular;
    }

    public void setCodigoDocumentoTitular(String codigoDocumentoTitular) {
        this.codigoDocumentoTitular = codigoDocumentoTitular;
    }

    public String getDescripcionOtroDocumentoTitular() {
        return descripcionOtroDocumentoTitular;
    }

    public void setDescripcionOtroDocumentoTitular(String descripcionOtroDocumentoTitular) {
        this.descripcionOtroDocumentoTitular = descripcionOtroDocumentoTitular;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public void setNumeroDocumentoTitular(String numeroDocumentoTitular) {
        this.numeroDocumentoTitular = numeroDocumentoTitular;
    }

    public String getApellidoPaternoTitular() {
        return apellidoPaternoTitular;
    }

    public void setApellidoPaternoTitular(String apellidoPaternoTitular) {
        this.apellidoPaternoTitular = apellidoPaternoTitular;
    }

    public String getApellidoMaternoTitular() {
        return apellidoMaternoTitular;
    }

    public void setApellidoMaternoTitular(String apellidoMaternoTitular) {
        this.apellidoMaternoTitular = apellidoMaternoTitular;
    }

    public String getPrimerNombreTitular() {
        return primerNombreTitular;
    }

    public void setPrimerNombreTitular(String primerNombreTitular) {
        this.primerNombreTitular = primerNombreTitular;
    }

    public String getSegundoNombreTitular() {
        return segundoNombreTitular;
    }

    public void setSegundoNombreTitular(String segundoNombreTitular) {
        this.segundoNombreTitular = segundoNombreTitular;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getNotificacionesCorreo() {
        return notificacionesCorreo;
    }

    public void setNotificacionesCorreo(String notificacionesCorreo) {
        this.notificacionesCorreo = notificacionesCorreo;
    }

    public String getTipoAsegurado() {
        return tipoAsegurado;
    }

    public void setTipoAsegurado(String tipoAsegurado) {
        this.tipoAsegurado = tipoAsegurado;
    }

    public String getCodigoPlanilla() {
        return codigoPlanilla;
    }

    public void setCodigoPlanilla(String codigoPlanilla) {
        this.codigoPlanilla = codigoPlanilla;
    }

    public String getDecretoLegislativo() {
        return decretoLegislativo;
    }

    public void setDecretoLegislativo(String decretoLegislativo) {
        this.decretoLegislativo = decretoLegislativo;
    }

    public String getConvenioCgbvp() {
        return convenioCgbvp;
    }

    public void setConvenioCgbvp(String convenioCgbvp) {
        this.convenioCgbvp = convenioCgbvp;
    }

    public String getRucEmpleador() {
        return rucEmpleador;
    }

    public void setRucEmpleador(String rucEmpleador) {
        this.rucEmpleador = rucEmpleador;
    }

    public String getRazonSocialEntidad() {
        return razonSocialEntidad;
    }

    public void setRazonSocialEntidad(String razonSocialEntidad) {
        this.razonSocialEntidad = razonSocialEntidad;
    }

    public String getCodigoDocumentoConyuge() {
        return codigoDocumentoConyuge;
    }

    public void setCodigoDocumentoConyuge(String codigoDocumentoConyuge) {
        this.codigoDocumentoConyuge = codigoDocumentoConyuge;
    }

    public String getDescripcionOtroDocumentoConyuge() {
        return descripcionOtroDocumentoConyuge;
    }

    public void setDescripcionOtroDocumentoConyuge(String descripcionOtroDocumentoConyuge) {
        this.descripcionOtroDocumentoConyuge = descripcionOtroDocumentoConyuge;
    }

    public String getNumeroDocumentoConyuge() {
        return numeroDocumentoConyuge;
    }

    public void setNumeroDocumentoConyuge(String numeroDocumentoConyuge) {
        this.numeroDocumentoConyuge = numeroDocumentoConyuge;
    }

    public String getApellidoPaternoConyuge() {
        return apellidoPaternoConyuge;
    }

    public void setApellidoPaternoConyuge(String apellidoPaternoConyuge) {
        this.apellidoPaternoConyuge = apellidoPaternoConyuge;
    }

    public String getApellidoMaternoConyuge() {
        return apellidoMaternoConyuge;
    }

    public void setApellidoMaternoConyuge(String apellidoMaternoConyuge) {
        this.apellidoMaternoConyuge = apellidoMaternoConyuge;
    }

    public String getPrimerNombreConyuge() {
        return primerNombreConyuge;
    }

    public void setPrimerNombreConyuge(String primerNombreConyuge) {
        this.primerNombreConyuge = primerNombreConyuge;
    }

    public String getSegundoNombreConyuge() {
        return segundoNombreConyuge;
    }

    public void setSegundoNombreConyuge(String segundoNombreConyuge) {
        this.segundoNombreConyuge = segundoNombreConyuge;
    }

    public String getTipoRelacion() {
        return tipoRelacion;
    }

    public void setTipoRelacion(String tipoRelacion) {
        this.tipoRelacion = tipoRelacion;
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
    public Long getIdEstadoNavegacion() {
        return idEstadoNavegacion;
    }

    public void setIdEstadoNavegacion(
            Long idEstadoNavegacion
    ) {
        this.idEstadoNavegacion =
                idEstadoNavegacion;
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

    public boolean isBeneficiarioBorradorAbierto() {
        return beneficiarioBorradorAbierto;
    }

    public void setBeneficiarioBorradorAbierto(
            boolean beneficiarioBorradorAbierto
    ) {
        this.beneficiarioBorradorAbierto =
                beneficiarioBorradorAbierto;
    }
}