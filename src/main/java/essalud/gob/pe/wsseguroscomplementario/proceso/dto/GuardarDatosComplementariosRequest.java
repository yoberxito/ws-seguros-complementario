package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

public class GuardarDatosComplementariosRequest {

    private String codigoPlanilla;
    private String decretoLegislativo;
    private String convenioCgbvp;

    private String rucEmpleador;
    private String razonSocial;

    public GuardarDatosComplementariosRequest() {
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

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }
}