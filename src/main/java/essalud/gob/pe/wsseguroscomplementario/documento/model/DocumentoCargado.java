package essalud.gob.pe.wsseguroscomplementario.documento.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DocumentoCargado {

    private String idDocumentoCargado;
    private String registroInternoProceso;

    private String tipoDocumento;
    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;
    private String nombresApellidosTrabajador;

    private String nombreArchivoOriginal;
    private long tamanioBytes;
    private int numeroPaginas;
    private String hashSha256ArchivoCargado;

    private LocalDateTime fechaHoraCarga;
    private String ipOrigen;
    private String datosSesionDispositivo;

    private boolean validacionTecnicaPdf;
    private List<String> observaciones = new ArrayList<>();

    private byte[] contenidoArchivo;

    public DocumentoCargado() {
    }

    public String getIdDocumentoCargado() {
        return idDocumentoCargado;
    }

    public void setIdDocumentoCargado(String idDocumentoCargado) {
        this.idDocumentoCargado = idDocumentoCargado;
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

    public String getNombreArchivoOriginal() {
        return nombreArchivoOriginal;
    }

    public void setNombreArchivoOriginal(String nombreArchivoOriginal) {
        this.nombreArchivoOriginal = nombreArchivoOriginal;
    }

    public long getTamanioBytes() {
        return tamanioBytes;
    }

    public void setTamanioBytes(long tamanioBytes) {
        this.tamanioBytes = tamanioBytes;
    }

    public int getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(int numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public String getHashSha256ArchivoCargado() {
        return hashSha256ArchivoCargado;
    }

    public void setHashSha256ArchivoCargado(String hashSha256ArchivoCargado) {
        this.hashSha256ArchivoCargado = hashSha256ArchivoCargado;
    }

    public LocalDateTime getFechaHoraCarga() {
        return fechaHoraCarga;
    }

    public void setFechaHoraCarga(LocalDateTime fechaHoraCarga) {
        this.fechaHoraCarga = fechaHoraCarga;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public String getDatosSesionDispositivo() {
        return datosSesionDispositivo;
    }

    public void setDatosSesionDispositivo(String datosSesionDispositivo) {
        this.datosSesionDispositivo = datosSesionDispositivo;
    }

    public boolean isValidacionTecnicaPdf() {
        return validacionTecnicaPdf;
    }

    public void setValidacionTecnicaPdf(boolean validacionTecnicaPdf) {
        this.validacionTecnicaPdf = validacionTecnicaPdf;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }

    public byte[] getContenidoArchivo() {
        return contenidoArchivo;
    }

    public void setContenidoArchivo(byte[] contenidoArchivo) {
        this.contenidoArchivo = contenidoArchivo;
    }
}