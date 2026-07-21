package essalud.gob.pe.wsseguroscomplementario.aceptacion.repository;

import essalud.gob.pe.wsseguroscomplementario.aceptacion.model.AceptacionLegal;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAceptacionRepository implements AceptacionRepository {

    private final Map<String, AceptacionLegal> almacenamiento = new ConcurrentHashMap<>();

    @Override
    public AceptacionLegal guardar(AceptacionLegal aceptacionLegal) {
        almacenamiento.put(aceptacionLegal.getIdAceptacion(), aceptacionLegal);
        return aceptacionLegal;
    }

    @Override
    public Optional<AceptacionLegal> buscarPorId(String idAceptacion) {
        return Optional.ofNullable(almacenamiento.get(idAceptacion));
    }

    @Override
    public List<AceptacionLegal> listar() {
        return new ArrayList<>(almacenamiento.values());
    }
}