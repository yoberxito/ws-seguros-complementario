package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidarPaginasDocumentoResponse {

    private boolean paginasValidas;
    private String mensajeValidacion;

    private String estadoValidacionDocumental;
    private boolean permiteNuevaCarga;

    private String idDocumentoGenerado;
    private String registroInternoProceso;
    private String tipoDocumento;
    private String numeroDocumentoTrabajador;

    private int numeroPaginasArchivo;
    private int numeroPaginasEsperadas;

    private List<Integer> paginasEsperadas = new ArrayList<>();
    private List<Integer> paginasLeidas = new ArrayList<>();
    private List<Integer> paginasFaltantes = new ArrayList<>();
    private List<Integer> paginasDuplicadas = new ArrayList<>();
    private List<String> observaciones = new ArrayList<>();

    public ValidarPaginasDocumentoResponse() {
    }

    public boolean isPaginasValidas() {
        return paginasValidas;
    }

    public void setPaginasValidas(boolean paginasValidas) {
        this.paginasValidas = paginasValidas;
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

    public int getNumeroPaginasArchivo() {
        return numeroPaginasArchivo;
    }

    public void setNumeroPaginasArchivo(int numeroPaginasArchivo) {
        this.numeroPaginasArchivo = numeroPaginasArchivo;
    }

    public int getNumeroPaginasEsperadas() {
        return numeroPaginasEsperadas;
    }

    public void setNumeroPaginasEsperadas(int numeroPaginasEsperadas) {
        this.numeroPaginasEsperadas = numeroPaginasEsperadas;
    }

    public List<Integer> getPaginasEsperadas() {
        return paginasEsperadas;
    }

    public void setPaginasEsperadas(List<Integer> paginasEsperadas) {
        this.paginasEsperadas = paginasEsperadas;
    }

    public List<Integer> getPaginasLeidas() {
        return paginasLeidas;
    }

    public void setPaginasLeidas(List<Integer> paginasLeidas) {
        this.paginasLeidas = paginasLeidas;
    }

    public List<Integer> getPaginasFaltantes() {
        return paginasFaltantes;
    }

    public void setPaginasFaltantes(List<Integer> paginasFaltantes) {
        this.paginasFaltantes = paginasFaltantes;
    }

    public List<Integer> getPaginasDuplicadas() {
        return paginasDuplicadas;
    }

    public void setPaginasDuplicadas(List<Integer> paginasDuplicadas) {
        this.paginasDuplicadas = paginasDuplicadas;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
}