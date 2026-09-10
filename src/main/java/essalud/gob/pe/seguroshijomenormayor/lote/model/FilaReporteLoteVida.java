package essalud.gob.pe.seguroshijomenormayor.lote.model;

import java.time.LocalDate;

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

    public void setNumeroDocumentoTitular(String valor) {
        this.numeroDocumentoTitular = valor;
    }

    public String getNombresApellidosTitular() {
        return nombresApellidosTitular;
    }

    public void setNombresApellidosTitular(String valor) {
        this.nombresApellidosTitular = valor;
    }

    public LocalDate getFechaAfiliacion() {
        return fechaAfiliacion;
    }

    public void setFechaAfiliacion(LocalDate valor) {
        this.fechaAfiliacion = valor;
    }

    public Integer getCantidadBeneficiarios() {
        return cantidadBeneficiarios;
    }

    public void setCantidadBeneficiarios(Integer valor) {
        this.cantidadBeneficiarios = valor;
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String valor) {
        this.registroInternoProceso = valor;
    }

    public String getUrlPdf() {
        return urlPdf;
    }

    public void setUrlPdf(String valor) {
        this.urlPdf = valor;
    }
}