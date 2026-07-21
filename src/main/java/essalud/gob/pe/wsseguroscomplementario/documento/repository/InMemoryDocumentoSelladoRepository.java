package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoSellado;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryDocumentoSelladoRepository implements DocumentoSelladoRepository {

    private final ConcurrentHashMap<String, DocumentoSellado> documentosSellados = new ConcurrentHashMap<>();

    @Override
    public DocumentoSellado guardar(DocumentoSellado documentoSellado) {
        documentosSellados.put(documentoSellado.getIdDocumentoSellado(), documentoSellado);
        return documentoSellado;
    }

    @Override
    public Optional<DocumentoSellado> buscarPorId(String idDocumentoSellado) {
        return Optional.ofNullable(documentosSellados.get(idDocumentoSellado));
    }

    @Override
    public List<DocumentoSellado> listar() {
        return new ArrayList<>(documentosSellados.values());
    }
}