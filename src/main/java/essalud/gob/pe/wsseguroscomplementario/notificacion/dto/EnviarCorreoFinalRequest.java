package essalud.gob.pe.wsseguroscomplementario.notificacion.dto;

public class EnviarCorreoFinalRequest {

    private String correo;
    private String descripcionSeguro;
    private String nombreSeguro;
    private String usuario;
    private String nombreCompleto;
    private int montoVida;
    private boolean enviaFormulario;

    public EnviarCorreoFinalRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDescripcionSeguro() {
        return descripcionSeguro;
    }

    public void setDescripcionSeguro(
            String descripcionSeguro
    ) {
        this.descripcionSeguro =
                descripcionSeguro;
    }

    public String getNombreSeguro() {
        return nombreSeguro;
    }

    public void setNombreSeguro(
            String nombreSeguro
    ) {
        this.nombreSeguro =
                nombreSeguro;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(
            String usuario
    ) {
        this.usuario = usuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(
            String nombreCompleto
    ) {
        this.nombreCompleto =
                nombreCompleto;
    }

    public int getMontoVida() {
        return montoVida;
    }

    public void setMontoVida(
            int montoVida
    ) {
        this.montoVida = montoVida;
    }

    public boolean isEnviaFormulario() {
        return enviaFormulario;
    }

    public void setEnviaFormulario(
            boolean enviaFormulario
    ) {
        this.enviaFormulario =
                enviaFormulario;
    }
}