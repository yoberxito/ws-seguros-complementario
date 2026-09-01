package essalud.gob.pe.wsseguroscomplementario.notificacion.service;

import essalud.gob.pe.wsseguroscomplementario.incidencia.service.IncidenciaOperativaService;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.ProcesoVidaRepository;
import essalud.gob.pe.wsseguroscomplementario.proceso.service.DepuracionFinalProcesoVidaService;
import org.springframework.stereotype.Service;

@Service
public class ReintentoCorreoFinalVidaService {

    private static final String
            CICLO_COMPLETO =
            "COMPLETO";

    private static final String
            CICLO_SOLO_AUTORIZACION =
            "SOLO_AUTORIZACION";

    private static final String
            CICLO_6012_POSTERIOR =
            "FORMULARIO_6012_POSTERIOR";

    private static final String
            DOCUMENTO_AUTORIZACION =
            "AUTORIZACION_DESCUENTO";

    private static final String
            DOCUMENTO_6012 =
            "FORMULARIO_6012";

    private static final String
            SISTEMA_CORREO =
            "NOTIFICACION_CORREO_FINAL";

    private final NotificacionCierreVidaService
            notificacionCierreVidaService;

    private final ProcesoVidaRepository
            procesoVidaRepository;

    private final IncidenciaOperativaService
            incidenciaOperativaService;

    private final DepuracionFinalProcesoVidaService
            depuracionFinalProcesoVidaService;

    public ReintentoCorreoFinalVidaService(
            NotificacionCierreVidaService
                    notificacionCierreVidaService,

            ProcesoVidaRepository
                    procesoVidaRepository,

            IncidenciaOperativaService
                    incidenciaOperativaService,

            DepuracionFinalProcesoVidaService
                    depuracionFinalProcesoVidaService
    ) {

        this.notificacionCierreVidaService =
                notificacionCierreVidaService;

        this.procesoVidaRepository =
                procesoVidaRepository;

        this.incidenciaOperativaService =
                incidenciaOperativaService;

        this.depuracionFinalProcesoVidaService =
                depuracionFinalProcesoVidaService;
    }

    public boolean reintentar(
            String registroInternoProceso,
            String cicloDocumental,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        String registro =
                requerido(
                        registroInternoProceso
                );

        String ciclo =
                normalizarCiclo(
                        cicloDocumental
                );

        ProcesoVida proceso =
                procesoVidaRepository
                        .buscarPorRegistroInternoProceso(
                                registro
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No se encontró el proceso +Vida indicado."
                                        )
                        );

        boolean enviado =
                notificacionCierreVidaService
                        .reintentarCiclo(
                                registro,
                                ciclo,
                                usuarioResponsable,
                                ipOrigen,
                                datosSesionDispositivo
                        );

        if (!enviado) {

            throw new IllegalStateException(
                    "El ciclo documental todavía no está completo "
                            + "y no puede reenviarse el correo."
            );
        }

        cerrarIncidenciasDelCiclo(
                proceso,
                ciclo,
                usuarioResponsable,
                ipOrigen,
                datosSesionDispositivo
        );

        return depuracionFinalProcesoVidaService
                .depurarSiCorresponde(
                        registro
                );
    }

    private void cerrarIncidenciasDelCiclo(
            ProcesoVida proceso,
            String ciclo,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        if (
                CICLO_SOLO_AUTORIZACION.equals(
                        ciclo
                )
        ) {

            cerrarIncidenciaDocumento(
                    proceso,
                    DOCUMENTO_AUTORIZACION,
                    usuarioResponsable,
                    ipOrigen,
                    datosSesionDispositivo
            );

            return;
        }

        if (
                CICLO_6012_POSTERIOR.equals(
                        ciclo
                )
        ) {

            cerrarIncidenciaDocumento(
                    proceso,
                    DOCUMENTO_6012,
                    usuarioResponsable,
                    ipOrigen,
                    datosSesionDispositivo
            );

            return;
        }

        /*
         * En COMPLETO el correo puede haberse
         * disparado con el cierre del último
         * documento procesado.
         *
         * Cerramos cualquiera de las dos
         * incidencias de correo que pudiera
         * haber quedado abierta.
         */
        cerrarIncidenciaDocumento(
                proceso,
                DOCUMENTO_AUTORIZACION,
                usuarioResponsable,
                ipOrigen,
                datosSesionDispositivo
        );

        cerrarIncidenciaDocumento(
                proceso,
                DOCUMENTO_6012,
                usuarioResponsable,
                ipOrigen,
                datosSesionDispositivo
        );
    }

    private void cerrarIncidenciaDocumento(
            ProcesoVida proceso,
            String tipoDocumentoProceso,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        incidenciaOperativaService
                .cerrarIncidenciasAbiertasPorSistema(
                        proceso
                                .getRegistroInternoProceso(),

                        proceso
                                .getNumeroDocumentoTitular(),

                        tipoDocumentoProceso,

                        SISTEMA_CORREO,

                        "El correo final fue reenviado correctamente.",

                        usuarioResponsable,

                        ipOrigen,

                        datosSesionDispositivo,

                        null,
                        null
                );
    }

    private String normalizarCiclo(
            String cicloDocumental
    ) {

        String ciclo =
                requerido(
                        cicloDocumental
                )
                        .toUpperCase();

        if (
                !CICLO_COMPLETO.equals(ciclo)
                        && !CICLO_SOLO_AUTORIZACION
                        .equals(ciclo)
                        && !CICLO_6012_POSTERIOR
                        .equals(ciclo)
        ) {

            throw new IllegalArgumentException(
                    "El ciclo documental indicado no es válido."
            );
        }

        return ciclo;
    }

    private String requerido(
            String valor
    ) {

        if (
                valor == null
                        || valor.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El valor obligatorio no puede estar vacío."
            );
        }

        return valor.trim();
    }
}