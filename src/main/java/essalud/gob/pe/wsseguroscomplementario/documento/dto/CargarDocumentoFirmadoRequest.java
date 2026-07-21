package essalud.gob.pe.wsseguroscomplementario.documento.dto;

public class CargarDocumentoFirmadoRequest {

    private String registroInternoProceso;
    private String tipoDocumento;

    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String nombresApellidosTrabajador;

    private String datosSesionDispositivo;

    public CargarDocumentoFirmadoRequest() {
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String registroInternoProceso) {
        this.registroInternoProceso = registroInternoProceso;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
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

    public String getDatosSesionDispositivo() {
        return datosSesionDispositivo;
    }

    public void setDatosSesionDispositivo(String datosSesionDispositivo) {
        this.datosSesionDispositivo = datosSesionDispositivo;
    }
}