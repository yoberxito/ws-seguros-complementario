package essalud.gob.pe.wsseguroscomplementario.entrega.dto;

public class SolicitarOtpEntregaResponse {

    private boolean otpDisponible;
    private boolean nuevoOtpGenerado;
    private boolean correoEnviado;
    private boolean otpYaValidado;

    private String correoEnmascarado;
    private String mensaje;

    public SolicitarOtpEntregaResponse() {
    }

    public boolean isOtpDisponible() {
        return otpDisponible;
    }

    public void setOtpDisponible(
            boolean otpDisponible
    ) {
        this.otpDisponible =
                otpDisponible;
    }

    public boolean isNuevoOtpGenerado() {
        return nuevoOtpGenerado;
    }

    public void setNuevoOtpGenerado(
            boolean nuevoOtpGenerado
    ) {
        this.nuevoOtpGenerado =
                nuevoOtpGenerado;
    }

    public boolean isCorreoEnviado() {
        return correoEnviado;
    }

    public void setCorreoEnviado(
            boolean correoEnviado
    ) {
        this.correoEnviado =
                correoEnviado;
    }

    public boolean isOtpYaValidado() {
        return otpYaValidado;
    }

    public void setOtpYaValidado(
            boolean otpYaValidado
    ) {
        this.otpYaValidado =
                otpYaValidado;
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