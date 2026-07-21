package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoGenerado;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryDocumentoGeneradoRepository implements DocumentoGeneradoRepository {

    private final Map<String, DocumentoGenerado> almacenamiento = new ConcurrentHashMap<>();

    @Override
    public DocumentoGenerado guardar(DocumentoGenerado documentoGenerado) {
        almacenamiento.put(documentoGenerado.getIdDocumentoGenerado(), documentoGenerado);
        return documentoGenerado;
    }

    @Override
    public Optional<DocumentoGenerado> buscarPorId(String idDocumentoGenerado) {
        return Optional.ofNullable(almacenamiento.get(idDocumentoGenerado));
    }

    @Override
    public List<DocumentoGenerado> buscarPorRegistroInternoProceso(String registroInternoProceso) {
        return almacenamiento.values()
                .stream()
                .filter(documento -> documento.getRegistroInternoProceso().equalsIgnoreCase(registroInternoProceso))
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentoGenerado> listar() {
        return new ArrayList<>(almacenamiento.values());
    }
}