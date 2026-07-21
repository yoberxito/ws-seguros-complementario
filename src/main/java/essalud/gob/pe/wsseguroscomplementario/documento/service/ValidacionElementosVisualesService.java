package essalud.gob.pe.wsseguroscomplementario.documento.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.PaginaElementoVisualResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarElementosVisualesResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidacionElementosVisualesService {

    private static final float DPI_RENDER = 220f;

    private static final String ESTADO_ELEMENTOS_VISUALES_VALIDADOS = "ELEMENTOS_VISUALES_VALIDADOS";
    private static final String ESTADO_RECHAZADO_ELEMENTOS_VISUALES = "RECHAZADO_ELEMENTOS_VISUALES";

    private static final int ANCHO_MINIMO_RENDER = 1000;
    private static final int ALTO_MINIMO_RENDER = 1300;

    private static final int MINIMO_PIXELES_TINTA_ENCABEZADO = 350;
    private static final int MINIMO_PIXELES_TINTA_CUERPO = 1200;
    private static final int MINIMO_PIXELES_TINTA_ZONA_FIRMA = 250;

    private static final double MINIMO_PORCENTAJE_TINTA_ENCABEZADO = 0.02;
    private static final double MINIMO_PORCENTAJE_TINTA_CUERPO = 0.03;
    private static final double MINIMO_PORCENTAJE_TINTA_ZONA_FIRMA = 0.015;

    public ValidarElementosVisualesResponse validarElementosVisuales(
            MultipartFile archivo,
            String tipoDocumento
    ) {
        validarParametros(archivo, tipoDocumento);

        try (PDDocument documento = Loader.loadPDF(archivo.getBytes())) {
            PDFRenderer renderer = new PDFRenderer(documento);

            List<PaginaElementoVisualResponse> detallePaginas = new ArrayList<>();

            for (int indicePagina = 0; indicePagina < documento.getNumberOfPages(); indicePagina++) {
                BufferedImage imagenPagina = renderer.renderImageWithDPI(indicePagina, DPI_RENDER, ImageType.RGB);

                PaginaElementoVisualResponse detallePagina =
                        analizarPagina(imagenPagina, indicePagina + 1, tipoDocumento);

                detallePaginas.add(detallePagina);
            }

            return construirResponse(archivo, tipoDocumento, detallePaginas);

        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo analizar visualmente el archivo PDF.");
        }
    }

    private PaginaElementoVisualResponse analizarPagina(
            BufferedImage imagenPagina,
            int numeroPagina,
            String tipoDocumento
    ) {
        PaginaElementoVisualResponse response = new PaginaElementoVisualResponse();
        response.setNumeroPaginaPdf(numeroPagina);

        boolean dimensionesValidas = validarDimensionesPagina(imagenPagina);
        response.setPaginaConDimensionesValidas(dimensionesValidas);

        ZonaRelativa zonaEncabezado = obtenerZonaEncabezado(tipoDocumento);
        ZonaRelativa zonaCuerpo = obtenerZonaCuerpo(tipoDocumento);
        ZonaRelativa zonaFirma = obtenerZonaFirma(tipoDocumento);

        AnalisisZona analisisEncabezado = analizarZona(imagenPagina, zonaEncabezado);
        AnalisisZona analisisCuerpo = analizarZona(imagenPagina, zonaCuerpo);
        AnalisisZona analisisFirma = analizarZona(imagenPagina, zonaFirma);

        response.setPixelesTintaEncabezado(analisisEncabezado.pixelesTinta());
        response.setPixelesTintaCuerpo(analisisCuerpo.pixelesTinta());
        response.setPixelesTintaZonaFirma(analisisFirma.pixelesTinta());

        response.setPorcentajeTintaEncabezado(analisisEncabezado.porcentajeTinta());
        response.setPorcentajeTintaCuerpo(analisisCuerpo.porcentajeTinta());
        response.setPorcentajeTintaZonaFirma(analisisFirma.porcentajeTinta());

        boolean encabezadoVisible = analisisEncabezado.pixelesTinta() >= MINIMO_PIXELES_TINTA_ENCABEZADO
                && analisisEncabezado.porcentajeTinta() >= MINIMO_PORCENTAJE_TINTA_ENCABEZADO;

        boolean cuerpoVisible = analisisCuerpo.pixelesTinta() >= MINIMO_PIXELES_TINTA_CUERPO
                && analisisCuerpo.porcentajeTinta() >= MINIMO_PORCENTAJE_TINTA_CUERPO;

        boolean zonaFirmaVisible = analisisFirma.pixelesTinta() >= MINIMO_PIXELES_TINTA_ZONA_FIRMA
                && analisisFirma.porcentajeTinta() >= MINIMO_PORCENTAJE_TINTA_ZONA_FIRMA;

        boolean qrVisible = existeQrLegible(imagenPagina);

        response.setEncabezadoVisible(encabezadoVisible);
        response.setCuerpoDocumentoVisible(cuerpoVisible);
        response.setZonaFirmaVisible(zonaFirmaVisible);
        response.setQrVisible(qrVisible);

        List<String> observaciones = construirObservacionesPagina(
                numeroPagina,
                dimensionesValidas,
                encabezadoVisible,
                cuerpoVisible,
                zonaFirmaVisible,
                qrVisible
        );

        response.setObservaciones(observaciones);
        response.setElementosVisualesCompletos(observaciones.isEmpty());

        return response;
    }

    private ValidarElementosVisualesResponse construirResponse(
            MultipartFile archivo,
            String tipoDocumento,
            List<PaginaElementoVisualResponse> detallePaginas
    ) {
        ValidarElementosVisualesResponse response = new ValidarElementosVisualesResponse();

        int paginasCompletas = 0;
        int paginasConObservaciones = 0;
        List<String> observacionesGenerales = new ArrayList<>();

        for (PaginaElementoVisualResponse pagina : detallePaginas) {
            if (pagina.isElementosVisualesCompletos()) {
                paginasCompletas++;
            } else {
                paginasConObservaciones++;
                observacionesGenerales.addAll(pagina.getObservaciones());
            }
        }

        boolean elementosValidos = paginasConObservaciones == 0;

        response.setElementosVisualesValidos(elementosValidos);
        response.setTipoDocumento(tipoDocumento);
        response.setNombreArchivo(archivo.getOriginalFilename());
        response.setNumeroPaginasAnalizadas(detallePaginas.size());
        response.setPaginasConElementosCompletos(paginasCompletas);
        response.setPaginasConObservaciones(paginasConObservaciones);
        response.setDetallePaginas(detallePaginas);
        response.setObservaciones(observacionesGenerales);

        if (elementosValidos) {
            response.setMensajeValidacion("El documento conserva los elementos visuales obligatorios.");
            response.setEstadoValidacionDocumental(ESTADO_ELEMENTOS_VISUALES_VALIDADOS);
            response.setPermiteNuevaCarga(false);
        } else {
            response.setMensajeValidacion("El documento presenta recortes o pérdida de elementos visuales obligatorios.");
            response.setEstadoValidacionDocumental(ESTADO_RECHAZADO_ELEMENTOS_VISUALES);
            response.setPermiteNuevaCarga(true);
        }

        return response;
    }

    private List<String> construirObservacionesPagina(
            int numeroPagina,
            boolean dimensionesValidas,
            boolean encabezadoVisible,
            boolean cuerpoVisible,
            boolean zonaFirmaVisible,
            boolean qrVisible
    ) {
        List<String> observaciones = new ArrayList<>();

        if (!dimensionesValidas) {
            observaciones.add("La página " + numeroPagina + " no presenta dimensiones mínimas adecuadas para validación visual.");
        }

        if (!encabezadoVisible) {
            observaciones.add("No se detecta correctamente el encabezado del documento en la página " + numeroPagina + ".");
        }

        if (!cuerpoVisible) {
            observaciones.add("No se detecta correctamente el cuerpo principal del documento en la página " + numeroPagina + ".");
        }

        if (!zonaFirmaVisible) {
            observaciones.add("No se detecta correctamente la zona de firma en la página " + numeroPagina + ".");
        }

        if (!qrVisible) {
            observaciones.add("No se detecta un código QR legible en la página " + numeroPagina + ".");
        }

        return observaciones;
    }

    private boolean validarDimensionesPagina(BufferedImage imagenPagina) {
        return imagenPagina.getWidth() >= ANCHO_MINIMO_RENDER
                && imagenPagina.getHeight() >= ALTO_MINIMO_RENDER;
    }

    private AnalisisZona analizarZona(
            BufferedImage imagenPagina,
            ZonaRelativa zonaRelativa
    ) {
        int xInicio = Math.max(0, (int) (imagenPagina.getWidth() * zonaRelativa.x()));
        int yInicio = Math.max(0, (int) (imagenPagina.getHeight() * zonaRelativa.y()));
        int ancho = Math.min(imagenPagina.getWidth() - xInicio, (int) (imagenPagina.getWidth() * zonaRelativa.ancho()));
        int alto = Math.min(imagenPagina.getHeight() - yInicio, (int) (imagenPagina.getHeight() * zonaRelativa.alto()));

        int totalPixeles = Math.max(ancho * alto, 1);
        int pixelesTinta = 0;

        for (int y = yInicio; y < yInicio + alto; y++) {
            for (int x = xInicio; x < xInicio + ancho; x++) {
                int rgb = imagenPagina.getRGB(x, y);

                if (esPixelConTinta(rgb)) {
                    pixelesTinta++;
                }
            }
        }

        double porcentajeTinta = (pixelesTinta * 100.0) / totalPixeles;

        return new AnalisisZona(pixelesTinta, porcentajeTinta);
    }

    private boolean existeQrLegible(BufferedImage imagenPagina) {
        try {
            BinaryBitmap bitmap = new BinaryBitmap(
                    new HybridBinarizer(
                            new BufferedImageLuminanceSource(imagenPagina)
                    )
            );

            new MultiFormatReader().decode(bitmap);
            return true;

        } catch (NotFoundException e) {
            return false;
        }
    }

    private boolean esPixelConTinta(int rgb) {
        int rojo = (rgb >> 16) & 0xFF;
        int verde = (rgb >> 8) & 0xFF;
        int azul = rgb & 0xFF;

        int promedio = (rojo + verde + azul) / 3;
        int maximo = Math.max(rojo, Math.max(verde, azul));
        int minimo = Math.min(rojo, Math.min(verde, azul));
        int saturacion = maximo - minimo;

        boolean tintaOscura = promedio < 215 && saturacion > 10;
        boolean lineaNegraGris = promedio < 190;
        boolean tintaAzul = azul > rojo + 20 && azul > verde + 5 && promedio < 240;

        return tintaOscura || lineaNegraGris || tintaAzul;
    }

    private ZonaRelativa obtenerZonaEncabezado(String tipoDocumento) {
        if ("FORMULARIO_6012".equalsIgnoreCase(tipoDocumento)) {
            return new ZonaRelativa(0.03, 0.03, 0.94, 0.14);
        }

        if ("AUTORIZACION_DESCUENTO".equalsIgnoreCase(tipoDocumento)) {
            return new ZonaRelativa(0.05, 0.04, 0.90, 0.18);
        }

        return new ZonaRelativa(0.03, 0.03, 0.94, 0.15);
    }

    private ZonaRelativa obtenerZonaCuerpo(String tipoDocumento) {
        if ("FORMULARIO_6012".equalsIgnoreCase(tipoDocumento)) {
            return new ZonaRelativa(0.03, 0.18, 0.94, 0.55);
        }

        if ("AUTORIZACION_DESCUENTO".equalsIgnoreCase(tipoDocumento)) {
            return new ZonaRelativa(0.05, 0.22, 0.90, 0.48);
        }

        return new ZonaRelativa(0.03, 0.18, 0.94, 0.55);
    }

    private ZonaRelativa obtenerZonaFirma(String tipoDocumento) {
        if ("FORMULARIO_6012".equalsIgnoreCase(tipoDocumento)) {
            return new ZonaRelativa(0.05, 0.75, 0.90, 0.18);
        }

        if ("AUTORIZACION_DESCUENTO".equalsIgnoreCase(tipoDocumento)) {
            return new ZonaRelativa(0.15, 0.62, 0.70, 0.22);
        }

        return new ZonaRelativa(0.05, 0.75, 0.90, 0.18);
    }

    private void validarParametros(
            MultipartFile archivo,
            String tipoDocumento
    ) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF es obligatorio.");
        }

        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }

        if (!"FORMULARIO_6012".equalsIgnoreCase(tipoDocumento)
                && !"AUTORIZACION_DESCUENTO".equalsIgnoreCase(tipoDocumento)) {
            throw new IllegalArgumentException("El tipo de documento informado no es válido.");
        }
    }

    private record ZonaRelativa(
            double x,
            double y,
            double ancho,
            double alto
    ) {
    }

    private record AnalisisZona(
            int pixelesTinta,
            double porcentajeTinta
    ) {
    }
}