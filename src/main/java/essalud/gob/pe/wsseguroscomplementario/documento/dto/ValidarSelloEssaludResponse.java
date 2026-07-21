package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidarSelloEssaludResponse {

    private boolean selloValido;
    private String mensajeValidacion;

    private String estadoValidacionDocumental;
    private boolean permiteNuevaCarga;

    private String tipoDocumento;
    private int numeroPaginasArchivo;
    private boolean requiereReintentoSellado;
    private int paginasEvaluadas;
    private int paginasConSello;
    private int paginasSinSello;

    private List<PaginaSelloEssaludResponse> detallePaginas = new ArrayList<>();
    private List<String> observaciones = new ArrayList<>();

    public ValidarSelloEssaludResponse() {
    }

    public boolean isSelloValido() {
        return selloValido;
    }

    public void setSelloValido(boolean selloValido) {
        this.selloValido = selloValido;
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

    public int getPaginasConSello() {
        return paginasConSello;
    }

    public void setPaginasConSello(int paginasConSello) {
        this.paginasConSello = paginasConSello;
    }

    public int getPaginasSinSello() {
        return paginasSinSello;
    }

    public void setPaginasSinSello(int paginasSinSello) {
        this.paginasSinSello = paginasSinSello;
    }

    public List<PaginaSelloEssaludResponse> getDetallePaginas() {
        return detallePaginas;
    }

    public void setDetallePaginas(List<PaginaSelloEssaludResponse> detallePaginas) {
        this.detallePaginas = detallePaginas;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
    public boolean isRequiereReintentoSellado() {
        return requiereReintentoSellado;
    }

    public void setRequiereReintentoSellado(boolean requiereReintentoSellado) {
        this.requiereReintentoSellado = requiereReintentoSellado;
    }

}