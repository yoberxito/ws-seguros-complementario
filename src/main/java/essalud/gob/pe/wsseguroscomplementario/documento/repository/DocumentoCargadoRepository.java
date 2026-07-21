package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoCargado;

import java.util.List;
import java.util.Optional;

public interface DocumentoCargadoRepository {

    DocumentoCargado guardar(DocumentoCargado documentoCargado);

    Optional<DocumentoCargado> buscarPorId(String idDocumentoCargado);

    List<DocumentoCargado> listar();
}