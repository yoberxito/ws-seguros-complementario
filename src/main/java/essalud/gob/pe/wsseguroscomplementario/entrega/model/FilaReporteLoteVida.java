package essalud.gob.pe.wsseguroscomplementario.entrega.model;

import java.time.LocalDate;

/*
 * Fila definitiva del reporte quincenal +Vida.
 *
 * Esta clase ya representa datos listos para Excel.
 *
 * El origen de los datos puede ser:
 *
 * - Oracle +Vida para datos del titular;
 * - Google Drive para el enlace al PDF.
 *
 * fechaAfiliacion se recibe ya resuelta por la
 * orquestación del Job. El generador Excel no decide
 * qué fecha del proceso representa la afiliación.
 */
public class FilaReporteLoteVida {

    private String numeroDocumentoTitular;
    private String nombresApellidosTitular;
    private LocalDate fechaAfiliacion;
    private Integer cantidadBeneficiarios;
    private String registroInternoProceso;
    private String urlPdf;

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public void setNumeroDocumentoTitular(
            String numeroDocumentoTitular
    ) {
        this.numeroDocumentoTitular =
                numeroDocumentoTitular;
    }

    public String getNombresApellidosTitular() {
        return nombresApellidosTitular;
    }

    public void setNombresApellidosTitular(
            String nombresApellidosTitular
    ) {
        this.nombresApellidosTitular =
                nombresApellidosTitular;
    }

    public LocalDate getFechaAfiliacion() {
        return fechaAfiliacion;
    }

    public void setFechaAfiliacion(
            LocalDate fechaAfiliacion
    ) {
        this.fechaAfiliacion =
                fechaAfiliacion;
    }

    public Integer getCantidadBeneficiarios() {
        return cantidadBeneficiarios;
    }

    public void setCantidadBeneficiarios(
            Integer cantidadBeneficiarios
    ) {
        this.cantidadBeneficiarios =
                cantidadBeneficiarios;
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(
            String registroInternoProceso
    ) {
        this.registroInternoProceso =
                registroInternoProceso;
    }

    public String getUrlPdf() {
        return urlPdf;
    }

    public void setUrlPdf(
            String urlPdf
    ) {
        this.urlPdf = urlPdf;
    }
}