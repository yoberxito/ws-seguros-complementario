package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoCargado;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryDocumentoCargadoRepository implements DocumentoCargadoRepository {

    private final Map<String, DocumentoCargado> almacenamiento = new ConcurrentHashMap<>();

    @Override
    public DocumentoCargado guardar(DocumentoCargado documentoCargado) {
        almacenamiento.put(documentoCargado.getIdDocumentoCargado(), documentoCargado);
        return documentoCargado;
    }

    @Override
    public Optional<DocumentoCargado> buscarPorId(String idDocumentoCargado) {
        return Optional.ofNullable(almacenamiento.get(idDocumentoCargado));
    }

    @Override
    public List<DocumentoCargado> listar() {
        return new ArrayList<>(almacenamiento.values());
    }
}