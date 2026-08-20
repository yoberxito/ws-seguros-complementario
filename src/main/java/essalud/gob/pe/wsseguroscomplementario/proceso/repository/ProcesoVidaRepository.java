package essalud.gob.pe.wsseguroscomplementario.proceso.repository;

import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;

import java.util.Optional;

public interface ProcesoVidaRepository {

    ProcesoVida crear(ProcesoVida procesoVida);

    Optional<ProcesoVida> buscarPorRegistroInternoProceso(
            String registroInternoProceso
    );

    Optional<ProcesoVida> buscarUltimoPorTrabajador(
            String tipoDocumento,
            String numeroDocumento
    );
    ProcesoVida actualizarTitularYEstado(
            String registroInternoProceso,
            ProcesoVida procesoVida,
            String codigoSiguienteEstado
    );
    ProcesoVida actualizarDatosComplementariosYEstado(
            String registroInternoProceso,
            ProcesoVida procesoVida,
            String codigoSiguienteEstado
    );
    ProcesoVida actualizarConyugeYEstado(
            String registroInternoProceso,
            ProcesoVida procesoVida,
            String codigoSiguienteEstado
    );

    ProcesoVida actualizarTipoFlujoInicialYEstado(
            String registroInternoProceso,
            String tipoFlujo,
            String codigoSiguienteEstado
    );

    ProcesoVida actualizarEstado(
            String registroInternoProceso,
            String codigoSiguienteEstado
    );

    ProcesoVida actualizarEstadoNavegacion(
            String registroInternoProceso,
            String codigoEstadoNavegacion
    );
    ProcesoVida actualizarTitularBorrador(
            String registroInternoProceso,
            ProcesoVida procesoVida
    );

    ProcesoVida actualizarDatosComplementariosBorrador(
            String registroInternoProceso,
            ProcesoVida procesoVida
    );

    ProcesoVida actualizarBorradorBeneficiarios(
            String registroInternoProceso,
            boolean beneficiarioBorradorAbierto
    );
}