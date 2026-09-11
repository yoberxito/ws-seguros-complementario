package essalud.gob.pe.wsseguroscomplementario.entrega.repository;

import essalud.gob.pe.wsseguroscomplementario.entrega.model.ReporteLoteVidaItem;

import java.util.Optional;

public interface ReporteLoteVidaRepository {

    /*
     * Recupera los datos Oracle correspondientes al
     * documento publicado de un trabajador.
     *
     * La identificación del trabajador proviene
     * del documento encontrado en Drive.
     */
    Optional<ReporteLoteVidaItem> buscarDocumentoPublicado(
            String tipoDocumentoTitular,
            String numeroDocumentoTitular,
            String tipoDocumentoLogico
    );
}