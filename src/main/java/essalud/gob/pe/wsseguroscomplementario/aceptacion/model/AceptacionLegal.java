package essalud.gob.pe.wsseguroscomplementario.aceptacion.model;

import java.time.LocalDateTime;

public class AceptacionLegal {

    private String idAceptacion;
    private String registroInternoProceso;

    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String nombresApellidosTrabajador;

    private boolean aceptaDeclaracionJurada;
    private LocalDateTime fechaHoraAceptacionDeclaracionJurada;

    private boolean aceptaTratamientoDatosPersonales;
    private LocalDateTime fechaHoraAceptacionTratamientoDatosPersonales;

    private String ipOrigen;
    private String canalAcceso;
    private String datosSesionDispositivo;

    private String versionTextoDeclaracionJurada;
    private String versionTextoTratamientoDatos;
    private String referenciaPoliticaPrivacidad;

    public AceptacionLegal() {
    }

    public String getIdAceptacion() {
        return idAceptacion;
    }

    public void setIdAceptacion(String idAceptacion) {
        this.idAceptacion = idAceptacion;
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

    public boolean isAceptaDeclaracionJurada() {
        return aceptaDeclaracionJurada;
    }

    public void setAceptaDeclaracionJurada(boolean aceptaDeclaracionJurada) {
        this.aceptaDeclaracionJurada = aceptaDeclaracionJurada;
    }

    public LocalDateTime getFechaHoraAceptacionDeclaracionJurada() {
        return fechaHoraAceptacionDeclaracionJurada;
    }

    public void setFechaHoraAceptacionDeclaracionJurada(LocalDateTime fechaHoraAceptacionDeclaracionJurada) {
        this.fechaHoraAceptacionDeclaracionJurada = fechaHoraAceptacionDeclaracionJurada;
    }

    public boolean isAceptaTratamientoDatosPersonales() {
        return aceptaTratamientoDatosPersonales;
    }

    public void setAceptaTratamientoDatosPersonales(boolean aceptaTratamientoDatosPersonales) {
        this.aceptaTratamientoDatosPersonales = aceptaTratamientoDatosPersonales;
    }

    public LocalDateTime getFechaHoraAceptacionTratamientoDatosPersonales() {
        return fechaHoraAceptacionTratamientoDatosPersonales;
    }

    public void setFechaHoraAceptacionTratamientoDatosPersonales(LocalDateTime fechaHoraAceptacionTratamientoDatosPersonales) {
        this.fechaHoraAceptacionTratamientoDatosPersonales = fechaHoraAceptacionTratamientoDatosPersonales;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public String getCanalAcceso() {
        return canalAcceso;
    }

    public void setCanalAcceso(String canalAcceso) {
        this.canalAcceso = canalAcceso;
    }

    public String getDatosSesionDispositivo() {
        return datosSesionDispositivo;
    }

    public void setDatosSesionDispositivo(String datosSesionDispositivo) {
        this.datosSesionDispositivo = datosSesionDispositivo;
    }

    public String getVersionTextoDeclaracionJurada() {
        return versionTextoDeclaracionJurada;
    }

    public void setVersionTextoDeclaracionJurada(String versionTextoDeclaracionJurada) {
        this.versionTextoDeclaracionJurada = versionTextoDeclaracionJurada;
    }

    public String getVersionTextoTratamientoDatos() {
        return versionTextoTratamientoDatos;
    }

    public void setVersionTextoTratamientoDatos(String versionTextoTratamientoDatos) {
        this.versionTextoTratamientoDatos = versionTextoTratamientoDatos;
    }

    public String getReferenciaPoliticaPrivacidad() {
        return referenciaPoliticaPrivacidad;
    }

    public void setReferenciaPoliticaPrivacidad(String referenciaPoliticaPrivacidad) {
        this.referenciaPoliticaPrivacidad = referenciaPoliticaPrivacidad;
    }
}