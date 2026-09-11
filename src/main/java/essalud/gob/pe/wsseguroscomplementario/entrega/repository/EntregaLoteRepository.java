package essalud.gob.pe.wsseguroscomplementario.entrega.repository;

import essalud.gob.pe.wsseguroscomplementario.entrega.model.EntregaLote;

import java.util.Optional;

public interface EntregaLoteRepository {

    EntregaLote crear(
            EntregaLote entrega
    );

    Optional<EntregaLote> buscarPorLoteYDestinatario(
            Long idLote,
            String tipoDestinatario
    );

    Optional<EntregaLote> buscarPorTokenHash(
            String tokenHash
    );

    boolean actualizarPreparacionPendiente(
            Long idEntrega,
            String correoDestinatario,
            String tokenHash,
            int cantidadDocumentos,
            String urlAcceso
    );

    boolean marcarEnviando(
            Long idEntrega
    );

    boolean marcarEnviado(
            Long idEntrega
    );

    boolean marcarErrorEnvio(
            Long idEntrega
    );

    boolean registrarAcuseSiPendiente(
            String tokenHash,
            String textoAcuse,
            String versionTextoAcuse,
            String ipAcuse,
            String datosSesionDispositivo
    );
}