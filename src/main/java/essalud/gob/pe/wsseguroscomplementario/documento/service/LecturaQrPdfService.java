package essalud.gob.pe.wsseguroscomplementario.documento.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DatosQrDocumento;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LecturaQrPdfService {

    private static final int DPI_LECTURA_QR = 220;

    public List<DatosQrDocumento> leerCodigosQr(MultipartFile archivo) {
        byte[] bytesArchivo = obtenerBytesArchivo(archivo);

        try (PDDocument documento = Loader.loadPDF(bytesArchivo)) {
            PDFRenderer renderer = new PDFRenderer(documento);

            List<DatosQrDocumento> qrsLeidos = new ArrayList<>();

            for (int indicePagina = 0; indicePagina < documento.getNumberOfPages(); indicePagina++) {
                BufferedImage imagenPagina = renderer.renderImageWithDPI(indicePagina, DPI_LECTURA_QR);

                Optional<String> contenidoQr = decodificarQr(imagenPagina);

                if (contenidoQr.isPresent()) {

                    try {

                        /*
                         * ZXing puede detectar códigos QR que pertenecen
                         * a otros sistemas o documentos.
                         *
                         * Que exista un QR legible no significa que sea
                         * un QR documental generado por +Vida.
                         *
                         * Si su contenido no cumple nuestro formato
                         * ID_DOCUMENTO / TIPO_DOCUMENTO /
                         * PAGINA / TOTAL_PAGINAS, se ignora como QR
                         * documental válido.
                         *
                         * Posteriormente la validación de correspondencia
                         * rechazará funcionalmente el archivo al no
                         * encontrar un QR +Vida válido.
                         */
                        DatosQrDocumento datosQr =
                                DatosQrDocumento.desdeContenidoQr(
                                        contenidoQr.get(),
                                        indicePagina + 1
                                );

                        qrsLeidos.add(
                                datosQr
                        );

                    } catch (IllegalArgumentException e) {

                        /*
                         * QR ajeno, incompleto o con formato distinto
                         * al estándar documental +Vida.
                         *
                         * No es una falla técnica del sistema.
                         * Por eso no propagamos la excepción como
                         * ERROR_VALIDACION_CORRESPONDENCIA.
                         */
                    }
                }
            }

            return qrsLeidos;

        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el PDF para extraer el código QR.", e);
        }
    }

    public int contarPaginas(MultipartFile archivo) {
        byte[] bytesArchivo = obtenerBytesArchivo(archivo);

        try (PDDocument documento = Loader.loadPDF(bytesArchivo)) {
            return documento.getNumberOfPages();

        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo contar las páginas del PDF.", e);
        }
    }

    private Optional<String> decodificarQr(BufferedImage imagenPagina) {
        try {
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(imagenPagina);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            Result resultado = new MultiFormatReader().decode(bitmap, hints);

            return Optional.ofNullable(resultado.getText());

        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    private byte[] obtenerBytesArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF es obligatorio.");
        }

        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo PDF cargado.", e);
        }
    }
}