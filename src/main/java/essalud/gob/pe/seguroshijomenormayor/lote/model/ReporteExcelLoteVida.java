package essalud.gob.pe.seguroshijomenormayor.lote.model;

import java.util.Arrays;

public class ReporteExcelLoteVida {

    private final String nombreArchivo;
    private final String contentType;
    private final byte[] contenido;

    public ReporteExcelLoteVida(
            String nombreArchivo,
            String contentType,
            byte[] contenido
    ) {

        if (nombreArchivo == null
                || nombreArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre del reporte es obligatorio."
            );
        }

        if (contentType == null
                || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El content type es obligatorio."
            );
        }

        if (contenido == null || contenido.length == 0) {
            throw new IllegalArgumentException(
                    "El contenido del reporte es obligatorio."
            );
        }

        this.nombreArchivo = nombreArchivo.trim();
        this.contentType = contentType.trim();
        this.contenido =
                Arrays.copyOf(contenido, contenido.length);
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContenido() {
        return Arrays.copyOf(
                contenido,
                contenido.length
        );
    }

    public int getTamanioBytes() {
        return contenido.length;
    }
}