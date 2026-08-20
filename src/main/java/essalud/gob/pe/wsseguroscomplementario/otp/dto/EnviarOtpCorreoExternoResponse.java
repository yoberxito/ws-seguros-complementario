package essalud.gob.pe.wsseguroscomplementario.otp.dto;

public class EnviarOtpCorreoExternoResponse {

    private String codigoResultado;
    private String mensaje;
    private Object body;

    public EnviarOtpCorreoExternoResponse() {
    }

    public String getCodigoResultado() {
        return codigoResultado;
    }

    public void setCodigoResultado(
            String codigoResultado
    ) {
        this.codigoResultado =
                codigoResultado;
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

    public Object getBody() {
        return body;
    }

    public void setBody(
            Object body
    ) {
        this.body =
                body;
    }
}