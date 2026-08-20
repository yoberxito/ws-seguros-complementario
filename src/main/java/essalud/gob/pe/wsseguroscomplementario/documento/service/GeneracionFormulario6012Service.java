package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.common.util.ByteArrayMultipartFile;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.Beneficiario6012Request;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarFormulario6012Request;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarFormulario6012Response;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RegistrarDocumentoGeneradoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RegistrarDocumentoGeneradoResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class GeneracionFormulario6012Service {

    private static final String TIPO_DOCUMENTO_6012 = "FORMULARIO_6012";
    private static final String VERSION_FORMATO_6012 = "FORM_6012_VIDA_001";
    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String RUTA_PLANTILLA_6012 = "/plantillas/formulario-6012.pdf";

    private static final int BENEFICIARIOS_POR_PAGINA = 5;

    private final QrDocumentoService qrDocumentoService;
    private final RegistroDocumentoGeneradoService registroDocumentoGeneradoService;

    public GeneracionFormulario6012Service(
            QrDocumentoService qrDocumentoService,
            RegistroDocumentoGeneradoService registroDocumentoGeneradoService
    ) {
        this.qrDocumentoService = qrDocumentoService;
        this.registroDocumentoGeneradoService = registroDocumentoGeneradoService;
    }

    public synchronized GenerarFormulario6012Response generarFormulario6012(
            GenerarFormulario6012Request request
    ) {
        validarRequest(request);

        int totalBeneficiarios =
                request.getBeneficiarios().size();

        int totalPaginas =
                calcularTotalPaginas(totalBeneficiarios);

        Optional<RegistrarDocumentoGeneradoResponse> metadataExistente =
                registroDocumentoGeneradoService
                        .buscarMetadataPorProcesoYTipoDocumento(
                                request.getRegistroInternoProceso(),
                                TIPO_DOCUMENTO_6012
                        );

        if (metadataExistente.isPresent()) {
            validarMetadataExistenteFormulario6012(
                    metadataExistente.get(),
                    totalBeneficiarios,
                    totalPaginas
            );
        }

        String idDocumentoGenerado =
                metadataExistente
                        .map(
                                RegistrarDocumentoGeneradoResponse::
                                        getIdDocumentoGenerado
                        )
                        .orElseGet(
                                this::generarIdDocumentoGenerado
                        );

        String nombreArchivo =
                construirNombreArchivo(request);

        byte[] pdfGenerado =
                generarPdfFormulario6012(
                        request,
                        idDocumentoGenerado,
                        totalPaginas
                );

        RegistrarDocumentoGeneradoResponse metadataDocumentoGenerado =
                metadataExistente.orElseGet(
                        () -> registrarMetadataDocumentoGenerado(
                                request,
                                idDocumentoGenerado,
                                nombreArchivo,
                                pdfGenerado
                        )
                );

        GenerarFormulario6012Response response =
                new GenerarFormulario6012Response();

        response.setGenerado(true);

        response.setMensajeGeneracion(
                metadataExistente.isPresent()
                        ? "Formulario 6012 regenerado correctamente utilizando la metadata documental existente."
                        : "Formulario 6012 generado correctamente."
        );

        response.setNombreArchivo(nombreArchivo);
        response.setContentType(CONTENT_TYPE_PDF);

        response.setArchivoBase64(
                Base64.getEncoder()
                        .encodeToString(pdfGenerado)
        );

        response.setCantidadBeneficiariosRegistrados(
                totalBeneficiarios
        );

        response.setNumeroPaginasGeneradas(
                totalPaginas
        );

        response.setMetadataDocumentoGenerado(
                metadataDocumentoGenerado
        );

        return response;
    }


    private void validarMetadataExistenteFormulario6012(
            RegistrarDocumentoGeneradoResponse metadata,
            int totalBeneficiarios,
            int totalPaginas
    ) {
        if (
                metadata == null
                        || campoVacio(
                        metadata.getIdDocumentoGenerado()
                )
        ) {
            throw new IllegalArgumentException(
                    "La metadata existente del Formulario 6012 no contiene un identificador documental válido."
            );
        }

        if (
                !VERSION_FORMATO_6012.equalsIgnoreCase(
                        valor(
                                metadata.getVersionFormato()
                        )
                )
        ) {
            throw new IllegalArgumentException(
                    "La versión del Formulario 6012 no coincide con la metadata documental existente."
            );
        }

        if (
                metadata.getNumeroPaginasGeneradas()
                        != totalPaginas
        ) {
            throw new IllegalArgumentException(
                    "Los datos actuales producirían una cantidad de páginas distinta a la metadata del Formulario 6012 ya generado."
            );
        }

        if (
                metadata
                        .getCantidadBeneficiariosRegistrados()
                        != totalBeneficiarios
        ) {
            throw new IllegalArgumentException(
                    "La cantidad de beneficiarios no coincide con la metadata del Formulario 6012 ya generado."
            );
        }
    }

    private byte[] generarPdfFormulario6012(
            GenerarFormulario6012Request request,
            String idDocumentoGenerado,
            int totalPaginas
    ) {
        try (
                PDDocument documentoFinal = new PDDocument();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()
        ) {
            PDFMergerUtility merger = new PDFMergerUtility();

            for (int indicePagina = 0; indicePagina < totalPaginas; indicePagina++) {
                int numeroPagina = indicePagina + 1;

                List<Beneficiario6012Request> beneficiariosPagina =
                        obtenerBeneficiariosPorPagina(request.getBeneficiarios(), indicePagina);

                byte[] paginaGenerada = generarPaginaFormulario6012(
                        request,
                        idDocumentoGenerado,
                        numeroPagina,
                        totalPaginas,
                        beneficiariosPagina
                );

                try (PDDocument documentoPagina = Loader.loadPDF(paginaGenerada)) {
                    merger.appendDocument(documentoFinal, documentoPagina);
                }
            }

            documentoFinal.save(salida);
            return salida.toByteArray();

        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo generar el Formulario 6012.", e);
        }
    }

    private byte[] generarPaginaFormulario6012(
            GenerarFormulario6012Request request,
            String idDocumentoGenerado,
            int numeroPagina,
            int totalPaginas,
            List<Beneficiario6012Request> beneficiariosPagina
    ) {
        try (
                PDDocument documento = cargarPlantilla6012();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()
        ) {
            PDPage pagina = documento.getPage(0);

            PDFont fuenteNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fuenteNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            String contenidoQr = construirContenidoQr(idDocumentoGenerado, numeroPagina, totalPaginas);

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
                llenarDatosTitular(contenido, fuenteNormal, fuenteNegrita, request);
                llenarDatosConyuge(contenido, fuenteNormal, fuenteNegrita, request);
                llenarBeneficiarios(contenido, fuenteNormal, beneficiariosPagina);
                marcarNotificacionCorreo(contenido, fuenteNegrita, request);

                if (numeroPagina > 1) {
                    escribirTexto(
                            contenido,
                            fuenteNegrita,
                            7.2f,
                            330,
                            820,
                            "CONTINUACIÓN DE BENEFICIARIOS"
                    );
                }

                // QR de trazabilidad documental en el recuadro superior derecho.
                contenido.drawImage(qrPdf, 500, 765, 72, 72);

                escribirTexto(
                        contenido,
                        fuenteNormal,
                        7.2f,
                        512,
                        760,
                        "Página " + numeroPagina + " de " + totalPaginas
                );
            }

            documento.save(salida);
            return salida.toByteArray();

        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo generar una página del Formulario 6012.", e);
        }
    }

    private void llenarDatosTitular(
            PDPageContentStream contenido,
            PDFont fuenteNormal,
            PDFont fuenteNegrita,
            GenerarFormulario6012Request request
    ) throws IOException {

        marcarTipoDocumento(contenido, fuenteNegrita, request.getTipoDocumentoTitular(), 51, 702, 113, 702, 175, 702);

        escribirTexto(
                contenido,
                fuenteNormal,
                8.5f,
                423,
                702,
                request.getNumeroDocumentoTitular()
        );

        escribirTextoAjustado(
                contenido,
                fuenteNormal,
                8.2f,
                44,
                658,
                request.getNombresApellidosTitular(),
                520
        );

        escribirTextoAjustado(
                contenido,
                fuenteNormal,
                8.2f,
                44,
                616,
                request.getCorreoElectronico(),
                520
        );

        marcarTipoAsegurado(contenido, fuenteNegrita, request.getTipoAsegurado());

        if ("SI".equalsIgnoreCase(valor(request.getConvenioCgbvp()))) {
            escribirTexto(contenido, fuenteNegrita, 10, 468, 574, "X");
        }

        escribirTexto(
                contenido,
                fuenteNormal,
                8.5f,
                325,
                532,
                request.getRucEmpleador()
        );
    }

    private void llenarDatosConyuge(
            PDPageContentStream contenido,
            PDFont fuenteNormal,
            PDFont fuenteNegrita,
            GenerarFormulario6012Request request
    ) throws IOException {

        if (campoVacio(request.getNumeroDocumentoConyuge()) && campoVacio(request.getNombresApellidosConyuge())) {
            return;
        }

        marcarTipoDocumento(contenido, fuenteNegrita, request.getTipoDocumentoConyuge(), 51, 470, 113, 470, 175, 470);

        escribirTexto(
                contenido,
                fuenteNormal,
                8.5f,
                423,
                470,
                request.getNumeroDocumentoConyuge()
        );

        escribirTextoAjustado(
                contenido,
                fuenteNormal,
                8.2f,
                44,
                427,
                request.getNombresApellidosConyuge(),
                520
        );
    }

    private void llenarBeneficiarios(
            PDPageContentStream contenido,
            PDFont fuenteNormal,
            List<Beneficiario6012Request> beneficiarios
    ) throws IOException {

        float[] coordenadasY = {359, 336, 313, 290, 267};

        for (int i = 0; i < beneficiarios.size(); i++) {
            Beneficiario6012Request beneficiario = beneficiarios.get(i);
            float y = coordenadasY[i];

            escribirTexto(
                    contenido,
                    fuenteNormal,
                    7.4f,
                    40,
                    y,
                    valor(beneficiario.getTipoDocumento())
            );

            escribirTexto(
                    contenido,
                    fuenteNormal,
                    7.4f,
                    120,
                    y,
                    valor(beneficiario.getNumeroDocumento())
            );

            escribirTextoAjustado(
                    contenido,
                    fuenteNormal,
                    7.4f,
                    225,
                    y,
                    valor(beneficiario.getNombresApellidos()),
                    325
            );

            escribirTexto(
                    contenido,
                    fuenteNormal,
                    7.4f,
                    543,
                    y,
                    beneficiario.getPorcentaje() == null
                            ? ""
                            : beneficiario.getPorcentaje().stripTrailingZeros().toPlainString()
            );
        }
    }

    private void marcarNotificacionCorreo(
            PDPageContentStream contenido,
            PDFont fuenteNegrita,
            GenerarFormulario6012Request request
    ) throws IOException {

        if ("NO".equalsIgnoreCase(valor(request.getNotificacionCorreo()))) {
            escribirTexto(contenido, fuenteNegrita, 8.5f, 545, 157, "X");
            return;
        }

        escribirTexto(contenido, fuenteNegrita, 8.5f, 500, 157, "X");
    }

    private void marcarTipoDocumento(
            PDPageContentStream contenido,
            PDFont fuenteNegrita,
            String tipoDocumento,
            float xDni,
            float yDni,
            float xCe,
            float yCe,
            float xOtro,
            float yOtro
    ) throws IOException {

        String tipo = valor(tipoDocumento).trim().toUpperCase();

        if ("01".equals(tipo) || "DNI".equals(tipo)) {
            escribirTexto(contenido, fuenteNegrita, 10, xDni, yDni, "X");
            return;
        }

        if ("04".equals(tipo) || "CE".equals(tipo) || "C.E.".equals(tipo)) {
            escribirTexto(contenido, fuenteNegrita, 10, xCe, yCe, "X");
            return;
        }

        escribirTexto(contenido, fuenteNegrita, 10, xOtro, yOtro, "X");
    }

    private void marcarTipoAsegurado(
            PDPageContentStream contenido,
            PDFont fuenteNegrita,
            String tipoAsegurado
    ) throws IOException {

        String tipo = valor(tipoAsegurado).trim().toUpperCase();

        if (tipo.contains("REGULAR")) {
            escribirTexto(contenido, fuenteNegrita, 10, 51, 574, "X");
            return;
        }

        if (tipo.contains("AGRARIO")) {
            escribirTexto(contenido, fuenteNegrita, 10, 144, 574, "X");
            return;
        }

        escribirTexto(contenido, fuenteNegrita, 10, 239, 574, "X");
    }

    private RegistrarDocumentoGeneradoResponse registrarMetadataDocumentoGenerado(
            GenerarFormulario6012Request request,
            String idDocumentoGenerado,
            String nombreArchivo,
            byte[] pdfGenerado
    ) {
        RegistrarDocumentoGeneradoRequest requestRegistro = new RegistrarDocumentoGeneradoRequest();

        requestRegistro.setIdDocumentoGenerado(idDocumentoGenerado);
        requestRegistro.setRegistroInternoProceso(request.getRegistroInternoProceso());
        requestRegistro.setTipoDocumento(TIPO_DOCUMENTO_6012);
        requestRegistro.setVersionFormato(VERSION_FORMATO_6012);

        requestRegistro.setTipoDocumentoTrabajador(request.getTipoDocumentoTitular());
        requestRegistro.setNumeroDocumentoTrabajador(request.getNumeroDocumentoTitular());
        requestRegistro.setNombresApellidosTrabajador(request.getNombresApellidosTitular());

        requestRegistro.setCantidadBeneficiariosRegistrados(request.getBeneficiarios().size());
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

    private PDDocument cargarPlantilla6012() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(RUTA_PLANTILLA_6012)) {
            if (inputStream == null) {
                throw new IllegalArgumentException(
                        "No se encontró la plantilla oficial en resources: " + RUTA_PLANTILLA_6012
                );
            }

            byte[] bytesPlantilla = inputStream.readAllBytes();
            return Loader.loadPDF(bytesPlantilla);
        }
    }

    private List<Beneficiario6012Request> obtenerBeneficiariosPorPagina(
            List<Beneficiario6012Request> beneficiarios,
            int indicePagina
    ) {
        int desde = indicePagina * BENEFICIARIOS_POR_PAGINA;
        int hasta = Math.min(desde + BENEFICIARIOS_POR_PAGINA, beneficiarios.size());

        return new ArrayList<>(beneficiarios.subList(desde, hasta));
    }

    private int calcularTotalPaginas(int totalBeneficiarios) {
        return (int) Math.ceil((double) totalBeneficiarios / BENEFICIARIOS_POR_PAGINA);
    }

    private String construirContenidoQr(
            String idDocumentoGenerado,
            int numeroPagina,
            int totalPaginas
    ) {
        return "ID_DOCUMENTO=" + idDocumentoGenerado
                + "|TIPO_DOCUMENTO=" + TIPO_DOCUMENTO_6012
                + "|PAGINA=" + numeroPagina
                + "|TOTAL_PAGINAS=" + totalPaginas;
    }

    private String construirNombreArchivo(GenerarFormulario6012Request request) {
        return "Formulario-6012-" + request.getNumeroDocumentoTitular() + ".pdf";
    }

    private String generarIdDocumentoGenerado() {
        return "DOC-GEN-" + UUID.randomUUID();
    }

    private void validarRequest(GenerarFormulario6012Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de generación del Formulario 6012 no puede estar vacía.");
        }

        if (campoVacio(request.getRegistroInternoProceso())) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(request.getTipoDocumentoTitular())) {
            throw new IllegalArgumentException("El tipo de documento del titular es obligatorio.");
        }

        if (campoVacio(request.getNumeroDocumentoTitular())) {
            throw new IllegalArgumentException("El número de documento del titular es obligatorio.");
        }

        if (campoVacio(request.getNombresApellidosTitular())) {
            throw new IllegalArgumentException("Los nombres y apellidos del titular son obligatorios.");
        }

        if (request.getBeneficiarios() == null || request.getBeneficiarios().isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un beneficiario para generar el Formulario 6012.");
        }

        BigDecimal sumaPorcentajes = BigDecimal.ZERO;

        for (Beneficiario6012Request beneficiario : request.getBeneficiarios()) {
            if (beneficiario == null) {
                throw new IllegalArgumentException("Existe un beneficiario vacío en la solicitud.");
            }

            if (campoVacio(beneficiario.getTipoDocumento())) {
                throw new IllegalArgumentException("El tipo de documento del beneficiario es obligatorio.");
            }

            if (campoVacio(beneficiario.getNumeroDocumento())) {
                throw new IllegalArgumentException("El número de documento del beneficiario es obligatorio.");
            }

            if (campoVacio(beneficiario.getNombresApellidos())) {
                throw new IllegalArgumentException("Los nombres y apellidos del beneficiario son obligatorios.");
            }

            if (beneficiario.getPorcentaje() == null || beneficiario.getPorcentaje().compareTo(BigDecimal.ONE) < 0) {
                throw new IllegalArgumentException("Cada beneficiario debe tener un porcentaje mínimo de 1%.");
            }

            sumaPorcentajes = sumaPorcentajes.add(beneficiario.getPorcentaje());
        }

        if (sumaPorcentajes.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException("La suma de porcentajes de beneficiarios debe ser igual a 100%.");
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
                tamanioFuente > 5.8f
                        && fuente.getStringWidth(textoNormalizado) / 1000 * tamanioFuente > anchoMaximo
        ) {
            tamanioFuente -= 0.4f;
        }

        escribirTexto(contenido, fuente, tamanioFuente, x, y, textoNormalizado);
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

    private String valor(String valor) {
        return campoVacio(valor) ? "" : valor;
    }

    private String valorPorDefecto(String valor, String valorDefecto) {
        return campoVacio(valor) ? valorDefecto : valor;
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}