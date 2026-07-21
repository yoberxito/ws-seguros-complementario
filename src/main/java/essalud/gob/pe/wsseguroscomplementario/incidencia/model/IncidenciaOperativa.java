package essalud.gob.pe.wsseguroscomplementario.incidencia.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IncidenciaOperativa {

    private String idIncidenciaOperativa;
    private String registroInternoProceso;

    private String tipoDocumentoTrabajador;
    private String numeroDocumentoTrabajador;

    private String tipoDocumentoProceso;
    private String idDocumentoGenerado;
    private String idDocumentoCargado;
    private String idDocumentoSellado;
    private String idDocumentoPublicado;

    private String sistemaInvolucrado;
    private String etapaProceso;
    private String tipoIncidenciaOperativa;

    private String motivoObservado;
    private String detalleIncidencia;

    private String estadoIncidencia;
    private boolean observadoOperativo;
    private boolean permiteNuevaCargaTrabajador;
    private boolean requiereIntervencionInterna;

    private int numeroReintentosInternos;

    private LocalDateTime fechaHoraRegistro;
    private LocalDateTime fechaHoraUltimoReintento;
    private LocalDateTime fechaHoraCierre;

    private String usuarioResponsable;
    private String ipOrigen;
    private String datosSesionDispositivo;

    private String resultadoCierre;

    private List<ReintentoIncidenciaOperativa> reintentos = new ArrayList<>();

    public IncidenciaOperativa() {
    }

    public String getIdIncidenciaOperativa() {
        return idIncidenciaOperativa;
    }

    public void setIdIncidenciaOperativa(String idIncidenciaOperativa) {
        this.idIncidenciaOperativa = idIncidenciaOperativa;
    }

    public String getRegistroInternoProceso() {
        return registroInternoProceso;
    }

    public void setRegistroInternoProceso(String registroInternoProceso) {
        this.registroInternoProceso = registroInternoProceso;
    }

    public String getTipoDocumentoTrabajador() {
        return tipoDocumentoTrabajador;
    }

    public void setTipoDocumentoTrabajador(String tipoDocumentoTrabajador) {
        this.tipoDocumentoTrabajador = tipoDocumentoTrabajador;
    }

    public String getNumeroDocumentoTrabajador() {
        return numeroDocumentoTrabajador;
    }

    public void setNumeroDocumentoTrabajador(String numeroDocumentoTrabajador) {
        this.numeroDocumentoTrabajador = numeroDocumentoTrabajador;
    }

    public String getTipoDocumentoProceso() {
        return tipoDocumentoProceso;
    }

    public void setTipoDocumentoProceso(String tipoDocumentoProceso) {
        this.tipoDocumentoProceso = tipoDocumentoProceso;
    }

    public String getIdDocumentoGenerado() {
        return idDocumentoGenerado;
    }

    public void setIdDocumentoGenerado(String idDocumentoGenerado) {
        this.idDocumentoGenerado = idDocumentoGenerado;
    }

    public String getIdDocumentoCargado() {
        return idDocumentoCargado;
    }

    public void setIdDocumentoCargado(String idDocumentoCargado) {
        this.idDocumentoCargado = idDocumentoCargado;
    }

    public String getIdDocumentoSellado() {
        return idDocumentoSellado;
    }

    public void setIdDocumentoSellado(String idDocumentoSellado) {
        this.idDocumentoSellado = idDocumentoSellado;
    }

    public String getIdDocumentoPublicado() {
        return idDocumentoPublicado;
    }

    public void setIdDocumentoPublicado(String idDocumentoPublicado) {
        this.idDocumentoPublicado = idDocumentoPublicado;
    }

    public String getSistemaInvolucrado() {
        return sistemaInvolucrado;
    }

    public void setSistemaInvolucrado(String sistemaInvolucrado) {
        this.sistemaInvolucrado = sistemaInvolucrado;
    }

    public String getEtapaProceso() {
        return etapaProceso;
    }

    public void setEtapaProceso(String etapaProceso) {
        this.etapaProceso = etapaProceso;
    }

    public String getTipoIncidenciaOperativa() {
        return tipoIncidenciaOperativa;
    }

    public void setTipoIncidenciaOperativa(String tipoIncidenciaOperativa) {
        this.tipoIncidenciaOperativa = tipoIncidenciaOperativa;
    }

    public String getMotivoObservado() {
        return motivoObservado;
    }

    public void setMotivoObservado(String motivoObservado) {
        this.motivoObservado = motivoObservado;
    }

    public String getDetalleIncidencia() {
        return detalleIncidencia;
    }

    public void setDetalleIncidencia(String detalleIncidencia) {
        this.detalleIncidencia = detalleIncidencia;
    }

    public String getEstadoIncidencia() {
        return estadoIncidencia;
    }

    public void setEstadoIncidencia(String estadoIncidencia) {
        this.estadoIncidencia = estadoIncidencia;
    }

    public boolean isObservadoOperativo() {
        return observadoOperativo;
    }

    public void setObservadoOperativo(boolean observadoOperativo) {
        this.observadoOperativo = observadoOperativo;
    }

    public boolean isPermiteNuevaCargaTrabajador() {
        return permiteNuevaCargaTrabajador;
    }

    public void setPermiteNuevaCargaTrabajador(boolean permiteNuevaCargaTrabajador) {
        this.permiteNuevaCargaTrabajador = permiteNuevaCargaTrabajador;
    }

    public boolean isRequiereIntervencionInterna() {
        return requiereIntervencionInterna;
    }

    public void setRequiereIntervencionInterna(boolean requiereIntervencionInterna) {
        this.requiereIntervencionInterna = requiereIntervencionInterna;
    }

    public int getNumeroReintentosInternos() {
        return numeroReintentosInternos;
    }

    public void setNumeroReintentosInternos(int numeroReintentosInternos) {
        this.numeroReintentosInternos = numeroReintentosInternos;
    }

    public LocalDateTime getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public void setFechaHoraRegistro(LocalDateTime fechaHoraRegistro) {
        this.fechaHoraRegistro = fechaHoraRegistro;
    }

    public LocalDateTime getFechaHoraUltimoReintento() {
        return fechaHoraUltimoReintento;
    }

    public void setFechaHoraUltimoReintento(LocalDateTime fechaHoraUltimoReintento) {
        this.fechaHoraUltimoReintento = fechaHoraUltimoReintento;
    }

    public LocalDateTime getFechaHoraCierre() {
        return fechaHoraCierre;
    }

    public void setFechaHoraCierre(LocalDateTime fechaHoraCierre) {
        this.fechaHoraCierre = fechaHoraCierre;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public void setUsuarioResponsable(String usuarioResponsable) {
        this.usuarioResponsable = usuarioResponsable;
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

    public String getResultadoCierre() {
        return resultadoCierre;
    }

    public void setResultadoCierre(String resultadoCierre) {
        this.resultadoCierre = resultadoCierre;
    }

    public List<ReintentoIncidenciaOperativa> getReintentos() {
        return reintentos;
    }

    public void setReintentos(List<ReintentoIncidenciaOperativa> reintentos) {
        this.reintentos = reintentos;
    }
}