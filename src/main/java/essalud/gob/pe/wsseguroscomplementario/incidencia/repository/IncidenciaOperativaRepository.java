package essalud.gob.pe.wsseguroscomplementario.incidencia.repository;

import essalud.gob.pe.wsseguroscomplementario.incidencia.model.IncidenciaOperativa;
import essalud.gob.pe.wsseguroscomplementario.incidencia.model.ReintentoIncidenciaOperativa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IncidenciaOperativaRepository {

    IncidenciaOperativa guardar(
            IncidenciaOperativa incidenciaOperativa
    );

    Optional<IncidenciaOperativa> buscarPorId(
            String idIncidenciaOperativa
    );

    List<IncidenciaOperativa> buscarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    );

    List<IncidenciaOperativa> listarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    );

    List<IncidenciaOperativa> listar();

    void registrarEventoIncidencia(
            String idIncidenciaOperativa,
            String codigoEventoHistorial,
            String tipoEvento,
            String estadoAnterior,
            String estadoNuevo,
            String resultadoEvento,
            String descripcionEvento,
            String usuarioAutenticado,
            String ipOrigen,
            String datosSesionDispositivo,
            String idDocumentoSellado,
            String idDocumentoPublicado,
            LocalDateTime fechaEvento
    );

    List<ReintentoIncidenciaOperativa>
    listarReintentosPorIncidencia(
            String idIncidenciaOperativa
    );

    boolean existenIncidenciasAbiertas(
            String registroInternoProceso
    );

    void depurarTrazabilidadCerrada(
            String registroInternoProceso
    );
}