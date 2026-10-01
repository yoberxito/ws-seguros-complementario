package essalud.gob.pe.seguroshijomenormayor.service;

import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoPipelinePersonalDestino;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoReporteLoteVida;


public interface ReporteServiceSeguroMasVida {

    void generarReporteMafre();

    void generarReportePersonal();
    void senEmail(String correoDestino, String destinatario, ResultadoReporteLoteVida resultado, String tokenGenerado);
    void senEmailPersonal(String correoDestino, String destinatario, Periodo resultado, ResultadoPipelinePersonalDestino tokenGenerado);
}