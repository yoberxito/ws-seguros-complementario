package essalud.gob.pe.wsseguroscomplementario.documento.dto;

public class GenerarFormulario6012Response {

    private boolean generado;
    private String mensajeGeneracion;

    private String nombreArchivo;
    private String contentType;
    private String archivoBase64;

    private int cantidadBeneficiariosRegistrados;
    private int numeroPaginasGeneradas;

    private RegistrarDocumentoGeneradoResponse metadataDocumentoGenerado;

    public GenerarFormulario6012Response() {
    }

    public boolean isGenerado() {
        return generado;
    }

    public void setGenerado(boolean generado) {
        this.generado = generado;
    }

    public String getMensajeGeneracion() {
        return mensajeGeneracion;
    }

    public void setMensajeGeneracion(String mensajeGeneracion) {
        this.mensajeGeneracion = mensajeGeneracion;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getArchivoBase64() {
        return archivoBase64;
    }

    public void setArchivoBase64(String archivoBase64) {
        this.archivoBase64 = archivoBase64;
    }

    public int getCantidadBeneficiariosRegistrados() {
        return cantidadBeneficiariosRegistrados;
    }

    public void setCantidadBeneficiariosRegistrados(int cantidadBeneficiariosRegistrados) {
        this.cantidadBeneficiariosRegistrados = cantidadBeneficiariosRegistrados;
    }

    public int getNumeroPaginasGeneradas() {
        return numeroPaginasGeneradas;
    }

    public void setNumeroPaginasGeneradas(int numeroPaginasGeneradas) {
        this.numeroPaginasGeneradas = numeroPaginasGeneradas;
    }

    public RegistrarDocumentoGeneradoResponse getMetadataDocumentoGenerado() {
        return metadataDocumentoGenerado;
    }

    public void setMetadataDocumentoGenerado(RegistrarDocumentoGeneradoResponse metadataDocumentoGenerado) {
        this.metadataDocumentoGenerado = metadataDocumentoGenerado;
    }
}