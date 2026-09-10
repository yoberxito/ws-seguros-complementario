package essalud.gob.pe.seguroshijomenormayor.entrega.repository;

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