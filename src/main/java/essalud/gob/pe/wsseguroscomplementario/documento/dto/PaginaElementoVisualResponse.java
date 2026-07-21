package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class PaginaElementoVisualResponse {

    private int numeroPaginaPdf;

    private boolean paginaConDimensionesValidas;
    private boolean encabezadoVisible;
    private boolean cuerpoDocumentoVisible;
    private boolean zonaFirmaVisible;
    private boolean qrVisible;

    private int pixelesTintaEncabezado;
    private int pixelesTintaCuerpo;
    private int pixelesTintaZonaFirma;

    private double porcentajeTintaEncabezado;
    private double porcentajeTintaCuerpo;
    private double porcentajeTintaZonaFirma;

    private boolean elementosVisualesCompletos;

    private List<String> observaciones = new ArrayList<>();

    public PaginaElementoVisualResponse() {
    }

    public int getNumeroPaginaPdf() {
        return numeroPaginaPdf;
    }

    public void setNumeroPaginaPdf(int numeroPaginaPdf) {
        this.numeroPaginaPdf = numeroPaginaPdf;
    }

    public boolean isPaginaConDimensionesValidas() {
        return paginaConDimensionesValidas;
    }

    public void setPaginaConDimensionesValidas(boolean paginaConDimensionesValidas) {
        this.paginaConDimensionesValidas = paginaConDimensionesValidas;
    }

    public boolean isEncabezadoVisible() {
        return encabezadoVisible;
    }

    public void setEncabezadoVisible(boolean encabezadoVisible) {
        this.encabezadoVisible = encabezadoVisible;
    }

    public boolean isCuerpoDocumentoVisible() {
        return cuerpoDocumentoVisible;
    }

    public void setCuerpoDocumentoVisible(boolean cuerpoDocumentoVisible) {
        this.cuerpoDocumentoVisible = cuerpoDocumentoVisible;
    }

    public boolean isZonaFirmaVisible() {
        return zonaFirmaVisible;
    }

    public void setZonaFirmaVisible(boolean zonaFirmaVisible) {
        this.zonaFirmaVisible = zonaFirmaVisible;
    }

    public boolean isQrVisible() {
        return qrVisible;
    }

    public void setQrVisible(boolean qrVisible) {
        this.qrVisible = qrVisible;
    }

    public int getPixelesTintaEncabezado() {
        return pixelesTintaEncabezado;
    }

    public void setPixelesTintaEncabezado(int pixelesTintaEncabezado) {
        this.pixelesTintaEncabezado = pixelesTintaEncabezado;
    }

    public int getPixelesTintaCuerpo() {
        return pixelesTintaCuerpo;
    }

    public void setPixelesTintaCuerpo(int pixelesTintaCuerpo) {
        this.pixelesTintaCuerpo = pixelesTintaCuerpo;
    }

    public int getPixelesTintaZonaFirma() {
        return pixelesTintaZonaFirma;
    }

    public void setPixelesTintaZonaFirma(int pixelesTintaZonaFirma) {
        this.pixelesTintaZonaFirma = pixelesTintaZonaFirma;
    }

    public double getPorcentajeTintaEncabezado() {
        return porcentajeTintaEncabezado;
    }

    public void setPorcentajeTintaEncabezado(double porcentajeTintaEncabezado) {
        this.porcentajeTintaEncabezado = porcentajeTintaEncabezado;
    }

    public double getPorcentajeTintaCuerpo() {
        return porcentajeTintaCuerpo;
    }

    public void setPorcentajeTintaCuerpo(double porcentajeTintaCuerpo) {
        this.porcentajeTintaCuerpo = porcentajeTintaCuerpo;
    }

    public double getPorcentajeTintaZonaFirma() {
        return porcentajeTintaZonaFirma;
    }

    public void setPorcentajeTintaZonaFirma(double porcentajeTintaZonaFirma) {
        this.porcentajeTintaZonaFirma = porcentajeTintaZonaFirma;
    }

    public boolean isElementosVisualesCompletos() {
        return elementosVisualesCompletos;
    }

    public void setElementosVisualesCompletos(boolean elementosVisualesCompletos) {
        this.elementosVisualesCompletos = elementosVisualesCompletos;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
}