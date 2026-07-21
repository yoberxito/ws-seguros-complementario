package essalud.gob.pe.wsseguroscomplementario.documento.model;

import essalud.gob.pe.wsseguroscomplementario.documento.constants.PdfValidationConstants;

public class AnalisisImagenPdf {

    private final double porcentajeBlanco;
    private final int rangoContraste;

    public AnalisisImagenPdf(double porcentajeBlanco, int rangoContraste) {
        this.porcentajeBlanco = porcentajeBlanco;
        this.rangoContraste = rangoContraste;
    }

    public double getPorcentajeBlanco() {
        return porcentajeBlanco;
    }

    public int getRangoContraste() {
        return rangoContraste;
    }

    public boolean aparentementeEnBlanco() {
        return porcentajeBlanco >= PdfValidationConstants.MAX_PORCENTAJE_BLANCO
                || rangoContraste < PdfValidationConstants.MIN_RANGO_CONTRASTE;
    }
}