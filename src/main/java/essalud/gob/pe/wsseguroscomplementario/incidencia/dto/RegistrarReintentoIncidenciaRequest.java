package essalud.gob.pe.wsseguroscomplementario.incidencia.dto;

public class RegistrarReintentoIncidenciaRequest {

    private String descripcionReintento;
    private String resultadoReintento;

    private String usuarioResponsable;
    private String ipOrigen;
    private String datosSesionDispositivo;

    private String idDocumentoSellado;
    private String idDocumentoPublicado;

    public RegistrarReintentoIncidenciaRequest() {
    }

    public String getDescripcionReintento() {
        return descripcionReintento;
    }

    public void setDescripcionReintento(String descripcionReintento) {
        this.descripcionReintento = descripcionReintento;
    }

    public String getResultadoReintento() {
        return resultadoReintento;
    }

    public void setResultadoReintento(String resultadoReintento) {
        this.resultadoReintento = resultadoReintento;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public void setUsuarioResponsable(String usuarioResponsable) {
        this.usuarioResponsable = usuarioResponsable;
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
}