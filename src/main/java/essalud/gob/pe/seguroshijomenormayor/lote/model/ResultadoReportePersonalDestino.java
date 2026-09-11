package essalud.gob.pe.seguroshijomenormayor.lote.model;

/*
 * Resultado del Excel PERSONAL correspondiente
 * a un unico destino institucional.
 */
public class ResultadoReportePersonalDestino {

    private final DestinoPersonalVida destino;

    private final String carpetaPeriodoId;

    private final String urlCarpetaPeriodo;

    private final int cantidadDocumentos;

    private final String reporteDriveId;

    private final String nombreReporte;

    private final String urlReporteDrive;

    public ResultadoReportePersonalDestino(
            DestinoPersonalVida destino,
            String carpetaPeriodoId,
            String urlCarpetaPeriodo,
            int cantidadDocumentos,
            String reporteDriveId,
            String nombreReporte,
            String urlReporteDrive
    ) {

        if (destino == null) {
            throw new IllegalArgumentException(
                    "El destino PERSONAL es obligatorio."
            );
        }

        if (cantidadDocumentos <= 0) {
            throw new IllegalArgumentException(
                    "El reporte PERSONAL debe contener al menos un documento."
            );
        }

        this.destino =
                destino;

        this.carpetaPeriodoId =
                requerir(
                        carpetaPeriodoId,
                        "El ID de la carpeta del periodo es obligatorio."
                );

        this.urlCarpetaPeriodo =
                requerir(
                        urlCarpetaPeriodo,
                        "La URL de la carpeta del periodo es obligatoria."
                );

        this.cantidadDocumentos =
                cantidadDocumentos;

        this.reporteDriveId =
                requerir(
                        reporteDriveId,
                        "El ID Drive del reporte es obligatorio."
                );

        this.nombreReporte =
                requerir(
                        nombreReporte,
                        "El nombre del reporte es obligatorio."
                );

        this.urlReporteDrive =
                requerir(
                        urlReporteDrive,
                        "La URL Drive del reporte es obligatoria."
                );
    }

    public DestinoPersonalVida getDestino() {
        return destino;
    }

    public String getCarpetaPeriodoId() {
        return carpetaPeriodoId;
    }

    public String getUrlCarpetaPeriodo() {
        return urlCarpetaPeriodo;
    }

    public int getCantidadDocumentos() {
        return cantidadDocumentos;
    }

    public String getReporteDriveId() {
        return reporteDriveId;
    }

    public String getNombreReporte() {
        return nombreReporte;
    }

    public String getUrlReporteDrive() {
        return urlReporteDrive;
    }

    private static String requerir(
            String valor,
            String mensaje
    ) {

        if (
                valor == null
                        || valor.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }

        return valor.trim();
    }
}