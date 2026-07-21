package essalud.gob.pe.wsseguroscomplementario.notificacion.repository;

import essalud.gob.pe.wsseguroscomplementario.notificacion.model.NotificacionTrabajador;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryNotificacionTrabajadorRepository implements NotificacionTrabajadorRepository {

    private final ConcurrentHashMap<String, NotificacionTrabajador> notificaciones = new ConcurrentHashMap<>();

    @Override
    public NotificacionTrabajador guardar(NotificacionTrabajador notificacionTrabajador) {
        notificaciones.put(notificacionTrabajador.getIdNotificacion(), notificacionTrabajador);
        return notificacionTrabajador;
    }

    @Override
    public Optional<NotificacionTrabajador> buscarPorId(String idNotificacion) {
        return Optional.ofNullable(notificaciones.get(idNotificacion));
    }

    @Override
    public List<NotificacionTrabajador> listar() {
        return new ArrayList<>(notificaciones.values());
    }

    @Override
    public List<NotificacionTrabajador> buscarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {
        return notificaciones.values()
                .stream()
                .filter(notificacion ->
                        notificacion.getRegistroInternoProceso().equalsIgnoreCase(registroInternoProceso)
                                && notificacion.getNumeroDocumentoTrabajador().equalsIgnoreCase(numeroDocumentoTrabajador)
                )
                .collect(Collectors.toList());
    }
}