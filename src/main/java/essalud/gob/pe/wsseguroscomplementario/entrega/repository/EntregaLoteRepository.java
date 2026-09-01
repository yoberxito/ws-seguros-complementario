package essalud.gob.pe.wsseguroscomplementario.entrega.repository;

import essalud.gob.pe.wsseguroscomplementario.entrega.model.EntregaLote;

import java.util.Optional;

public interface EntregaLoteRepository {

    Optional<EntregaLote> buscarPorTokenHash(
            String tokenHash
    );

    boolean marcarOtpValidadoSiPendiente(
            String tokenHash
    );
    boolean registrarAcuseSiPendiente(
            String tokenHash,
            String textoAcuse,
            String versionTextoAcuse,
            String ipAcuse,
            String datosSesionDispositivo
    );
}