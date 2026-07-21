package essalud.gob.pe.wsseguroscomplementario.common.dto;

import essalud.gob.pe.wsseguroscomplementario.common.constants.ResultadoOperacionConstants;

public class ApiResponse<T> {

    private String codResultado;
    private String mensaje;
    private T body;

    public ApiResponse() {
    }

    public ApiResponse(String codResultado, String mensaje, T body) {
        this.codResultado = codResultado;
        this.mensaje = mensaje;
        this.body = body;
    }

    public static <T> ApiResponse<T> exito(String mensaje, T body) {
        return new ApiResponse<>(
                ResultadoOperacionConstants.EXITO,
                mensaje,
                body
        );
    }

    public static <T> ApiResponse<T> error(String mensaje, T body) {
        return new ApiResponse<>(
                ResultadoOperacionConstants.ERROR,
                mensaje,
                body
        );
    }

    public String getCodResultado() {
        return codResultado;
    }

    public void setCodResultado(String codResultado) {
        this.codResultado = codResultado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}