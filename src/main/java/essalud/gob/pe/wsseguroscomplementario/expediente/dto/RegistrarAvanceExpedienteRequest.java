package essalud.gob.pe.wsseguroscomplementario.expediente.dto;

public class RegistrarAvanceExpedienteRequest {

    private String registroInternoProceso;

    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String nombresApellidosTrabajador;

    private String canalAcceso;
    private String estadoOperativo;
    private String descripcionAvance;

    private String usuarioAutenticado;
    private String ipOrigen;
    private String datosSesionDispositivo;

    private String tipoDocumentoProceso;

    private String idDocumentoGenerado;
    private String idDocumentoCargado;
    private String idDocumentoSellado;
    private String idDocumentoPublicado;
    private String idRechazoDocumental;

    public RegistrarAvanceExpedienteRequest() {
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

    public String getEstadoOperativo() {
        return estadoOperativo;
    }

    public void setEstadoOperativo(String estadoOperativo) {
        this.estadoOperativo = estadoOperativo;
    }

    public String getDescripcionAvance() {
        return descripcionAvance;
    }

    public void setDescripcionAvance(String descripcionAvance) {
        this.descripcionAvance = descripcionAvance;
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

    public String getTipoDocumentoProceso() {
        return tipoDocumentoProceso;
    }

    public void setTipoDocumentoProceso(String tipoDocumentoProceso) {
        this.tipoDocumentoProceso = tipoDocumentoProceso;
    }

    public String getIdDocumentoGenerado() {
        return idDocumentoGenerado;
    }

    public void setIdDocumentoGenerado(String idDocumentoGenerado) {
        this.idDocumentoGenerado = idDocumentoGenerado;
    }

    public String getIdDocumentoCargado() {
        return idDocumentoCargado;
    }

    public void setIdDocumentoCargado(String idDocumentoCargado) {
        this.idDocumentoCargado = idDocumentoCargado;
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

    public String getIdRechazoDocumental() {
        return idRechazoDocumental;
    }

    public void setIdRechazoDocumental(String idRechazoDocumental) {
        this.idRechazoDocumental = idRechazoDocumental;
    }
}