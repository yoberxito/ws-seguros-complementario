package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RechazoDocumentoResponse {

    private String idRechazoDocumental;
    private String registroInternoProceso;
    private String tipoDocumento;

    private String nombreArchivoOriginal;
    private long tamanioBytes;
    private int numeroPaginas;

    private String estadoValidacionDocumental;
    private boolean permiteNuevaCarga;

    private List<String> motivosRechazo = new ArrayList<>();

    private LocalDateTime fechaHoraRechazo;
    private String ipOrigen;
    private String datosSesionDispositivo;

    public RechazoDocumentoResponse() {
    }

    public String getIdRechazoDocumental() {
        return idRechazoDocumental;
    }

    public void setIdRechazoDocumental(String idRechazoDocumental) {
        this.idRechazoDocumental = idRechazoDocumental;
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

    public String getEstadoValidacionDocumental() {
        return estadoValidacionDocumental;
    }

    public void setEstadoValidacionDocumental(String estadoValidacionDocumental) {
        this.estadoValidacionDocumental = estadoValidacionDocumental;
    }

    public boolean isPermiteNuevaCarga() {
        return permiteNuevaCarga;
    }

    public void setPermiteNuevaCarga(boolean permiteNuevaCarga) {
        this.permiteNuevaCarga = permiteNuevaCarga;
    }

    public List<String> getMotivosRechazo() {
        return motivosRechazo;
    }

    public void setMotivosRechazo(List<String> motivosRechazo) {
        this.motivosRechazo = motivosRechazo;
    }

    public LocalDateTime getFechaHoraRechazo() {
        return fechaHoraRechazo;
    }

    public void setFechaHoraRechazo(LocalDateTime fechaHoraRechazo) {
        this.fechaHoraRechazo = fechaHoraRechazo;
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
}