package essalud.gob.pe.wsseguroscomplementario.documento.validator;

import essalud.gob.pe.wsseguroscomplementario.documento.constants.PdfValidationConstants;
import essalud.gob.pe.wsseguroscomplementario.documento.model.AnalisisImagenPdf;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@Component
public class PdfRenderValidator {

    public void validarPaginasRenderizables(PDDocument documento, List<String> observaciones) {
        if (documento == null) {
            observaciones.add("El documento PDF no está disponible para validación visual.");
            return;
        }

        int numeroPaginas = documento.getNumberOfPages();

        if (numeroPaginas <= 0) {
            observaciones.add("El PDF no contiene páginas renderizables.");
            return;
        }

        PDFRenderer renderer = new PDFRenderer(documento);

        int paginasAValidar = Math.min(
                numeroPaginas,
                PdfValidationConstants.MAX_PAGINAS_RENDER_VALIDACION
        );

        int paginasAparentementeEnBlanco = 0;

        for (int indicePagina = 0; indicePagina < paginasAValidar; indicePagina++) {
            try {
                BufferedImage imagen = renderer.renderImageWithDPI(
                        indicePagina,
                        PdfValidationConstants.DPI_RENDER_VALIDACION
                );

                validarDimensionRenderizada(imagen, indicePagina, observaciones);

                AnalisisImagenPdf analisisImagen = analizarImagen(imagen);

                if (analisisImagen.aparentementeEnBlanco()) {
                    paginasAparentementeEnBlanco++;
                }

            } catch (IOException e) {
                observaciones.add("La página " + (indicePagina + 1) + " no pudo ser renderizada correctamente.");
            }
        }

        if (paginasAValidar > 0 && paginasAparentementeEnBlanco == paginasAValidar) {
            observaciones.add("El documento aparenta estar en blanco o no contiene información visual procesable en las páginas evaluadas.");
        }
    }

    private void validarDimensionRenderizada(
            BufferedImage imagen,
            int indicePagina,
            List<String> observaciones
    ) {
        if (imagen == null) {
            observaciones.add("La página " + (indicePagina + 1) + " no pudo ser convertida a imagen para su validación.");
            return;
        }

        if (
                imagen.getWidth() < PdfValidationConstants.MIN_ANCHO_RENDER
                        || imagen.getHeight() < PdfValidationConstants.MIN_ALTO_RENDER
        ) {
            observaciones.add("La página " + (indicePagina + 1) + " presenta una resolución o dimensión renderizada inferior al mínimo técnico definido.");
        }
    }

    private AnalisisImagenPdf analizarImagen(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();

        int totalMuestras = 0;
        int pixelesBlancos = 0;

        int luminanciaMinima = 255;
        int luminanciaMaxima = 0;

        int salto = 10;

        for (int y = 0; y < alto; y += salto) {
            for (int x = 0; x < ancho; x += salto) {
                int rgb = imagen.getRGB(x, y);

                int rojo = (rgb >> 16) & 0xff;
                int verde = (rgb >> 8) & 0xff;
                int azul = rgb & 0xff;

                int luminancia = (rojo + verde + azul) / 3;

                if (luminancia > 245) {
                    pixelesBlancos++;
                }

                luminanciaMinima = Math.min(luminanciaMinima, luminancia);
                luminanciaMaxima = Math.max(luminanciaMaxima, luminancia);

                totalMuestras++;
            }
        }

        double porcentajeBlanco = totalMuestras == 0
                ? 1.0
                : (double) pixelesBlancos / totalMuestras;

        int rangoContraste = luminanciaMaxima - luminanciaMinima;

        return new AnalisisImagenPdf(porcentajeBlanco, rangoContraste);
    }
}