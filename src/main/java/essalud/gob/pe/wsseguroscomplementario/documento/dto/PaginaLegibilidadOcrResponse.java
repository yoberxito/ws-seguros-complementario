package essalud.gob.pe.wsseguroscomplementario.documento.dto;

public class PaginaLegibilidadOcrResponse {

    private int numeroPaginaPdf;

    private boolean paginaLegible;

    private boolean textoPrincipalReconocible;
    private boolean zonaCriticaBorrosa;

    private int caracteresAlfanumericosReconocidos;
    private int palabrasReconocibles;

    private double puntuacionNitidezGeneral;
    private double puntuacionNitidezMinimaZonaCritica;

    private String observacion;

    public PaginaLegibilidadOcrResponse() {
    }

    public int getNumeroPaginaPdf() {
        return numeroPaginaPdf;
    }

    public void setNumeroPaginaPdf(
            int numeroPaginaPdf
    ) {
        this.numeroPaginaPdf =
                numeroPaginaPdf;
    }

    public boolean isPaginaLegible() {
        return paginaLegible;
    }

    public void setPaginaLegible(
            boolean paginaLegible
    ) {
        this.paginaLegible =
                paginaLegible;
    }

    public boolean isTextoPrincipalReconocible() {
        return textoPrincipalReconocible;
    }

    public void setTextoPrincipalReconocible(
            boolean textoPrincipalReconocible
    ) {
        this.textoPrincipalReconocible =
                textoPrincipalReconocible;
    }

    public boolean isZonaCriticaBorrosa() {
        return zonaCriticaBorrosa;
    }

    public void setZonaCriticaBorrosa(
            boolean zonaCriticaBorrosa
    ) {
        this.zonaCriticaBorrosa =
                zonaCriticaBorrosa;
    }

    public int getCaracteresAlfanumericosReconocidos() {
        return caracteresAlfanumericosReconocidos;
    }

    public void setCaracteresAlfanumericosReconocidos(
            int caracteresAlfanumericosReconocidos
    ) {
        this.caracteresAlfanumericosReconocidos =
                caracteresAlfanumericosReconocidos;
    }

    public int getPalabrasReconocibles() {
        return palabrasReconocibles;
    }

    public void setPalabrasReconocibles(
            int palabrasReconocibles
    ) {
        this.palabrasReconocibles =
                palabrasReconocibles;
    }

    public double getPuntuacionNitidezGeneral() {
        return puntuacionNitidezGeneral;
    }

    public void setPuntuacionNitidezGeneral(
            double puntuacionNitidezGeneral
    ) {
        this.puntuacionNitidezGeneral =
                puntuacionNitidezGeneral;
    }

    public double getPuntuacionNitidezMinimaZonaCritica() {
        return puntuacionNitidezMinimaZonaCritica;
    }

    public void setPuntuacionNitidezMinimaZonaCritica(
            double puntuacionNitidezMinimaZonaCritica
    ) {
        this.puntuacionNitidezMinimaZonaCritica =
                puntuacionNitidezMinimaZonaCritica;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(
            String observacion
    ) {
        this.observacion =
                observacion;
    }
}