package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.common.util.ByteArrayMultipartFile;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarAutorizacionDescuentoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarAutorizacionDescuentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RegistrarDocumentoGeneradoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RegistrarDocumentoGeneradoResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

@Service
public class GeneracionAutorizacionDescuentoService {

    private static final String TIPO_DOCUMENTO_AUTORIZACION = "AUTORIZACION_DESCUENTO";
    private static final String VERSION_FORMATO_AUTORIZACION = "AUT_DESC_VIDA_001";
    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String RUTA_PLANTILLA_AUTORIZACION = "/plantillas/autorizacion-descuento.pdf";

    private final QrDocumentoService qrDocumentoService;
    private final RegistroDocumentoGeneradoService registroDocumentoGeneradoService;

    public GeneracionAutorizacionDescuentoService(
            QrDocumentoService qrDocumentoService,
            RegistroDocumentoGeneradoService registroDocumentoGeneradoService
    ) {
        this.qrDocumentoService = qrDocumentoService;
        this.registroDocumentoGeneradoService = registroDocumentoGeneradoService;
    }

    public GenerarAutorizacionDescuentoResponse generarAutorizacionDescuento(
            GenerarAutorizacionDescuentoRequest request
    ) {
        validarRequest(request);

        String idDocumentoGenerado = generarIdDocumentoGenerado();
        String nombreArchivo = construirNombreArchivo(request);
        String contenidoQr = construirContenidoQr(idDocumentoGenerado);

        byte[] pdfGenerado = generarPdfAutorizacionDesdePlantillaOficial(
                request,
                idDocumentoGenerado,
                contenidoQr
        );

        RegistrarDocumentoGeneradoResponse metadataDocumentoGenerado =
                registrarMetadataDocumentoGenerado(
                        request,
                        idDocumentoGenerado,
                        nombreArchivo,
                        pdfGenerado
                );

        GenerarAutorizacionDescuentoResponse response = new GenerarAutorizacionDescuentoResponse();
        response.setGenerado(true);
        response.setMensajeGeneracion("Autorización de Descuento generada correctamente.");
        response.setNombreArchivo(nombreArchivo);
        response.setContentType(CONTENT_TYPE_PDF);
        response.setArchivoBase64(Base64.getEncoder().encodeToString(pdfGenerado));
        response.setMetadataDocumentoGenerado(metadataDocumentoGenerado);

        return response;
    }

    private byte[] generarPdfAutorizacionDesdePlantillaOficial(
            GenerarAutorizacionDescuentoRequest request,
            String idDocumentoGenerado,
            String contenidoQr
    ) {
        try (
                PDDocument documento = cargarPlantillaOficial();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()
        ) {
            PDPage pagina = documento.getPage(0);

            PDFont fuenteNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fuenteNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            BufferedImage imagenQr = qrDocumentoService.generarQr(contenidoQr, 160, 160);
            PDImageXObject qrPdf = LosslessFactory.createFromImage(documento, imagenQr);

            try (
                    PDPageContentStream contenido = new PDPageContentStream(
                            documento,
                            pagina,
                            PDPageContentStream.AppendMode.APPEND,
                            true,
                            true
                    )
            ) {
                LocalDate fechaServidor = LocalDate.now();

                /*
                 * Coordenadas ajustadas sobre la plantilla oficial.
                 * Sistema PDFBox: X crece hacia la derecha, Y crece hacia arriba.
                 */

// Yo, __________________________
                escribirTextoAjustado(
                        contenido,
                        fuenteNormal,
                        7.8f,
                        86,
                        618,
                        request.getNombresApellidosTrabajador(),
                        215
                );

// DNI N° _______________________
                escribirTexto(
                        contenido,
                        fuenteNormal,
                        8.5f,
                        430,
                        618,
                        request.getNumeroDocumentoTrabajador()
                );

// código de planilla __________________
                escribirTexto(
                        contenido,
                        fuenteNormal,
                        8.5f,
                        178,
                        604,
                        request.getCodigoPlanilla()
                );

// decreto legislativo N° ______
                escribirTexto(
                        contenido,
                        fuenteNormal,
                        8.5f,
                        437,
                        604,
                        request.getDecretoLegislativo()
                );

// a los ____ días del mes de ____ del año 2026
                escribirTexto(
                        contenido,
                        fuenteNormal,
                        8.5f,
                        275,
                        551,
                        String.valueOf(fechaServidor.getDayOfMonth())
                );

                escribirTexto(
                        contenido,
                        fuenteNormal,
                        8.5f,
                        400,
                        551,
                        obtenerNombreMes(fechaServidor)
                );

// QR de trazabilidad documental
                contenido.drawImage(qrPdf, 462, 385, 75, 75);

                escribirTexto(
                        contenido,
                        fuenteNormal,
                        5.5f,
                        450,
                        375,
                        "ID: " + abreviar(idDocumentoGenerado, 32)
                );

                escribirTexto(
                        contenido,
                        fuenteNormal,
                        5.5f,
                        450,
                        367,
                        "Página 1 de 1"
                );
            }

            documento.save(salida);
            return salida.toByteArray();

        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo generar la Autorización de Descuento con la plantilla oficial.", e);
        }
    }

