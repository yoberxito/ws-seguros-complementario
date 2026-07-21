package essalud.gob.pe.wsseguroscomplementario.documento.dto;

public class RegistrarDocumentoGeneradoRequest {

    private String registroInternoProceso;
    private String tipoDocumento;
    private String versionFormato;
    private String idDocumentoGenerado;
    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String nombresApellidosTrabajador;

    private int cantidadBeneficiariosRegistrados;

    private String generadoPor;
    private String canalGeneracion;

    public RegistrarDocumentoGeneradoRequest() {
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

    public String getVersionFormato() {
        return versionFormato;
    }

    public void setVersionFormato(String versionFormato) {
        this.versionFormato = versionFormato;
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

    public int getCantidadBeneficiariosRegistrados() {
        return cantidadBeneficiariosRegistrados;
    }

    public void setCantidadBeneficiariosRegistrados(int cantidadBeneficiariosRegistrados) {
        this.cantidadBeneficiariosRegistrados = cantidadBeneficiariosRegistrados;
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

    public String getIdDocumentoGenerado() {
        return idDocumentoGenerado;
    }

    public void setIdDocumentoGenerado(String idDocumentoGenerado) {
        this.idDocumentoGenerado = idDocumentoGenerado;
    }
}