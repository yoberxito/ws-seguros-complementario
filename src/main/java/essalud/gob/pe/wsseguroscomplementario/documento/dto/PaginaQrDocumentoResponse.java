package essalud.gob.pe.wsseguroscomplementario.documento.dto;

public class PaginaQrDocumentoResponse {

    private int numeroPaginaPdf;
    private String idDocumento;
    private String tipoDocumento;
    private int paginaQr;
    private int totalPaginasQr;
    private String contenidoOriginal;

    public PaginaQrDocumentoResponse() {
    }

    public int getNumeroPaginaPdf() {
        return numeroPaginaPdf;
    }

    public void setNumeroPaginaPdf(int numeroPaginaPdf) {
        this.numeroPaginaPdf = numeroPaginaPdf;
    }

    public String getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(String idDocumento) {
        this.idDocumento = idDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public int getPaginaQr() {
        return paginaQr;
    }

    public void setPaginaQr(int paginaQr) {
        this.paginaQr = paginaQr;
    }

    public int getTotalPaginasQr() {
        return totalPaginasQr;
    }

    public void setTotalPaginasQr(int totalPaginasQr) {
        this.totalPaginasQr = totalPaginasQr;
    }

    public String getContenidoOriginal() {
        return contenidoOriginal;
    }

    public void setContenidoOriginal(String contenidoOriginal) {
        this.contenidoOriginal = contenidoOriginal;
    }
}