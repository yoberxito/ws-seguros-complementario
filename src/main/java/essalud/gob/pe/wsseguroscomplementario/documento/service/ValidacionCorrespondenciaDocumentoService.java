package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.documento.dto.PaginaQrDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarCorrespondenciaDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DatosQrDocumento;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoGenerado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.TipoDocumentoDigital;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidacionCorrespondenciaDocumentoService {

    private static final String ESTADO_CORRESPONDENCIA_VALIDADA = "CORRESPONDENCIA_VALIDADA";
    private static final String ESTADO_RECHAZADO_CORRESPONDENCIA = "RECHAZADO_CORRESPONDENCIA";

    private final LecturaQrPdfService lecturaQrPdfService;
    private final RegistroDocumentoGeneradoService registroDocumentoGeneradoService;

    public ValidacionCorrespondenciaDocumentoService(
            LecturaQrPdfService lecturaQrPdfService,
            RegistroDocumentoGeneradoService registroDocumentoGeneradoService
    ) {
        this.lecturaQrPdfService = lecturaQrPdfService;
        this.registroDocumentoGeneradoService = registroDocumentoGeneradoService;
    }

    public ValidarCorrespondenciaDocumentoResponse validarCorrespondencia(
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        validarParametros(registroInternoProceso, tipoDocumento, numeroDocumentoTrabajador);

        int numeroPaginasArchivo = lecturaQrPdfService.contarPaginas(archivo);
        List<DatosQrDocumento> qrsLeidos = lecturaQrPdfService.leerCodigosQr(archivo);

        ValidarCorrespondenciaDocumentoResponse response = new ValidarCorrespondenciaDocumentoResponse();

        response.setRegistroInternoProceso(registroInternoProceso);
        response.setTipoDocumentoEsperado(normalizar(tipoDocumento));
        response.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);
        response.setNumeroPaginasArchivo(numeroPaginasArchivo);
        response.setPaginasQrLeidas(convertirPaginasQrAResponse(qrsLeidos));

        List<String> observaciones = new ArrayList<>();

        if (qrsLeidos.isEmpty()) {
            observaciones.add("No se pudo leer ningún código QR documental en el archivo cargado.");
            return construirRespuestaRechazada(response, observaciones);
        }

        DatosQrDocumento qrPrincipal = qrsLeidos.get(0);

        response.setIdDocumentoGenerado(qrPrincipal.getIdDocumento());
        response.setTipoDocumentoQr(qrPrincipal.getTipoDocumento());
        response.setTotalPaginasDeclaradasQr(qrPrincipal.getTotalPaginasQr());

        DocumentoGenerado documentoGenerado = buscarDocumentoGenerado(qrPrincipal.getIdDocumento(), observaciones);

        if (documentoGenerado == null) {
            return construirRespuestaRechazada(response, observaciones);
        }

        validarContraMetadata(
                response,
                observaciones,
                documentoGenerado,
                qrsLeidos,
                registroInternoProceso,
                tipoDocumento,
                numeroDocumentoTrabajador
        );

        if (!observaciones.isEmpty()) {
            return construirRespuestaRechazada(response, observaciones);
        }

        response.setCorrespondenciaValida(true);
        response.setMensajeValidacion("El documento cargado corresponde al documento generado por el sistema.");
        response.setEstadoValidacionDocumental(ESTADO_CORRESPONDENCIA_VALIDADA);
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

    private void validarContraMetadata(
            ValidarCorrespondenciaDocumentoResponse response,
            List<String> observaciones,
            DocumentoGenerado documentoGenerado,
            List<DatosQrDocumento> qrsLeidos,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        String tipoDocumentoNormalizado = normalizar(tipoDocumento);

        if (!documentoGenerado.getRegistroInternoProceso().equalsIgnoreCase(registroInternoProceso)) {
            observaciones.add("El documento no corresponde al registro interno del proceso informado.");
        }

        if (!documentoGenerado.getTipoDocumento().equalsIgnoreCase(tipoDocumentoNormalizado)) {
            observaciones.add("El tipo de documento registrado en metadata no coincide con el tipo de documento informado.");
        }

        if (!documentoGenerado.getNumeroDocumentoTrabajador().equalsIgnoreCase(numeroDocumentoTrabajador)) {
            observaciones.add("El documento no corresponde al trabajador informado.");
        }

        for (DatosQrDocumento qr : qrsLeidos) {
            if (!qr.getIdDocumento().equalsIgnoreCase(documentoGenerado.getIdDocumentoGenerado())) {
                observaciones.add("Se detectó un QR con ID_DOCUMENTO distinto dentro del archivo cargado.");
                break;
            }

            if (!qr.getTipoDocumento().equalsIgnoreCase(documentoGenerado.getTipoDocumento())) {
                observaciones.add("Se detectó un QR con TIPO_DOCUMENTO distinto al registrado en metadata.");
                break;
            }

            if (qr.getTotalPaginasQr() != documentoGenerado.getNumeroPaginasGeneradas()) {
                observaciones.add("El total de páginas declarado en el QR no coincide con la metadata documental.");
                break;
            }
        }

        response.setIdDocumentoGenerado(documentoGenerado.getIdDocumentoGenerado());
        response.setTipoDocumentoQr(documentoGenerado.getTipoDocumento());
        response.setTotalPaginasDeclaradasQr(documentoGenerado.getNumeroPaginasGeneradas());
    }

    private ValidarCorrespondenciaDocumentoResponse construirRespuestaRechazada(
            ValidarCorrespondenciaDocumentoResponse response,
            List<String> observaciones
    ) {
        response.setCorrespondenciaValida(false);
        response.setMensajeValidacion("El documento cargado no corresponde al documento generado por el sistema.");
        response.setEstadoValidacionDocumental(ESTADO_RECHAZADO_CORRESPONDENCIA);
        response.setPermiteNuevaCarga(true);
        response.setObservaciones(observaciones);

        return response;
    }

    private List<PaginaQrDocumentoResponse> convertirPaginasQrAResponse(List<DatosQrDocumento> qrsLeidos) {
        return qrsLeidos.stream()
                .map(this::convertirPaginaQrAResponse)
                .collect(Collectors.toList());
    }

    private PaginaQrDocumentoResponse convertirPaginaQrAResponse(DatosQrDocumento datosQr) {
        PaginaQrDocumentoResponse response = new PaginaQrDocumentoResponse();

        response.setNumeroPaginaPdf(datosQr.getNumeroPaginaPdf());
        response.setIdDocumento(datosQr.getIdDocumento());
        response.setTipoDocumento(datosQr.getTipoDocumento());
        response.setPaginaQr(datosQr.getPaginaQr());
        response.setTotalPaginasQr(datosQr.getTotalPaginasQr());
        response.setContenidoOriginal(datosQr.getContenidoOriginal());

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