package essalud.gob.pe.seguroshijomenormayor.entrega.service;

import essalud.gob.pe.seguroshijomenormayor.entrega.repository.EntregaLoteRepository;
import essalud.gob.pe.seguroshijomenormayor.entrega.repository.HistorialEntregaLoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("masVidaEntregaLoteTransicionService")
public class EntregaLoteTransicionService {

    private static final String
            EVENTO_ACUSE_REGISTRADO =
            "ACUSE_REGISTRADO";

    private static final String
            RESULTADO_OK =
            "OK";

    private static final String
            DESCRIPCION_ACUSE_REGISTRADO =
            "Acuse de recepción del lote registrado correctamente.";

    private final EntregaLoteRepository
            entregaLoteRepository;

    private final HistorialEntregaLoteRepository
            historialEntregaLoteRepository;

    public EntregaLoteTransicionService(
            EntregaLoteRepository entregaLoteRepository,
            HistorialEntregaLoteRepository
                    historialEntregaLoteRepository
    ) {
        this.entregaLoteRepository =
                entregaLoteRepository;

        this.historialEntregaLoteRepository =
                historialEntregaLoteRepository;
    }

    @Transactional
    public boolean registrarAcuseConHistorial(
            String tokenHash,
            String textoAcuse,
            String versionTextoAcuse,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        boolean actualizado =
                entregaLoteRepository
                        .registrarAcuseSiPendiente(
                                tokenHash,
                                textoAcuse,
                                versionTextoAcuse,
                                ipOrigen,
                                datosSesionDispositivo
                        );

        /*
         * Solo registramos historial cuando Oracle
         * realizó una transición real.
         */
        if (!actualizado) {
            return false;
        }

        historialEntregaLoteRepository
                .registrarEvento(
                        tokenHash,
                        generarCodigoEvento(),
                        EVENTO_ACUSE_REGISTRADO,
                        RESULTADO_OK,
                        DESCRIPCION_ACUSE_REGISTRADO,
                        ipOrigen,
                        datosSesionDispositivo
                );

        return true;
    }

    private String generarCodigoEvento() {

        return "HIST-ACUSE-"
                + UUID.randomUUID();
    }
}