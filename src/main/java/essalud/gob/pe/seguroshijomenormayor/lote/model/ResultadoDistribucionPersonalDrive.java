package essalud.gob.pe.seguroshijomenormayor.lote.model;

import java.util.List;

/*
 * Resultado fisico de distribuir un destino PERSONAL
 * en Google Drive.
 *
 * Los documentos contenidos aqui corresponden a las COPIAS
 * finales y no a los archivos fuente de Preparacion.
 */
public class ResultadoDistribucionPersonalDrive {

    private final DestinoPersonalVida destino;

    private final String carpetaDestinoId;

    private final String carpetaPeriodoId;

    private final String urlCarpetaPeriodo;

    private final List<DocumentoDriveLoteVida>
            documentosFinales;

    public ResultadoDistribucionPersonalDrive(
            DestinoPersonalVida destino,
            String carpetaDestinoId,
            String carpetaPeriodoId,
            String urlCarpetaPeriodo,
            List<DocumentoDriveLoteVida> documentosFinales
    ) {

        if (destino == null) {
            throw new IllegalArgumentException(
                    "El destino PERSONAL es obligatorio."
            );
        }

        this.destino =
                destino;

        this.carpetaDestinoId =
                requerir(
                        carpetaDestinoId,
                        "El ID de la carpeta destino es obligatorio."
                );

        this.carpetaPeriodoId =
                requerir(
                        carpetaPeriodoId,
                        "El ID de la carpeta del periodo es obligatorio."
                );

        this.urlCarpetaPeriodo =
                requerir(
                        urlCarpetaPeriodo,
                        "La URL de la carpeta del periodo es obligatoria."
                );

        if (documentosFinales == null) {
            throw new IllegalArgumentException(
                    "Los documentos finales son obligatorios."
            );
        }

        this.documentosFinales =
                List.copyOf(
                        documentosFinales
                );
    }

    public DestinoPersonalVida getDestino() {
        return destino;
    }

    public String getCarpetaDestinoId() {
        return carpetaDestinoId;
    }

    public String getCarpetaPeriodoId() {
        return carpetaPeriodoId;
    }

    public String getUrlCarpetaPeriodo() {
        return urlCarpetaPeriodo;
    }

    public List<DocumentoDriveLoteVida> getDocumentosFinales() {
        return documentosFinales;
    }

    public int getCantidadDocumentos() {
        return documentosFinales.size();
    }

    private static String requerir(
            String valor,
            String mensaje
    ) {

        if (
                valor == null
                        || valor.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }

        return valor.trim();
    }
}