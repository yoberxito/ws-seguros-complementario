package essalud.gob.pe.wsseguroscomplementario.otp.dto;

public class GenerarOtpExternoResponse {

    private String codResultado;
    private String mensaje;
    private String codigoGenerado;

    public GenerarOtpExternoResponse() {
    }

    public String getCodResultado() {
        return codResultado;
    }

    public void setCodResultado(
            String codResultado
    ) {
        this.codResultado =
                codResultado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(
            String mensaje
    ) {
        this.mensaje =
                mensaje;
    }

    public String getCodigoGenerado() {
        return codigoGenerado;
    }

    public void setCodigoGenerado(
            String codigoGenerado
    ) {
        this.codigoGenerado =
                codigoGenerado;
    }
}