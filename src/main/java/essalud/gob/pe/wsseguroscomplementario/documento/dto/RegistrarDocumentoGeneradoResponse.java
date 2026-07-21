package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import essalud.gob.pe.wsseguroscomplementario.documento.model.PaginaDocumentoGenerado;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RegistrarDocumentoGeneradoResponse {

    private String idDocumentoGenerado;
    private String registroInternoProceso;

    private String tipoDocumento;
    private String versionFormato;

    private String nombreArchivoOriginal;
    private long tamanioBytes;
    private int numeroPaginasGeneradas;
    private String hashSha256DocumentoOriginal;

    private int cantidadBeneficiariosRegistrados;

    private String estadoDocumentoGenerado;
    private LocalDateTime fechaHoraGeneracion;

    private String generadoPor;
    private String canalGeneracion;

    private List<PaginaDocumentoGenerado> paginasEsperadas = new ArrayList<>();

    public RegistrarDocumentoGeneradoResponse() {
    }

    public String getIdDocumentoGenerado() {
        return idDocumentoGenerado;
    }

    public void setIdDocumentoGenerado(String idDocumentoGenerado) {
        this.idDocumentoGenerado = idDocumentoGenerado;
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

    public int getNumeroPaginasGeneradas() {
        return numeroPaginasGeneradas;
    }

    public void setNumeroPaginasGeneradas(int numeroPaginasGeneradas) {
        this.numeroPaginasGeneradas = numeroPaginasGeneradas;
    }

    public String getHashSha256DocumentoOriginal() {
        return hashSha256DocumentoOriginal;
    }

    public void setHashSha256DocumentoOriginal(String hashSha256DocumentoOriginal) {
        this.hashSha256DocumentoOriginal = hashSha256DocumentoOriginal;
    }

    public int getCantidadBeneficiariosRegistrados() {
        return cantidadBeneficiariosRegistrados;
    }

    public void setCantidadBeneficiariosRegistrados(int cantidadBeneficiariosRegistrados) {
        this.cantidadBeneficiariosRegistrados = cantidadBeneficiariosRegistrados;
    }

    public String getEstadoDocumentoGenerado() {
        return estadoDocumentoGenerado;
    }

    public void setEstadoDocumentoGenerado(String estadoDocumentoGenerado) {
        this.estadoDocumentoGenerado = estadoDocumentoGenerado;
    }

    public LocalDateTime getFechaHoraGeneracion() {
        return fechaHoraGeneracion;
    }

    public void setFechaHoraGeneracion(LocalDateTime fechaHoraGeneracion) {
        this.fechaHoraGeneracion = fechaHoraGeneracion;
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

    public List<PaginaDocumentoGenerado> getPaginasEsperadas() {
        return paginasEsperadas;
    }

    public void setPaginasEsperadas(List<PaginaDocumentoGenerado> paginasEsperadas) {
        this.paginasEsperadas = paginasEsperadas;
    }
}