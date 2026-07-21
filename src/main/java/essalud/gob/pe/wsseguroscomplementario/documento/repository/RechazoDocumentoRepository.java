package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.RechazoDocumento;

import java.util.List;
import java.util.Optional;

public interface RechazoDocumentoRepository {

    RechazoDocumento guardar(RechazoDocumento rechazoDocumento);

    Optional<RechazoDocumento> buscarPorId(String idRechazoDocumental);

    List<RechazoDocumento> listar();
}