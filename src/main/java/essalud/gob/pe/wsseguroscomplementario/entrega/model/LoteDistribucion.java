package essalud.gob.pe.wsseguroscomplementario.entrega.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoteDistribucion {

    private Long idLote;
    private String codLote;

    private LocalDate fechaInicioPeriodo;
    private LocalDate fechaFinPeriodo;

    private String estadoLote;

    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    public LoteDistribucion() {
    }

    public Long getIdLote() {
        return idLote;
    }

    public void setIdLote(Long idLote) {
        this.idLote = idLote;
    }

    public String getCodLote() {
        return codLote;
    }

    public void setCodLote(String codLote) {
        this.codLote = codLote;
    }

    public LocalDate getFechaInicioPeriodo() {
        return fechaInicioPeriodo;
    }

    public void setFechaInicioPeriodo(
            LocalDate fechaInicioPeriodo
    ) {
        this.fechaInicioPeriodo = fechaInicioPeriodo;
    }

    public LocalDate getFechaFinPeriodo() {
        return fechaFinPeriodo;
    }

    public void setFechaFinPeriodo(
            LocalDate fechaFinPeriodo
    ) {
        this.fechaFinPeriodo = fechaFinPeriodo;
    }

    public String getEstadoLote() {
        return estadoLote;
    }

    public void setEstadoLote(String estadoLote) {
        this.estadoLote = estadoLote;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(
            LocalDateTime fechaPublicacion
    ) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDateTime fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            LocalDateTime fechaActualizacion
    ) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
