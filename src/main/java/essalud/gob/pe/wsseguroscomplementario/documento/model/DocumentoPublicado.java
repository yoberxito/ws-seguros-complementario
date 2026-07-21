package essalud.gob.pe.wsseguroscomplementario.documento.model;

import java.time.LocalDateTime;

public class DocumentoPublicado {

    private String idDocumentoPublicado;
    private String idDocumentoSellado;

    private String registroInternoProceso;
    private String tipoDocumento;
    private String numeroDocumentoTrabajador;

    private String nombreArchivo;
    private String contentType;
    private byte[] contenidoArchivo;

    private String hashSha256DocumentoPublicado;
    private LocalDateTime fechaHoraPublicacion;

    private String canalPublicacion;
    private String publicadoPor;
    private String estadoPublicacionDocumental;

    private boolean disponibleParaUsuario;

    public DocumentoPublicado() {
    }

    public String getIdDocumentoPublicado() {
        return idDocumentoPublicado;
    }

    public void setIdDocumentoPublicado(String idDocumentoPublicado) {
        this.idDocumentoPublicado = idDocumentoPublicado;
    }

    public String getIdDocumentoSellado() {
        return idDocumentoSellado;
    }

    public void setIdDocumentoSellado(String idDocumentoSellado) {
        this.idDocumentoSellado = idDocumentoSellado;
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

    public String getNumeroDocumentoTrabajador() {
        return numeroDocumentoTrabajador;
    }

    public void setNumeroDocumentoTrabajador(String numeroDocumentoTrabajador) {
        this.numeroDocumentoTrabajador = numeroDocumentoTrabajador;
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

    public byte[] getContenidoArchivo() {
        return contenidoArchivo;
    }

    public void setContenidoArchivo(byte[] contenidoArchivo) {
        this.contenidoArchivo = contenidoArchivo;
    }

    public String getHashSha256DocumentoPublicado() {
        return hashSha256DocumentoPublicado;
    }

    public void setHashSha256DocumentoPublicado(String hashSha256DocumentoPublicado) {
        this.hashSha256DocumentoPublicado = hashSha256DocumentoPublicado;
    }

    public LocalDateTime getFechaHoraPublicacion() {
        return fechaHoraPublicacion;
    }

    public void setFechaHoraPublicacion(LocalDateTime fechaHoraPublicacion) {
        this.fechaHoraPublicacion = fechaHoraPublicacion;
    }

    public String getCanalPublicacion() {
        return canalPublicacion;
    }

    public void setCanalPublicacion(String canalPublicacion) {
        this.canalPublicacion = canalPublicacion;
    }

    public String getPublicadoPor() {
        return publicadoPor;
    }

    public void setPublicadoPor(String publicadoPor) {
        this.publicadoPor = publicadoPor;
    }

    public String getEstadoPublicacionDocumental() {
        return estadoPublicacionDocumental;
    }

    public void setEstadoPublicacionDocumental(String estadoPublicacionDocumental) {
        this.estadoPublicacionDocumental = estadoPublicacionDocumental;
    }

    public boolean isDisponibleParaUsuario() {
        return disponibleParaUsuario;
    }

    public void setDisponibleParaUsuario(boolean disponibleParaUsuario) {
        this.disponibleParaUsuario = disponibleParaUsuario;
    }
}