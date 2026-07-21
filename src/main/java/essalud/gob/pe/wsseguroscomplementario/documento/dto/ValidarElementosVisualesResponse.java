package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidarElementosVisualesResponse {

    private boolean elementosVisualesValidos;
    private String mensajeValidacion;
    private String estadoValidacionDocumental;

    private String tipoDocumento;
    private String nombreArchivo;

    private int numeroPaginasAnalizadas;
    private int paginasConElementosCompletos;
    private int paginasConObservaciones;

    private boolean permiteNuevaCarga;

    private List<PaginaElementoVisualResponse> detallePaginas = new ArrayList<>();
    private List<String> observaciones = new ArrayList<>();

    public ValidarElementosVisualesResponse() {
    }

    public boolean isElementosVisualesValidos() {
        return elementosVisualesValidos;
    }

    public void setElementosVisualesValidos(boolean elementosVisualesValidos) {
        this.elementosVisualesValidos = elementosVisualesValidos;
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

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public int getNumeroPaginasAnalizadas() {
        return numeroPaginasAnalizadas;
    }

    public void setNumeroPaginasAnalizadas(int numeroPaginasAnalizadas) {
        this.numeroPaginasAnalizadas = numeroPaginasAnalizadas;
    }

    public int getPaginasConElementosCompletos() {
        return paginasConElementosCompletos;
    }

    public void setPaginasConElementosCompletos(int paginasConElementosCompletos) {
        this.paginasConElementosCompletos = paginasConElementosCompletos;
    }

    public int getPaginasConObservaciones() {
        return paginasConObservaciones;
    }

    public void setPaginasConObservaciones(int paginasConObservaciones) {
        this.paginasConObservaciones = paginasConObservaciones;
    }

    public boolean isPermiteNuevaCarga() {
        return permiteNuevaCarga;
    }

    public void setPermiteNuevaCarga(boolean permiteNuevaCarga) {
        this.permiteNuevaCarga = permiteNuevaCarga;
    }

    public List<PaginaElementoVisualResponse> getDetallePaginas() {
        return detallePaginas;
    }

    public void setDetallePaginas(List<PaginaElementoVisualResponse> detallePaginas) {
        this.detallePaginas = detallePaginas;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
}