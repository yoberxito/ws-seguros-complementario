package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoGenerado;

import java.util.List;
import java.util.Optional;

public interface DocumentoGeneradoRepository {

    DocumentoGenerado guardar(DocumentoGenerado documentoGenerado);

    Optional<DocumentoGenerado> buscarPorId(String idDocumentoGenerado);


    Optional<DocumentoGenerado> buscarPorRegistroInternoProcesoYTipoDocumento(
            String registroInternoProceso,
            String tipoDocumento
    );

    List<DocumentoGenerado> buscarPorRegistroInternoProceso(String registroInternoProceso);

    List<DocumentoGenerado> listar();

}