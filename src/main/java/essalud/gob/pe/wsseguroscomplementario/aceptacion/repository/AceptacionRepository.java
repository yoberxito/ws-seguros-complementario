package essalud.gob.pe.wsseguroscomplementario.aceptacion.repository;

import essalud.gob.pe.wsseguroscomplementario.aceptacion.model.AceptacionLegal;

import java.util.List;
import java.util.Optional;

public interface AceptacionRepository {

    AceptacionLegal guardar(AceptacionLegal aceptacionLegal);

    Optional<AceptacionLegal> buscarPorId(String idAceptacion);

    Optional<AceptacionLegal> buscarPorRegistroInternoProceso(
            String registroInternoProceso
    );

    List<AceptacionLegal> listar();
}