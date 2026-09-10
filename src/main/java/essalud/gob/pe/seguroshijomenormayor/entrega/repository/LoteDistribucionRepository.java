package essalud.gob.pe.seguroshijomenormayor.entrega.repository;

import essalud.gob.pe.seguroshijomenormayor.entrega.model.LoteDistribucion;

import java.time.LocalDate;
import java.util.Optional;

public interface LoteDistribucionRepository {

    LoteDistribucion crear(
            LoteDistribucion lote
    );

    Optional<LoteDistribucion> buscarPorCodigo(
            String codLote
    );

    Optional<LoteDistribucion> buscarPorPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    boolean marcarPublicado(
            Long idLote
    );
}
