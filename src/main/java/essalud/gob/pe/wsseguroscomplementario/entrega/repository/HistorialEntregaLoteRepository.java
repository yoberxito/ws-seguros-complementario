package essalud.gob.pe.wsseguroscomplementario.entrega.repository;

public interface HistorialEntregaLoteRepository {

    void registrarEvento(
            String tokenHash,
            String codigoEvento,
            String tipoEvento,
            String resultadoEvento,
            String descripcionEvento,
            String ipOrigen,
            String datosSesionDispositivo
    );
}