package essalud.gob.pe.wsseguroscomplementario.expediente.repository;

import essalud.gob.pe.wsseguroscomplementario.expediente.model.ExpedienteDigital;

import java.util.List;
import java.util.Optional;

public interface ExpedienteDigitalRepository {

    ExpedienteDigital guardar(ExpedienteDigital expedienteDigital);

    Optional<ExpedienteDigital> buscarPorRegistroInternoProceso(String registroInternoProceso);

    List<ExpedienteDigital> buscarPorNumeroDocumentoTrabajador(String numeroDocumentoTrabajador);

    List<ExpedienteDigital> listar();
}