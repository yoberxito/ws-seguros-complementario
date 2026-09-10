package essalud.gob.pe.seguroshijomenormayor.lote.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultadoReporteLoteVidaTest {

    @Test
    void conservaUrlDeCarpetaFinalSeparadaDeUrlReporte() {

        String urlCarpetaFinal =
                "https://drive.google.com/drive/folders/CARPETA_FINAL";

        String urlReporte =
                "https://drive.google.com/file/d/REPORTE/view";

        ResultadoReporteLoteVida resultado =
                new ResultadoReporteLoteVida(
                        "MAPFRE",
                        LocalDate.of(2097, 1, 1),
                        LocalDate.of(2097, 1, 15),
                        3,
                        "CARPETA_FINAL",
                        urlCarpetaFinal,
                        "REPORTE",
                        "Reporte_MAPFRE_0101_1501.xlsx",
                        urlReporte
                );

        assertEquals(
                "CARPETA_FINAL",
                resultado.getCarpetaDriveId()
        );

        assertEquals(
                urlCarpetaFinal,
                resultado.getUrlCarpetaFinal()
        );

        assertEquals(
                urlReporte,
                resultado.getUrlReporteDrive()
        );
    }
}