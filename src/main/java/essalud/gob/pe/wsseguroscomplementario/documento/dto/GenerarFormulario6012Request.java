package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class GenerarFormulario6012Request {

    private String registroInternoProceso;

    private String tipoDocumentoTitular;
    private String numeroDocumentoTitular;
    private String nombresApellidosTitular;
    private String correoElectronico;

    private String tipoAsegurado;
    private String convenioCgbvp;
    private String rucEmpleador;

    private String tipoDocumentoConyuge;
    private String numeroDocumentoConyuge;
    private String nombresApellidosConyuge;

    private String notificacionCorreo;

    private String generadoPor;
    private String canalGeneracion;

    private List<Beneficiario6012Request> beneficiarios = new ArrayList<>();

    public GenerarFormulario6012Request() {
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String registroInternoProceso) {
        this.registroInternoProceso = registroInternoProceso;
    }

    public String getTipoDocumentoTitular() {
        return tipoDocumentoTitular;
    }

    public void setTipoDocumentoTitular(String tipoDocumentoTitular) {
        this.tipoDocumentoTitular = tipoDocumentoTitular;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public void setNumeroDocumentoTitular(String numeroDocumentoTitular) {
        this.numeroDocumentoTitular = numeroDocumentoTitular;
    }

    public String getNombresApellidosTitular() {
        return nombresApellidosTitular;
    }

    public void setNombresApellidosTitular(String nombresApellidosTitular) {
        this.nombresApellidosTitular = nombresApellidosTitular;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getTipoAsegurado() {
        return tipoAsegurado;
    }

    public void setTipoAsegurado(String tipoAsegurado) {
        this.tipoAsegurado = tipoAsegurado;
    }

    public String getConvenioCgbvp() {
        return convenioCgbvp;
    }

    public void setConvenioCgbvp(String convenioCgbvp) {
        this.convenioCgbvp = convenioCgbvp;
    }

    public String getRucEmpleador() {
        return rucEmpleador;
    }

    public void setRucEmpleador(String rucEmpleador) {
        this.rucEmpleador = rucEmpleador;
    }

    public String getTipoDocumentoConyuge() {
        return tipoDocumentoConyuge;
    }

    public void setTipoDocumentoConyuge(String tipoDocumentoConyuge) {
        this.tipoDocumentoConyuge = tipoDocumentoConyuge;
    }

    public String getNumeroDocumentoConyuge() {
        return numeroDocumentoConyuge;
    }

    public void setNumeroDocumentoConyuge(String numeroDocumentoConyuge) {
        this.numeroDocumentoConyuge = numeroDocumentoConyuge;
    }

    public String getNombresApellidosConyuge() {
        return nombresApellidosConyuge;
    }

    public void setNombresApellidosConyuge(String nombresApellidosConyuge) {
        this.nombresApellidosConyuge = nombresApellidosConyuge;
    }

    public String getNotificacionCorreo() {
        return notificacionCorreo;
    }

    public void setNotificacionCorreo(String notificacionCorreo) {
        this.notificacionCorreo = notificacionCorreo;
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

    public List<Beneficiario6012Request> getBeneficiarios() {
        return beneficiarios;
    }

    public void setBeneficiarios(List<Beneficiario6012Request> beneficiarios) {
        this.beneficiarios = beneficiarios;
    }
}