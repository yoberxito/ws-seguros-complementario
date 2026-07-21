package essalud.gob.pe.wsseguroscomplementario.incidencia.repository;

import essalud.gob.pe.wsseguroscomplementario.incidencia.model.IncidenciaOperativa;
import java.util.List;

import java.util.Optional;

public interface IncidenciaOperativaRepository {

    IncidenciaOperativa guardar(IncidenciaOperativa incidenciaOperativa);

    Optional<IncidenciaOperativa> buscarPorId(String idIncidenciaOperativa);

    List<IncidenciaOperativa> buscarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    );

    List<IncidenciaOperativa> listarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    );

    List<IncidenciaOperativa> listar();
}