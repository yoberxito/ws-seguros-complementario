package essalud.gob.pe.wsseguroscomplementario.notificacion.dto;

public class EnviarCorreoFinalExternoResponse {

    private String codigoResultado;
    private String mensaje;
    private Object body;

    public EnviarCorreoFinalExternoResponse() {
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
        this.mensaje = mensaje;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(
            Object body
    ) {
        this.body = body;
    }
}