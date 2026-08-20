package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.DetalleEtapaValidacionResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionDocumentalCompletaResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionPdfResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarLegibilidadOcrResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarCorrespondenciaDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarElementosVisualesResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarFirmaTrabajadorResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarPaginasDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.RegistrarAvanceExpedienteRequest;
import essalud.gob.pe.wsseguroscomplementario.expediente.service.ExpedienteDigitalService;
import essalud.gob.pe.wsseguroscomplementario.documento.model.RechazoDocumento;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.RegistrarIncidenciaOperativaRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.service.IncidenciaOperativaService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoSustentoRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidacionDocumentalCompletaService {

    private static final ZoneId ZONA_HORARIA_LIMA =
            ZoneId.of(EstadoProcesoConstants.ZONA_HORARIA_LIMA);
    private final RechazoDocumentoService rechazoDocumentoService;
    private final ValidacionPdfService validacionPdfService;
    private final ValidacionCorrespondenciaDocumentoService validacionCorrespondenciaDocumentoService;
    private final ValidacionPaginasDocumentoService validacionPaginasDocumentoService;
    private final ValidacionElementosVisualesService validacionElementosVisualesService;
    private final ValidacionFirmaTrabajadorService validacionFirmaTrabajadorService;
    private final ExpedienteDigitalService expedienteDigitalService;
    private final IncidenciaOperativaService
            incidenciaOperativaService;
    private final ValidacionLegibilidadOcrService
            validacionLegibilidadOcrService;
    private final DocumentoSustentoRepository documentoSustentoRepository;

    public ValidacionDocumentalCompletaService(
            ValidacionPdfService validacionPdfService,
            ValidacionCorrespondenciaDocumentoService validacionCorrespondenciaDocumentoService,
            ValidacionPaginasDocumentoService validacionPaginasDocumentoService,
            ValidacionLegibilidadOcrService validacionLegibilidadOcrService,
            ValidacionElementosVisualesService validacionElementosVisualesService,
            ValidacionFirmaTrabajadorService validacionFirmaTrabajadorService,
            RechazoDocumentoService rechazoDocumentoService,
            ExpedienteDigitalService expedienteDigitalService,
            DocumentoSustentoRepository documentoSustentoRepository,
            IncidenciaOperativaService incidenciaOperativaService
    ) {
        this.validacionPdfService = validacionPdfService;
        this.validacionCorrespondenciaDocumentoService = validacionCorrespondenciaDocumentoService;
        this.validacionPaginasDocumentoService = validacionPaginasDocumentoService;
        this.validacionElementosVisualesService = validacionElementosVisualesService;
        this.validacionFirmaTrabajadorService = validacionFirmaTrabajadorService;
        this.expedienteDigitalService = expedienteDigitalService;
        this.rechazoDocumentoService =
                rechazoDocumentoService;
        this.validacionLegibilidadOcrService =
                validacionLegibilidadOcrService;
        this.documentoSustentoRepository =
                documentoSustentoRepository;
        this.incidenciaOperativaService =
                incidenciaOperativaService;
    }
    @Transactional
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

        if (
                documentoSustentoRepository
                        .estaPublicado(
                                registroInternoProceso,
                                tipoDocumento
                        )
        ) {
            throw new IllegalArgumentException(
                    "El documento ya se encuentra publicado y no admite nuevas validaciones."
            );
        }

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

        ejecutarValidacionPaginas(
                response,
                archivo,
                registroInternoProceso,
                tipoDocumento,
                numeroDocumentoTrabajador
        );

        if (validacionRechazada(response)) {
            finalizarValidacion(
                    response,
                    usuarioAutenticado,
                    ipOrigen,
                    datosSesionDispositivo
            );

            return response;
        }

        /*
         * Etapa 19:
         * Validación de legibilidad mediante OCR
         * y análisis técnico de nitidez.
         *
         * Esta etapa no valida identidad,
         * correspondencia ni secuencia.
         */
        ejecutarValidacionLegibilidad(
                response,
                archivo,
                tipoDocumento
        );

        if (validacionRechazada(response)) {
            finalizarValidacion(
                    response,
                    usuarioAutenticado,
                    ipOrigen,
                    datosSesionDispositivo
            );

            return response;
        }

        ejecutarValidacionElementosVisuales(
                response,
                archivo,
                tipoDocumento
        );
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

    private void ejecutarValidacionLegibilidad(
            ValidacionDocumentalCompletaResponse response,
            MultipartFile archivo,
            String tipoDocumento
    ) {
        try {

            ValidarLegibilidadOcrResponse resultado =
                    validacionLegibilidadOcrService
                            .validarLegibilidad(
                                    archivo,
                                    tipoDocumento
                            );

            registrarEtapa(
                    response,
                    "19",
                    "Validar legibilidad mediante OCR",
                    resultado.isLegibilidadValida(),
                    resultado.getEstadoValidacionDocumental(),
                    resultado.getMensajeValidacion(),
                    resultado.getObservaciones()
            );

        } catch (Exception e) {

            registrarEtapa(
                    response,
                    "19",
                    "Validar legibilidad mediante OCR",
                    false,
                    "ERROR_VALIDACION_LEGIBILIDAD_OCR",
                    "No se pudo ejecutar la validación de legibilidad del documento.",
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

        for (
                DetalleEtapaValidacionResponse etapa
                : response.getEtapas()
        ) {
            if (etapa.isEtapaAprobada()) {
                aprobadas++;
            } else {
                rechazadas++;
            }
        }

        response.setValidacionCompletaEjecutada(
                true
        );

        response.setTotalEtapasEjecutadas(
                response.getEtapas().size()
        );

        response.setTotalEtapasAprobadas(
                aprobadas
        );

        response.setTotalEtapasRechazadas(
                rechazadas
        );

        DetalleEtapaValidacionResponse
                errorTecnico =
                obtenerErrorTecnico(
                        response
                );

        /*
         * Un ERROR_* representa un problema técnico
         * del sistema, no un defecto atribuible al PDF.
         *
         * Por tanto:
         * - no genera RECH-DOC;
         * - no cambia DOCUMENTOS_SUSTENTO a RECHAZADO;
         * - no exige una nueva carga corregida;
         * - registra INCIDENCIA_OPERATIVA.
         */
        if (errorTecnico != null) {

            response.setDocumentoAprobado(
                    false
            );

            response.setPermiteNuevaCargaTrabajador(
                    false
            );

            response.setEstadoValidacionDocumental(
                    EstadoProcesoConstants
                            .OBSERVADO_OPERATIVO
            );

            response.setMensajeValidacion(
                    "No fue posible completar la validación "
                            + "documental por una incidencia interna. "
                            + "El documento no fue rechazado. "
                            + "Intente nuevamente."
            );

            registrarIncidenciaTecnicaValidacion(
                    response,
                    errorTecnico,
                    usuarioAutenticado,
                    ipOrigen,
                    datosSesionDispositivo
            );

            registrarAvanceExpediente(
                    response,
                    usuarioAutenticado,
                    ipOrigen,
                    datosSesionDispositivo
            );

            return;
        }

        boolean documentoAprobado =
                rechazadas == 0;

        response.setDocumentoAprobado(
                documentoAprobado
        );

        response.setPermiteNuevaCargaTrabajador(
                !documentoAprobado
        );

        /*
         * Si llegamos hasta aquí, el motor volvió a
         * ejecutarse normalmente.
         *
         * Una incidencia técnica anterior de validación
         * ya puede cerrarse, independientemente de que
         * el nuevo resultado funcional sea APROBADO
         * o RECHAZADO.
         */
        incidenciaOperativaService
                .cerrarIncidenciasAbiertasPorSistema(
                        response
                                .getRegistroInternoProceso(),

                        response
                                .getNumeroDocumentoTrabajador(),

                        response
                                .getTipoDocumento(),

                        "VALIDACION_DOCUMENTAL",

                        "El motor de validación documental "
                                + "volvió a ejecutarse correctamente.",

                        valorPorDefecto(
                                usuarioAutenticado,
                                EstadoProcesoConstants
                                        .USUARIO_SISTEMA
                        ),

                        ipOrigen,
                        datosSesionDispositivo,
                        null,
                        null
                );

        if (documentoAprobado) {

            response.setEstadoValidacionDocumental(
                    EstadoProcesoConstants
                            .VALIDACION_DOCUMENTAL_APROBADA
            );

            response.setMensajeValidacion(
                    "El documento superó la validación "
                            + "documental completa."
            );

            documentoSustentoRepository
                    .registrarValidacionAprobada(
                            response
                                    .getRegistroInternoProceso(),

                            response
                                    .getTipoDocumento(),

                            response
                                    .getFechaHoraValidacion()
                    );

        } else {

            response.setEstadoValidacionDocumental(
                    EstadoProcesoConstants
                            .RECHAZADO_VALIDACION_DOCUMENTAL
            );

            response.setMensajeValidacion(
                    construirMensajeRechazo(
                            response
                    )
            );

            RechazoDocumento rechazoDocumento =
                    rechazoDocumentoService
                            .registrarRechazoDesdeValidacionCompleta(
                                    response,
                                    ipOrigen,
                                    datosSesionDispositivo
                            );

            documentoSustentoRepository
                    .registrarValidacionRechazada(
                            response
                                    .getRegistroInternoProceso(),

                            response
                                    .getTipoDocumento(),

                            response
                                    .getFechaHoraValidacion(),

                            rechazoDocumento
                                    .getIdRechazoDocumental(),

                            response
                                    .getMensajeValidacion()
                    );
        }

        registrarAvanceExpediente(
                response,
                usuarioAutenticado,
                ipOrigen,
                datosSesionDispositivo
        );
    }

    private String construirMensajeRechazo(
            ValidacionDocumentalCompletaResponse response
    ) {
        for (
                DetalleEtapaValidacionResponse etapa
                : response.getEtapas()
        ) {
            if (etapa.isEtapaAprobada()) {
                continue;
            }

            String mensajeBase =
                    campoVacio(etapa.getMensajeEtapa())
                            ? "El documento no superó la etapa: "
                              + etapa.getNombreEtapa()
                              + "."
                            : etapa.getMensajeEtapa().trim();

            /*
             * En errores técnicos no se muestran al usuario
             * mensajes internos ni excepciones del backend.
             */
            boolean errorTecnico =
                    !campoVacio(etapa.getEstadoEtapa())
                            && etapa
                            .getEstadoEtapa()
                            .trim()
                            .startsWith("ERROR_");

            if (errorTecnico) {
                return mensajeBase
                        + " Intente nuevamente. "
                        + "Si el problema continúa, comuníquese con soporte.";
            }

            List<String> motivos =
                    new ArrayList<>();

            if (etapa.getObservaciones() != null) {
                for (
                        String observacion
                        : etapa.getObservaciones()
                ) {
                    if (campoVacio(observacion)) {
                        continue;
                    }

                    String motivo =
                            observacion.trim();

                    /*
                     * Evita repetir el mismo mensaje
                     * dentro del detalle.
                     */
                    if (
                            motivo.equalsIgnoreCase(mensajeBase)
                                    || motivos.contains(motivo)
                    ) {
                        continue;
                    }

                    motivos.add(motivo);
                }
            }

            if (motivos.isEmpty()) {
                return mensajeBase
                        + " Corrija el documento y vuelva a cargarlo.";
            }

            return mensajeBase
                    + " Motivo: "
                    + String.join(" ", motivos)
                    + " Corrija el documento y vuelva a cargarlo.";
        }

        return "El documento no superó la validación documental. "
                + "Corrija el archivo y vuelva a cargarlo.";
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

    private DetalleEtapaValidacionResponse
    obtenerErrorTecnico(
            ValidacionDocumentalCompletaResponse response
    ) {

        for (
                DetalleEtapaValidacionResponse etapa
                : response.getEtapas()
        ) {

            if (
                    !etapa.isEtapaAprobada()
                            && !campoVacio(
                            etapa.getEstadoEtapa()
                    )
                            && etapa
                            .getEstadoEtapa()
                            .trim()
                            .toUpperCase()
                            .startsWith("ERROR_")
            ) {
                return etapa;
            }
        }

        return null;
    }

    private void registrarIncidenciaTecnicaValidacion(
            ValidacionDocumentalCompletaResponse response,
            DetalleEtapaValidacionResponse etapa,
            String usuarioAutenticado,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        RegistrarIncidenciaOperativaRequest request =
                new RegistrarIncidenciaOperativaRequest();

        request.setRegistroInternoProceso(
                response
                        .getRegistroInternoProceso()
        );

        request.setTipoDocumentoTrabajador(
                response
                        .getTipoDocumentoTrabajador()
        );

        request.setNumeroDocumentoTrabajador(
                response
                        .getNumeroDocumentoTrabajador()
        );

        request.setTipoDocumentoProceso(
                response
                        .getTipoDocumento()
        );

        request.setIdDocumentoCargado(
                response
                        .getIdDocumentoCargado()
        );

        request.setSistemaInvolucrado(
                "VALIDACION_DOCUMENTAL"
        );

        request.setEtapaProceso(
                "ETAPA_"
                        + etapa.getCodigoEtapa()
        );

        request.setTipoIncidenciaOperativa(
                etapa.getEstadoEtapa()
        );

        request.setMotivoObservado(
                campoVacio(
                        etapa.getMensajeEtapa()
                )
                        ? "No fue posible ejecutar una etapa "
                          + "de validación documental."
                        : etapa.getMensajeEtapa()
        );

        request.setDetalleIncidencia(
                construirDetalleTecnico(
                        etapa
                )
        );

        request.setUsuarioResponsable(
                valorPorDefecto(
                        usuarioAutenticado,
                        EstadoProcesoConstants
                                .USUARIO_SISTEMA
                )
        );

        request.setIpOrigen(
                ipOrigen
        );

        request.setDatosSesionDispositivo(
                datosSesionDispositivo
        );

        incidenciaOperativaService
                .registrarOReintentarObservadoOperativo(
                        request
                );
    }

    private String construirDetalleTecnico(
            DetalleEtapaValidacionResponse etapa
    ) {

        if (
                etapa.getObservaciones() == null
                        || etapa.getObservaciones().isEmpty()
        ) {
            return etapa.getMensajeEtapa();
        }

        List<String> detalles =
                new ArrayList<>();

        for (
                String observacion
                : etapa.getObservaciones()
        ) {
            if (!campoVacio(observacion)) {
                detalles.add(
                        observacion.trim()
                );
            }
        }

        if (detalles.isEmpty()) {
            return etapa.getMensajeEtapa();
        }

        return String.join(
                " ",
                detalles
        );
    }
}