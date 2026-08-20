package essalud.gob.pe.wsseguroscomplementario.documento.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ValidacionElementosVisualesService {

    private static final float DPI_RENDER = 220f;

    private static final String ESTADO_ELEMENTOS_VISUALES_VALIDADOS =
            "ELEMENTOS_VISUALES_VALIDADOS";

    private static final String ESTADO_RECHAZADO_ELEMENTOS_VISUALES =
            "RECHAZADO_ELEMENTOS_VISUALES";

    private static final int ANCHO_MINIMO_RENDER = 1000;
    private static final int ALTO_MINIMO_RENDER = 1300;

    private static final int MINIMO_PIXELES_TINTA_ENCABEZADO = 350;
    private static final int MINIMO_PIXELES_TINTA_CUERPO = 1200;
    private static final int MINIMO_PIXELES_TINTA_ZONA_FIRMA = 250;

    private static final double MINIMO_PORCENTAJE_TINTA_ENCABEZADO =
            0.02;

    private static final double MINIMO_PORCENTAJE_TINTA_CUERPO =
            0.03;

    private static final double MINIMO_PORCENTAJE_TINTA_ZONA_FIRMA =
            0.015;

    /*
     * Dimensiones exactas de la plantilla A4 de la
     * Autorización de Descuento, expresadas en puntos PDF.
     */
    private static final double ANCHO_AUTORIZACION_PUNTOS =
            595.32d;

    private static final double ALTO_AUTORIZACION_PUNTOS =
            841.92d;

    /*
     * Coordenadas exactas de la zona de firma de la
     * Autorización de Descuento.
     *
     * Sistema PDF:
     * origen en la esquina inferior izquierda.
     */
    private static final double FIRMA_AUTORIZACION_X_MIN =
            74.0d;

    private static final double FIRMA_AUTORIZACION_Y_MIN =
            424.0d;

    private static final double FIRMA_AUTORIZACION_X_MAX =
            224.0d;

    private static final double FIRMA_AUTORIZACION_Y_MAX =
            538.0d;

    public ValidarElementosVisualesResponse validarElementosVisuales(
            MultipartFile archivo,
            String tipoDocumento
    ) {
        validarParametros(
                archivo,
                tipoDocumento
        );

        try (
                PDDocument documento =
                        Loader.loadPDF(
                                archivo.getBytes()
                        )
        ) {
            PDFRenderer renderer =
                    new PDFRenderer(documento);

            List<PaginaElementoVisualResponse> detallePaginas =
                    new ArrayList<>();

            for (
                    int indicePagina = 0;
                    indicePagina < documento.getNumberOfPages();
                    indicePagina++
            ) {
                BufferedImage imagenPagina =
                        renderer.renderImageWithDPI(
                                indicePagina,
                                DPI_RENDER,
                                ImageType.RGB
                        );

                PaginaElementoVisualResponse detallePagina =
                        analizarPagina(
                                imagenPagina,
                                indicePagina + 1,
                                tipoDocumento
                        );

                detallePaginas.add(
                        detallePagina
                );
            }

            return construirResponse(
                    archivo,
                    tipoDocumento,
                    detallePaginas
            );

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "No se pudo analizar visualmente el archivo PDF.",
                    e
            );
        }
    }

    private PaginaElementoVisualResponse analizarPagina(
            BufferedImage imagenPagina,
            int numeroPagina,
            String tipoDocumento
    ) {
        PaginaElementoVisualResponse response =
                new PaginaElementoVisualResponse();

        response.setNumeroPaginaPdf(
                numeroPagina
        );

        boolean dimensionesValidas =
                validarDimensionesPagina(
                        imagenPagina
                );

        response.setPaginaConDimensionesValidas(
                dimensionesValidas
        );

        ZonaRelativa zonaEncabezado =
                obtenerZonaEncabezado(
                        tipoDocumento
                );

        ZonaRelativa zonaCuerpo =
                obtenerZonaCuerpo(
                        tipoDocumento
                );

        ZonaRelativa zonaFirma =
                obtenerZonaFirma(
                        tipoDocumento
                );

        AnalisisZona analisisEncabezado =
                analizarZona(
                        imagenPagina,
                        zonaEncabezado
                );

        AnalisisZona analisisCuerpo =
                analizarZona(
                        imagenPagina,
                        zonaCuerpo
                );

        AnalisisZona analisisFirma =
                analizarZona(
                        imagenPagina,
                        zonaFirma
                );

        response.setPixelesTintaEncabezado(
                analisisEncabezado.pixelesTinta()
        );

        response.setPixelesTintaCuerpo(
                analisisCuerpo.pixelesTinta()
        );

        response.setPixelesTintaZonaFirma(
                analisisFirma.pixelesTinta()
        );

        response.setPorcentajeTintaEncabezado(
                analisisEncabezado.porcentajeTinta()
        );

        response.setPorcentajeTintaCuerpo(
                analisisCuerpo.porcentajeTinta()
        );

        response.setPorcentajeTintaZonaFirma(
                analisisFirma.porcentajeTinta()
        );

        boolean encabezadoVisible =
                analisisEncabezado.pixelesTinta()
                        >= MINIMO_PIXELES_TINTA_ENCABEZADO
                        && analisisEncabezado.porcentajeTinta()
                        >= MINIMO_PORCENTAJE_TINTA_ENCABEZADO;

        boolean cuerpoVisible =
                analisisCuerpo.pixelesTinta()
                        >= MINIMO_PIXELES_TINTA_CUERPO
                        && analisisCuerpo.porcentajeTinta()
                        >= MINIMO_PORCENTAJE_TINTA_CUERPO;

        boolean zonaFirmaVisible =
                analisisFirma.pixelesTinta()
                        >= MINIMO_PIXELES_TINTA_ZONA_FIRMA
                        && analisisFirma.porcentajeTinta()
                        >= MINIMO_PORCENTAJE_TINTA_ZONA_FIRMA;

        boolean qrVisible =
                existeQrLegible(
                        imagenPagina,
                        tipoDocumento
                );

        response.setEncabezadoVisible(
                encabezadoVisible
        );

        response.setCuerpoDocumentoVisible(
                cuerpoVisible
        );

        response.setZonaFirmaVisible(
                zonaFirmaVisible
        );

        response.setQrVisible(
                qrVisible
        );

        List<String> observaciones =
                construirObservacionesPagina(
                        numeroPagina,
                        dimensionesValidas,
                        encabezadoVisible,
                        cuerpoVisible,
                        zonaFirmaVisible,
                        qrVisible
                );

        response.setObservaciones(
                observaciones
        );

        response.setElementosVisualesCompletos(
                observaciones.isEmpty()
        );

        return response;
    }

    private ValidarElementosVisualesResponse construirResponse(
            MultipartFile archivo,
            String tipoDocumento,
            List<PaginaElementoVisualResponse> detallePaginas
    ) {
        ValidarElementosVisualesResponse response =
                new ValidarElementosVisualesResponse();

        int paginasCompletas = 0;
        int paginasConObservaciones = 0;

        List<String> observacionesGenerales =
                new ArrayList<>();

        for (
                PaginaElementoVisualResponse pagina :
                detallePaginas
        ) {
            if (
                    pagina.isElementosVisualesCompletos()
            ) {
                paginasCompletas++;

            } else {
                paginasConObservaciones++;

                observacionesGenerales.addAll(
                        pagina.getObservaciones()
                );
            }
        }

        boolean elementosValidos =
                paginasConObservaciones == 0;

        response.setElementosVisualesValidos(
                elementosValidos
        );

        response.setTipoDocumento(
                tipoDocumento
        );

        response.setNombreArchivo(
                archivo.getOriginalFilename()
        );

        response.setNumeroPaginasAnalizadas(
                detallePaginas.size()
        );

        response.setPaginasConElementosCompletos(
                paginasCompletas
        );

        response.setPaginasConObservaciones(
                paginasConObservaciones
        );

        response.setDetallePaginas(
                detallePaginas
        );

        response.setObservaciones(
                observacionesGenerales
        );

        if (elementosValidos) {
            response.setMensajeValidacion(
                    "El documento conserva los elementos visuales obligatorios."
            );

            response.setEstadoValidacionDocumental(
                    ESTADO_ELEMENTOS_VISUALES_VALIDADOS
            );

            response.setPermiteNuevaCarga(
                    false
            );

        } else {
            response.setMensajeValidacion(
                    "El documento presenta recortes o pérdida de elementos visuales obligatorios."
            );

            response.setEstadoValidacionDocumental(
                    ESTADO_RECHAZADO_ELEMENTOS_VISUALES
            );

            response.setPermiteNuevaCarga(
                    true
            );
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
        List<String> observaciones =
                new ArrayList<>();

        if (!dimensionesValidas) {
            observaciones.add(
                    "La página "
                            + numeroPagina
                            + " no presenta dimensiones mínimas adecuadas para validación visual."
            );
        }

        if (!encabezadoVisible) {
            observaciones.add(
                    "No se detecta correctamente el encabezado del documento en la página "
                            + numeroPagina
                            + "."
            );
        }

        if (!cuerpoVisible) {
            observaciones.add(
                    "No se detecta correctamente el cuerpo principal del documento en la página "
                            + numeroPagina
                            + "."
            );
        }

        if (!zonaFirmaVisible) {
            observaciones.add(
                    "No se detecta correctamente la zona de firma en la página "
                            + numeroPagina
                            + "."
            );
        }

        if (!qrVisible) {
            observaciones.add(
                    "No se detecta un código QR legible en la página "
                            + numeroPagina
                            + "."
            );
        }

        return observaciones;
    }

    private boolean validarDimensionesPagina(
            BufferedImage imagenPagina
    ) {
        return imagenPagina.getWidth()
                >= ANCHO_MINIMO_RENDER
                && imagenPagina.getHeight()
                >= ALTO_MINIMO_RENDER;
    }

    private AnalisisZona analizarZona(
            BufferedImage imagenPagina,
            ZonaRelativa zonaRelativa
    ) {
        int anchoPagina =
                imagenPagina.getWidth();

        int altoPagina =
                imagenPagina.getHeight();

        int xInicio =
                (int) Math.round(
                        anchoPagina
                                * zonaRelativa.x()
                );

        int yInicio =
                (int) Math.round(
                        altoPagina
                                * zonaRelativa.y()
                );

        int xFin =
                (int) Math.round(
                        anchoPagina
                                * (
                                zonaRelativa.x()
                                        + zonaRelativa.ancho()
                        )
                );

        int yFin =
                (int) Math.round(
                        altoPagina
                                * (
                                zonaRelativa.y()
                                        + zonaRelativa.alto()
                        )
                );

        xInicio = limitarValor(
                xInicio,
                0,
                Math.max(anchoPagina - 1, 0)
        );

        yInicio = limitarValor(
                yInicio,
                0,
                Math.max(altoPagina - 1, 0)
        );

        xFin = limitarValor(
                xFin,
                xInicio + 1,
                anchoPagina
        );

        yFin = limitarValor(
                yFin,
                yInicio + 1,
                altoPagina
        );

        int ancho =
                Math.max(
                        xFin - xInicio,
                        1
                );

        int alto =
                Math.max(
                        yFin - yInicio,
                        1
                );

        int totalPixeles =
                Math.max(
                        ancho * alto,
                        1
                );

        int pixelesTinta = 0;

        for (
                int y = yInicio;
                y < yFin;
                y++
        ) {
            for (
                    int x = xInicio;
                    x < xFin;
                    x++
            ) {
                int rgb =
                        imagenPagina.getRGB(
                                x,
                                y
                        );

                if (esPixelConTinta(rgb)) {
                    pixelesTinta++;
                }
            }
        }

        double porcentajeTinta =
                (
                        pixelesTinta * 100.0
                )
                        / totalPixeles;

        return new AnalisisZona(
                pixelesTinta,
                porcentajeTinta
        );
    }

    private boolean existeQrLegible(
            BufferedImage imagenPagina,
            String tipoDocumento
    ) {
        Map<DecodeHintType, Object> hints =
                new EnumMap<>(
                        DecodeHintType.class
                );

        hints.put(
                DecodeHintType.POSSIBLE_FORMATS,
                List.of(
                        BarcodeFormat.QR_CODE
                )
        );

        hints.put(
                DecodeHintType.TRY_HARDER,
                Boolean.TRUE
        );

        /*
         * Intento 1:
         * búsqueda sobre la página completa.
         */
        if (
                decodificarQr(
                        imagenPagina,
                        hints
                )
        ) {
            return true;
        }

        /*
         * Intento 2:
         * búsqueda dentro de la zona específica
         * utilizada por cada plantilla.
         */
        ZonaRelativa zonaQr =
                obtenerZonaQr(
                        tipoDocumento
                );

        if (zonaQr == null) {
            return false;
        }

        BufferedImage imagenZonaQr =
                recortarZonaRelativa(
                        imagenPagina,
                        zonaQr
                );

        if (
                decodificarQr(
                        imagenZonaQr,
                        hints
                )
        ) {
            return true;
        }

        /*
         * Intento 3:
         * ampliar el recorte al doble mediante
         * interpolación de vecino más cercano.
         */
        BufferedImage imagenQrDoble =
                escalarImagen(
                        imagenZonaQr,
                        2
                );

        if (
                decodificarQr(
                        imagenQrDoble,
                        hints
                )
        ) {
            return true;
        }

        /*
         * Intento 4:
         * ampliar el recorte al triple.
         */
        BufferedImage imagenQrTriple =
                escalarImagen(
                        imagenZonaQr,
                        3
                );

        return decodificarQr(
                imagenQrTriple,
                hints
        );
    }

    private boolean decodificarQr(
            BufferedImage imagen,
            Map<DecodeHintType, Object> hints
    ) {
        if (
                imagen == null
                        || imagen.getWidth() <= 0
                        || imagen.getHeight() <= 0
        ) {
            return false;
        }

        MultiFormatReader lector =
                new MultiFormatReader();

        try {
            BinaryBitmap bitmap =
                    new BinaryBitmap(
                            new HybridBinarizer(
                                    new BufferedImageLuminanceSource(
                                            imagen
                                    )
                            )
                    );

            Result resultado =
                    lector.decode(
                            bitmap,
                            hints
                    );

            return resultado != null
                    && resultado.getText() != null
                    && !resultado
                    .getText()
                    .trim()
                    .isEmpty();

        } catch (NotFoundException e) {
            return false;

        } finally {
            lector.reset();
        }
    }

    private BufferedImage recortarZonaRelativa(
            BufferedImage imagen,
            ZonaRelativa zonaRelativa
    ) {
        int anchoPagina =
                imagen.getWidth();

        int altoPagina =
                imagen.getHeight();

        int xInicio =
                (int) Math.round(
                        anchoPagina
                                * zonaRelativa.x()
                );

        int yInicio =
                (int) Math.round(
                        altoPagina
                                * zonaRelativa.y()
                );

        int xFin =
                (int) Math.round(
                        anchoPagina
                                * (
                                zonaRelativa.x()
                                        + zonaRelativa.ancho()
                        )
                );

        int yFin =
                (int) Math.round(
                        altoPagina
                                * (
                                zonaRelativa.y()
                                        + zonaRelativa.alto()
                        )
                );

        xInicio = limitarValor(
                xInicio,
                0,
                Math.max(anchoPagina - 1, 0)
        );

        yInicio = limitarValor(
                yInicio,
                0,
                Math.max(altoPagina - 1, 0)
        );

        xFin = limitarValor(
                xFin,
                xInicio + 1,
                anchoPagina
        );

        yFin = limitarValor(
                yFin,
                yInicio + 1,
                altoPagina
        );

        int ancho =
                Math.max(
                        xFin - xInicio,
                        1
                );

        int alto =
                Math.max(
                        yFin - yInicio,
                        1
                );

        BufferedImage recorte =
                new BufferedImage(
                        ancho,
                        alto,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics =
                recorte.createGraphics();

        try {
            graphics.drawImage(
                    imagen,
                    0,
                    0,
                    ancho,
                    alto,
                    xInicio,
                    yInicio,
                    xFin,
                    yFin,
                    null
            );

        } finally {
            graphics.dispose();
        }

        return recorte;
    }

    private BufferedImage escalarImagen(
            BufferedImage imagenOriginal,
            int factor
    ) {
        if (factor <= 1) {
            return imagenOriginal;
        }

        int nuevoAncho =
                Math.max(
                        imagenOriginal.getWidth()
                                * factor,
                        1
                );

        int nuevoAlto =
                Math.max(
                        imagenOriginal.getHeight()
                                * factor,
                        1
                );

        BufferedImage imagenEscalada =
                new BufferedImage(
                        nuevoAncho,
                        nuevoAlto,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics =
                imagenEscalada.createGraphics();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF
            );

            graphics.drawImage(
                    imagenOriginal,
                    0,
                    0,
                    nuevoAncho,
                    nuevoAlto,
                    null
            );

        } finally {
            graphics.dispose();
        }

        return imagenEscalada;
    }

    private int limitarValor(
            int valor,
            int minimo,
            int maximo
    ) {
        if (maximo < minimo) {
            return minimo;
        }

        return Math.max(
                minimo,
                Math.min(
                        maximo,
                        valor
                )
        );
    }

    private boolean esPixelConTinta(
            int rgb
    ) {
        int rojo =
                (rgb >> 16) & 0xFF;

        int verde =
                (rgb >> 8) & 0xFF;

        int azul =
                rgb & 0xFF;

        int promedio =
                (rojo + verde + azul)
                        / 3;

        int maximo =
                Math.max(
                        rojo,
                        Math.max(
                                verde,
                                azul
                        )
                );

        int minimo =
                Math.min(
                        rojo,
                        Math.min(
                                verde,
                                azul
                        )
                );

        int saturacion =
                maximo - minimo;

        boolean tintaOscura =
                promedio < 215
                        && saturacion > 10;

        boolean lineaNegraGris =
                promedio < 190;

        boolean tintaAzul =
                azul > rojo + 20
                        && azul > verde + 5
                        && promedio < 240;

        return tintaOscura
                || lineaNegraGris
                || tintaAzul;
    }

    private ZonaRelativa obtenerZonaEncabezado(
            String tipoDocumento
    ) {
        if (
                "FORMULARIO_6012"
                        .equalsIgnoreCase(tipoDocumento)
        ) {
            return new ZonaRelativa(
                    0.03,
                    0.03,
                    0.94,
                    0.14
            );
        }

        if (
                "AUTORIZACION_DESCUENTO"
                        .equalsIgnoreCase(tipoDocumento)
        ) {
            return new ZonaRelativa(
                    0.05,
                    0.04,
                    0.90,
                    0.18
            );
        }

        return new ZonaRelativa(
                0.03,
                0.03,
                0.94,
                0.15
        );
    }

    private ZonaRelativa obtenerZonaCuerpo(
            String tipoDocumento
    ) {
        if (
                "FORMULARIO_6012"
                        .equalsIgnoreCase(tipoDocumento)
        ) {
            return new ZonaRelativa(
                    0.03,
                    0.18,
                    0.94,
                    0.55
            );
        }

        if (
                "AUTORIZACION_DESCUENTO"
                        .equalsIgnoreCase(tipoDocumento)
        ) {
            return new ZonaRelativa(
                    0.05,
                    0.22,
                    0.90,
                    0.48
            );
        }

        return new ZonaRelativa(
                0.03,
                0.18,
                0.94,
                0.55
        );
    }

    private ZonaRelativa obtenerZonaFirma(
            String tipoDocumento
    ) {
        if (
                "FORMULARIO_6012"
                        .equalsIgnoreCase(tipoDocumento)
        ) {
            return new ZonaRelativa(
                    0.05,
                    0.75,
                    0.90,
                    0.18
            );
        }

        if (
                "AUTORIZACION_DESCUENTO"
                        .equalsIgnoreCase(tipoDocumento)
        ) {
            return convertirZonaPdfAZonaRelativa(
                    FIRMA_AUTORIZACION_X_MIN,
                    FIRMA_AUTORIZACION_Y_MIN,
                    FIRMA_AUTORIZACION_X_MAX,
                    FIRMA_AUTORIZACION_Y_MAX,
                    ANCHO_AUTORIZACION_PUNTOS,
                    ALTO_AUTORIZACION_PUNTOS
            );
        }

        return new ZonaRelativa(
                0.05,
                0.75,
                0.90,
                0.18
        );
    }

    private ZonaRelativa obtenerZonaQr(
            String tipoDocumento
    ) {
        if (
                "FORMULARIO_6012"
                        .equalsIgnoreCase(tipoDocumento)
        ) {
            /*
             * QR ubicado en la esquina superior derecha
             * del Formulario 6012.
             */
            return new ZonaRelativa(
                    0.78,
                    0.00,
                    0.22,
                    0.20
            );
        }

        if (
                "AUTORIZACION_DESCUENTO"
                        .equalsIgnoreCase(tipoDocumento)
        ) {
            /*
             * QR ubicado en el sector medio derecho
             * de la Autorización de Descuento.
             */
            return new ZonaRelativa(
                    0.68,
                    0.38,
                    0.31,
                    0.30
            );
        }

        return null;
    }

    private ZonaRelativa convertirZonaPdfAZonaRelativa(
            double xMin,
            double yMin,
            double xMax,
            double yMax,
            double anchoPaginaPuntos,
            double altoPaginaPuntos
    ) {
        if (
                anchoPaginaPuntos <= 0
                        || altoPaginaPuntos <= 0
        ) {
            throw new IllegalArgumentException(
                    "Las dimensiones de la página PDF deben ser mayores que cero."
            );
        }

        if (
                xMin < 0
                        || yMin < 0
                        || xMax <= xMin
                        || yMax <= yMin
                        || xMax > anchoPaginaPuntos
                        || yMax > altoPaginaPuntos
        ) {
            throw new IllegalArgumentException(
                    "Las coordenadas de la zona PDF no son válidas."
            );
        }

        double xRelativo =
                xMin
                        / anchoPaginaPuntos;

        /*
         * El PDF utiliza origen inferior izquierdo.
         * BufferedImage utiliza origen superior izquierdo.
         *
         * Se utiliza yMax para obtener el borde superior
         * de la región en el sistema de BufferedImage.
         */
        double yRelativo =
                (
                        altoPaginaPuntos - yMax
                )
                        / altoPaginaPuntos;

        double anchoRelativo =
                (
                        xMax - xMin
                )
                        / anchoPaginaPuntos;

        double altoRelativo =
                (
                        yMax - yMin
                )
                        / altoPaginaPuntos;

        return new ZonaRelativa(
                xRelativo,
                yRelativo,
                anchoRelativo,
                altoRelativo
        );
    }

    private void validarParametros(
            MultipartFile archivo,
            String tipoDocumento
    ) {
        if (
                archivo == null
                        || archivo.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "El archivo PDF es obligatorio."
            );
        }

        if (
                tipoDocumento == null
                        || tipoDocumento.trim().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "El tipo de documento es obligatorio."
            );
        }

        if (
                !"FORMULARIO_6012"
                        .equalsIgnoreCase(tipoDocumento)
                        && !"AUTORIZACION_DESCUENTO"
                        .equalsIgnoreCase(tipoDocumento)
        ) {
            throw new IllegalArgumentException(
                    "El tipo de documento informado no es válido."
            );
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