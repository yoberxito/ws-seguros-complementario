package essalud.gob.pe.wsseguroscomplementario.incidencia.repository;

import essalud.gob.pe.wsseguroscomplementario.incidencia.model.IncidenciaOperativa;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryIncidenciaOperativaRepository implements IncidenciaOperativaRepository {

    private final ConcurrentHashMap<String, IncidenciaOperativa> incidencias = new ConcurrentHashMap<>();

    @Override
    public IncidenciaOperativa guardar(IncidenciaOperativa incidenciaOperativa) {
        incidencias.put(incidenciaOperativa.getIdIncidenciaOperativa(), incidenciaOperativa);
        return incidenciaOperativa;
    }

    @Override
    public Optional<IncidenciaOperativa> buscarPorId(String idIncidenciaOperativa) {
        return Optional.ofNullable(incidencias.get(idIncidenciaOperativa));
    }

    @Override
    public List<IncidenciaOperativa> buscarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {
        return incidencias.values()
                .stream()
                .filter(incidencia ->
                        incidencia.getRegistroInternoProceso() != null
                                && incidencia.getNumeroDocumentoTrabajador() != null
                                && incidencia.getRegistroInternoProceso().equalsIgnoreCase(registroInternoProceso)
                                && incidencia.getNumeroDocumentoTrabajador().equalsIgnoreCase(numeroDocumentoTrabajador)
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidenciaOperativa> listar() {
        return new ArrayList<>(incidencias.values());
    }
    @Override
    public List<IncidenciaOperativa> listarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {
        return incidencias.values()
                .stream()
                .filter(incidencia ->
                        sonIguales(incidencia.getRegistroInternoProceso(), registroInternoProceso)
                                && sonIguales(incidencia.getNumeroDocumentoTrabajador(), numeroDocumentoTrabajador)
                )
                .toList();
    }

    private boolean sonIguales(String valorA, String valorB) {
        return normalizar(valorA).equals(normalizar(valorB));
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim().toUpperCase();
    }
}