package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

public class GuardarConyugeRequest {

    private String tipoDocumentoConyuge;
    private String descripcionOtroDocumentoConyuge;
    private String numeroDocumentoConyuge;

    private String apellidoPaternoConyuge;
    private String apellidoMaternoConyuge;
    private String primerNombreConyuge;
    private String segundoNombreConyuge;

    private String tipoRelacion;

    public GuardarConyugeRequest() {
    }

    public String getTipoDocumentoConyuge() {
        return tipoDocumentoConyuge;
    }

    public void setTipoDocumentoConyuge(
            String tipoDocumentoConyuge
    ) {
        this.tipoDocumentoConyuge =
                tipoDocumentoConyuge;
    }

    public String getDescripcionOtroDocumentoConyuge() {
        return descripcionOtroDocumentoConyuge;
    }

    public void setDescripcionOtroDocumentoConyuge(
            String descripcionOtroDocumentoConyuge
    ) {
        this.descripcionOtroDocumentoConyuge =
                descripcionOtroDocumentoConyuge;
    }

    public String getNumeroDocumentoConyuge() {
        return numeroDocumentoConyuge;
    }

    public void setNumeroDocumentoConyuge(
            String numeroDocumentoConyuge
    ) {
        this.numeroDocumentoConyuge =
                numeroDocumentoConyuge;
    }

    public String getApellidoPaternoConyuge() {
        return apellidoPaternoConyuge;
    }

    public void setApellidoPaternoConyuge(
            String apellidoPaternoConyuge
    ) {
        this.apellidoPaternoConyuge =
                apellidoPaternoConyuge;
    }

    public String getApellidoMaternoConyuge() {
        return apellidoMaternoConyuge;
    }

    public void setApellidoMaternoConyuge(
            String apellidoMaternoConyuge
    ) {
        this.apellidoMaternoConyuge =
                apellidoMaternoConyuge;
    }

    public String getPrimerNombreConyuge() {
        return primerNombreConyuge;
    }

    public void setPrimerNombreConyuge(
            String primerNombreConyuge
    ) {
        this.primerNombreConyuge =
                primerNombreConyuge;
    }

    public String getSegundoNombreConyuge() {
        return segundoNombreConyuge;
    }

    public void setSegundoNombreConyuge(
            String segundoNombreConyuge
    ) {
        this.segundoNombreConyuge =
                segundoNombreConyuge;
    }

    public String getTipoRelacion() {
        return tipoRelacion;
    }

    public void setTipoRelacion(
            String tipoRelacion
    ) {
        this.tipoRelacion =
                tipoRelacion;
    }
}