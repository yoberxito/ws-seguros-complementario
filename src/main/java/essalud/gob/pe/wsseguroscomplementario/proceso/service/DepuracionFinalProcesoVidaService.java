package essalud.gob.pe.wsseguroscomplementario.proceso.service;

import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoSustentoRepository;
import essalud.gob.pe.wsseguroscomplementario.incidencia.repository.IncidenciaOperativaRepository;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.ProcesoVidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepuracionFinalProcesoVidaService {

    private static final String
            ESTADO_FINALIZACION =
            "FINALIZACION";

    private static final String
            FLUJO_COMPLETO =
            "COMPLETO";

    private static final String
            FLUJO_SOLO_AUTORIZACION =
            "SOLO_AUTORIZACION";

    private static final String
            FLUJO_6012_POSTERIOR =
            "FORMULARIO_6012_POSTERIOR";

    private static final String
            DOCUMENTO_AUTORIZACION =
            "AUTORIZACION_DESCUENTO";

    private static final String
            DOCUMENTO_6012 =
            "FORMULARIO_6012";

    private final ProcesoVidaRepository
            procesoVidaRepository;

    private final DocumentoSustentoRepository
            documentoSustentoRepository;

    private final IncidenciaOperativaRepository
            incidenciaOperativaRepository;

    public DepuracionFinalProcesoVidaService(
            ProcesoVidaRepository procesoVidaRepository,
            DocumentoSustentoRepository documentoSustentoRepository,
            IncidenciaOperativaRepository incidenciaOperativaRepository
    ) {
        this.procesoVidaRepository =
                procesoVidaRepository;

        this.documentoSustentoRepository =
                documentoSustentoRepository;

        this.incidenciaOperativaRepository =
                incidenciaOperativaRepository;
    }

    @Transactional
    public boolean depurarSiCorresponde(
            String registroInternoProceso
    ) {

        ProcesoVida proceso =
                procesoVidaRepository
                        .buscarPorRegistroInternoProceso(
                                registroInternoProceso
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No se encontró el proceso +Vida para ejecutar la depuración final."
                                        )
                        );

        /*
         * El purge únicamente se ejecuta cuando
         * el trámite completo ya llegó al estado
         * funcional de FINALIZACION.
         *
         * Por ejemplo, publicar solamente la
         * Autorización de un flujo COMPLETO no
         * ejecuta ninguna depuración.
         */
        if (
                proceso.getCodigoEstadoProceso()
                        == null

                        || !ESTADO_FINALIZACION
                        .equalsIgnoreCase(
                                proceso
                                        .getCodigoEstadoProceso()
                        )
        ) {
            return false;
        }


        /* AQUÍ PEGAS EL BLOQUE NUEVO */


        String tipoFlujo =
                proceso.getTipoFlujo();

        if (
                tipoFlujo == null
                        || tipoFlujo.trim().isEmpty()
        ) {
            return false;
        }

        String flujo =
                tipoFlujo
                        .trim()
                        .toUpperCase();

        boolean cicloDocumentalCompleto;

        switch (flujo) {

            case FLUJO_COMPLETO ->

                    cicloDocumentalCompleto =
                            documentoSustentoRepository
                                    .estaPublicado(
                                            registroInternoProceso,
                                            DOCUMENTO_AUTORIZACION
                                    )
                                    &&
                                    documentoSustentoRepository
                                            .estaPublicado(
                                                    registroInternoProceso,
                                                    DOCUMENTO_6012
                                            );

            case FLUJO_SOLO_AUTORIZACION ->

                    cicloDocumentalCompleto =
                            documentoSustentoRepository
                                    .estaPublicado(
                                            registroInternoProceso,
                                            DOCUMENTO_AUTORIZACION
                                    );

            case FLUJO_6012_POSTERIOR ->

                    cicloDocumentalCompleto =
                            documentoSustentoRepository
                                    .estaPublicado(
                                            registroInternoProceso,
                                            DOCUMENTO_6012
                                    );

            default -> {
                return false;
            }
        }

        if (!cicloDocumentalCompleto) {
            return false;
        }


        /*
         * No se destruye evidencia técnica si
         * todavía existe una incidencia activa.
         */
        if (
                incidenciaOperativaRepository
                        .existenIncidenciasAbiertas(
                                registroInternoProceso
                        )
        ) {
            return false;
        }

        /*
         * Primero desaparecen las filas que
         * todavía dependen de metadata técnica.
         */
        incidenciaOperativaRepository
                .depurarTrazabilidadCerrada(
                        registroInternoProceso
                );

        /*
         * Después DOCUMENTOS_SUSTENTO queda como
         * registro del resultado documental final.
         */
        documentoSustentoRepository
                .depurarMetadataOperativaPublicada(
                        registroInternoProceso
                );

        /*
         * Estado puramente temporal utilizado para
         * reconstruir una fila vacía del frontend.
         */
        procesoVidaRepository
                .actualizarBorradorBeneficiarios(
                        registroInternoProceso,
                        false
                );

        return true;
    }
}