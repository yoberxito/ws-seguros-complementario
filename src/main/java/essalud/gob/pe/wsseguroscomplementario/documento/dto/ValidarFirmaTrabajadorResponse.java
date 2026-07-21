package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidarFirmaTrabajadorResponse {

    private boolean firmaValida;
    private String mensajeValidacion;

    private String estadoValidacionDocumental;
    private boolean permiteNuevaCarga;

    private String tipoDocumento;
    private int numeroPaginasArchivo;

    private int paginasEvaluadas;
    private int paginasConFirma;
    private int paginasSinFirma;

    private List<PaginaFirmaTrabajadorResponse> detallePaginas = new ArrayList<>();
    private List<String> observaciones = new ArrayList<>();

    public ValidarFirmaTrabajadorResponse() {
    }

    public boolean isFirmaValida() {
        return firmaValida;
    }

    public void setFirmaValida(boolean firmaValida) {
        this.firmaValida = firmaValida;
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

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public int getNumeroPaginasArchivo() {
        return numeroPaginasArchivo;
    }

    public void setNumeroPaginasArchivo(int numeroPaginasArchivo) {
        this.numeroPaginasArchivo = numeroPaginasArchivo;
    }

    public int getPaginasEvaluadas() {
        return paginasEvaluadas;
    }

    public void setPaginasEvaluadas(int paginasEvaluadas) {
        this.paginasEvaluadas = paginasEvaluadas;
    }

    public int getPaginasConFirma() {
        return paginasConFirma;
    }

    public void setPaginasConFirma(int paginasConFirma) {
        this.paginasConFirma = paginasConFirma;
    }

    public int getPaginasSinFirma() {
        return paginasSinFirma;
    }

    public void setPaginasSinFirma(int paginasSinFirma) {
        this.paginasSinFirma = paginasSinFirma;
    }

    public List<PaginaFirmaTrabajadorResponse> getDetallePaginas() {
        return detallePaginas;
    }

    public void setDetallePaginas(List<PaginaFirmaTrabajadorResponse> detallePaginas) {
        this.detallePaginas = detallePaginas;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
}