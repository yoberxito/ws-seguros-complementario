package essalud.gob.pe.wsseguroscomplementario.documento.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class QrDocumentoService {

    public BufferedImage generarQr(String contenido, int ancho, int alto) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    contenido,
                    BarcodeFormat.QR_CODE,
                    ancho,
                    alto
            );

            return MatrixToImageWriter.toBufferedImage(bitMatrix);

        } catch (WriterException e) {
            throw new IllegalArgumentException("No se pudo generar el código QR del documento.", e);
        }
    }
}