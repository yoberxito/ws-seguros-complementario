package essalud.gob.pe.wsseguroscomplementario.documento.validator;

import essalud.gob.pe.wsseguroscomplementario.documento.constants.PdfValidationConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class PdfArchivoValidator {

    public List<String> validarArchivo(MultipartFile archivo) {
        List<String> observaciones = new ArrayList<>();

        if (archivo == null || archivo.isEmpty()) {
            observaciones.add("No se recibió archivo o el archivo se encuentra vacío.");
            return observaciones;
        }

        validarNombreExtension(archivo.getOriginalFilename(), observaciones);
        validarTamanioArchivo(archivo.getSize(), observaciones);

        return observaciones;
    }

    public byte[] obtenerBytesArchivo(MultipartFile archivo, List<String> observaciones) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        try {
            return archivo.getBytes();
        } catch (IOException e) {
            observaciones.add("No se pudo leer el contenido del archivo cargado.");
            return null;
        }
    }

    private void validarNombreExtension(String nombreArchivo, List<String> observaciones) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            observaciones.add("El archivo no cuenta con nombre válido.");
            return;
        }

        if (!nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            observaciones.add("El archivo debe tener extensión PDF.");
        }
    }

    private void validarTamanioArchivo(long tamanioBytes, List<String> observaciones) {
        if (tamanioBytes <= 0) {
            observaciones.add("El archivo se encuentra vacío.");
            return;
        }

        if (tamanioBytes < PdfValidationConstants.TAMANIO_MINIMO_BYTES) {
            observaciones.add("El archivo es demasiado pequeño para corresponder a un PDF válido.");
        }

        if (tamanioBytes > PdfValidationConstants.TAMANIO_MAXIMO_BYTES) {
            observaciones.add("El archivo supera el tamaño máximo permitido de 10 MB.");
        }
    }
}