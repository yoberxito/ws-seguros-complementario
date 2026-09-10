package essalud.gob.pe.seguroshijomenormayor.entrega.exception;

public class EstadoEntregaException
        extends RuntimeException {

    public EstadoEntregaException(
            String mensaje
    ) {
        super(mensaje);
    }
}