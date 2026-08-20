package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

public class GuardarTitularRequest {

    private String tipoDocumentoTitular;
    private String descripcionOtroDocumentoTitular;
    private String numeroDocumentoTitular;

    private String apellidoPaternoTitular;
    private String apellidoMaternoTitular;
    private String primerNombreTitular;
    private String segundoNombreTitular;

    private String correo;
    private String celular;
    private String tipoAsegurado;

    public GuardarTitularRequest() {
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getTipoAsegurado() {
        return tipoAsegurado;
    }

    public void setTipoAsegurado(String tipoAsegurado) {
        this.tipoAsegurado = tipoAsegurado;
    }
}