package essalud.gob.pe.wsseguroscomplementario.documento.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidacionPdfResponse {

    private boolean valido;
    private String mensajeValidacion;
    private String nombreArchivo;
    private long tamanioBytes;
    private int numeroPaginas;
    private List<String> observaciones = new ArrayList<>();

    public ValidacionPdfResponse() {
    }

    public ValidacionPdfResponse(
            boolean valido,
            String mensajeValidacion,
            String nombreArchivo,
            long tamanioBytes,
            int numeroPaginas,
            List<String> observaciones
    ) {
        this.valido = valido;
        this.mensajeValidacion = mensajeValidacion;
        this.nombreArchivo = nombreArchivo;
        this.tamanioBytes = tamanioBytes;
        this.numeroPaginas = numeroPaginas;
        this.observaciones = observaciones;
    }

    public static ValidacionPdfResponse valido(
            String nombreArchivo,
            long tamanioBytes,
            int numeroPaginas
    ) {
        return new ValidacionPdfResponse(
                true,
                "El archivo PDF supera las validaciones técnicas.",
                nombreArchivo,
                tamanioBytes,
                numeroPaginas,
                new ArrayList<>()
        );
    }

    public static ValidacionPdfResponse invalido(
            String mensajeValidacion,
            String nombreArchivo,
            long tamanioBytes,
            int numeroPaginas,
            List<String> observaciones
    ) {
        return new ValidacionPdfResponse(
                false,
                mensajeValidacion,
                nombreArchivo,
                tamanioBytes,
                numeroPaginas,
                observaciones
        );
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public String getMensajeValidacion() {
        return mensajeValidacion;
    }

    public void setMensajeValidacion(String mensajeValidacion) {
        this.mensajeValidacion = mensajeValidacion;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public long getTamanioBytes() {
        return tamanioBytes;
    }

    public void setTamanioBytes(long tamanioBytes) {
        this.tamanioBytes = tamanioBytes;
    }

    public int getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(int numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }
}