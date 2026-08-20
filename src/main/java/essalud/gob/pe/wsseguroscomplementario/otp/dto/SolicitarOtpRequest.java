package essalud.gob.pe.wsseguroscomplementario.otp.dto;

public class SolicitarOtpRequest {

    private String correo;

    public SolicitarOtpRequest() {
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
}