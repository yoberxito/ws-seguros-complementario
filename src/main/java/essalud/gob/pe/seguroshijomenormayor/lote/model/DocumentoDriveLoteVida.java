package essalud.gob.pe.seguroshijomenormayor.lote.model;

public class DocumentoDriveLoteVida {

    private final String fileId;
    private final String nombreArchivo;
    private final String webViewLink;

    private final String idTpDoc;
    private final String tipoDocumentoTitular;
    private final String numeroDocumentoTitular;

    public DocumentoDriveLoteVida(
            String fileId,
            String nombreArchivo,
            String webViewLink,
            String idTpDoc,
            String tipoDocumentoTitular,
            String numeroDocumentoTitular
    ) {

        this.fileId = fileId;
        this.nombreArchivo = nombreArchivo;
        this.webViewLink = webViewLink;
        this.idTpDoc = idTpDoc;
        this.tipoDocumentoTitular =
                tipoDocumentoTitular;
        this.numeroDocumentoTitular =
                numeroDocumentoTitular;
    }

    public String getFileId() {
        return fileId;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public String getIdTpDoc() {
        return idTpDoc;
    }

    public String getTipoDocumentoTitular() {
        return tipoDocumentoTitular;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public String getTipoDocumentoLogico() {

        if ("244".equals(idTpDoc)) {
            return "FORMULARIO_6012";
        }

        if ("247".equals(idTpDoc)) {
            return "AUTORIZACION_DESCUENTO";
        }

        throw new IllegalStateException(
                "El documento Drive no tiene un tipo "
                        + "institucional +Vida válido."
        );
    }
}