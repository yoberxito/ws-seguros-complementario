package essalud.gob.pe.wsseguroscomplementario.entrega.service;

import essalud.gob.pe.wsseguroscomplementario.entrega.repository.EntregaLoteRepository;
import essalud.gob.pe.wsseguroscomplementario.entrega.repository.HistorialEntregaLoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EntregaLoteTransicionService {

    private static final String
            EVENTO_OTP_VALIDADO =
            "OTP_VALIDADO";

    private static final String
            EVENTO_ACUSE_REGISTRADO =
            "ACUSE_REGISTRADO";

    private static final String
            RESULTADO_OK =
            "OK";

    private static final String
            DESCRIPCION_OTP_VALIDADO =
            "Validación OTP de la entrega registrada correctamente.";

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
    public boolean marcarOtpValidadoConHistorial(
            String tokenHash,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        boolean actualizado =
                entregaLoteRepository
                        .marcarOtpValidadoSiPendiente(
                                tokenHash
                        );

        /*
         * Si no hubo transición real,
         * tampoco generamos historial.
         *
         * Esto evita duplicados por F5,
         * doble llamada o concurrencia.
         */
        if (!actualizado) {

            return false;
        }

        historialEntregaLoteRepository
                .registrarEvento(
                        tokenHash,
                        generarCodigoEvento(
                                "OTP"
                        ),
                        EVENTO_OTP_VALIDADO,
                        RESULTADO_OK,
                        DESCRIPCION_OTP_VALIDADO,
                        ipOrigen,
                        datosSesionDispositivo
                );

        return true;
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
                        generarCodigoEvento(
                                "ACUSE"
                        ),
                        EVENTO_ACUSE_REGISTRADO,
                        RESULTADO_OK,
                        DESCRIPCION_ACUSE_REGISTRADO,
                        ipOrigen,
                        datosSesionDispositivo
                );

        return true;
    }

    private String generarCodigoEvento(
            String tipo
    ) {

        return "HIST-"
                + tipo
                + "-"
                + UUID.randomUUID();
    }
}