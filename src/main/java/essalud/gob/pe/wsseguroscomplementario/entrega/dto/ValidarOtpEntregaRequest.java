package essalud.gob.pe.wsseguroscomplementario.entrega.dto;

public class ValidarOtpEntregaRequest {

    private String codigo;

    public ValidarOtpEntregaRequest() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(
            String codigo
    ) {
        this.codigo = codigo;
    }
}