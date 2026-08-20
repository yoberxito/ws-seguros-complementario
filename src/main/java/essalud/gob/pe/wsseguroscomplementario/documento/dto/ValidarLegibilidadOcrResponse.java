package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidarLegibilidadOcrResponse {

    private boolean legibilidadValida;

    private String mensajeValidacion;
    private String estadoValidacionDocumental;

    private boolean permiteNuevaCarga;

    private String tipoDocumento;

    private int numeroPaginasAnalizadas;
    private int paginasLegibles;
    private int paginasIlegibles;

    private List<PaginaLegibilidadOcrResponse>
            detallePaginas =
            new ArrayList<>();

    private List<String>
            observaciones =
            new ArrayList<>();

    public ValidarLegibilidadOcrResponse() {
    }

    public boolean isLegibilidadValida() {
        return legibilidadValida;
    }

    public void setLegibilidadValida(
            boolean legibilidadValida
    ) {
        this.legibilidadValida =
                legibilidadValida;
    }

    public String getMensajeValidacion() {
        return mensajeValidacion;
    }

    public void setMensajeValidacion(
            String mensajeValidacion
    ) {
        this.mensajeValidacion =
                mensajeValidacion;
    }

    public String getEstadoValidacionDocumental() {
        return estadoValidacionDocumental;
    }

    public void setEstadoValidacionDocumental(
            String estadoValidacionDocumental
    ) {
        this.estadoValidacionDocumental =
                estadoValidacionDocumental;
    }

    public boolean isPermiteNuevaCarga() {
        return permiteNuevaCarga;
    }

    public void setPermiteNuevaCarga(
            boolean permiteNuevaCarga
    ) {
        this.permiteNuevaCarga =
                permiteNuevaCarga;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(
            String tipoDocumento
    ) {
        this.tipoDocumento =
                tipoDocumento;
    }

    public int getNumeroPaginasAnalizadas() {
        return numeroPaginasAnalizadas;
    }

    public void setNumeroPaginasAnalizadas(
            int numeroPaginasAnalizadas
    ) {
        this.numeroPaginasAnalizadas =
                numeroPaginasAnalizadas;
    }

    public int getPaginasLegibles() {
        return paginasLegibles;
    }

    public void setPaginasLegibles(
            int paginasLegibles
    ) {
        this.paginasLegibles =
                paginasLegibles;
    }

    public int getPaginasIlegibles() {
        return paginasIlegibles;
    }

    public void setPaginasIlegibles(
            int paginasIlegibles
    ) {
        this.paginasIlegibles =
                paginasIlegibles;
    }

    public List<PaginaLegibilidadOcrResponse>
    getDetallePaginas() {
        return detallePaginas;
    }

    public void setDetallePaginas(
            List<PaginaLegibilidadOcrResponse>
                    detallePaginas
    ) {
        this.detallePaginas =
                detallePaginas;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            List<String> observaciones
    ) {
        this.observaciones =
                observaciones;
    }
}