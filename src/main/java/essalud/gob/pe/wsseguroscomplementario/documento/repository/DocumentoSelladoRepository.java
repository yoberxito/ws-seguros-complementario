package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoSellado;

import java.util.List;
import java.util.Optional;

public interface DocumentoSelladoRepository {

    DocumentoSellado guardar(DocumentoSellado documentoSellado);

    Optional<DocumentoSellado> buscarPorId(String idDocumentoSellado);

    List<DocumentoSellado> listar();
}