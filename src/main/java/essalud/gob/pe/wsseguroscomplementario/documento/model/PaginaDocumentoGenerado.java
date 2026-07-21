package essalud.gob.pe.wsseguroscomplementario.documento.model;

public class PaginaDocumentoGenerado {

    private int numeroPagina;
    private int totalPaginas;
    private String identificadorPagina;
    private String contenidoQrEsperado;

    public PaginaDocumentoGenerado() {
    }

    public PaginaDocumentoGenerado(
            int numeroPagina,
            int totalPaginas,
            String identificadorPagina,
            String contenidoQrEsperado
    ) {
        this.numeroPagina = numeroPagina;
        this.totalPaginas = totalPaginas;
        this.identificadorPagina = identificadorPagina;
        this.contenidoQrEsperado = contenidoQrEsperado;
    }

    public int getNumeroPagina() {
        return numeroPagina;
    }

    public void setNumeroPagina(int numeroPagina) {
        this.numeroPagina = numeroPagina;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public String getIdentificadorPagina() {
        return identificadorPagina;
    }

    public void setIdentificadorPagina(String identificadorPagina) {
        this.identificadorPagina = identificadorPagina;
    }

    public String getContenidoQrEsperado() {
        return contenidoQrEsperado;
    }

    public void setContenidoQrEsperado(String contenidoQrEsperado) {
        this.contenidoQrEsperado = contenidoQrEsperado;
    }
}