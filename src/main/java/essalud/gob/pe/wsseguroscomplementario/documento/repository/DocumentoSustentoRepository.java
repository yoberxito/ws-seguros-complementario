package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoCargado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;
import java.util.Optional;
import java.time.LocalDateTime;

public interface DocumentoSustentoRepository {

    void registrarCargaExitosa(
            DocumentoCargado documentoCargado
    );

    void registrarValidacionAprobada(
            String registroInternoProceso,
            String tipoDocumento,
            LocalDateTime fechaValidacion
    );

    void registrarValidacionRechazada(
            String registroInternoProceso,
            String tipoDocumento,
            LocalDateTime fechaValidacion,
            String idRechazoDocumental,
            String motivoRechazo
    );

    void registrarPublicacion(
            DocumentoPublicado documentoPublicado
    );

    void registrarResultadoSftp(
            DocumentoPublicado documentoPublicado
    );

    Optional<DocumentoPublicado> buscarResultadoSftpPendiente(
            String registroInternoProceso,
            String tipoDocumento
    );

    boolean estaPublicado(
            String registroInternoProceso,
            String tipoDocumento
    );

    void registrarRespaldoFormulario6012(
            String registroInternoProceso,
            String contenidoRespaldo,
            String hashRespaldo,
            LocalDateTime fechaRespaldo
    );

    String obtenerRespaldoFormulario6012(
            String registroInternoProceso
    );

    void depurarMetadataOperativaPublicada(
            String registroInternoProceso
    );

}