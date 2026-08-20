package essalud.gob.pe.wsseguroscomplementario.proceso.repository;

import essalud.gob.pe.wsseguroscomplementario.proceso.model.BeneficiarioVida;

import java.util.List;

public interface BeneficiarioVidaRepository {

    void eliminarPorIdSecomasvida(
            Long idSecomasvida
    );

    void guardarTodos(
            List<BeneficiarioVida> beneficiarios
    );

    List<BeneficiarioVida> listarPorIdSecomasvida(
            Long idSecomasvida
    );
}