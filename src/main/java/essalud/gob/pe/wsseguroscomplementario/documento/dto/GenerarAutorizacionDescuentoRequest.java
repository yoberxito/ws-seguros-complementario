package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.math.BigDecimal;

public class GenerarAutorizacionDescuentoRequest {

    private String registroInternoProceso;

    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String nombresApellidosTrabajador;

    private String codigoPlanilla;
    private String decretoLegislativo;
    private String tipoAsegurado;

    private String rucEmpleador;
    private String razonSocialEmpleador;

    private BigDecimal montoPrimaMensual;

    private String correoElectronico;
    private String celular;

    private String generadoPor;
    private String canalGeneracion;

    public GenerarAutorizacionDescuentoRequest() {
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String registroInternoProceso) {
        this.registroInternoProceso = registroInternoProceso;
    }

    public String getTipoDocumentoTrabajador() {
        return tipoDocumentoTrabajador;
    }

    public void setTipoDocumentoTrabajador(String tipoDocumentoTrabajador) {
        this.tipoDocumentoTrabajador = tipoDocumentoTrabajador;
    }

    public String getNumeroDocumentoTrabajador() {
        return numeroDocumentoTrabajador;
    }

    public void setNumeroDocumentoTrabajador(String numeroDocumentoTrabajador) {
        this.numeroDocumentoTrabajador = numeroDocumentoTrabajador;
    }

    public String getNombresApellidosTrabajador() {
        return nombresApellidosTrabajador;
    }

    public void setNombresApellidosTrabajador(String nombresApellidosTrabajador) {
        this.nombresApellidosTrabajador = nombresApellidosTrabajador;
    }

    public String getCodigoPlanilla() {
        return codigoPlanilla;
    }

    public void setCodigoPlanilla(String codigoPlanilla) {
        this.codigoPlanilla = codigoPlanilla;
    }

    public String getDecretoLegislativo() {
        return decretoLegislativo;
    }

    public void setDecretoLegislativo(String decretoLegislativo) {
        this.decretoLegislativo = decretoLegislativo;
    }

    public String getTipoAsegurado() {
        return tipoAsegurado;
    }

    public void setTipoAsegurado(String tipoAsegurado) {
        this.tipoAsegurado = tipoAsegurado;
    }

    public String getRucEmpleador() {
        return rucEmpleador;
    }

    public void setRucEmpleador(String rucEmpleador) {
        this.rucEmpleador = rucEmpleador;
    }

    public String getRazonSocialEmpleador() {
        return razonSocialEmpleador;
    }

    public void setRazonSocialEmpleador(String razonSocialEmpleador) {
        this.razonSocialEmpleador = razonSocialEmpleador;
    }

    public BigDecimal getMontoPrimaMensual() {
        return montoPrimaMensual;
    }

    public void setMontoPrimaMensual(BigDecimal montoPrimaMensual) {
        this.montoPrimaMensual = montoPrimaMensual;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getGeneradoPor() {
        return generadoPor;
    }

    public void setGeneradoPor(String generadoPor) {
        this.generadoPor = generadoPor;
    }

    public String getCanalGeneracion() {
        return canalGeneracion;
    }

    public void setCanalGeneracion(String canalGeneracion) {
        this.canalGeneracion = canalGeneracion;
    }
}