package essalud.gob.pe.wsseguroscomplementario.documento.dto;

public class PublicarDocumentoRequest {

    private String idDocumentoSellado;
    private String registroInternoProceso;
    private String tipoDocumento;
    private String numeroDocumentoTrabajador;

    private String canalPublicacion;
    private String publicadoPor;

    public PublicarDocumentoRequest() {
    }

    public String getIdDocumentoSellado() {
        return idDocumentoSellado;
    }

    public void setIdDocumentoSellado(String idDocumentoSellado) {
        this.idDocumentoSellado = idDocumentoSellado;
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

    public String getNumeroDocumentoTrabajador() {
        return numeroDocumentoTrabajador;
    }

    public void setNumeroDocumentoTrabajador(String numeroDocumentoTrabajador) {
        this.numeroDocumentoTrabajador = numeroDocumentoTrabajador;
    }

    public String getCanalPublicacion() {
        return canalPublicacion;
    }

    public void setCanalPublicacion(String canalPublicacion) {
        this.canalPublicacion = canalPublicacion;
    }

    public String getPublicadoPor() {
        return publicadoPor;
    }

    public void setPublicadoPor(String publicadoPor) {
        this.publicadoPor = publicadoPor;
    }
}