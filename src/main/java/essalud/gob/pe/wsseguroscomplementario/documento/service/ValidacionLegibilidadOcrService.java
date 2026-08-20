package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.documento.dto.PaginaLegibilidadOcrResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarLegibilidadOcrResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.TipoDocumentoDigital;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidacionLegibilidadOcrService {

    private static final int DPI_RENDER =
            220;

    /*
     * UMBRALES PROVISIONALES.
     *
     * Deben calibrarse posteriormente con:
     * - PDF claramente legible.
     * - PDF moderadamente borroso.
     * - PDF claramente ilegible.
     *
     * No considerarlos todavía parámetros
     * institucionales definitivos.
     */
    private static final int
            MINIMO_CARACTERES_ALFANUMERICOS =
            60;

    private static final int
            MINIMO_PALABRAS_RECONOCIBLES =
            10;

    private static final double
            MINIMO_NITIDEZ_GENERAL =
            35.0;

    private static final double
            MINIMO_NITIDEZ_ZONA_CRITICA =
            18.0;

    private static final int
            PASO_ANALISIS_NITIDEZ =
            2;

    private static final String
            ESTADO_LEGIBILIDAD_VALIDADA =
            "LEGIBILIDAD_OCR_VALIDADA";

    private static final String
            ESTADO_RECHAZADO_LEGIBILIDAD =
            "RECHAZADO_LEGIBILIDAD_OCR";

    private final String tessdataPath;

    public ValidacionLegibilidadOcrService(
            @Value(
                    "${integraciones.ocr.tessdata-path:tessdata}"
            )
            String tessdataPath
    ) {
        this.tessdataPath =
                tessdataPath;
    }

    public ValidarLegibilidadOcrResponse
    validarLegibilidad(
            MultipartFile archivo,
            String tipoDocumento
    ) {

        validarParametros(
                archivo,
                tipoDocumento
        );

        validarConfiguracionOcr();

        String tipoNormalizado =
                tipoDocumento
                        .trim()
                        .toUpperCase();

        try (
                PDDocument documento =
                        Loader.loadPDF(
                                archivo.getBytes()
                        )
        ) {

            PDFRenderer renderer =
                    new PDFRenderer(
                            documento
                    );

            Tesseract tesseract =
                    construirTesseract();

            List<PaginaLegibilidadOcrResponse>
                    detallePaginas =
                    new ArrayList<>();

            for (
                    int indicePagina = 0;
                    indicePagina
                            < documento.getNumberOfPages();
                    indicePagina++
            ) {

                BufferedImage imagenPagina =
                        renderer.renderImageWithDPI(
                                indicePagina,
                                DPI_RENDER,
                                ImageType.RGB
                        );

                PaginaLegibilidadOcrResponse detalle =
                        analizarPagina(
                                imagenPagina,
                                indicePagina + 1,
                                tipoNormalizado,
                                tesseract
                        );

                detallePaginas.add(
                        detalle
                );
            }

            return construirResponse(
                    tipoNormalizado,
                    detallePaginas
            );

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "No se pudo leer el PDF para validar su legibilidad.",
                    e
            );
        }
    }

    private PaginaLegibilidadOcrResponse
    analizarPagina(
            BufferedImage imagenPagina,
            int numeroPagina,
            String tipoDocumento,
            Tesseract tesseract
    ) {

        String textoReconocido;

        try {

            textoReconocido =
                    tesseract.doOCR(
                            imagenPagina
                    );

        } catch (TesseractException e) {

            throw new IllegalStateException(
                    "El motor OCR no pudo analizar la página "
                            + numeroPagina
                            + ".",
                    e
            );
        }

        int caracteresAlfanumericos =
                contarCaracteresAlfanumericos(
                        textoReconocido
                );

        int palabrasReconocibles =
                contarPalabrasReconocibles(
                        textoReconocido
                );

        double nitidezGeneral =
                calcularVarianzaLaplaciano(
                        imagenPagina,
                        new ZonaRelativa(
                                0.03,
                                0.03,
                                0.94,
                                0.85
                        )
                );

        double nitidezMinimaZonaCritica =
                calcularNitidezMinimaZonaCritica(
                        imagenPagina,
                        tipoDocumento
                );

        boolean textoPrincipalReconocible =
                caracteresAlfanumericos
                        >= MINIMO_CARACTERES_ALFANUMERICOS

                        && palabrasReconocibles
                        >= MINIMO_PALABRAS_RECONOCIBLES;

        /*
         * Una zona crítica se considera borrosa
         * cuando su nivel mínimo de nitidez está
         * por debajo del umbral establecido.
         *
         * La nitidez general se conserva como
         * métrica diagnóstica, pero no impide
         * detectar una zona textual crítica
         * borrosa aunque existan elementos muy
         * nítidos, como el código QR.
         */
        boolean zonaCriticaBorrosa =
                nitidezMinimaZonaCritica
                        < MINIMO_NITIDEZ_ZONA_CRITICA;

        boolean paginaLegible =
                textoPrincipalReconocible
                        && !zonaCriticaBorrosa;

        PaginaLegibilidadOcrResponse response =
                new PaginaLegibilidadOcrResponse();

        response.setNumeroPaginaPdf(
                numeroPagina
        );

        response.setPaginaLegible(
                paginaLegible
        );

        response.setTextoPrincipalReconocible(
                textoPrincipalReconocible
        );

        response.setZonaCriticaBorrosa(
                zonaCriticaBorrosa
        );

        response.setCaracteresAlfanumericosReconocidos(
                caracteresAlfanumericos
        );

        response.setPalabrasReconocibles(
                palabrasReconocibles
        );

        response.setPuntuacionNitidezGeneral(
                redondear(
                        nitidezGeneral
                )
        );

        response.setPuntuacionNitidezMinimaZonaCritica(
                redondear(
                        nitidezMinimaZonaCritica
                )
        );

        response.setObservacion(
                construirObservacionPagina(
                        numeroPagina,
                        textoPrincipalReconocible,
                        zonaCriticaBorrosa
                )
        );

        return response;
    }

    private ValidarLegibilidadOcrResponse
    construirResponse(
            String tipoDocumento,
            List<PaginaLegibilidadOcrResponse>
                    detallePaginas
    ) {

        ValidarLegibilidadOcrResponse response =
                new ValidarLegibilidadOcrResponse();

        int paginasLegibles = 0;

        List<String> observaciones =
                new ArrayList<>();

        for (
                PaginaLegibilidadOcrResponse pagina :
                detallePaginas
        ) {

            if (pagina.isPaginaLegible()) {

                paginasLegibles++;

            } else {

                observaciones.add(
                        pagina.getObservacion()
                );
            }
        }

        int paginasIlegibles =
                detallePaginas.size()
                        - paginasLegibles;

        boolean legibilidadValida =
                !detallePaginas.isEmpty()
                        && paginasIlegibles == 0;

        response.setLegibilidadValida(
                legibilidadValida
        );

        response.setTipoDocumento(
                tipoDocumento
        );

        response.setNumeroPaginasAnalizadas(
                detallePaginas.size()
        );

        response.setPaginasLegibles(
                paginasLegibles
        );

        response.setPaginasIlegibles(
                paginasIlegibles
        );

        response.setDetallePaginas(
                detallePaginas
        );

        response.setObservaciones(
                observaciones
        );

        if (legibilidadValida) {

            response.setMensajeValidacion(
                    "El texto principal del documento presenta legibilidad suficiente."
            );

            response.setEstadoValidacionDocumental(
                    ESTADO_LEGIBILIDAD_VALIDADA
            );

            response.setPermiteNuevaCarga(
                    false
            );

        } else {

            response.setMensajeValidacion(
                    "El documento presenta problemas de legibilidad que impiden reconocer adecuadamente su contenido principal."
            );

            response.setEstadoValidacionDocumental(
                    ESTADO_RECHAZADO_LEGIBILIDAD
            );

            response.setPermiteNuevaCarga(
                    true
            );
        }

        return response;
    }

    private double calcularNitidezMinimaZonaCritica(
            BufferedImage imagen,
            String tipoDocumento
    ) {

        ZonaRelativa zonaPrincipal =
                obtenerZonaTextoPrincipal(
                        tipoDocumento
                );

        double altoBanda =
                zonaPrincipal.alto()
                        / 3.0;

        double minima =
                Double.MAX_VALUE;

        for (
                int i = 0;
                i < 3;
                i++
        ) {

            ZonaRelativa banda =
                    new ZonaRelativa(
                            zonaPrincipal.x(),
                            zonaPrincipal.y()
                                    + altoBanda * i,
                            zonaPrincipal.ancho(),
                            altoBanda
                    );

            double puntuacion =
                    calcularVarianzaLaplaciano(
                            imagen,
                            banda
                    );

            minima =
                    Math.min(
                            minima,
                            puntuacion
                    );
        }

        return minima
                == Double.MAX_VALUE
                ? 0.0
                : minima;
    }

    private ZonaRelativa obtenerZonaTextoPrincipal(
            String tipoDocumento
    ) {

        if (
                "FORMULARIO_6012"
                        .equalsIgnoreCase(
                                tipoDocumento
                        )
        ) {

            return new ZonaRelativa(
                    0.03,
                    0.06,
                    0.94,
                    0.68
            );
        }

        if (
                "AUTORIZACION_DESCUENTO"
                        .equalsIgnoreCase(
                                tipoDocumento
                        )
        ) {

            return new ZonaRelativa(
                    0.05,
                    0.08,
                    0.90,
                    0.62
            );
        }

        return new ZonaRelativa(
                0.05,
                0.08,
                0.90,
                0.68
        );
    }

    /*
     * Estimación de nitidez mediante
     * varianza del Laplaciano.
     *
     * Una imagen con bordes definidos produce
     * mayor variación.
     *
     * Una imagen fuertemente desenfocada
     * produce menor variación.
     */
    private double calcularVarianzaLaplaciano(
            BufferedImage imagen,
            ZonaRelativa zona
    ) {

        int ancho =
                imagen.getWidth();

        int alto =
                imagen.getHeight();

        int xInicio =
                limitar(
                        (int) Math.round(
                                ancho * zona.x()
                        ),
                        1,
                        Math.max(
                                ancho - 2,
                                1
                        )
                );

        int yInicio =
                limitar(
                        (int) Math.round(
                                alto * zona.y()
                        ),
                        1,
                        Math.max(
                                alto - 2,
                                1
                        )
                );

        int xFin =
                limitar(
                        (int) Math.round(
                                ancho
                                        * (
                                        zona.x()
                                                + zona.ancho()
                                )
                        ),
                        xInicio + 1,
                        Math.max(
                                ancho - 1,
                                xInicio + 1
                        )
                );

        int yFin =
                limitar(
                        (int) Math.round(
                                alto
                                        * (
                                        zona.y()
                                                + zona.alto()
                                )
                        ),
                        yInicio + 1,
                        Math.max(
                                alto - 1,
                                yInicio + 1
                        )
                );

        double suma = 0.0;
        double sumaCuadrados = 0.0;

        long cantidad = 0;

        for (
                int y = yInicio;
                y < yFin;
                y += PASO_ANALISIS_NITIDEZ
        ) {

            for (
                    int x = xInicio;
                    x < xFin;
                    x += PASO_ANALISIS_NITIDEZ
            ) {

                double centro =
                        luminancia(
                                imagen.getRGB(
                                        x,
                                        y
                                )
                        );

                double arriba =
                        luminancia(
                                imagen.getRGB(
                                        x,
                                        y - 1
                                )
                        );

                double abajo =
                        luminancia(
                                imagen.getRGB(
                                        x,
                                        y + 1
                                )
                        );

                double izquierda =
                        luminancia(
                                imagen.getRGB(
                                        x - 1,
                                        y
                                )
                        );

                double derecha =
                        luminancia(
                                imagen.getRGB(
                                        x + 1,
                                        y
                                )
                        );

                double laplaciano =
                        4.0 * centro
                                - arriba
                                - abajo
                                - izquierda
                                - derecha;

                suma +=
                        laplaciano;

                sumaCuadrados +=
                        laplaciano
                                * laplaciano;

                cantidad++;
            }
        }

        if (cantidad == 0) {
            return 0.0;
        }

        double media =
                suma
                        / cantidad;

        return (
                sumaCuadrados
                        / cantidad
        )
                - (
                media
                        * media
        );
    }

    private int contarCaracteresAlfanumericos(
            String texto
    ) {

        if (texto == null) {
            return 0;
        }

        int cantidad = 0;

        for (
                int i = 0;
                i < texto.length();
                i++
        ) {

            if (
                    Character.isLetterOrDigit(
                            texto.charAt(i)
                    )
            ) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private int contarPalabrasReconocibles(
            String texto
    ) {

        if (
                texto == null
                        || texto.trim().isEmpty()
        ) {
            return 0;
        }

        String[] tokens =
                texto
                        .trim()
                        .split("\\s+");

        int cantidad = 0;

        for (
                String token :
                tokens
        ) {

            int alfanumericos =
                    contarCaracteresAlfanumericos(
                            token
                    );

            if (alfanumericos >= 2) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private String construirObservacionPagina(
            int numeroPagina,
            boolean textoReconocible,
            boolean zonaCriticaBorrosa
    ) {

        List<String> motivos =
                new ArrayList<>();

        if (!textoReconocible) {

            motivos.add(
                    "el OCR no reconoció suficiente texto principal"
            );
        }

        if (zonaCriticaBorrosa) {

            motivos.add(
                    "se detectó baja nitidez en zonas críticas"
            );
        }

        if (motivos.isEmpty()) {

            return "La página "
                    + numeroPagina
                    + " presenta legibilidad suficiente.";
        }

        return "La página "
                + numeroPagina
                + " presenta problemas de legibilidad: "
                + String.join(
                " y ",
                motivos
        )
                + ".";
    }

    private Tesseract construirTesseract() {

        Tesseract tesseract =
                new Tesseract();

        tesseract.setDatapath(
                new File(
                        tessdataPath
                ).getAbsolutePath()
        );

        tesseract.setLanguage(
                "spa"
        );

        tesseract.setVariable(
                "user_defined_dpi",
                String.valueOf(
                        DPI_RENDER
                )
        );

        return tesseract;
    }

    private void validarConfiguracionOcr() {

        File carpetaTessdata =
                new File(
                        tessdataPath
                );

        File modeloEspanol =
                new File(
                        carpetaTessdata,
                        "spa.traineddata"
                );

        if (
                !carpetaTessdata.exists()
                        || !carpetaTessdata.isDirectory()
        ) {

            throw new IllegalStateException(
                    "No se encontró el directorio tessdata configurado para OCR: "
                            + carpetaTessdata
                            .getAbsolutePath()
            );
        }

        if (
                !modeloEspanol.exists()
                        || !modeloEspanol.isFile()
        ) {

            throw new IllegalStateException(
                    "No se encontró el modelo OCR español spa.traineddata."
            );
        }
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
                        || tipoDocumento
                        .trim()
                        .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El tipo de documento es obligatorio."
            );
        }

        if (
                !TipoDocumentoDigital
                        .esValido(
                                tipoDocumento
                        )
        ) {

            throw new IllegalArgumentException(
                    "El tipo de documento no es válido. Valores permitidos: "
                            + TipoDocumentoDigital
                            .valoresPermitidos()
            );
        }
    }

    private double luminancia(
            int rgb
    ) {

        int rojo =
                (rgb >> 16)
                        & 0xFF;

        int verde =
                (rgb >> 8)
                        & 0xFF;

        int azul =
                rgb
                        & 0xFF;

        return 0.299 * rojo
                + 0.587 * verde
                + 0.114 * azul;
    }

    private int limitar(
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

    private double redondear(
            double valor
    ) {

        return Math.round(
                valor * 100.0
        )
                / 100.0;
    }

    private record ZonaRelativa(
            double x,
            double y,
            double ancho,
            double alto
    ) {
    }
}