package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.documento.dto.PaginaFirmaTrabajadorResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarFirmaTrabajadorResponse;
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
public class ValidacionFirmaTrabajadorService {

    private static final String ESTADO_FIRMA_VALIDADA = "FIRMA_TRABAJADOR_VALIDADA";
    private static final String ESTADO_RECHAZADO_FIRMA = "RECHAZADO_FIRMA_TRABAJADOR";

    private static final int DPI_RENDER = 180;

    // Umbrales conservadores para detectar trazos de firma dentro de la zona.
    private static final int MINIMO_PIXELES_TINTA = 180;
    private static final double MINIMO_PORCENTAJE_TINTA = 0.20;

    public ValidarFirmaTrabajadorResponse validarFirmaTrabajador(
            MultipartFile archivo,
            String tipoDocumento
    ) {
        validarParametros(archivo, tipoDocumento);

        String tipoDocumentoNormalizado = normalizar(tipoDocumento);

        try (PDDocument documento = Loader.loadPDF(archivo.getBytes())) {
            PDFRenderer renderer = new PDFRenderer(documento);

            ValidarFirmaTrabajadorResponse response = new ValidarFirmaTrabajadorResponse();

            response.setTipoDocumento(tipoDocumentoNormalizado);
            response.setNumeroPaginasArchivo(documento.getNumberOfPages());

            List<PaginaFirmaTrabajadorResponse> detallePaginas = new ArrayList<>();

            for (int indicePagina = 0; indicePagina < documento.getNumberOfPages(); indicePagina++) {
                PDPage pagina = documento.getPage(indicePagina);
                BufferedImage imagenPagina = renderer.renderImageWithDPI(indicePagina, DPI_RENDER);

                ZonaFirma zonaFirma = obtenerZonaFirmaTrabajador(tipoDocumentoNormalizado);

                PaginaFirmaTrabajadorResponse detallePagina = analizarZonaFirma(
                        imagenPagina,
                        pagina,
                        zonaFirma,
                        indicePagina + 1
                );

                detallePaginas.add(detallePagina);
            }

            response.setDetallePaginas(detallePaginas);
            response.setPaginasEvaluadas(detallePaginas.size());
            response.setPaginasConFirma(contarPaginasConFirma(detallePaginas));
            response.setPaginasSinFirma(detallePaginas.size() - response.getPaginasConFirma());

            if (response.getPaginasSinFirma() > 0) {
                response.setFirmaValida(false);
                response.setMensajeValidacion("No se detectó firma del trabajador en todas las páginas requeridas.");
                response.setEstadoValidacionDocumental(ESTADO_RECHAZADO_FIRMA);
                response.setPermiteNuevaCarga(true);
                response.setObservaciones(construirObservacionesRechazo(detallePaginas));
                return response;
            }

            response.setFirmaValida(true);
            response.setMensajeValidacion("Se detectó firma del trabajador en las páginas requeridas.");
            response.setEstadoValidacionDocumental(ESTADO_FIRMA_VALIDADA);
            response.setPermiteNuevaCarga(false);
            response.setObservaciones(new ArrayList<>());

            return response;

        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el PDF para validar la firma del trabajador.", e);
        }
    }

    private PaginaFirmaTrabajadorResponse analizarZonaFirma(
            BufferedImage imagenPagina,
            PDPage pagina,
            ZonaFirma zonaFirma,
            int numeroPaginaPdf
    ) {
        RectanguloImagen rectangulo = convertirZonaPdfAImagen(
                zonaFirma,
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

                if (esPixelConTinta(rgb)) {
                    pixelesConTinta++;
                }
            }
        }

        double porcentajeTinta = pixelesEvaluados == 0
                ? 0
                : (pixelesConTinta * 100.0) / pixelesEvaluados;

        boolean firmaDetectada =
                pixelesConTinta >= MINIMO_PIXELES_TINTA
                        && porcentajeTinta >= MINIMO_PORCENTAJE_TINTA;

        PaginaFirmaTrabajadorResponse response = new PaginaFirmaTrabajadorResponse();

        response.setNumeroPaginaPdf(numeroPaginaPdf);
        response.setFirmaDetectada(firmaDetectada);
        response.setPixelesEvaluados(pixelesEvaluados);
        response.setPixelesConTinta(pixelesConTinta);
        response.setPorcentajeTintaZona(redondear(porcentajeTinta));

        if (firmaDetectada) {
            response.setObservacion("Se detectó contenido gráfico compatible con firma del trabajador.");
        } else {
            response.setObservacion("No se detectó contenido gráfico suficiente en la zona de firma del trabajador.");
        }

        return response;
    }

    private ZonaFirma obtenerZonaFirmaTrabajador(String tipoDocumento) {
        if ("FORMULARIO_6012".equalsIgnoreCase(tipoDocumento)) {
            // Zona superior a la línea de "FIRMA DEL ASEGURADO TITULAR".
            // Si la firma sale muy arriba o muy abajo, aquí afinamos coordenadas.
            return new ZonaFirma(92, 66, 135, 86);
        }

        if ("AUTORIZACION_DESCUENTO".equalsIgnoreCase(tipoDocumento)) {
            // Zona estimada para firma del trabajador en la Autorización de Descuento.
            // Probablemente la ajustemos visualmente cuando probemos con el PDF firmado.
            return new ZonaFirma(70, 424, 133, 107);
        }

        return new ZonaFirma(92, 66, 135, 86);
    }

    private RectanguloImagen convertirZonaPdfAImagen(
            ZonaFirma zonaFirma,
            float altoPaginaPdf,
            int dpi
    ) {
        double escala = dpi / 72.0;

        int x = (int) Math.round(zonaFirma.x() * escala);
        int y = (int) Math.round((altoPaginaPdf - zonaFirma.y() - zonaFirma.alto()) * escala);
        int ancho = (int) Math.round(zonaFirma.ancho() * escala);
        int alto = (int) Math.round(zonaFirma.alto() * escala);

        return new RectanguloImagen(x, y, ancho, alto);
    }

    private boolean esPixelConTinta(int rgb) {
        int rojo = (rgb >> 16) & 0xFF;
        int verde = (rgb >> 8) & 0xFF;
        int azul = rgb & 0xFF;

        int promedio = (rojo + verde + azul) / 3;

        boolean tintaOscura = promedio < 205;

        boolean tintaAzul =
                azul > rojo + 18
                        && azul > verde + 10
                        && promedio < 235;

        boolean tintaRoja =
                rojo > verde + 25
                        && rojo > azul + 25
                        && promedio < 235;

        return tintaOscura || tintaAzul || tintaRoja;
    }

    private int contarPaginasConFirma(List<PaginaFirmaTrabajadorResponse> detallePaginas) {
        int contador = 0;

        for (PaginaFirmaTrabajadorResponse detalle : detallePaginas) {
            if (detalle.isFirmaDetectada()) {
                contador++;
            }
        }

        return contador;
    }

    private List<String> construirObservacionesRechazo(
            List<PaginaFirmaTrabajadorResponse> detallePaginas
    ) {
        List<String> observaciones = new ArrayList<>();

        for (PaginaFirmaTrabajadorResponse detalle : detallePaginas) {
            if (!detalle.isFirmaDetectada()) {
                observaciones.add(
                        "No se detectó firma del trabajador en la página "
                                + detalle.getNumeroPaginaPdf()
                                + "."
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

    private record ZonaFirma(
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