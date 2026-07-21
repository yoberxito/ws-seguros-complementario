package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;

import java.util.List;
import java.util.Optional;

public interface DocumentoPublicadoRepository {

    DocumentoPublicado guardar(DocumentoPublicado documentoPublicado);

    Optional<DocumentoPublicado> buscarPorId(String idDocumentoPublicado);

    List<DocumentoPublicado> listar();

    List<DocumentoPublicado> buscarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    );
}