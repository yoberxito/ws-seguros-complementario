package essalud.gob.pe.seguroshijomenormayor.lote.model;

import java.time.LocalDate;

public class ResultadoReporteLoteVida {

    private final String destinatario;

    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    private final int cantidadDocumentos;

    private final String carpetaDriveId;
    private final String urlCarpetaFinal;

    private final String reporteDriveId;
    private final String nombreReporte;
    private final String urlReporteDrive;

    public ResultadoReporteLoteVida(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            int cantidadDocumentos,
            String carpetaDriveId,
            String urlCarpetaFinal,
            String reporteDriveId,
            String nombreReporte,
            String urlReporteDrive
    ) {

        this.destinatario =
                destinatario;

        this.fechaInicio =
                fechaInicio;

        this.fechaFin =
                fechaFin;

        this.cantidadDocumentos =
                cantidadDocumentos;

        this.carpetaDriveId =
                carpetaDriveId;

        this.urlCarpetaFinal =
                urlCarpetaFinal;

        this.reporteDriveId =
                reporteDriveId;

        this.nombreReporte =
                nombreReporte;

        this.urlReporteDrive =
                urlReporteDrive;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public int getCantidadDocumentos() {
        return cantidadDocumentos;
    }

    public String getCarpetaDriveId() {
        return carpetaDriveId;
    }

    public String getUrlCarpetaFinal() {
        return urlCarpetaFinal;
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
}