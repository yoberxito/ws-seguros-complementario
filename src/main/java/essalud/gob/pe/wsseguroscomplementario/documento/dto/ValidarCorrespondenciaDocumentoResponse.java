package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidarCorrespondenciaDocumentoResponse {

    private boolean correspondenciaValida;
    private String mensajeValidacion;

    private String estadoValidacionDocumental;
    private boolean permiteNuevaCarga;

    private String idDocumentoGenerado;
    private String registroInternoProceso;
    private String tipoDocumentoEsperado;
    private String tipoDocumentoQr;
    private String numeroDocumentoTrabajador;

    private int numeroPaginasArchivo;
    private int totalPaginasDeclaradasQr;

    private List<PaginaQrDocumentoResponse> paginasQrLeidas = new ArrayList<>();
    private List<String> observaciones = new ArrayList<>();

    public ValidarCorrespondenciaDocumentoResponse() {
    }

    public boolean isCorrespondenciaValida() {
        return correspondenciaValida;
    }

    public void setCorrespondenciaValida(boolean correspondenciaValida) {
        this.correspondenciaValida = correspondenciaValida;
    }

    public String getMensajeValidacion() {
        return mensajeValidacion;
    }

    public void setMensajeValidacion(String mensajeValidacion) {
        this.mensajeValidacion = mensajeValidacion;
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

    public String getTipoDocumentoEsperado() {
        return tipoDocumentoEsperado;
    }

    public void setTipoDocumentoEsperado(String tipoDocumentoEsperado) {
        this.tipoDocumentoEsperado = tipoDocumentoEsperado;
    }

    public String getTipoDocumentoQr() {
        return tipoDocumentoQr;
    }

    public void setTipoDocumentoQr(String tipoDocumentoQr) {
        this.tipoDocumentoQr = tipoDocumentoQr;
    }

    public String getNumeroDocumentoTrabajador() {
        return numeroDocumentoTrabajador;
    }

    public void setNumeroDocumentoTrabajador(String numeroDocumentoTrabajador) {
        this.numeroDocumentoTrabajador = numeroDocumentoTrabajador;
    }

    public int getNumeroPaginasArchivo() {
        return numeroPaginasArchivo;
    }

    public void setNumeroPaginasArchivo(int numeroPaginasArchivo) {
        this.numeroPaginasArchivo = numeroPaginasArchivo;
    }

    public int getTotalPaginasDeclaradasQr() {
        return totalPaginasDeclaradasQr;
    }

    public void setTotalPaginasDeclaradasQr(int totalPaginasDeclaradasQr) {
        this.totalPaginasDeclaradasQr = totalPaginasDeclaradasQr;
    }

    public List<PaginaQrDocumentoResponse> getPaginasQrLeidas() {
        return paginasQrLeidas;
    }

    public void setPaginasQrLeidas(List<PaginaQrDocumentoResponse> paginasQrLeidas) {
        this.paginasQrLeidas = paginasQrLeidas;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
}