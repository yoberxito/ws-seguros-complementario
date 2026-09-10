package essalud.gob.pe.seguroshijomenormayor.lote.model;

/*
 * Resultado definitivo de la etapa Google Drive
 * correspondiente al cierre de un período +Vida.
 *
 * La implementación Drive debe ser idempotente:
 *
 * - si la carpeta final ya existe, debe reutilizarla;
 * - si algunos documentos ya fueron movidos, no debe duplicarlos;
 * - una reejecución debe devolver el mismo lote documental final.
 */
public class ResultadoCierreDrive {

    private final String idCarpetaFinal;
    private final String nombreCarpetaFinal;
    private final String urlCarpetaFinal;
    private final int cantidadDocumentos;

    public ResultadoCierreDrive(
            String idCarpetaFinal,
            String nombreCarpetaFinal,
            String urlCarpetaFinal,
            int cantidadDocumentos
    ) {
        this.idCarpetaFinal = idCarpetaFinal;
        this.nombreCarpetaFinal = nombreCarpetaFinal;
        this.urlCarpetaFinal = urlCarpetaFinal;
        this.cantidadDocumentos = cantidadDocumentos;
    }

    public String getIdCarpetaFinal() {
        return idCarpetaFinal;
    }

    public String getNombreCarpetaFinal() {
        return nombreCarpetaFinal;
    }

    public String getUrlCarpetaFinal() {
        return urlCarpetaFinal;
    }

    public int getCantidadDocumentos() {
        return cantidadDocumentos;
    }
}