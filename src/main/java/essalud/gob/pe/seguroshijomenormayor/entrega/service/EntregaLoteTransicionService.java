package essalud.gob.pe.seguroshijomenormayor.entrega.service;

import essalud.gob.pe.seguroshijomenormayor.entrega.repository.EntregaLoteRepository;
import essalud.gob.pe.seguroshijomenormayor.entrega.repository.HistorialEntregaLoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("masVidaEntregaLoteTransicionService")
public class EntregaLoteTransicionService {

    private static final String
            EVENTO_OTP_VALIDADO =
            "OTP_VALIDADO";

    private static final String
            EVENTO_DESCARGA_LOTE_COMPLETADA =
            "DESCARGA_LOTE_COMPLETADA";

    private static final String
            EVENTO_PUBLICACION_DRIVE_COMPLETADA =
            "PUBLICACION_DRIVE_COMPLETADA";

    private static final String
            EVENTO_ACUSE_REGISTRADO =
            "ACUSE_REGISTRADO";

    private static final String
            RESULTADO_OK =
            "OK";

    private static final String
            DESCRIPCION_OTP_VALIDADO =
            "Validacion OTP institucional reportada como exitosa.";

    private static final String
            DESCRIPCION_DESCARGA_LOTE_COMPLETADA =
            "Descarga completa del lote reportada por la aplicación.";

    private static final String
            DESCRIPCION_PUBLICACION_DRIVE_COMPLETADA =
            "Publicacion del lote PERSONAL en Drive completada correctamente.";

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
    public void registrarOtpValidado(
            String tokenHash,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        historialEntregaLoteRepository
                .registrarEvento(
                        tokenHash,
                        generarCodigoEventoOtp(),
                        EVENTO_OTP_VALIDADO,
                        RESULTADO_OK,
                        DESCRIPCION_OTP_VALIDADO,
                        ipOrigen,
                        datosSesionDispositivo
                );
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

        /*
         * Reintentos post-acuse no deben multiplicar el evento.
         */
        if (
                existePublicacionDriveCompletada(
                        tokenHash
                )
        ) {
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

    private String generarCodigoEventoOtp() {

        return "HIST-OTP-"
                + UUID.randomUUID();
    }

    private String generarCodigoEventoDescarga() {

        return "HIST-DESCARGA-"
                + UUID.randomUUID();
    }

    private String generarCodigoEventoPublicacionDrive() {

        return "HIST-PUBLICACION-"
                + UUID.randomUUID();
    }

    private String generarCodigoEvento() {

        return "HIST-ACUSE-"
                + UUID.randomUUID();
    }
}