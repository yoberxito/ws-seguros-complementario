package essalud.gob.pe.wsseguroscomplementario.documento.validator;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class PdfEstructuraValidator {

    public PDDocument cargarDocumentoPdf(byte[] bytesArchivo, List<String> observaciones) {
        validarCabeceraPdf(bytesArchivo, observaciones);

        if (!observaciones.isEmpty()) {
            return null;
        }

        try {
            PDDocument documento = Loader.loadPDF(bytesArchivo);
            validarDocumentoPdf(documento, observaciones);

            if (!observaciones.isEmpty()) {
                cerrarDocumentoSilenciosamente(documento);
                return null;
            }

            return documento;

        } catch (IOException e) {
            observaciones.add("El archivo no corresponde a un PDF válido, se encuentra corrupto o no puede ser procesado.");
            return null;
        }
    }

    private void validarCabeceraPdf(byte[] bytesArchivo, List<String> observaciones) {
        if (bytesArchivo == null || bytesArchivo.length < 4) {
            observaciones.add("El archivo no contiene información suficiente para validar su estructura.");
            return;
        }

        String cabecera = new String(bytesArchivo, 0, 4, StandardCharsets.US_ASCII);

        if (!"%PDF".equals(cabecera)) {
            observaciones.add("El archivo no presenta una cabecera PDF válida.");
        }
    }

    private void validarDocumentoPdf(PDDocument documento, List<String> observaciones) {
        if (documento == null) {
            observaciones.add("El documento PDF no pudo ser cargado.");
            return;
        }

        if (documento.isEncrypted()) {
            observaciones.add("El PDF se encuentra protegido o cifrado y no puede ser procesado.");
            return;
        }

        if (documento.getNumberOfPages() <= 0) {
            observaciones.add("El PDF no contiene páginas.");
        }
    }

    private void cerrarDocumentoSilenciosamente(PDDocument documento) {
        if (documento == null) {
            return;
        }

        try {
            documento.close();
        } catch (IOException ignored) {
        }
    }
}