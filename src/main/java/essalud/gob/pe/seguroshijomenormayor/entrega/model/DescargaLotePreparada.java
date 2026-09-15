package essalud.gob.pe.seguroshijomenormayor.entrega.model;

import java.util.Arrays;

public class DescargaLotePreparada {

    private final String nombreArchivo;
    private final byte[] contenido;

    public DescargaLotePreparada(
            String nombreArchivo,
            byte[] contenido
    ) {

        if (
                nombreArchivo == null
                        || nombreArchivo.trim().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "El nombre del ZIP es obligatorio."
            );
        }

        if (
                contenido == null
                        || contenido.length == 0
        ) {
            throw new IllegalArgumentException(
                    "El contenido del ZIP es obligatorio."
            );
        }

        this.nombreArchivo =
                nombreArchivo.trim();

        this.contenido =
                Arrays.copyOf(
                        contenido,
                        contenido.length
                );
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public byte[] getContenido() {
        return Arrays.copyOf(
                contenido,
                contenido.length
        );
    }
}