    private PDDocument cargarPlantillaOficial() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(RUTA_PLANTILLA_AUTORIZACION)) {
            if (inputStream == null) {
                throw new IllegalArgumentException(
                        "No se encontró la plantilla oficial en resources: " + RUTA_PLANTILLA_AUTORIZACION
                );
            }

            byte[] bytesPlantilla = inputStream.readAllBytes();
            return Loader.loadPDF(bytesPlantilla);
        }
    }

    private RegistrarDocumentoGeneradoResponse registrarMetadataDocumentoGenerado(
            GenerarAutorizacionDescuentoRequest request,
            String idDocumentoGenerado,
            String nombreArchivo,
            byte[] pdfGenerado
    ) {
        RegistrarDocumentoGeneradoRequest requestRegistro = new RegistrarDocumentoGeneradoRequest();

        requestRegistro.setIdDocumentoGenerado(idDocumentoGenerado);
        requestRegistro.setRegistroInternoProceso(request.getRegistroInternoProceso());
        requestRegistro.setTipoDocumento(TIPO_DOCUMENTO_AUTORIZACION);
        requestRegistro.setVersionFormato(VERSION_FORMATO_AUTORIZACION);

        requestRegistro.setTipoDocumentoTrabajador(request.getTipoDocumentoTrabajador());
        requestRegistro.setNumeroDocumentoTrabajador(request.getNumeroDocumentoTrabajador());
        requestRegistro.setNombresApellidosTrabajador(request.getNombresApellidosTrabajador());

        requestRegistro.setCantidadBeneficiariosRegistrados(0);
        requestRegistro.setGeneradoPor(valorPorDefecto(request.getGeneradoPor(), "SISTEMA"));
        requestRegistro.setCanalGeneracion(valorPorDefecto(request.getCanalGeneracion(), "MODULO_AFILIACION_DIGITAL"));

        ByteArrayMultipartFile archivoGenerado = new ByteArrayMultipartFile(
                "archivo",
                nombreArchivo,
                CONTENT_TYPE_PDF,
                pdfGenerado
        );

        return registroDocumentoGeneradoService.registrarDocumentoGenerado(
                requestRegistro,
                archivoGenerado
        );
    }

    private String construirContenidoQr(String idDocumentoGenerado) {
        return "ID_DOCUMENTO=" + idDocumentoGenerado
                + "|TIPO_DOCUMENTO=" + TIPO_DOCUMENTO_AUTORIZACION
                + "|PAGINA=1"
                + "|TOTAL_PAGINAS=1";
    }

    private String construirNombreArchivo(GenerarAutorizacionDescuentoRequest request) {
        return "Autorizacion-Descuento-" + request.getNumeroDocumentoTrabajador() + ".pdf";
    }

    private String generarIdDocumentoGenerado() {
        return "DOC-GEN-" + UUID.randomUUID();
    }

    private void validarRequest(GenerarAutorizacionDescuentoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de generación de autorización no puede estar vacía.");
        }

        if (campoVacio(request.getRegistroInternoProceso())) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(request.getTipoDocumentoTrabajador())) {
            throw new IllegalArgumentException("El tipo de documento del trabajador es obligatorio.");
        }

        if (campoVacio(request.getNumeroDocumentoTrabajador())) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        if (campoVacio(request.getNombresApellidosTrabajador())) {
            throw new IllegalArgumentException("Los nombres y apellidos del trabajador son obligatorios.");
        }

        if (campoVacio(request.getCodigoPlanilla())) {
            throw new IllegalArgumentException("El código de planilla es obligatorio.");
        }

        if (campoVacio(request.getDecretoLegislativo())) {
            throw new IllegalArgumentException("El Decreto Legislativo es obligatorio.");
        }
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

    private void escribirTextoAjustado(
            PDPageContentStream contenido,
            PDFont fuente,
            float tamanioFuenteInicial,
            float x,
            float y,
            String texto,
            float anchoMaximo
    ) throws IOException {
        String textoNormalizado = normalizarTexto(texto);
        float tamanioFuente = tamanioFuenteInicial;

        while (
                tamanioFuente > 6.5f
                        && fuente.getStringWidth(textoNormalizado) / 1000 * tamanioFuente > anchoMaximo
        ) {
            tamanioFuente -= 0.5f;
        }

        escribirTexto(contenido, fuente, tamanioFuente, x, y, textoNormalizado);
    }

    private String obtenerNombreMes(LocalDate fecha) {
        return switch (fecha.getMonthValue()) {
            case 1 -> "enero";
            case 2 -> "febrero";
            case 3 -> "marzo";
            case 4 -> "abril";
            case 5 -> "mayo";
            case 6 -> "junio";
            case 7 -> "julio";
            case 8 -> "agosto";
            case 9 -> "setiembre";
            case 10 -> "octubre";
            case 11 -> "noviembre";
            case 12 -> "diciembre";
            default -> "";
        };
    }

    private String abreviar(String texto, int longitudMaxima) {
        if (texto == null) {
            return "";
        }

        if (texto.length() <= longitudMaxima) {
            return texto;
        }

        return texto.substring(0, longitudMaxima) + "...";
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

    private String valorPorDefecto(String valor, String valorDefecto) {
        return campoVacio(valor) ? valorDefecto : valor;
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}