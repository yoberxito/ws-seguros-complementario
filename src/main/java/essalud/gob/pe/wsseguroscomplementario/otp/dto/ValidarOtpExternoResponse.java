package essalud.gob.pe.wsseguroscomplementario.otp.dto;

public class ValidarOtpExternoResponse {

    private boolean valido;
    private String mensaje;
    private Integer intentosRestantes;

    public ValidarOtpExternoResponse() {
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(
            boolean valido
    ) {
        this.valido =
                valido;
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

    public Integer getIntentosRestantes() {
        return intentosRestantes;
    }

    public void setIntentosRestantes(
            Integer intentosRestantes
    ) {
        this.intentosRestantes =
                intentosRestantes;
    }
}