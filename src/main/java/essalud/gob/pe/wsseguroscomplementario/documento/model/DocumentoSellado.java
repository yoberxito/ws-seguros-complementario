package essalud.gob.pe.wsseguroscomplementario.documento.model;

import java.time.LocalDateTime;

public class DocumentoSellado {

    private String idDocumentoSellado;
    private String registroInternoProceso;
    private String tipoDocumento;
    private String numeroDocumentoTrabajador;

    private String nombreArchivo;
    private String contentType;
    private byte[] contenidoArchivo;

    private int numeroPaginasSelladas;
    private String hashSha256DocumentoSellado;
    private LocalDateTime fechaHoraSellado;

    private String estadoDocumentoSellado;

    public DocumentoSellado() {
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

    public int getNumeroPaginasSelladas() {
        return numeroPaginasSelladas;
    }

    public void setNumeroPaginasSelladas(int numeroPaginasSelladas) {
        this.numeroPaginasSelladas = numeroPaginasSelladas;
    }

    public String getHashSha256DocumentoSellado() {
        return hashSha256DocumentoSellado;
    }

    public void setHashSha256DocumentoSellado(String hashSha256DocumentoSellado) {
        this.hashSha256DocumentoSellado = hashSha256DocumentoSellado;
    }

    public LocalDateTime getFechaHoraSellado() {
        return fechaHoraSellado;
    }

    public void setFechaHoraSellado(LocalDateTime fechaHoraSellado) {
        this.fechaHoraSellado = fechaHoraSellado;
    }

    public String getEstadoDocumentoSellado() {
        return estadoDocumentoSellado;
    }

    public void setEstadoDocumentoSellado(String estadoDocumentoSellado) {
        this.estadoDocumentoSellado = estadoDocumentoSellado;
    }
}