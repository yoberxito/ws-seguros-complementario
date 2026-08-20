package essalud.gob.pe.wsseguroscomplementario.otp.dto;

public class SolicitarOtpResponse {

    private boolean otpGenerado;
    private boolean correoEnviado;

    private String codResultadoGeneracion;
    private String codigoResultadoCorreo;

    private String mensaje;
    private String mensajeCorreo;

    public SolicitarOtpResponse() {
    }

    public boolean isOtpGenerado() {
        return otpGenerado;
    }

    public void setOtpGenerado(
            boolean otpGenerado
    ) {
        this.otpGenerado =
                otpGenerado;
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

    public String getCodResultadoGeneracion() {
        return codResultadoGeneracion;
    }

    public void setCodResultadoGeneracion(
            String codResultadoGeneracion
    ) {
        this.codResultadoGeneracion =
                codResultadoGeneracion;
    }

    public String getCodigoResultadoCorreo() {
        return codigoResultadoCorreo;
    }

    public void setCodigoResultadoCorreo(
            String codigoResultadoCorreo
    ) {
        this.codigoResultadoCorreo =
                codigoResultadoCorreo;
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

    public String getMensajeCorreo() {
        return mensajeCorreo;
    }

    public void setMensajeCorreo(
            String mensajeCorreo
    ) {
        this.mensajeCorreo =
                mensajeCorreo;
    }
}