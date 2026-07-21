package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.documento.dto.PaginaSelloEssaludResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarSelloEssaludResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.TipoDocumentoDigital;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidacionSelloEssaludService {

    private static final String ESTADO_SELLO_VALIDADO = "SELLO_ESSALUD_VALIDADO";
    private static final String ESTADO_ERROR_VERIFICACION_SELLO = "ERROR_VERIFICACION_SELLO_ESSALUD";

    private static final int DPI_RENDER = 180;

    // Umbrales para detectar contenido gráfico tipo sello.
    // Evitan contar líneas celestes claras de la plantilla como sello.
    private static final int MINIMO_PIXELES_TINTA = 1200;
    private static final double MINIMO_PORCENTAJE_TINTA = 0.80;

    public ValidarSelloEssaludResponse validarSelloEssalud(
            MultipartFile archivo,
            String tipoDocumento
    ) {
        validarParametros(archivo, tipoDocumento);

        String tipoDocumentoNormalizado = normalizar(tipoDocumento);

        try (PDDocument documento = Loader.loadPDF(archivo.getBytes())) {
            PDFRenderer renderer = new PDFRenderer(documento);

            ValidarSelloEssaludResponse response = new ValidarSelloEssaludResponse();

            response.setTipoDocumento(tipoDocumentoNormalizado);
            response.setNumeroPaginasArchivo(documento.getNumberOfPages());

            List<PaginaSelloEssaludResponse> detallePaginas = new ArrayList<>();

            for (int indicePagina = 0; indicePagina < documento.getNumberOfPages(); indicePagina++) {
                PDPage pagina = documento.getPage(indicePagina);
                BufferedImage imagenPagina = renderer.renderImageWithDPI(indicePagina, DPI_RENDER);

                ZonaSello zonaSello = obtenerZonaSelloEssalud(tipoDocumentoNormalizado);

                PaginaSelloEssaludResponse detallePagina = analizarZonaSello(
                        imagenPagina,
                        pagina,
                        zonaSello,
                        indicePagina + 1
                );

                detallePaginas.add(detallePagina);
            }

            response.setDetallePaginas(detallePaginas);
            response.setPaginasEvaluadas(detallePaginas.size());
            response.setPaginasConSello(contarPaginasConSello(detallePaginas));
            response.setPaginasSinSello(detallePaginas.size() - response.getPaginasConSello());

            if (response.getPaginasSinSello() > 0) {
                response.setSelloValido(false);
                response.setMensajeValidacion("No se pudo verificar el sello institucional de EsSalud en todas las páginas requeridas.");
                response.setEstadoValidacionDocumental(ESTADO_ERROR_VERIFICACION_SELLO);
                response.setPermiteNuevaCarga(false);
                response.setRequiereReintentoSellado(true);
                response.setObservaciones(construirObservacionesErrorSellado(detallePaginas));
                return response;
            }

            response.setSelloValido(true);
            response.setMensajeValidacion("Se detectó sello de EsSalud en las páginas requeridas.");
            response.setEstadoValidacionDocumental(ESTADO_SELLO_VALIDADO);
            response.setPermiteNuevaCarga(false);
            response.setRequiereReintentoSellado(false);
            response.setObservaciones(new ArrayList<>());

            return response;

        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el PDF para validar el sello de EsSalud.", e);
        }
    }

    private PaginaSelloEssaludResponse analizarZonaSello(
            BufferedImage imagenPagina,
            PDPage pagina,
            ZonaSello zonaSello,
            int numeroPaginaPdf
    ) {
        RectanguloImagen rectangulo = convertirZonaPdfAImagen(
                zonaSello,
                pagina.getMediaBox().getHeight(),
                DPI_RENDER
        );

        int pixelesEvaluados = 0;
        int pixelesConTinta = 0;

        int xFinal = Math.min(rectangulo.x() + rectangulo.ancho(), imagenPagina.getWidth());
        int yFinal = Math.min(rectangulo.y() + rectangulo.alto(), imagenPagina.getHeight());

        for (int y = Math.max(rectangulo.y(), 0); y < yFinal; y++) {
            for (int x = Math.max(rectangulo.x(), 0); x < xFinal; x++) {
                int rgb = imagenPagina.getRGB(x, y);

                pixelesEvaluados++;

                if (esPixelCompatibleConSello(rgb)) {
                    pixelesConTinta++;
                }
            }
        }

        double porcentajeTinta = pixelesEvaluados == 0
                ? 0
                : (pixelesConTinta * 100.0) / pixelesEvaluados;

        boolean selloDetectado =
                pixelesConTinta >= MINIMO_PIXELES_TINTA
                        && porcentajeTinta >= MINIMO_PORCENTAJE_TINTA;

        PaginaSelloEssaludResponse response = new PaginaSelloEssaludResponse();

        response.setNumeroPaginaPdf(numeroPaginaPdf);
        response.setSelloDetectado(selloDetectado);
        response.setPixelesEvaluados(pixelesEvaluados);
        response.setPixelesConTinta(pixelesConTinta);
        response.setPorcentajeTintaZona(redondear(porcentajeTinta));

        if (selloDetectado) {
            response.setObservacion("Se detectó contenido gráfico compatible con sello de EsSalud.");
        } else {
            response.setObservacion("No se detectó contenido gráfico suficiente en la zona de sello de EsSalud.");
        }

        return response;
    }

    private ZonaSello obtenerZonaSelloEssalud(String tipoDocumento) {
        if ("FORMULARIO_6012".equalsIgnoreCase(tipoDocumento)) {
            // Zona sobre la línea de "FIRMA Y SELLO DE ESSALUD".
            // Está pensada para el 6012 actual, en la parte inferior derecha.
            return new ZonaSello(360, 70, 134, 85);
        }

        if ("AUTORIZACION_DESCUENTO".equalsIgnoreCase(tipoDocumento)) {
            // Zona estimada para sello en la autorización.
            // Se ajusta cuando probemos con el PDF actual sellado.
            return new ZonaSello(355, 428, 134, 85);
        }

        return new ZonaSello(360, 70, 134, 85);
    }

    private RectanguloImagen convertirZonaPdfAImagen(
            ZonaSello zonaSello,
            float altoPaginaPdf,
            int dpi
    ) {
        double escala = dpi / 72.0;

        int x = (int) Math.round(zonaSello.x() * escala);
        int y = (int) Math.round((altoPaginaPdf - zonaSello.y() - zonaSello.alto()) * escala);
        int ancho = (int) Math.round(zonaSello.ancho() * escala);
        int alto = (int) Math.round(zonaSello.alto() * escala);

        return new RectanguloImagen(x, y, ancho, alto);
    }

    private boolean esPixelCompatibleConSello(int rgb) {
        int rojo = (rgb >> 16) & 0xFF;
        int verde = (rgb >> 8) & 0xFF;
        int azul = rgb & 0xFF;

        int promedio = (rojo + verde + azul) / 3;
        int maximo = Math.max(rojo, Math.max(verde, azul));
        int minimo = Math.min(rojo, Math.min(verde, azul));
        int saturacion = maximo - minimo;

        // Azul/celeste fuerte del sello EsSalud | Viva
        boolean azulInstitucional =
                azul > rojo + 45
                        && azul > verde + 18
                        && promedio < 235
                        && saturacion > 50;

        // Azul marino del texto "RECIBIDO"
        // No cuenta negro puro ni gris, solo tonos oscuros con predominancia azul.
        boolean azulMarinoRecibido =
                promedio < 85
                        && azul > rojo + 20
                        && azul > verde + 8
                        && saturacion > 25;

        return azulInstitucional || azulMarinoRecibido;
    }

    private int contarPaginasConSello(List<PaginaSelloEssaludResponse> detallePaginas) {
        int contador = 0;

        for (PaginaSelloEssaludResponse detalle : detallePaginas) {
            if (detalle.isSelloDetectado()) {
                contador++;
            }
        }

        return contador;
    }

    private List<String> construirObservacionesErrorSellado(
            List<PaginaSelloEssaludResponse> detallePaginas
    ) {
        List<String> observaciones = new ArrayList<>();

        for (PaginaSelloEssaludResponse detalle : detallePaginas) {
            if (!detalle.isSelloDetectado()) {
                observaciones.add(
                        "No se detectó sello institucional de EsSalud en la página "
                                + detalle.getNumeroPaginaPdf()
                                + ". Se requiere reintentar el proceso interno de sellado."
                );
            }
        }

        return observaciones;
    }

    private void validarParametros(MultipartFile archivo, String tipoDocumento) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF es obligatorio.");
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
    }

    private String normalizar(String valor) {
        return valor.trim().toUpperCase();
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private record ZonaSello(
            float x,
            float y,
            float ancho,
            float alto
    ) {
    }

    private record RectanguloImagen(
            int x,
            int y,
            int ancho,
            int alto
    ) {
    }
}