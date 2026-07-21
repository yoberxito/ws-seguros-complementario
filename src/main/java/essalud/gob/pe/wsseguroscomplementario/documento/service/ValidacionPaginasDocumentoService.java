package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarPaginasDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DatosQrDocumento;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoGenerado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.TipoDocumentoDigital;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ValidacionPaginasDocumentoService {

    private static final String ESTADO_VALIDACION_PAGINAS_APROBADA = "VALIDACION_PAGINAS_APROBADA";
    private static final String ESTADO_RECHAZADO_PAGINAS = "RECHAZADO_PAGINAS";

    private final LecturaQrPdfService lecturaQrPdfService;
    private final RegistroDocumentoGeneradoService registroDocumentoGeneradoService;

    public ValidacionPaginasDocumentoService(
            LecturaQrPdfService lecturaQrPdfService,
            RegistroDocumentoGeneradoService registroDocumentoGeneradoService
    ) {
        this.lecturaQrPdfService = lecturaQrPdfService;
        this.registroDocumentoGeneradoService = registroDocumentoGeneradoService;
    }

    public ValidarPaginasDocumentoResponse validarPaginas(
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        validarParametros(registroInternoProceso, tipoDocumento, numeroDocumentoTrabajador);

        int numeroPaginasArchivo = lecturaQrPdfService.contarPaginas(archivo);
        List<DatosQrDocumento> qrsLeidos = lecturaQrPdfService.leerCodigosQr(archivo);

        ValidarPaginasDocumentoResponse response = new ValidarPaginasDocumentoResponse();

        response.setRegistroInternoProceso(registroInternoProceso);
        response.setTipoDocumento(normalizar(tipoDocumento));
        response.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);
        response.setNumeroPaginasArchivo(numeroPaginasArchivo);

        List<String> observaciones = new ArrayList<>();

        if (qrsLeidos.isEmpty()) {
            observaciones.add("No se pudo leer ningún código QR documental en el archivo cargado.");
            return construirRespuestaRechazada(response, observaciones);
        }

        DatosQrDocumento qrPrincipal = qrsLeidos.get(0);

        response.setIdDocumentoGenerado(qrPrincipal.getIdDocumento());

        DocumentoGenerado documentoGenerado = buscarDocumentoGenerado(qrPrincipal.getIdDocumento(), observaciones);

        if (documentoGenerado == null) {
            return construirRespuestaRechazada(response, observaciones);
        }

        int numeroPaginasEsperadas = documentoGenerado.getNumeroPaginasGeneradas();

        response.setNumeroPaginasEsperadas(numeroPaginasEsperadas);
        response.setPaginasEsperadas(construirListaPaginasEsperadas(numeroPaginasEsperadas));
        response.setPaginasLeidas(obtenerPaginasLeidas(qrsLeidos));

        validarCorrespondenciaBasica(
                documentoGenerado,
                qrsLeidos,
                registroInternoProceso,
                tipoDocumento,
                numeroDocumentoTrabajador,
                observaciones
        );

        validarCantidadFisicaPaginas(
                numeroPaginasArchivo,
                numeroPaginasEsperadas,
                qrsLeidos,
                observaciones
        );

        List<Integer> paginasFaltantes = detectarPaginasFaltantes(
                numeroPaginasEsperadas,
                qrsLeidos
        );

        List<Integer> paginasDuplicadas = detectarPaginasDuplicadas(qrsLeidos);

        response.setPaginasFaltantes(paginasFaltantes);
        response.setPaginasDuplicadas(paginasDuplicadas);

        if (!paginasFaltantes.isEmpty()) {
            observaciones.add("El archivo cargado no contiene todas las páginas esperadas del documento.");
        }

        if (!paginasDuplicadas.isEmpty()) {
            observaciones.add("El archivo cargado contiene páginas duplicadas según la numeración del QR.");
        }

        validarOrdenSecuencia(qrsLeidos, observaciones);

        if (!observaciones.isEmpty()) {
            return construirRespuestaRechazada(response, observaciones);
        }

        response.setPaginasValidas(true);
        response.setMensajeValidacion("El archivo contiene todas las páginas esperadas, sin omisiones, duplicidades ni desorden.");
        response.setEstadoValidacionDocumental(ESTADO_VALIDACION_PAGINAS_APROBADA);
        response.setPermiteNuevaCarga(false);
        response.setObservaciones(new ArrayList<>());

        return response;
    }

    private DocumentoGenerado buscarDocumentoGenerado(
            String idDocumentoGenerado,
            List<String> observaciones
    ) {
        try {
            return registroDocumentoGeneradoService.obtenerDocumentoGenerado(idDocumentoGenerado);
        } catch (IllegalArgumentException e) {
            observaciones.add("El ID_DOCUMENTO contenido en el QR no existe en la metadata documental registrada.");
            return null;
        }
    }

    private void validarCorrespondenciaBasica(
            DocumentoGenerado documentoGenerado,
            List<DatosQrDocumento> qrsLeidos,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador,
            List<String> observaciones
    ) {
        String tipoDocumentoNormalizado = normalizar(tipoDocumento);

        if (!documentoGenerado.getRegistroInternoProceso().equalsIgnoreCase(registroInternoProceso)) {
            observaciones.add("El documento no corresponde al registro interno del proceso informado.");
        }

        if (!documentoGenerado.getTipoDocumento().equalsIgnoreCase(tipoDocumentoNormalizado)) {
            observaciones.add("El tipo de documento no coincide con la metadata documental registrada.");
        }

        if (!documentoGenerado.getNumeroDocumentoTrabajador().equalsIgnoreCase(numeroDocumentoTrabajador)) {
            observaciones.add("El documento no corresponde al trabajador informado.");
        }

        for (DatosQrDocumento qr : qrsLeidos) {
            if (!qr.getIdDocumento().equalsIgnoreCase(documentoGenerado.getIdDocumentoGenerado())) {
                observaciones.add("Se detectó una página con ID_DOCUMENTO distinto al documento generado.");
                break;
            }

            if (!qr.getTipoDocumento().equalsIgnoreCase(documentoGenerado.getTipoDocumento())) {
                observaciones.add("Se detectó una página con TIPO_DOCUMENTO distinto al registrado en metadata.");
                break;
            }

            if (qr.getTotalPaginasQr() != documentoGenerado.getNumeroPaginasGeneradas()) {
                observaciones.add("Se detectó una página cuyo TOTAL_PAGINAS no coincide con la metadata documental.");
                break;
            }
        }
    }

    private void validarCantidadFisicaPaginas(
            int numeroPaginasArchivo,
            int numeroPaginasEsperadas,
            List<DatosQrDocumento> qrsLeidos,
            List<String> observaciones
    ) {
        if (numeroPaginasArchivo != numeroPaginasEsperadas) {
            observaciones.add("La cantidad de páginas físicas del archivo no coincide con la cantidad esperada registrada en metadata.");
        }

        if (qrsLeidos.size() != numeroPaginasArchivo) {
            observaciones.add("No se pudo leer el QR documental en todas las páginas del archivo cargado.");
        }
    }

    private List<Integer> detectarPaginasFaltantes(
            int numeroPaginasEsperadas,
            List<DatosQrDocumento> qrsLeidos
    ) {
        Set<Integer> paginasLeidas = new HashSet<>();

        for (DatosQrDocumento qr : qrsLeidos) {
            paginasLeidas.add(qr.getPaginaQr());
        }

        List<Integer> paginasFaltantes = new ArrayList<>();

        for (int paginaEsperada = 1; paginaEsperada <= numeroPaginasEsperadas; paginaEsperada++) {
            if (!paginasLeidas.contains(paginaEsperada)) {
                paginasFaltantes.add(paginaEsperada);
            }
        }

        return paginasFaltantes;
    }

    private List<Integer> detectarPaginasDuplicadas(List<DatosQrDocumento> qrsLeidos) {
        Set<Integer> paginasUnicas = new HashSet<>();
        Set<Integer> paginasDuplicadas = new HashSet<>();

        for (DatosQrDocumento qr : qrsLeidos) {
            if (!paginasUnicas.add(qr.getPaginaQr())) {
                paginasDuplicadas.add(qr.getPaginaQr());
            }
        }

        return new ArrayList<>(paginasDuplicadas);
    }

    private void validarOrdenSecuencia(
            List<DatosQrDocumento> qrsLeidos,
            List<String> observaciones
    ) {
        for (DatosQrDocumento qr : qrsLeidos) {
            if (qr.getNumeroPaginaPdf() != qr.getPaginaQr()) {
                observaciones.add(
                        "El orden de páginas del archivo no coincide con la secuencia declarada en los QR."
                );
                return;
            }
        }
    }

    private List<Integer> construirListaPaginasEsperadas(int numeroPaginasEsperadas) {
        List<Integer> paginasEsperadas = new ArrayList<>();

        for (int i = 1; i <= numeroPaginasEsperadas; i++) {
            paginasEsperadas.add(i);
        }

        return paginasEsperadas;
    }

    private List<Integer> obtenerPaginasLeidas(List<DatosQrDocumento> qrsLeidos) {
        List<Integer> paginasLeidas = new ArrayList<>();

        for (DatosQrDocumento qr : qrsLeidos) {
            paginasLeidas.add(qr.getPaginaQr());
        }

        return paginasLeidas;
    }

    private ValidarPaginasDocumentoResponse construirRespuestaRechazada(
            ValidarPaginasDocumentoResponse response,
            List<String> observaciones
    ) {
        response.setPaginasValidas(false);
        response.setMensajeValidacion("El archivo cargado no supera la validación de páginas del documento.");
        response.setEstadoValidacionDocumental(ESTADO_RECHAZADO_PAGINAS);
        response.setPermiteNuevaCarga(true);
        response.setObservaciones(observaciones);

        return response;
    }

    private void validarParametros(
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
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

    private String normalizar(String valor) {
        return valor.trim().toUpperCase();
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}