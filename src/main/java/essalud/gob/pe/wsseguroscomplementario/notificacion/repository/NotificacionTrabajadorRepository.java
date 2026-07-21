package essalud.gob.pe.wsseguroscomplementario.notificacion.repository;

import essalud.gob.pe.wsseguroscomplementario.notificacion.model.NotificacionTrabajador;

import java.util.List;
import java.util.Optional;

public interface NotificacionTrabajadorRepository {

    NotificacionTrabajador guardar(NotificacionTrabajador notificacionTrabajador);

    Optional<NotificacionTrabajador> buscarPorId(String idNotificacion);

    List<NotificacionTrabajador> listar();

    List<NotificacionTrabajador> buscarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    );
}