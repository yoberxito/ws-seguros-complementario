package essalud.gob.pe.seguroshijomenormayor.lote.model;

import java.time.LocalDateTime;

public class ReporteLoteVidaItem {

    private String tipoDocumentoTitular;
    private String numeroDocumentoTitular;

    private String apellidoPaternoTitular;
    private String apellidoMaternoTitular;
    private String primerNombreTitular;
    private String segundoNombreTitular;

    /*
     * Fecha en que se creó el proceso +Vida.
     * Se conserva como dato técnico.
     *
     * NO representa la fecha de afiliación
     * mostrada en el reporte.
     */
    private LocalDateTime fechaRegistroProceso;

    /*
     * Regla funcional del reporte:
     *
     * fechaAfiliacion =
     * FECHA_PUBLICACION de AUTORIZACION_DESCUENTO
     * correspondiente al mismo ID_SECOMASVIDA.
     */
    private LocalDateTime fechaAfiliacion;

    private int cantidadBeneficiarios;

    private String registroInternoProceso;
    private String tipoDocumentoLogico;

    private String idDocumentoPublicado;
    private String nombreArchivoFinal;

    private LocalDateTime fechaPublicacionDocumento;

    public String getTipoDocumentoTitular() {
        return tipoDocumentoTitular;
    }

    public void setTipoDocumentoTitular(String valor) {
        this.tipoDocumentoTitular = valor;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public void setNumeroDocumentoTitular(String valor) {
        this.numeroDocumentoTitular = valor;
    }

    public String getApellidoPaternoTitular() {
        return apellidoPaternoTitular;
    }

    public void setApellidoPaternoTitular(String valor) {
        this.apellidoPaternoTitular = valor;
    }

    public String getApellidoMaternoTitular() {
        return apellidoMaternoTitular;
    }

    public void setApellidoMaternoTitular(String valor) {
        this.apellidoMaternoTitular = valor;
    }

    public String getPrimerNombreTitular() {
        return primerNombreTitular;
    }

    public void setPrimerNombreTitular(String valor) {
        this.primerNombreTitular = valor;
    }

    public String getSegundoNombreTitular() {
        return segundoNombreTitular;
    }

    public void setSegundoNombreTitular(String valor) {
        this.segundoNombreTitular = valor;
    }

    public LocalDateTime getFechaRegistroProceso() {
        return fechaRegistroProceso;
    }

    public void setFechaRegistroProceso(LocalDateTime valor) {
        this.fechaRegistroProceso = valor;
    }

    public LocalDateTime getFechaAfiliacion() {
        return fechaAfiliacion;
    }

    public void setFechaAfiliacion(LocalDateTime valor) {
        this.fechaAfiliacion = valor;
    }

    public int getCantidadBeneficiarios() {
        return cantidadBeneficiarios;
    }

    public void setCantidadBeneficiarios(int valor) {
        this.cantidadBeneficiarios = valor;
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String valor) {
        this.registroInternoProceso = valor;
    }

    public String getTipoDocumentoLogico() {
        return tipoDocumentoLogico;
    }

    public void setTipoDocumentoLogico(String valor) {
        this.tipoDocumentoLogico = valor;
    }

    public String getIdDocumentoPublicado() {
        return idDocumentoPublicado;
    }

    public void setIdDocumentoPublicado(String valor) {
        this.idDocumentoPublicado = valor;
    }

    public String getNombreArchivoFinal() {
        return nombreArchivoFinal;
    }

    public void setNombreArchivoFinal(String valor) {
        this.nombreArchivoFinal = valor;
    }

    public LocalDateTime getFechaPublicacionDocumento() {
        return fechaPublicacionDocumento;
    }

    public void setFechaPublicacionDocumento(LocalDateTime valor) {
        this.fechaPublicacionDocumento = valor;
    }

    public String getNombresApellidosTitular() {

        StringBuilder valor =
                new StringBuilder();

        agregar(
                valor,
                primerNombreTitular
        );

        agregar(
                valor,
                segundoNombreTitular
        );

        agregar(
                valor,
                apellidoPaternoTitular
        );

        agregar(
                valor,
                apellidoMaternoTitular
        );

        return valor.toString();
    }

    private void agregar(
            StringBuilder destino,
            String valor
    ) {

        if (
                valor == null
                        || valor.trim().isEmpty()
        ) {
            return;
        }

        if (destino.length() > 0) {
            destino.append(' ');
        }

        destino.append(
                valor.trim()
        );
    }
}