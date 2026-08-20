package essalud.gob.pe.wsseguroscomplementario.otp.dto;

public class ValidarOtpRequest {

    private String correo;
    private String codigo;

    public ValidarOtpRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(
            String correo
    ) {
        this.correo =
                correo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(
            String codigo
    ) {
        this.codigo =
                codigo;
    }
}