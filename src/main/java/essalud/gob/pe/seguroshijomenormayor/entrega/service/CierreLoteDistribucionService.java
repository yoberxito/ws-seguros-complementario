package essalud.gob.pe.seguroshijomenormayor.entrega.service;

import essalud.gob.pe.seguroshijomenormayor.entrega.model.CierreLotePreparado;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.LoteDistribucion;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.PreparacionEntregaLote;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/*
 * Orquesta la parte persistente del cierre quincenal +Vida.
 *
 * Esta clase NO:
 * - consulta Google Drive;
 * - genera Excel;
 * - consulta el origen documental;
 * - envia correo.
 *
 * MAPFRE conserva el flujo existente.
 *
 * PERSONAL puede cerrar un lote especifico por destino
 * institucional utilizando codigoDestino como parte de
 * la identidad deterministica del lote.
 */
@Service("masVidaCierreLoteDistribucionService")
public class CierreLoteDistribucionService {

    private static final String
            DESTINO_PERSONAL =
            "PERSONAL";

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

    /*
     * Contrato existente.
     *
     * Se conserva para MAPFRE y para no romper consumidores
     * actuales mientras PERSONAL migra al nuevo contrato
     * multidestino.
     */
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

        LoteDistribucion lote =
                loteDistribucionService
                        .obtenerOCrear(
                                destinatario,
                                fechaInicio,
                                fechaFin
                        );

        return completarPreparacion(
                lote,
                destinatario,
                null,
                fechaInicio,
                fechaFin,
                correoDestinatario,
                cantidadDocumentos,
                urlAcceso
        );
    }

    /*
     * PERSONAL MULTIDESTINO.
     *
     * codigoDestino identifica la Red/unidad institucional.
     *
     * TIPO_DESTINATARIO en ENTREGA_LOTE continua siendo
     * PERSONAL. La separacion entre Redes se consigue porque
     * cada codigoDestino produce un LOTE_DISTRIBUCION distinto.
     */
    @Transactional
    public CierreLotePreparado prepararLotePublicadoPersonal(
            String codigoDestino,
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

        validarCodigoDestinoPersonal(
                codigoDestino
        );

        LoteDistribucion lote =
                loteDistribucionService
                        .obtenerOCrearPersonal(
                                codigoDestino,
                                fechaInicio,
                                fechaFin
                        );

        return completarPreparacion(
                lote,
                DESTINO_PERSONAL,
                codigoDestino,
                fechaInicio,
                fechaFin,
                correoDestinatario,
                cantidadDocumentos,
                urlAcceso
        );
    }

    /*
     * Completa las etapas comunes:
     *
     * 1. prepara/reutiliza ENTREGA_LOTE;
     * 2. genera o reutiliza token segun estado;
     * 3. publica LOTE_DISTRIBUCION si corresponde;
     * 4. recupera el lote realmente persistido.
     */
    private CierreLotePreparado completarPreparacion(
            LoteDistribucion lote,
            String destinatario,
            String codigoDestinoPersonal,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String correoDestinatario,
            int cantidadDocumentos,
            String urlAcceso
    ) {

        if (
                lote == null
                        || lote.getIdLote() == null
        ) {

            throw new IllegalStateException(
                    "El lote de distribucion no pudo resolverse."
            );
        }

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
         * Publicamos solamente una vez.
         */
        if (lote.getFechaPublicacion() == null) {

            boolean publicado =
                    loteDistribucionService
                            .marcarPublicado(
                                    lote.getIdLote()
                            );

            if (!publicado) {

                throw new IllegalStateException(
                        "Oracle no confirmo la publicacion del lote."
                );
            }
        }

        LoteDistribucion loteActualizado;

        if (
                codigoDestinoPersonal == null
                        || codigoDestinoPersonal.trim().isEmpty()
        ) {

            loteActualizado =
                    loteDistribucionService
                            .obtenerOCrear(
                                    destinatario,
                                    fechaInicio,
                                    fechaFin
                            );

        } else {

            loteActualizado =
                    loteDistribucionService
                            .obtenerOCrearPersonal(
                                    codigoDestinoPersonal,
                                    fechaInicio,
                                    fechaFin
                            );
        }

        if (loteActualizado == null) {

            throw new IllegalStateException(
                    "El lote procesado no pudo recuperarse."
            );
        }

        if (loteActualizado.getFechaPublicacion() == null) {

            throw new IllegalStateException(
                    "El lote fue procesado, pero Oracle no registra fecha de publicacion."
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

        validarCierre(
                cierre
        );

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

        validarCierre(
                cierre
        );

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

        validarCierre(
                cierre
        );

        return entregaLoteService
                .marcarErrorNotificacion(
                        cierre
                                .getPreparacionEntrega()
                                .getEntrega()
                                .getIdEntrega()
                );
    }

    private void validarCodigoDestinoPersonal(
            String codigoDestino
    ) {

        if (
                codigoDestino == null
                        || codigoDestino.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El codigo del destino PERSONAL es obligatorio."
            );
        }
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
                    "El periodo del lote es obligatorio."
            );
        }

        if (
                fechaInicio.isAfter(
                        fechaFin
                )
        ) {

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
                        || cierre.getPreparacionEntrega()
                                .getEntrega() == null
                        || cierre.getPreparacionEntrega()
                                .getEntrega()
                                .getIdEntrega() == null
        ) {

            throw new IllegalArgumentException(
                    "La preparacion de la entrega es obligatoria."
            );
        }
    }
}