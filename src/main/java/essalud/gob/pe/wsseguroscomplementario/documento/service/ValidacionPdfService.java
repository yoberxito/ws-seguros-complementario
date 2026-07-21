package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionPdfResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.validator.PdfArchivoValidator;
import essalud.gob.pe.wsseguroscomplementario.documento.validator.PdfEstructuraValidator;
import essalud.gob.pe.wsseguroscomplementario.documento.validator.PdfRenderValidator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidacionPdfService {

    private final PdfArchivoValidator pdfArchivoValidator;
    private final PdfEstructuraValidator pdfEstructuraValidator;
    private final PdfRenderValidator pdfRenderValidator;

    public ValidacionPdfService(
            PdfArchivoValidator pdfArchivoValidator,
            PdfEstructuraValidator pdfEstructuraValidator,
            PdfRenderValidator pdfRenderValidator
    ) {
        this.pdfArchivoValidator = pdfArchivoValidator;
        this.pdfEstructuraValidator = pdfEstructuraValidator;
        this.pdfRenderValidator = pdfRenderValidator;
    }

    public ValidacionPdfResponse validarEstructuraTecnica(MultipartFile archivo) {
        String nombreArchivo = obtenerNombreArchivo(archivo);
        long tamanioBytes = obtenerTamanioArchivo(archivo);

        List<String> observaciones = new ArrayList<>();

        observaciones.addAll(pdfArchivoValidator.validarArchivo(archivo));

        byte[] bytesArchivo = pdfArchivoValidator.obtenerBytesArchivo(archivo, observaciones);

        if (!observaciones.isEmpty() || bytesArchivo == null) {
            return ValidacionPdfResponse.invalido(
                    "El archivo cargado no supera la validación técnica.",
                    nombreArchivo,
                    tamanioBytes,
                    0,
                    observaciones
            );
        }

        try (PDDocument documento = pdfEstructuraValidator.cargarDocumentoPdf(bytesArchivo, observaciones)) {

            if (!observaciones.isEmpty() || documento == null) {
                return ValidacionPdfResponse.invalido(
                        "El archivo cargado no supera la validación técnica.",
                        nombreArchivo,
                        tamanioBytes,
                        0,
                        observaciones
                );
            }

            int numeroPaginas = documento.getNumberOfPages();

            pdfRenderValidator.validarPaginasRenderizables(documento, observaciones);

            if (!observaciones.isEmpty()) {
                return ValidacionPdfResponse.invalido(
                        "El archivo cargado no supera la validación técnica.",
                        nombreArchivo,
                        tamanioBytes,
                        numeroPaginas,
                        observaciones
                );
            }

            return ValidacionPdfResponse.valido(
                    nombreArchivo,
                    tamanioBytes,
                    numeroPaginas
            );

        } catch (IOException e) {
            observaciones.add("El documento PDF no pudo cerrarse o procesarse correctamente.");

            return ValidacionPdfResponse.invalido(
                    "El archivo cargado no supera la validación técnica.",
                    nombreArchivo,
                    tamanioBytes,
                    0,
                    observaciones
            );
        }
    }

    private String obtenerNombreArchivo(MultipartFile archivo) {
        if (archivo == null) {
            return null;
        }

        return archivo.getOriginalFilename();
    }

    private long obtenerTamanioArchivo(MultipartFile archivo) {
        if (archivo == null) {
            return 0L;
        }

        return archivo.getSize();
    }
}