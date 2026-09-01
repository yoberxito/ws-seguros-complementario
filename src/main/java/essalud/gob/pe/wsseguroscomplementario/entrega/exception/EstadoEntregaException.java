package essalud.gob.pe.wsseguroscomplementario.entrega.exception;

public class EstadoEntregaException
        extends RuntimeException {

    public EstadoEntregaException(
            String mensaje
    ) {
        super(mensaje);
    }
}