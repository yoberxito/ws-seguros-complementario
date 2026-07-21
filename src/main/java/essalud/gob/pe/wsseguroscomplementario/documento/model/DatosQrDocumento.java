package essalud.gob.pe.wsseguroscomplementario.documento.model;

import java.util.HashMap;
import java.util.Map;

public class DatosQrDocumento {

    private int numeroPaginaPdf;
    private String idDocumento;
    private String tipoDocumento;
    private int paginaQr;
    private int totalPaginasQr;
    private String contenidoOriginal;

    public DatosQrDocumento() {
    }

    public static DatosQrDocumento desdeContenidoQr(String contenidoQr, int numeroPaginaPdf) {
        Map<String, String> valores = parsearContenidoQr(contenidoQr);

        DatosQrDocumento datosQr = new DatosQrDocumento();

        datosQr.setNumeroPaginaPdf(numeroPaginaPdf);
        datosQr.setIdDocumento(valores.get("ID_DOCUMENTO"));
        datosQr.setTipoDocumento(valores.get("TIPO_DOCUMENTO"));
        datosQr.setPaginaQr(convertirEntero(valores.get("PAGINA"), "PAGINA"));
        datosQr.setTotalPaginasQr(convertirEntero(valores.get("TOTAL_PAGINAS"), "TOTAL_PAGINAS"));
        datosQr.setContenidoOriginal(contenidoQr);

        validarDatosObligatorios(datosQr);

        return datosQr;
    }

    private static Map<String, String> parsearContenidoQr(String contenidoQr) {
        if (contenidoQr == null || contenidoQr.trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido del QR se encuentra vacío.");
        }

        Map<String, String> valores = new HashMap<>();
        String[] partes = contenidoQr.split("\\|");

        for (String parte : partes) {
            String[] claveValor = parte.split("=", 2);

            if (claveValor.length == 2) {
                valores.put(claveValor[0].trim(), claveValor[1].trim());
            }
        }

        return valores;
    }

    private static int convertirEntero(String valor, String campo) {
        try {
            return Integer.parseInt(valor);
        } catch (Exception e) {
            throw new IllegalArgumentException("El campo " + campo + " del QR no es válido.");
        }
    }

    private static void validarDatosObligatorios(DatosQrDocumento datosQr) {
        if (campoVacio(datosQr.getIdDocumento())) {
            throw new IllegalArgumentException("El QR no contiene ID_DOCUMENTO.");
        }

        if (campoVacio(datosQr.getTipoDocumento())) {
            throw new IllegalArgumentException("El QR no contiene TIPO_DOCUMENTO.");
        }

        if (datosQr.getPaginaQr() <= 0) {
            throw new IllegalArgumentException("El QR contiene número de página inválido.");
        }

        if (datosQr.getTotalPaginasQr() <= 0) {
            throw new IllegalArgumentException("El QR contiene total de páginas inválido.");
        }
    }

    private static boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
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