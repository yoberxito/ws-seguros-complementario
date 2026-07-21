package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarDocumentoSelladoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoSellado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.TipoDocumentoDigital;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoSelladoRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class GeneracionDocumentoSelladoService {

    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String RUTA_SELLO_VIVA = "/imagenes/sello-viva-recibido.png";

    private static final String ESTADO_DOCUMENTO_SELLADO = "DOCUMENTO_SELLADO";

    private static final ZoneId ZONA_HORARIA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_SELLO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA_SELLO = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DocumentoSelladoRepository documentoSelladoRepository;

    public GeneracionDocumentoSelladoService(
            DocumentoSelladoRepository documentoSelladoRepository
    ) {
        this.documentoSelladoRepository = documentoSelladoRepository;
    }

    public GenerarDocumentoSelladoResponse generarDocumentoSellado(
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        validarParametros(
                archivo,
                registroInternoProceso,
                tipoDocumento,
                numeroDocumentoTrabajador
        );

        String tipoDocumentoNormalizado = normalizar(tipoDocumento);
        String idDocumentoSellado = generarIdDocumentoSellado();

        LocalDateTime fechaHoraSellado = LocalDateTime.now(ZONA_HORARIA_LIMA);

        byte[] pdfSellado = sellarDocumentoPdf(
                archivo,
                tipoDocumentoNormalizado,
                fechaHoraSellado
        );

        String nombreArchivoSellado = construirNombreArchivoSellado(
                archivo.getOriginalFilename(),
                tipoDocumentoNormalizado,
                numeroDocumentoTrabajador
        );

        String hashDocumentoSellado = calcularSha256(pdfSellado);
        int numeroPaginasSelladas = contarPaginas(pdfSellado);

        DocumentoSellado documentoSellado = new DocumentoSellado();

        documentoSellado.setIdDocumentoSellado(idDocumentoSellado);
        documentoSellado.setRegistroInternoProceso(registroInternoProceso);
        documentoSellado.setTipoDocumento(tipoDocumentoNormalizado);
        documentoSellado.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);
        documentoSellado.setNombreArchivo(nombreArchivoSellado);
        documentoSellado.setContentType(CONTENT_TYPE_PDF);
        documentoSellado.setContenidoArchivo(pdfSellado);
        documentoSellado.setNumeroPaginasSelladas(numeroPaginasSelladas);
        documentoSellado.setHashSha256DocumentoSellado(hashDocumentoSellado);
        documentoSellado.setFechaHoraSellado(fechaHoraSellado);
        documentoSellado.setEstadoDocumentoSellado(ESTADO_DOCUMENTO_SELLADO);

        documentoSelladoRepository.guardar(documentoSellado);

        GenerarDocumentoSelladoResponse response = new GenerarDocumentoSelladoResponse();

        response.setDocumentoSellado(true);
        response.setMensajeSellado("Documento sellado internamente por el sistema.");
        response.setIdDocumentoSellado(idDocumentoSellado);
        response.setRegistroInternoProceso(registroInternoProceso);
        response.setTipoDocumento(tipoDocumentoNormalizado);
        response.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);
        response.setNombreArchivo(nombreArchivoSellado);
        response.setContentType(CONTENT_TYPE_PDF);
        response.setArchivoBase64(Base64.getEncoder().encodeToString(pdfSellado));
        response.setNumeroPaginasSelladas(numeroPaginasSelladas);
        response.setHashSha256DocumentoSellado(hashDocumentoSellado);
        response.setFechaHoraSellado(fechaHoraSellado);
        response.setEstadoDocumentoSellado(ESTADO_DOCUMENTO_SELLADO);
        response.setRequiereVerificacionSello(true);

        return response;
    }

    public DocumentoSellado obtenerDocumentoSellado(String idDocumentoSellado) {
        return documentoSelladoRepository.buscarPorId(idDocumentoSellado)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el documento sellado solicitado."));
    }

    private byte[] sellarDocumentoPdf(
            MultipartFile archivo,
            String tipoDocumento,
            LocalDateTime fechaHoraSellado
    ) {
        try (
                PDDocument documento = Loader.loadPDF(archivo.getBytes());
                ByteArrayOutputStream salida = new ByteArrayOutputStream()
        ) {
            byte[] bytesSello = cargarSelloViva();

            PDImageXObject imagenSello = PDImageXObject.createFromByteArray(
                    documento,
                    bytesSello,
                    "sello-viva-recibido"
            );

            ZonaSelloPdf zonaSello = obtenerZonaSelloPdf(tipoDocumento);

            for (PDPage pagina : documento.getPages()) {
                try (
                        PDPageContentStream contenido = new PDPageContentStream(
                                documento,
                                pagina,
                                PDPageContentStream.AppendMode.APPEND,
                                true,
                                true
                        )
                ) {
                    contenido.drawImage(
                            imagenSello,
                            zonaSello.x(),
                            zonaSello.y(),
                            zonaSello.ancho(),
                            zonaSello.alto()
                    );

                    escribirFechaHoraSellado(
                            contenido,
                            zonaSello,
                            fechaHoraSellado
                    );
                }
            }

            documento.save(salida);
            return salida.toByteArray();

        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo generar el documento sellado.", e);
        }
    }

    private void escribirFechaHoraSellado(
            PDPageContentStream contenido,
            ZonaSelloPdf zonaSello,
            LocalDateTime fechaHoraSellado
    ) throws IOException {

        PDFont fuenteNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        String fecha = fechaHoraSellado.format(FORMATO_FECHA_SELLO);
        String hora = fechaHoraSellado.format(FORMATO_HORA_SELLO);

        contenido.setNonStrokingColor(4f / 255f, 77f / 255f, 167f / 255f);

        escribirTexto(
                contenido,
                fuenteNegrita,
                6.2f,
                zonaSello.x() + 55,
                zonaSello.y() + 22,
                fecha
        );

        escribirTexto(
                contenido,
                fuenteNegrita,
                6.2f,
                zonaSello.x() + 55,
                zonaSello.y() + 10,
                hora
        );

        contenido.setNonStrokingColor(0f, 0f, 0f);
    }

    private void escribirTexto(
            PDPageContentStream contenido,
            PDFont fuente,
            float tamanioFuente,
            float x,
            float y,
            String texto
    ) throws IOException {
        contenido.beginText();
        contenido.setFont(fuente, tamanioFuente);
        contenido.newLineAtOffset(x, y);
        contenido.showText(normalizarTexto(texto));
        contenido.endText();
    }

    private ZonaSelloPdf obtenerZonaSelloPdf(String tipoDocumento) {
        if ("FORMULARIO_6012".equalsIgnoreCase(tipoDocumento)) {
            // Zona inferior derecha del Formulario 6012,
            // sobre el espacio de FIRMA Y SELLO DE ESSALUD.
            return new ZonaSelloPdf(360, 70, 134, 85);
        }

        if ("AUTORIZACION_DESCUENTO".equalsIgnoreCase(tipoDocumento)) {
            // Coordenada inicial estimada para la autorización.
            // Se ajusta visualmente cuando probemos con el PDF sellado.
            return new ZonaSelloPdf(355, 428, 134, 85);
        }

        return new ZonaSelloPdf(360, 70, 134, 85);
    }

    private byte[] cargarSelloViva() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(RUTA_SELLO_VIVA)) {
            if (inputStream == null) {
                throw new IllegalArgumentException(
                        "No se encontró la imagen del sello en resources: " + RUTA_SELLO_VIVA
                );
            }

            return inputStream.readAllBytes();
        }
    }

    private int contarPaginas(byte[] pdf) {
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            return documento.getNumberOfPages();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo contar las páginas del documento sellado.", e);
        }
    }

    private void validarParametros(
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF es obligatorio.");
        }

        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(tipoDocumento)) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }

        if (!TipoDocumentoDigital.esValido(tipoDocumento)) {
            throw new IllegalArgumentException(
                    "El tipo de documento no es válido. Valores permitidos: "
                            + TipoDocumentoDigital.valoresPermitidos()
            );
        }

        if (campoVacio(numeroDocumentoTrabajador)) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }
    }

    private String construirNombreArchivoSellado(
            String nombreOriginal,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        String nombreBase = campoVacio(nombreOriginal)
                ? tipoDocumento + "-" + numeroDocumentoTrabajador + ".pdf"
                : nombreOriginal;

        if (nombreBase.toLowerCase().endsWith(".pdf")) {
            nombreBase = nombreBase.substring(0, nombreBase.length() - 4);
        }

        return nombreBase + "-sellado.pdf";
    }

    private String calcularSha256(byte[] contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido);

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo calcular el hash SHA-256 del documento sellado.", e);
        }
    }

    private String generarIdDocumentoSellado() {
        return "DOC-SELL-" + UUID.randomUUID();
    }

    private String normalizar(String valor) {
        return valor.trim().toUpperCase();
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replace("–", "-")
                .replace("—", "-")
                .replace("“", "\"")
                .replace("”", "\"");
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private record ZonaSelloPdf(
            float x,
            float y,
            float ancho,
            float alto
    ) {
    }
}