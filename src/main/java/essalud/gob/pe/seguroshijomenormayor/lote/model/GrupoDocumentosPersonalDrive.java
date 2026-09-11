package essalud.gob.pe.seguroshijomenormayor.lote.model;

import java.util.List;

/*
 * Plan logico de documentos Drive pertenecientes
 * a un mismo destino institucional PERSONAL.
 *
 * Esta clase NO representa todavia una carpeta fisica
 * de Google Drive.
 */
public class GrupoDocumentosPersonalDrive {

    private final DestinoPersonalVida destino;

    private final List<DocumentoDriveLoteVida> documentos;

    public GrupoDocumentosPersonalDrive(
            DestinoPersonalVida destino,
            List<DocumentoDriveLoteVida> documentos
    ) {

        if (destino == null) {
            throw new IllegalArgumentException(
                    "El destino PERSONAL es obligatorio."
            );
        }

        if (documentos == null) {
            throw new IllegalArgumentException(
                    "Los documentos PERSONAL son obligatorios."
            );
        }

        this.destino =
                destino;

        this.documentos =
                List.copyOf(
                        documentos
                );
    }

    public DestinoPersonalVida getDestino() {
        return destino;
    }

    public List<DocumentoDriveLoteVida> getDocumentos() {
        return documentos;
    }

    public int getCantidadDocumentos() {
        return documentos.size();
    }
}