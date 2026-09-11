package essalud.gob.pe.wsseguroscomplementario.entrega.service;

import essalud.gob.pe.wsseguroscomplementario.entrega.model.CierreLotePreparado;
import essalud.gob.pe.wsseguroscomplementario.entrega.model.LoteDistribucion;
import essalud.gob.pe.wsseguroscomplementario.entrega.model.PreparacionEntregaLote;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/*
 * Orquesta la parte persistente del cierre quincenal +Vida.
 *
 * Esta clase NO:
 * - consulta Google Drive;
 * - genera Excel;
 * - consulta SAS;
 * - envía correo.
 *
 * Recibe el resultado definitivo de esas etapas:
 * período, destino, cantidad de documentos y URL Drive.
 *
 * A partir de ello:
 * 1. crea o recupera el lote;
 * 2. crea o reutiliza la entrega del destinatario;
 * 3. genera/renueva el token cuando corresponde;
 * 4. publica el lote;
 * 5. devuelve el token únicamente en memoria.
 */
@Service
public class CierreLoteDistribucionService {

    private final LoteDistribucionService
            loteDistribucionService;

    private final EntregaLoteService
            entregaLoteService;

    public CierreLoteDistribucionService(
            LoteDistribucionService loteDistribucionService,
            EntregaLoteService entregaLoteService
    ) {
        this.loteDistribucionService =
                loteDistribucionService;

        this.entregaLoteService =
                entregaLoteService;
    }

    @Transactional
    public CierreLotePreparado prepararLotePublicado(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String correoDestinatario,
            int cantidadDocumentos,
            String urlAcceso
    ) {

        validar(
                fechaInicio,
                fechaFin,
                cantidadDocumentos,
                urlAcceso
        );

        /*
         * Idempotencia del lote:
         * obtenerOCrear utiliza el código determinístico
         * formado por destino + período.
         */
        LoteDistribucion lote =
                loteDistribucionService
                        .obtenerOCrear(
                                destinatario,
                                fechaInicio,
                                fechaFin
                        );

        /*
         * La URL recibida aquí debe ser la carpeta final
         * del lote en Drive, no la carpeta temporal
         * de preparación.
         */
        PreparacionEntregaLote preparacion =
                entregaLoteService
                        .prepararEntrega(
                                lote.getIdLote(),
                                destinatario,
                                correoDestinatario,
                                cantidadDocumentos,
                                urlAcceso.trim()
                        );

        /*
         * Publicamos únicamente cuando todavía
         * no existe fecha de publicación.
         *
         * Esto permite ejecutar nuevamente el cierre
         * sin considerar un lote ya publicado como error.
         */
        if (lote.getFechaPublicacion() == null) {

            boolean publicado =
                    loteDistribucionService
                            .marcarPublicado(
                                    lote.getIdLote()
                            );

            if (!publicado) {

                throw new IllegalStateException(
                        "Oracle no confirmó la publicación del lote."
                );
            }
        }

        /*
         * Recuperamos el registro para devolver
         * el estado realmente persistido en Oracle.
         */
        LoteDistribucion loteActualizado =
                loteDistribucionService
                        .obtenerOCrear(
                                destinatario,
                                fechaInicio,
                                fechaFin
                        );

        if (loteActualizado.getFechaPublicacion() == null) {

            throw new IllegalStateException(
                    "El lote fue procesado, pero Oracle no registra fecha de publicación."
            );
        }

        return new CierreLotePreparado(
                loteActualizado,
                preparacion
        );
    }

    public boolean marcarNotificacionEnviando(
            CierreLotePreparado cierre
    ) {

        validarCierre(cierre);

        return entregaLoteService
                .marcarNotificacionEnviando(
                        cierre
                                .getPreparacionEntrega()
                                .getEntrega()
                                .getIdEntrega()
                );
    }

    public boolean marcarNotificacionEnviada(
            CierreLotePreparado cierre
    ) {

        validarCierre(cierre);

        return entregaLoteService
                .marcarNotificacionEnviada(
                        cierre
                                .getPreparacionEntrega()
                                .getEntrega()
                                .getIdEntrega()
                );
    }

    public boolean marcarErrorNotificacion(
            CierreLotePreparado cierre
    ) {

        validarCierre(cierre);

        return entregaLoteService
                .marcarErrorNotificacion(
                        cierre
                                .getPreparacionEntrega()
                                .getEntrega()
                                .getIdEntrega()
                );
    }

    private void validar(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            int cantidadDocumentos,
            String urlAcceso
    ) {

        if (
                fechaInicio == null
                        || fechaFin == null
        ) {

            throw new IllegalArgumentException(
                    "El período del lote es obligatorio."
            );
        }

        if (fechaInicio.isAfter(fechaFin)) {

            throw new IllegalArgumentException(
                    "La fecha inicial del lote no puede ser posterior a la fecha final."
            );
        }

        if (cantidadDocumentos < 0) {

            throw new IllegalArgumentException(
                    "La cantidad de documentos no puede ser negativa."
            );
        }

        if (
                urlAcceso == null
                        || urlAcceso.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "La URL final de acceso al lote es obligatoria."
            );
        }
    }

    private void validarCierre(
            CierreLotePreparado cierre
    ) {

        if (
                cierre == null
                        || cierre.getPreparacionEntrega() == null
                        || cierre
                                .getPreparacionEntrega()
                                .getEntrega() == null
                        || cierre
                                .getPreparacionEntrega()
                                .getEntrega()
                                .getIdEntrega() == null
        ) {

            throw new IllegalArgumentException(
                    "La preparación de la entrega es obligatoria."
            );
        }
    }
}