package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.DetalleEtapaValidacionResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionDocumentalCompletaResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionPdfResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarCorrespondenciaDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarElementosVisualesResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarFirmaTrabajadorResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarPaginasDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.RegistrarAvanceExpedienteRequest;
import essalud.gob.pe.wsseguroscomplementario.expediente.service.ExpedienteDigitalService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidacionDocumentalCompletaService {

    private static final ZoneId ZONA_HORARIA_LIMA =
            ZoneId.of(EstadoProcesoConstants.ZONA_HORARIA_LIMA);

    private final ValidacionPdfService validacionPdfService;
    private final ValidacionCorrespondenciaDocumentoService validacionCorrespondenciaDocumentoService;
    private final ValidacionPaginasDocumentoService validacionPaginasDocumentoService;
    private final ValidacionElementosVisualesService validacionElementosVisualesService;
    private final ValidacionFirmaTrabajadorService validacionFirmaTrabajadorService;
    private final ExpedienteDigitalService expedienteDigitalService;

    public ValidacionDocumentalCompletaService(
            ValidacionPdfService validacionPdfService,
            ValidacionCorrespondenciaDocumentoService validacionCorrespondenciaDocumentoService,
            ValidacionPaginasDocumentoService validacionPaginasDocumentoService,
            ValidacionElementosVisualesService validacionElementosVisualesService,
            ValidacionFirmaTrabajadorService validacionFirmaTrabajadorService,
            ExpedienteDigitalService expedienteDigitalService
    ) {
        this.validacionPdfService = validacionPdfService;
        this.validacionCorrespondenciaDocumentoService = validacionCorrespondenciaDocumentoService;
        this.validacionPaginasDocumentoService = validacionPaginasDocumentoService;
        this.validacionElementosVisualesService = validacionElementosVisualesService;
        this.validacionFirmaTrabajadorService = validacionFirmaTrabajadorService;
        this.expedienteDigitalService = expedienteDigitalService;
    }

    public ValidacionDocumentalCompletaResponse validarDocumentoCompleto(
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String tipoDocumentoTrabajador,
            String numeroDocumentoTrabajador,
            String idDocumentoCargado,
            String usuarioAutenticado,
            String ipOrigen,
            String datosSesionDispositivo
    ) {
        validarParametros(
                archivo,
                registroInternoProceso,
                tipoDocumento,
                numeroDocumentoTrabajador
        );

        ValidacionDocumentalCompletaResponse response = construirResponseBase(
                registroInternoProceso,
                tipoDocumento,
                tipoDocumentoTrabajador,
                numeroDocumentoTrabajador,
                idDocumentoCargado
        );

        ejecutarValidacionEstructuraPdf(response, archivo);
        if (validacionRechazada(response)) {
            finalizarValidacion(response, usuarioAutenticado, ipOrigen, datosSesionDispositivo);
            return response;
        }

        ejecutarValidacionCorrespondencia(response, archivo, registroInternoProceso, tipoDocumento, numeroDocumentoTrabajador);
        if (validacionRechazada(response)) {
            finalizarValidacion(response, usuarioAutenticado, ipOrigen, datosSesionDispositivo);
            return response;
        }

        ejecutarValidacionPaginas(response, archivo, registroInternoProceso, tipoDocumento, numeroDocumentoTrabajador);
        if (validacionRechazada(response)) {
            finalizarValidacion(response, usuarioAutenticado, ipOrigen, datosSesionDispositivo);
            return response;
        }

        ejecutarValidacionElementosVisuales(response, archivo, tipoDocumento);
        if (validacionRechazada(response)) {
            finalizarValidacion(response, usuarioAutenticado, ipOrigen, datosSesionDispositivo);
            return response;
        }

        ejecutarValidacionFirmaTrabajador(response, archivo, tipoDocumento);

        finalizarValidacion(response, usuarioAutenticado, ipOrigen, datosSesionDispositivo);

        return response;
    }

    private void ejecutarValidacionEstructuraPdf(
            ValidacionDocumentalCompletaResponse response,
            MultipartFile archivo
    ) {
        try {
            ValidacionPdfResponse resultado =
                    validacionPdfService.validarEstructuraTecnica(archivo);

            registrarEtapa(
                    response,
                    "17",
                    "Validar estructura técnica del PDF",
                    resultado.isValido(),
                    resultado.isValido() ? "PDF_VALIDO" : "PDF_INVALIDO",
                    resultado.getMensajeValidacion(),
                    resultado.getObservaciones()
            );

        } catch (Exception e) {
            registrarEtapa(
                    response,
                    "17",
                    "Validar estructura técnica del PDF",
                    false,
                    "ERROR_VALIDACION_ESTRUCTURA_PDF",
                    "No se pudo ejecutar la validación técnica del PDF.",
                    List.of(e.getMessage())
            );
        }
    }

    private void ejecutarValidacionCorrespondencia(
            ValidacionDocumentalCompletaResponse response,
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        try {
            ValidarCorrespondenciaDocumentoResponse resultado =
                    validacionCorrespondenciaDocumentoService.validarCorrespondencia(
                            archivo,
                            registroInternoProceso,
                            tipoDocumento,
                            numeroDocumentoTrabajador
                    );

            registrarEtapa(
                    response,
                    "16",
                    "Validar correspondencia QR y metadata",
                    resultado.isCorrespondenciaValida(),
                    resultado.getEstadoValidacionDocumental(),
                    resultado.getMensajeValidacion(),
                    resultado.getObservaciones()
            );

        } catch (Exception e) {
            registrarEtapa(
                    response,
                    "16",
                    "Validar correspondencia QR y metadata",
                    false,
                    "ERROR_VALIDACION_CORRESPONDENCIA",
                    "No se pudo validar la correspondencia documental.",
                    List.of(e.getMessage())
            );
        }
    }

    private void ejecutarValidacionPaginas(
            ValidacionDocumentalCompletaResponse response,
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        try {
            ValidarPaginasDocumentoResponse resultado =
                    validacionPaginasDocumentoService.validarPaginas(
                            archivo,
                            registroInternoProceso,
                            tipoDocumento,
                            numeroDocumentoTrabajador
                    );

            registrarEtapa(
                    response,
                    "18",
                    "Validar páginas completas, secuencia y duplicidades",
                    resultado.isPaginasValidas(),
                    resultado.getEstadoValidacionDocumental(),
                    resultado.getMensajeValidacion(),
                    resultado.getObservaciones()
            );

        } catch (Exception e) {
            registrarEtapa(
                    response,
                    "18",
                    "Validar páginas completas, secuencia y duplicidades",
                    false,
                    "ERROR_VALIDACION_PAGINAS",
                    "No se pudo validar la secuencia de páginas del documento.",
                    List.of(e.getMessage())
            );
        }
    }

    private void ejecutarValidacionElementosVisuales(
            ValidacionDocumentalCompletaResponse response,
            MultipartFile archivo,
            String tipoDocumento
    ) {
        try {
            ValidarElementosVisualesResponse resultado =
                    validacionElementosVisualesService.validarElementosVisuales(
                            archivo,
                            tipoDocumento
                    );

            registrarEtapa(
                    response,
                    "20",
                    "Validar elementos visuales obligatorios",
                    resultado.isElementosVisualesValidos(),
                    resultado.getEstadoValidacionDocumental(),
                    resultado.getMensajeValidacion(),
                    resultado.getObservaciones()
            );

        } catch (Exception e) {
            registrarEtapa(
                    response,
                    "20",
                    "Validar elementos visuales obligatorios",
                    false,
                    "ERROR_VALIDACION_ELEMENTOS_VISUALES",
                    "No se pudo validar los elementos visuales obligatorios.",
                    List.of(e.getMessage())
            );
        }
    }

    private void ejecutarValidacionFirmaTrabajador(
            ValidacionDocumentalCompletaResponse response,
            MultipartFile archivo,
            String tipoDocumento
    ) {
        try {
            ValidarFirmaTrabajadorResponse resultado =
                    validacionFirmaTrabajadorService.validarFirmaTrabajador(
                            archivo,
                            tipoDocumento
                    );

            registrarEtapa(
                    response,
                    "21",
                    "Validar presencia de firma manuscrita",
                    resultado.isFirmaValida(),
                    resultado.getEstadoValidacionDocumental(),
                    resultado.getMensajeValidacion(),
                    resultado.getObservaciones()
            );

        } catch (Exception e) {
            registrarEtapa(
                    response,
                    "21",
                    "Validar presencia de firma manuscrita",
                    false,
                    "ERROR_VALIDACION_FIRMA",
                    "No se pudo validar la presencia de firma manuscrita.",
                    List.of(e.getMessage())
            );
        }
    }

    private void finalizarValidacion(
            ValidacionDocumentalCompletaResponse response,
            String usuarioAutenticado,
            String ipOrigen,
            String datosSesionDispositivo
    ) {
        int aprobadas = 0;
        int rechazadas = 0;

        for (DetalleEtapaValidacionResponse etapa : response.getEtapas()) {
            if (etapa.isEtapaAprobada()) {
                aprobadas++;
            } else {
                rechazadas++;
            }
        }

        boolean documentoAprobado = rechazadas == 0;

        response.setValidacionCompletaEjecutada(true);
        response.setDocumentoAprobado(documentoAprobado);
        response.setPermiteNuevaCargaTrabajador(!documentoAprobado);

        response.setTotalEtapasEjecutadas(response.getEtapas().size());
        response.setTotalEtapasAprobadas(aprobadas);
        response.setTotalEtapasRechazadas(rechazadas);

        if (documentoAprobado) {
            response.setEstadoValidacionDocumental(EstadoProcesoConstants.VALIDACION_DOCUMENTAL_APROBADA);
            response.setMensajeValidacion("El documento superó la validación documental completa.");
        } else {
            response.setEstadoValidacionDocumental(EstadoProcesoConstants.RECHAZADO_VALIDACION_DOCUMENTAL);
            response.setMensajeValidacion("El documento no superó la validación documental completa. Debe cargarse nuevamente el documento corregido.");
        }

        registrarAvanceExpediente(
                response,
                usuarioAutenticado,
                ipOrigen,
                datosSesionDispositivo
        );
    }

    private void registrarAvanceExpediente(
            ValidacionDocumentalCompletaResponse response,
            String usuarioAutenticado,
            String ipOrigen,
            String datosSesionDispositivo
    ) {
        try {
            RegistrarAvanceExpedienteRequest request = new RegistrarAvanceExpedienteRequest();

            request.setRegistroInternoProceso(response.getRegistroInternoProceso());
            request.setTipoDocumentoTrabajador(response.getTipoDocumentoTrabajador());
            request.setNumeroDocumentoTrabajador(response.getNumeroDocumentoTrabajador());
            request.setCanalAcceso(EstadoProcesoConstants.CANAL_SOMOS_ESSALUD);
            request.setEstadoOperativo(response.getEstadoValidacionDocumental());
            request.setDescripcionAvance(response.getMensajeValidacion());
            request.setUsuarioAutenticado(valorPorDefecto(usuarioAutenticado, EstadoProcesoConstants.USUARIO_SISTEMA));
            request.setIpOrigen(ipOrigen);
            request.setDatosSesionDispositivo(datosSesionDispositivo);
            request.setTipoDocumentoProceso(response.getTipoDocumento());
            request.setIdDocumentoCargado(response.getIdDocumentoCargado());

            expedienteDigitalService.registrarAvance(request);
            response.setExpedienteActualizado(true);

        } catch (Exception e) {
            response.setExpedienteActualizado(false);
            response.getObservaciones().add(
                    "La validación fue ejecutada, pero no se pudo actualizar el expediente digital: "
                            + e.getMessage()
            );
        }
    }

    private void registrarEtapa(
            ValidacionDocumentalCompletaResponse response,
            String codigoEtapa,
            String nombreEtapa,
            boolean etapaAprobada,
            String estadoEtapa,
            String mensajeEtapa,
            List<String> observaciones
    ) {
        DetalleEtapaValidacionResponse etapa = new DetalleEtapaValidacionResponse();

        etapa.setCodigoEtapa(codigoEtapa);
        etapa.setNombreEtapa(nombreEtapa);
        etapa.setEtapaAprobada(etapaAprobada);
        etapa.setEstadoEtapa(estadoEtapa);
        etapa.setMensajeEtapa(mensajeEtapa);
        etapa.setObservaciones(copiarObservaciones(observaciones));

        response.getEtapas().add(etapa);

        if (!etapaAprobada) {
            response.getObservaciones().add(mensajeEtapa);

            if (observaciones != null) {
                response.getObservaciones().addAll(observaciones);
            }
        }
    }

    private boolean validacionRechazada(
            ValidacionDocumentalCompletaResponse response
    ) {
        if (response.getEtapas().isEmpty()) {
            return false;
        }

        DetalleEtapaValidacionResponse ultimaEtapa =
                response.getEtapas().get(response.getEtapas().size() - 1);

        return !ultimaEtapa.isEtapaAprobada();
    }

    private ValidacionDocumentalCompletaResponse construirResponseBase(
            String registroInternoProceso,
            String tipoDocumento,
            String tipoDocumentoTrabajador,
            String numeroDocumentoTrabajador,
            String idDocumentoCargado
    ) {
        ValidacionDocumentalCompletaResponse response = new ValidacionDocumentalCompletaResponse();

        response.setRegistroInternoProceso(registroInternoProceso);
        response.setTipoDocumento(tipoDocumento);
        response.setTipoDocumentoTrabajador(
                valorPorDefecto(tipoDocumentoTrabajador, EstadoProcesoConstants.TIPO_DOCUMENTO_TRABAJADOR_DNI)
        );
        response.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);
        response.setIdDocumentoCargado(idDocumentoCargado);
        response.setFechaHoraValidacion(LocalDateTime.now(ZONA_HORARIA_LIMA));

        return response;
    }

    private List<String> copiarObservaciones(List<String> observaciones) {
        if (observaciones == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(observaciones);
    }

    private void validarParametros(
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF es obligatorio.");
        }

        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(tipoDocumento)) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }

        if (campoVacio(numeroDocumentoTrabajador)) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }
    }

    private String valorPorDefecto(String valor, String valorDefecto) {
        return campoVacio(valor) ? valorDefecto : valor;
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}