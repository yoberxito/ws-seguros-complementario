package essalud.gob.pe.wsseguroscomplementario.documento.service;
import essalud.gob.pe.wsseguroscomplementario.proceso.service.DepuracionFinalProcesoVidaService;
import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.CierreDocumentalCompletoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.DetalleEtapaCierreDocumentalResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarDocumentoSelladoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.PublicarDocumentoRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.RegistrarIncidenciaOperativaRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.service.IncidenciaOperativaService;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.PublicarDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.RegistrarAvanceExpedienteRequest;
import essalud.gob.pe.wsseguroscomplementario.expediente.service.ExpedienteDigitalService;
import org.springframework.stereotype.Service;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoSellado;
import org.springframework.web.multipart.MultipartFile;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoSustentoRepository;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.ProcesoVidaRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class CierreDocumentalCompletoService {

    private static final ZoneId ZONA_HORARIA_LIMA =
            ZoneId.of(EstadoProcesoConstants.ZONA_HORARIA_LIMA);
    private static final String
            TIPO_FLUJO_COMPLETO =
            "COMPLETO";

    private static final String
            TIPO_FLUJO_SOLO_AUTORIZACION =
            "SOLO_AUTORIZACION";

    private static final String
            TIPO_FLUJO_FORMULARIO_6012_POSTERIOR =
            "FORMULARIO_6012_POSTERIOR";

    private static final String
            TIPO_DOCUMENTO_AUTORIZACION =
            "AUTORIZACION_DESCUENTO";

    private static final String
            TIPO_DOCUMENTO_6012 =
            "FORMULARIO_6012";

    private static final String
            ESTADO_FINALIZACION =
            "FINALIZACION";
    private final GeneracionDocumentoSelladoService generacionDocumentoSelladoService;
    private final PublicacionDocumentoService publicacionDocumentoService;
    private final ExpedienteDigitalService expedienteDigitalService;
    private final DocumentoSustentoRepository documentoSustentoRepository;
    private final ProcesoVidaRepository procesoVidaRepository;
    private final IncidenciaOperativaService
            incidenciaOperativaService;
    private final RespaldoFormulario6012Service
            respaldoFormulario6012Service;
    private final DepuracionFinalProcesoVidaService
            depuracionFinalProcesoVidaService;

    public CierreDocumentalCompletoService(
            GeneracionDocumentoSelladoService generacionDocumentoSelladoService,
            PublicacionDocumentoService publicacionDocumentoService,
            ExpedienteDigitalService expedienteDigitalService,
            DocumentoSustentoRepository documentoSustentoRepository,
            ProcesoVidaRepository procesoVidaRepository,
            IncidenciaOperativaService incidenciaOperativaService,
            RespaldoFormulario6012Service
                    respaldoFormulario6012Service,
            DepuracionFinalProcesoVidaService
                    depuracionFinalProcesoVidaService
    ) {
        this.generacionDocumentoSelladoService =
                generacionDocumentoSelladoService;

        this.publicacionDocumentoService =
                publicacionDocumentoService;

        this.expedienteDigitalService =
                expedienteDigitalService;

        this.documentoSustentoRepository =
                documentoSustentoRepository;

        this.procesoVidaRepository =
                procesoVidaRepository;
        this.incidenciaOperativaService =
                incidenciaOperativaService;
        this.respaldoFormulario6012Service =
                respaldoFormulario6012Service;
        this.depuracionFinalProcesoVidaService =
                depuracionFinalProcesoVidaService;
    }

    public CierreDocumentalCompletoResponse procesarCierreDocumental(
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String tipoDocumentoTrabajador,
            String numeroDocumentoTrabajador,
            String nombresApellidosTrabajador,
            String canalPublicacion,
            String publicadoPor,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {
        validarParametros(
                archivo,
                registroInternoProceso,
                tipoDocumento,
                tipoDocumentoTrabajador,
                numeroDocumentoTrabajador
        );

        CierreDocumentalCompletoResponse response = construirResponseBase(
                registroInternoProceso,
                tipoDocumento,
                tipoDocumentoTrabajador,
                numeroDocumentoTrabajador,
                nombresApellidosTrabajador,
                canalPublicacion
        );

        DocumentoSellado documentoSellado =
                ejecutarSellado(
                        response,
                        archivo,
                        registroInternoProceso,
                        tipoDocumento,
                        numeroDocumentoTrabajador
                );

        if (cierreTieneError(response)) {
            finalizarCierre(
                    response,
                    usuarioResponsable,
                    ipOrigen,
                    datosSesionDispositivo
            );
            return response;
        }

        ejecutarPublicacion(
                response,
                documentoSellado,
                registroInternoProceso,
                tipoDocumento,
                response.getTipoDocumentoTrabajador(),
                response.getNumeroDocumentoTrabajador(),
                canalPublicacion,
                publicadoPor
        );

        finalizarCierre(
                response,
                usuarioResponsable,
                ipOrigen,
                datosSesionDispositivo
        );

        return response;
    }

    private DocumentoSellado ejecutarSellado(
            CierreDocumentalCompletoResponse response,
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {

        try {

            DocumentoSellado documentoSellado =
                    generacionDocumentoSelladoService
                            .generarDocumentoSelladoTemporal(
                                    archivo,
                                    registroInternoProceso,
                                    tipoDocumento,
                                    numeroDocumentoTrabajador
                            );

            response.setIdDocumentoSellado(
                    documentoSellado
                            .getIdDocumentoSellado()
            );

            boolean selladoCorrecto =
                    !campoVacio(
                            documentoSellado
                                    .getIdDocumentoSellado()
                    );

            registrarEtapa(
                    response,
                    "23",
                    "Generar documento validado y sellado",
                    selladoCorrecto,
                    selladoCorrecto
                            ? EstadoProcesoConstants.DOCUMENTO_SELLADO
                            : "ERROR_SELLADO_DOCUMENTO",
                    selladoCorrecto
                            ? "Documento sellado generado correctamente."
                            : "No se pudo generar el documento sellado."
            );

            return selladoCorrecto
                    ? documentoSellado
                    : null;

        } catch (Exception e) {

            registrarEtapa(
                    response,
                    "23",
                    "Generar documento validado y sellado",
                    false,
                    "ERROR_SELLADO_DOCUMENTO",
                    "No se pudo generar el documento sellado: "
                            + e.getMessage()
            );

            return null;
        }
    }

    private void ejecutarPublicacion(
            CierreDocumentalCompletoResponse response,
            DocumentoSellado documentoSellado,
            String registroInternoProceso,
            String tipoDocumento,
            String tipoDocumentoTrabajador,
            String numeroDocumentoTrabajador,
            String canalPublicacion,
            String publicadoPor
    ) {
        try {
            PublicarDocumentoRequest request = new PublicarDocumentoRequest();

            request.setIdDocumentoSellado(
                    response.getIdDocumentoSellado()
            );

            request.setRegistroInternoProceso(
                    registroInternoProceso
            );

            request.setTipoDocumento(
                    tipoDocumento
            );

            request.setTipoDocumentoTrabajador(
                    tipoDocumentoTrabajador
            );

            request.setNumeroDocumentoTrabajador(
                    numeroDocumentoTrabajador
            );
            request.setCanalPublicacion(valorPorDefecto(canalPublicacion, EstadoProcesoConstants.CANAL_SOMOS_ESSALUD));
            request.setPublicadoPor(valorPorDefecto(publicadoPor, EstadoProcesoConstants.USUARIO_SISTEMA));

            PublicarDocumentoResponse publicacion =
                    publicacionDocumentoService
                            .publicarDocumento(
                                    request,
                                    documentoSellado
                            );

            response.setIdDocumentoPublicado(publicacion.getIdDocumentoPublicado());
            response.setIdDocumentoSellado(
                    publicacion.getIdDocumentoSellado()
            );
            response.setUrlVisualizacionSimulada(publicacion.getUrlVisualizacionSimulada());

            boolean publicacionCorrecta =
                    publicacion.isPublicado()
                            && publicacion.isDisponibleParaUsuario()
                            && publicacion.isSelloValidadoAntesPublicacion();

            registrarEtapa(
                    response,
                    "24",
                    "Publicar documento validado y sellado",
                    publicacionCorrecta,
                    publicacion.getEstadoPublicacionDocumental(),
                    publicacion.getMensajePublicacion()
            );

        } catch (Exception e) {
            registrarEtapa(
                    response,
                    "24",
                    "Publicar documento validado y sellado",
                    false,
                    "ERROR_PUBLICACION_DOCUMENTO",
                    "No se pudo publicar el documento sellado: " + e.getMessage()
            );
        }
    }

    private void finalizarCierre(
            CierreDocumentalCompletoResponse response,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {
        int aprobadas = 0;
        int rechazadas = 0;

        for (DetalleEtapaCierreDocumentalResponse etapa : response.getEtapas()) {
            if (etapa.isEtapaAprobada()) {
                aprobadas++;
            } else {
                rechazadas++;
            }
        }

        boolean cierreCompletado = rechazadas == 0;

        response.setCierreEjecutado(true);
        response.setCierreCompletado(cierreCompletado);
        response.setRequiereIntervencionInterna(!cierreCompletado);

        response.setTotalEtapasEjecutadas(response.getEtapas().size());
        response.setTotalEtapasAprobadas(aprobadas);
        response.setTotalEtapasRechazadas(rechazadas);

        if (cierreCompletado) {

            response.setEstadoCierreDocumental(
                    EstadoProcesoConstants
                            .DOCUMENTO_PUBLICADO
            );

            response.setMensajeCierre(
                    "El documento fue sellado y "
                            + "publicado correctamente."
            );

            try {

                if (
                        TIPO_DOCUMENTO_6012.equalsIgnoreCase(
                                response.getTipoDocumento()
                        )
                ) {
                    respaldoFormulario6012Service
                            .generarYPersistir(
                                    response
                                            .getRegistroInternoProceso(),

                                    response
                                            .getIdDocumentoPublicado()
                            );
                }

                actualizarFinalizacionSiCorresponde(
                        response
                );

                incidenciaOperativaService
                        .cerrarIncidenciasAbiertasPorSistema(
                                response
                                        .getRegistroInternoProceso(),

                                response
                                        .getNumeroDocumentoTrabajador(),

                                response
                                        .getTipoDocumento(),

                                "CIERRE_DOCUMENTAL",

                                "El sellado y la publicación "
                                        + "se completaron correctamente.",

                                valorPorDefecto(
                                        usuarioResponsable,
                                        EstadoProcesoConstants
                                                .USUARIO_SISTEMA
                                ),

                                ipOrigen,
                                datosSesionDispositivo,

                                response
                                        .getIdDocumentoSellado(),

                                response
                                        .getIdDocumentoPublicado()
                        );

                depuracionFinalProcesoVidaService
                        .depurarSiCorresponde(
                                response
                                        .getRegistroInternoProceso()
                        );

            } catch (Exception e) {

                response.setCierreCompletado(
                        false
                );

                response.setRequiereIntervencionInterna(
                        true
                );

                response.setEstadoCierreDocumental(
                        EstadoProcesoConstants
                                .ERROR_CIERRE_DOCUMENTAL
                );

                response.setMensajeCierre(
                        "Los documentos fueron procesados, "
                                + "pero no fue posible completar "
                                + "la actualización final del trámite."
                );

                registrarIncidenciaGeneralCierre(
                        response,
                        "FINALIZACION_TRAMITE",
                        "ERROR_ACTUALIZACION_FINALIZACION",
                        "No fue posible actualizar el estado "
                                + "final del trámite.",
                        e.getMessage(),
                        usuarioResponsable,
                        ipOrigen,
                        datosSesionDispositivo
                );
            }

        } else {

            response.setEstadoCierreDocumental(
                    EstadoProcesoConstants
                            .ERROR_CIERRE_DOCUMENTAL
            );

            response.setMensajeCierre(
                    "No se pudo completar el cierre documental. "
                            + "El documento validado no debe "
                            + "corregirse ni reemplazarse."
            );

            registrarIncidenciaCierre(
                    response,
                    usuarioResponsable,
                    ipOrigen,
                    datosSesionDispositivo
            );
        }

        registrarAvanceExpediente(
                response,
                usuarioResponsable,
                ipOrigen,
                datosSesionDispositivo
        );
    }

    private void actualizarFinalizacionSiCorresponde(
            CierreDocumentalCompletoResponse response
    ) {

        ProcesoVida procesoVida =
                procesoVidaRepository
                        .buscarPorRegistroInternoProceso(
                                response
                                        .getRegistroInternoProceso()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No se encontró el proceso +Vida asociado al cierre documental."
                                )
                        );

        String tipoFlujo =
                procesoVida.getTipoFlujo();

        if (
                tipoFlujo == null
                        || tipoFlujo.trim().isEmpty()
        ) {
            throw new IllegalStateException(
                    "El proceso +Vida no tiene un tipo de flujo documental definido."
            );
        }

        boolean flujoCompletado;

        switch (
                tipoFlujo.trim().toUpperCase()
        ) {

            case TIPO_FLUJO_COMPLETO ->

                    flujoCompletado =
                            documentoSustentoRepository
                                    .estaPublicado(
                                            response
                                                    .getRegistroInternoProceso(),

                                            TIPO_DOCUMENTO_AUTORIZACION
                                    )

                                    &&

                                    documentoSustentoRepository
                                            .estaPublicado(
                                                    response
                                                            .getRegistroInternoProceso(),

                                                    TIPO_DOCUMENTO_6012
                                            );

            case TIPO_FLUJO_SOLO_AUTORIZACION ->

                    flujoCompletado =
                            documentoSustentoRepository
                                    .estaPublicado(
                                            response
                                                    .getRegistroInternoProceso(),

                                            TIPO_DOCUMENTO_AUTORIZACION
                                    );

            case TIPO_FLUJO_FORMULARIO_6012_POSTERIOR ->

                    flujoCompletado =
                            documentoSustentoRepository
                                    .estaPublicado(
                                            response
                                                    .getRegistroInternoProceso(),

                                            TIPO_DOCUMENTO_6012
                                    );

            default ->
                    throw new IllegalStateException(
                            "El tipo de flujo documental del proceso +Vida no es reconocido: "
                                    + tipoFlujo
                    );
        }

        if (!flujoCompletado) {
            return;
        }

        procesoVidaRepository
                .actualizarEstado(
                        response
                                .getRegistroInternoProceso(),

                        ESTADO_FINALIZACION
                );
    }

    private void registrarAvanceExpediente(
            CierreDocumentalCompletoResponse response,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {
        try {
            RegistrarAvanceExpedienteRequest request = new RegistrarAvanceExpedienteRequest();

            request.setRegistroInternoProceso(response.getRegistroInternoProceso());
            request.setTipoDocumentoTrabajador(response.getTipoDocumentoTrabajador());
            request.setNumeroDocumentoTrabajador(response.getNumeroDocumentoTrabajador());
            request.setNombresApellidosTrabajador(response.getNombresApellidosTrabajador());
            request.setCanalAcceso(EstadoProcesoConstants.CANAL_SOMOS_ESSALUD);
            request.setEstadoOperativo(response.getEstadoCierreDocumental());
            request.setDescripcionAvance(response.getMensajeCierre());
            request.setUsuarioAutenticado(valorPorDefecto(usuarioResponsable, EstadoProcesoConstants.USUARIO_SISTEMA));
            request.setIpOrigen(ipOrigen);
            request.setDatosSesionDispositivo(datosSesionDispositivo);
            request.setTipoDocumentoProceso(response.getTipoDocumento());
            request.setIdDocumentoSellado(response.getIdDocumentoSellado());
            request.setIdDocumentoPublicado(response.getIdDocumentoPublicado());

            expedienteDigitalService.registrarAvance(request);
            response.setExpedienteActualizado(true);

        } catch (Exception e) {
            response.setExpedienteActualizado(false);
            response.getObservaciones().add(
                    "El cierre fue ejecutado, pero no se pudo actualizar el expediente digital: "
                            + e.getMessage()
            );
        }
    }

    private void registrarEtapa(
            CierreDocumentalCompletoResponse response,
            String codigoEtapa,
            String nombreEtapa,
            boolean etapaAprobada,
            String estadoEtapa,
            String mensajeEtapa
    ) {
        DetalleEtapaCierreDocumentalResponse etapa = new DetalleEtapaCierreDocumentalResponse();

        etapa.setCodigoEtapa(codigoEtapa);
        etapa.setNombreEtapa(nombreEtapa);
        etapa.setEtapaAprobada(etapaAprobada);
        etapa.setEstadoEtapa(estadoEtapa);
        etapa.setMensajeEtapa(mensajeEtapa);

        response.getEtapas().add(etapa);

        if (!etapaAprobada) {
            response.getObservaciones().add(mensajeEtapa);
        }
    }

    private boolean cierreTieneError(
            CierreDocumentalCompletoResponse response
    ) {
        if (response.getEtapas().isEmpty()) {
            return false;
        }

        DetalleEtapaCierreDocumentalResponse ultimaEtapa =
                response.getEtapas().get(response.getEtapas().size() - 1);

        return !ultimaEtapa.isEtapaAprobada();
    }

    private CierreDocumentalCompletoResponse construirResponseBase(
            String registroInternoProceso,
            String tipoDocumento,
            String tipoDocumentoTrabajador,
            String numeroDocumentoTrabajador,
            String nombresApellidosTrabajador,
            String canalPublicacion
    ) {
        CierreDocumentalCompletoResponse response = new CierreDocumentalCompletoResponse();

        response.setRegistroInternoProceso(registroInternoProceso);
        response.setTipoDocumento(tipoDocumento);
        response.setTipoDocumentoTrabajador(
                tipoDocumentoTrabajador.trim()
        );
        response.setNumeroDocumentoTrabajador(
                numeroDocumentoTrabajador.trim()
        );
        response.setNombresApellidosTrabajador(nombresApellidosTrabajador);
        response.setCanalPublicacion(valorPorDefecto(canalPublicacion, EstadoProcesoConstants.CANAL_SOMOS_ESSALUD));
        response.setFechaHoraCierre(LocalDateTime.now(ZONA_HORARIA_LIMA));

        return response;
    }

    private void validarParametros(
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String tipoDocumentoTrabajador,
            String numeroDocumentoTrabajador
    ) {
        if (
                archivo == null
                        || archivo.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "El archivo PDF firmado es obligatorio."
            );
        }

        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        if (campoVacio(tipoDocumento)) {
            throw new IllegalArgumentException(
                    "El tipo de documento lógico es obligatorio."
            );
        }

        if (campoVacio(tipoDocumentoTrabajador)) {
            throw new IllegalArgumentException(
                    "El tipo de documento del trabajador es obligatorio."
            );
        }

        if (campoVacio(numeroDocumentoTrabajador)) {
            throw new IllegalArgumentException(
                    "El número de documento del trabajador es obligatorio."
            );
        }
    }

    private String valorPorDefecto(String valor, String valorDefecto) {
        return campoVacio(valor) ? valorDefecto : valor;
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void registrarIncidenciaCierre(
            CierreDocumentalCompletoResponse response,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        DetalleEtapaCierreDocumentalResponse etapaFallida =
                response
                        .getEtapas()
                        .stream()
                        .filter(
                                etapa ->
                                        !etapa
                                                .isEtapaAprobada()
                        )
                        .findFirst()
                        .orElse(null);

        String etapa =
                etapaFallida == null
                        ? "CIERRE_DOCUMENTAL"
                        : "ETAPA_"
                          + etapaFallida
                        .getCodigoEtapa();

        String tipoIncidencia =
                etapaFallida == null
                        || campoVacio(
                        etapaFallida
                                .getEstadoEtapa()
                )
                        ? EstadoProcesoConstants
                          .ERROR_CIERRE_DOCUMENTAL
                        : etapaFallida
                        .getEstadoEtapa();

        String motivo =
                etapaFallida == null
                        || campoVacio(
                        etapaFallida
                                .getMensajeEtapa()
                )
                        ? response
                        .getMensajeCierre()
                        : etapaFallida
                        .getMensajeEtapa();

        registrarIncidenciaGeneralCierre(
                response,
                etapa,
                tipoIncidencia,
                motivo,
                String.join(
                        " ",
                        response
                                .getObservaciones()
                ),
                usuarioResponsable,
                ipOrigen,
                datosSesionDispositivo
        );
    }

    private void registrarIncidenciaGeneralCierre(
            CierreDocumentalCompletoResponse response,
            String etapaProceso,
            String tipoIncidencia,
            String motivo,
            String detalle,
            String usuarioResponsable,
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

        request.setIdDocumentoSellado(
                response
                        .getIdDocumentoSellado()
        );

        request.setIdDocumentoPublicado(
                response
                        .getIdDocumentoPublicado()
        );

        request.setSistemaInvolucrado(
                "CIERRE_DOCUMENTAL"
        );

        request.setEtapaProceso(
                etapaProceso
        );

        request.setTipoIncidenciaOperativa(
                tipoIncidencia
        );

        request.setMotivoObservado(
                motivo
        );

        request.setDetalleIncidencia(
                detalle
        );

        request.setUsuarioResponsable(
                valorPorDefecto(
                        usuarioResponsable,
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
}