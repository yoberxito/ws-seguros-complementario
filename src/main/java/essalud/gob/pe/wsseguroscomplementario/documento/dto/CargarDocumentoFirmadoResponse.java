package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CargarDocumentoFirmadoResponse {

    private boolean cargado;
    private String mensajeCarga;

    private String idDocumentoCargado;
    private String registroInternoProceso;
    private String tipoDocumento;

    private String nombreArchivoOriginal;
    private long tamanioBytes;
    private int numeroPaginas;
    private String hashSha256ArchivoCargado;

    private String estadoValidacionDocumental;
    private String idRechazoDocumental;
    private boolean permiteNuevaCarga;

    private LocalDateTime fechaHoraCarga;
    private String ipOrigen;
    private String datosSesionDispositivo;

    private boolean validacionTecnicaPdf;
    private List<String> observaciones = new ArrayList<>();

    public CargarDocumentoFirmadoResponse() {
    }

    public boolean isCargado() {
        return cargado;
    }

    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    public String getMensajeCarga() {
        return mensajeCarga;
    }

    public void setMensajeCarga(String mensajeCarga) {
        this.mensajeCarga = mensajeCarga;
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

    public String getEstadoValidacionDocumental() {
        return estadoValidacionDocumental;
    }

    public void setEstadoValidacionDocumental(String estadoValidacionDocumental) {
        this.estadoValidacionDocumental = estadoValidacionDocumental;
    }

    public String getIdRechazoDocumental() {
        return idRechazoDocumental;
    }

    public void setIdRechazoDocumental(String idRechazoDocumental) {
        this.idRechazoDocumental = idRechazoDocumental;
    }

    public boolean isPermiteNuevaCarga() {
        return permiteNuevaCarga;
    }

    public void setPermiteNuevaCarga(boolean permiteNuevaCarga) {
        this.permiteNuevaCarga = permiteNuevaCarga;
    }
}