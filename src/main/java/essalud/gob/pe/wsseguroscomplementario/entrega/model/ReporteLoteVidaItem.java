package essalud.gob.pe.wsseguroscomplementario.entrega.model;

import java.time.LocalDateTime;

/*
 * Datos persistidos en Oracle necesarios para construir
 * una fila del reporte quincenal +Vida.
 *
 * Importante:
 *
 * - la URL del PDF no se obtiene desde Oracle;
 * - la URL corresponde al archivo real almacenado en Drive;
 * - la pertenencia a una quincena la determina la carpeta Drive;
 * - fechaRegistroProceso conserva la semántica real de
 *   TEMP_SECOMASVIDA.FECHA_REGISTRO.
 */
public class ReporteLoteVidaItem {

    private String tipoDocumentoTitular;
    private String numeroDocumentoTitular;

    private String apellidoPaternoTitular;
    private String apellidoMaternoTitular;
    private String primerNombreTitular;
    private String segundoNombreTitular;

    private LocalDateTime fechaRegistroProceso;

    private int cantidadBeneficiarios;

    private String registroInternoProceso;

    private String tipoDocumentoLogico;

    private String idDocumentoPublicado;
    private String nombreArchivoFinal;

    private LocalDateTime fechaPublicacionDocumento;

    public String getTipoDocumentoTitular() {
        return tipoDocumentoTitular;
    }

    public void setTipoDocumentoTitular(
            String tipoDocumentoTitular
    ) {
        this.tipoDocumentoTitular =
                tipoDocumentoTitular;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public void setNumeroDocumentoTitular(
            String numeroDocumentoTitular
    ) {
        this.numeroDocumentoTitular =
                numeroDocumentoTitular;
    }

    public String getApellidoPaternoTitular() {
        return apellidoPaternoTitular;
    }

    public void setApellidoPaternoTitular(
            String apellidoPaternoTitular
    ) {
        this.apellidoPaternoTitular =
                apellidoPaternoTitular;
    }

    public String getApellidoMaternoTitular() {
        return apellidoMaternoTitular;
    }

    public void setApellidoMaternoTitular(
            String apellidoMaternoTitular
    ) {
        this.apellidoMaternoTitular =
                apellidoMaternoTitular;
    }

    public String getPrimerNombreTitular() {
        return primerNombreTitular;
    }

    public void setPrimerNombreTitular(
            String primerNombreTitular
    ) {
        this.primerNombreTitular =
                primerNombreTitular;
    }

    public String getSegundoNombreTitular() {
        return segundoNombreTitular;
    }

    public void setSegundoNombreTitular(
            String segundoNombreTitular
    ) {
        this.segundoNombreTitular =
                segundoNombreTitular;
    }

    public LocalDateTime getFechaRegistroProceso() {
        return fechaRegistroProceso;
    }

    public void setFechaRegistroProceso(
            LocalDateTime fechaRegistroProceso
    ) {
        this.fechaRegistroProceso =
                fechaRegistroProceso;
    }

    public int getCantidadBeneficiarios() {
        return cantidadBeneficiarios;
    }

    public void setCantidadBeneficiarios(
            int cantidadBeneficiarios
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

    public String getTipoDocumentoLogico() {
        return tipoDocumentoLogico;
    }

    public void setTipoDocumentoLogico(
            String tipoDocumentoLogico
    ) {
        this.tipoDocumentoLogico =
                tipoDocumentoLogico;
    }

    public String getIdDocumentoPublicado() {
        return idDocumentoPublicado;
    }

    public void setIdDocumentoPublicado(
            String idDocumentoPublicado
    ) {
        this.idDocumentoPublicado =
                idDocumentoPublicado;
    }

    public String getNombreArchivoFinal() {
        return nombreArchivoFinal;
    }

    public void setNombreArchivoFinal(
            String nombreArchivoFinal
    ) {
        this.nombreArchivoFinal =
                nombreArchivoFinal;
    }

    public LocalDateTime getFechaPublicacionDocumento() {
        return fechaPublicacionDocumento;
    }

    public void setFechaPublicacionDocumento(
            LocalDateTime fechaPublicacionDocumento
    ) {
        this.fechaPublicacionDocumento =
                fechaPublicacionDocumento;
    }

    public String getNombresApellidosTitular() {

        StringBuilder valor =
                new StringBuilder();

        agregar(valor, primerNombreTitular);
        agregar(valor, segundoNombreTitular);
        agregar(valor, apellidoPaternoTitular);
        agregar(valor, apellidoMaternoTitular);

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

        destino.append(valor.trim());
    }
}