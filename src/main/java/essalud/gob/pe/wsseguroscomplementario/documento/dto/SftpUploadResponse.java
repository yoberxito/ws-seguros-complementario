package essalud.gob.pe.wsseguroscomplementario.documento.dto;

public class SftpUploadResponse {

    private Archivo archivo;
    private String flagResultado;
    private String mensaje;

    public SftpUploadResponse() {
    }

    public Archivo getArchivo() {
        return archivo;
    }

    public void setArchivo(Archivo archivo) {
        this.archivo = archivo;
    }

    public String getFlagResultado() {
        return flagResultado;
    }

    public void setFlagResultado(String flagResultado) {
        this.flagResultado = flagResultado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public static class Archivo {

        private String nombreArchivo;
        private String rutaArchivo;

        public Archivo() {
        }

        public String getNombreArchivo() {
            return nombreArchivo;
        }

        public void setNombreArchivo(
                String nombreArchivo
        ) {
            this.nombreArchivo =
                    nombreArchivo;
        }

        public String getRutaArchivo() {
            return rutaArchivo;
        }

        public void setRutaArchivo(
                String rutaArchivo
        ) {
            this.rutaArchivo =
                    rutaArchivo;
        }
    }
}