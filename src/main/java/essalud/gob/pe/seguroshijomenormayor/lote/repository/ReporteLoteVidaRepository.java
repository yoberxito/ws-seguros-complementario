package essalud.gob.pe.seguroshijomenormayor.lote.repository;

import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteLoteVidaItem;

import java.time.LocalDate;
import java.util.Optional;

public interface ReporteLoteVidaRepository {

    Optional<ReporteLoteVidaItem> buscarDocumentoPublicado(
            String tipoDocumentoTitular,
            String numeroDocumentoTitular,
            String tipoDocumentoLogico,
            LocalDate fechaInicioPeriodo,
            LocalDate fechaFinPeriodo
    );
}