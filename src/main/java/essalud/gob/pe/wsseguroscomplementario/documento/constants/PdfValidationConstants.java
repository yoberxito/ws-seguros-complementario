package essalud.gob.pe.wsseguroscomplementario.documento.constants;

public final class PdfValidationConstants {

    private PdfValidationConstants() {
    }

    public static final long TAMANIO_MAXIMO_BYTES = 10L * 1024L * 1024L; // 10 MB
    public static final long TAMANIO_MINIMO_BYTES = 1024L; // 1 KB mínimo referencial

    public static final int DPI_RENDER_VALIDACION = 100;
    public static final int MIN_ANCHO_RENDER = 700;
    public static final int MIN_ALTO_RENDER = 900;

    public static final double MAX_PORCENTAJE_BLANCO = 0.995;
    public static final int MIN_RANGO_CONTRASTE = 15;

    public static final int MAX_PAGINAS_RENDER_VALIDACION = 3;
}