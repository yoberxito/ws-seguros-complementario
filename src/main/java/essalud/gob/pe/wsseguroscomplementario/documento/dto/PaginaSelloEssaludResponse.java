package essalud.gob.pe.wsseguroscomplementario.documento.dto;

public class PaginaSelloEssaludResponse {

    private int numeroPaginaPdf;
    private boolean selloDetectado;
    private int pixelesEvaluados;
    private int pixelesConTinta;
    private double porcentajeTintaZona;
    private String observacion;

    public PaginaSelloEssaludResponse() {
    }

    public int getNumeroPaginaPdf() {
        return numeroPaginaPdf;
    }

    public void setNumeroPaginaPdf(int numeroPaginaPdf) {
        this.numeroPaginaPdf = numeroPaginaPdf;
    }

    public boolean isSelloDetectado() {
        return selloDetectado;
    }

    public void setSelloDetectado(boolean selloDetectado) {
        this.selloDetectado = selloDetectado;
    }

    public int getPixelesEvaluados() {
        return pixelesEvaluados;
    }

    public void setPixelesEvaluados(int pixelesEvaluados) {
        this.pixelesEvaluados = pixelesEvaluados;
    }

    public int getPixelesConTinta() {
        return pixelesConTinta;
    }

    public void setPixelesConTinta(int pixelesConTinta) {
        this.pixelesConTinta = pixelesConTinta;
    }

    public double getPorcentajeTintaZona() {
        return porcentajeTintaZona;
    }

    public void setPorcentajeTintaZona(double porcentajeTintaZona) {
        this.porcentajeTintaZona = porcentajeTintaZona;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}