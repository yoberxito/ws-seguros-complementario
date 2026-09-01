package essalud.gob.pe.wsseguroscomplementario.entrega.dto;

public class ValidarOtpEntregaResponse {

    private boolean otpValidado;
    private boolean yaValidado;

    private String correoEnmascarado;
    private String mensaje;

    public ValidarOtpEntregaResponse() {
    }

    public boolean isOtpValidado() {
        return otpValidado;
    }

    public void setOtpValidado(
            boolean otpValidado
    ) {
        this.otpValidado =
                otpValidado;
    }

    public boolean isYaValidado() {
        return yaValidado;
    }

    public void setYaValidado(
            boolean yaValidado
    ) {
        this.yaValidado =
                yaValidado;
    }

    public String getCorreoEnmascarado() {
        return correoEnmascarado;
    }

    public void setCorreoEnmascarado(
            String correoEnmascarado
    ) {
        this.correoEnmascarado =
                correoEnmascarado;
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
}