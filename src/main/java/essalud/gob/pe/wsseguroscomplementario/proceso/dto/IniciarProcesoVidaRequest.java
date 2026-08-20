package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

public class IniciarProcesoVidaRequest {

    private String registroInternoProceso;

    private String tipoDocumentoTitular;
    private String descripcionOtroDocumentoTitular;
    private String numeroDocumentoTitular;

    private String apellidoPaternoTitular;
    private String apellidoMaternoTitular;
    private String primerNombreTitular;
    private String segundoNombreTitular;

    public IniciarProcesoVidaRequest() {
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
}