package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.RechazoDocumento;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRechazoDocumentoRepository implements RechazoDocumentoRepository {

    private final Map<String, RechazoDocumento> almacenamiento = new ConcurrentHashMap<>();

    @Override
    public RechazoDocumento guardar(RechazoDocumento rechazoDocumento) {
        almacenamiento.put(rechazoDocumento.getIdRechazoDocumental(), rechazoDocumento);
        return rechazoDocumento;
    }

    @Override
    public Optional<RechazoDocumento> buscarPorId(String idRechazoDocumental) {
        return Optional.ofNullable(almacenamiento.get(idRechazoDocumental));
    }

    @Override
    public List<RechazoDocumento> listar() {
        return new ArrayList<>(almacenamiento.values());
    }
}