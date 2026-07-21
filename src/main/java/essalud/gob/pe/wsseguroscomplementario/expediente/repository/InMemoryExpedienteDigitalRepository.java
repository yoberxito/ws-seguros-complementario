package essalud.gob.pe.wsseguroscomplementario.expediente.repository;

import essalud.gob.pe.wsseguroscomplementario.expediente.model.ExpedienteDigital;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryExpedienteDigitalRepository implements ExpedienteDigitalRepository {

    private final ConcurrentHashMap<String, ExpedienteDigital> expedientes = new ConcurrentHashMap<>();

    @Override
    public ExpedienteDigital guardar(ExpedienteDigital expedienteDigital) {
        expedientes.put(expedienteDigital.getRegistroInternoProceso(), expedienteDigital);
        return expedienteDigital;
    }

    @Override
    public Optional<ExpedienteDigital> buscarPorRegistroInternoProceso(String registroInternoProceso) {
        return Optional.ofNullable(expedientes.get(registroInternoProceso));
    }

    @Override
    public List<ExpedienteDigital> buscarPorNumeroDocumentoTrabajador(String numeroDocumentoTrabajador) {
        return expedientes.values()
                .stream()
                .filter(expediente ->
                        expediente.getNumeroDocumentoTrabajador() != null
                                && expediente.getNumeroDocumentoTrabajador().equalsIgnoreCase(numeroDocumentoTrabajador)
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpedienteDigital> listar() {
        return new ArrayList<>(expedientes.values());
    }
}