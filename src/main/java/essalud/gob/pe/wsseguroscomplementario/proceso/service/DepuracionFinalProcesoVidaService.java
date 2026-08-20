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