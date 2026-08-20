package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

public class ActualizarNavegacionRequest {

    private String codigoEstadoNavegacion;

    public ActualizarNavegacionRequest() {
    }

    public String getCodigoEstadoNavegacion() {
        return codigoEstadoNavegacion;
    }

    public void setCodigoEstadoNavegacion(
            String codigoEstadoNavegacion
    ) {
        this.codigoEstadoNavegacion =
                codigoEstadoNavegacion;
    }
}