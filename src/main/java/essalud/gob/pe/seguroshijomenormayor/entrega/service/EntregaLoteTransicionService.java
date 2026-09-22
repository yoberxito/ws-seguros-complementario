package essalud.gob.pe.seguroshijomenormayor.entrega.service;

import essalud.gob.pe.seguroshijomenormayor.entrega.repository.EntregaLoteRepository;
import essalud.gob.pe.seguroshijomenormayor.entrega.repository.HistorialEntregaLoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("masVidaEntregaLoteTransicionService")
public class EntregaLoteTransicionService {

    private static final String EVENTO_DESCARGA_LOTE_COMPLETADA =
            "DESCARGA_LOTE_COMPLETADA";

    private static final String EVENTO_PUBLICACION_DRIVE_COMPLETADA =
            "PUBLICACION_DRIVE_COMPLETADA";

    private static final String EVENTO_ACUSE_REGISTRADO =
            "ACUSE_REGISTRADO";

    private static final String RESULTADO_OK =
            "OK";

    private static final String DESCRIPCION_DESCARGA_LOTE_COMPLETADA =
            "Descarga completa del lote reportada por la aplicacion.";

    private static final String DESCRIPCION_PUBLICACION_DRIVE_COMPLETADA =
            "Movimiento del lote a Historical completado correctamente.";

    private static final String DESCRIPCION_ACUSE_REGISTRADO =
            "Confirmacion de recepcion del lote registrada correctamente.";

    private final EntregaLoteRepository entregaLoteRepository;
    private final HistorialEntregaLoteRepository historialEntregaLoteRepository;

    public EntregaLoteTransicionService(
            EntregaLoteRepository entregaLoteRepository,
            HistorialEntregaLoteRepository historialEntregaLoteRepository
    ) {
        this.entregaLoteRepository =
                entregaLoteRepository;
        this.historialEntregaLoteRepository =
                historialEntregaLoteRepository;
    }

    public boolean existeDescargaLoteCompletada(
            String tokenHash
    ) {
        return historialEntregaLoteRepository
                .existeEvento(
                        tokenHash,
                        EVENTO_DESCARGA_LOTE_COMPLETADA
                );
    }

    @Transactional
    public void registrarDescargaLoteCompletada(
            String tokenHash,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        if (existeDescargaLoteCompletada(tokenHash)) {
            return;
        }

        historialEntregaLoteRepository
                .registrarEvento(
                        tokenHash,
                        generarCodigoEventoDescarga(),
                        EVENTO_DESCARGA_LOTE_COMPLETADA,
                        RESULTADO_OK,
                        DESCRIPCION_DESCARGA_LOTE_COMPLETADA,
                        ipOrigen,
                        datosSesionDispositivo
                );
    }

    public boolean existePublicacionDriveCompletada(
            String tokenHash
    ) {
        return historialEntregaLoteRepository
                .existeEvento(
                        tokenHash,
                        EVENTO_PUBLICACION_DRIVE_COMPLETADA
                );
    }

    @Transactional
    public void registrarPublicacionDriveCompletada(
            String tokenHash,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        if (existePublicacionDriveCompletada(tokenHash)) {
            return;
        }

        historialEntregaLoteRepository
                .registrarEvento(
                        tokenHash,
                        generarCodigoEventoPublicacionDrive(),
                        EVENTO_PUBLICACION_DRIVE_COMPLETADA,
                        RESULTADO_OK,
                        DESCRIPCION_PUBLICACION_DRIVE_COMPLETADA,
                        ipOrigen,
                        datosSesionDispositivo
                );
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

        if (!actualizado) {
            return false;
        }

        historialEntregaLoteRepository
                .registrarEvento(
                        tokenHash,
                        generarCodigoEventoAcuse(),
                        EVENTO_ACUSE_REGISTRADO,
                        RESULTADO_OK,
                        DESCRIPCION_ACUSE_REGISTRADO,
                        ipOrigen,
                        datosSesionDispositivo
                );

        return true;
    }

    private String generarCodigoEventoDescarga() {
        return "HIST-DESCARGA-"
                + UUID.randomUUID();
    }

    private String generarCodigoEventoPublicacionDrive() {
        return "HIST-PUBLICACION-"
                + UUID.randomUUID();
    }

    private String generarCodigoEventoAcuse() {
        return "HIST-ACUSE-"
                + UUID.randomUUID();
    }
}
