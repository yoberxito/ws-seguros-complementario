package essalud.gob.pe.wsseguroscomplementario.proceso.service;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.ExpedienteDigitalResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.service.ExpedienteDigitalService;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.RecuperarAvanceProcesoResponse;
import org.springframework.stereotype.Service;
import essalud.gob.pe.wsseguroscomplementario.aceptacion.model.AceptacionLegal;
import essalud.gob.pe.wsseguroscomplementario.aceptacion.repository.AceptacionRepository;

import essalud.gob.pe.wsseguroscomplementario.proceso.dto.FormularioVidaRecuperadoResponse;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.BeneficiarioVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.BeneficiarioVidaRepository;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.ProcesoVidaRepository;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReanudacionProcesoService {

    private final ExpedienteDigitalService expedienteDigitalService;

    private final ProcesoVidaRepository
            procesoVidaRepository;

    private final BeneficiarioVidaRepository
            beneficiarioVidaRepository;

    private final AceptacionRepository
            aceptacionRepository;

    public ReanudacionProcesoService(

            ExpedienteDigitalService
                    expedienteDigitalService,

            ProcesoVidaRepository
                    procesoVidaRepository,

            BeneficiarioVidaRepository
                    beneficiarioVidaRepository,

            AceptacionRepository
                    aceptacionRepository
    ) {
        this.expedienteDigitalService =
                expedienteDigitalService;

        this.procesoVidaRepository =
                procesoVidaRepository;

        this.beneficiarioVidaRepository =
                beneficiarioVidaRepository;

        this.aceptacionRepository =
                aceptacionRepository;
    }

    public RecuperarAvanceProcesoResponse
    recuperarPorRegistroInternoProceso(
            String registroInternoProceso
    ) {

        if (campoVacio(
                registroInternoProceso
        )) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        String registro =
                registroInternoProceso.trim();

        Optional<ProcesoVida> procesoOptional =
                procesoVidaRepository
                        .buscarPorRegistroInternoProceso(
                                registro
                        );

        /*
         * TEMP_SECOMASVIDA pasa a ser la fuente
         * principal para determinar si el trámite
         * +Vida realmente existe.
         */
        if (procesoOptional.isEmpty()) {
            return construirResponseSinProcesoOracle(
                    registro
            );
        }

        ProcesoVida proceso =
                procesoOptional.get();

        RecuperarAvanceProcesoResponse response =
                construirResponseDesdeProcesoOracle(
                        proceso
                );

        /*
         * El expediente digital es complementario.
         *
         * Si existe en memoria, agregamos su
         * información documental.
         *
         * Si no existe —por ejemplo después de
         * reiniciar el backend— el recupero del
         * formulario sigue siendo válido.
         */
        complementarConExpedienteSiExiste(
                response,
                registro
        );

        return response;
    }

    public RecuperarAvanceProcesoResponse
    recuperarUltimoPorTrabajador(
            String tipoDocumentoTrabajador,
            String numeroDocumentoTrabajador
    ) {

        if (campoVacio(
                tipoDocumentoTrabajador
        )) {
            throw new IllegalArgumentException(
                    "El tipo de documento del trabajador es obligatorio."
            );
        }

        if (campoVacio(
                numeroDocumentoTrabajador
        )) {
            throw new IllegalArgumentException(
                    "El número de documento del trabajador es obligatorio."
            );
        }

        String tipoDocumento =
                tipoDocumentoTrabajador.trim();

        String numeroDocumento =
                numeroDocumentoTrabajador.trim();

        Optional<ProcesoVida> procesoOptional =
                procesoVidaRepository
                        .buscarUltimoPorTrabajador(
                                tipoDocumento,
                                numeroDocumento
                        );

        if (procesoOptional.isEmpty()) {

            RecuperarAvanceProcesoResponse response =
                    construirResponseSinProcesoOracle(
                            null
                    );

            response.setTipoDocumentoTrabajador(
                    tipoDocumento
            );

            response.setNumeroDocumentoTrabajador(
                    numeroDocumento
            );

            response.setMensajeConsulta(
                    "No se encontró un proceso +Vida persistido para el trabajador informado."
            );

            return response;
        }

        /*
         * Una vez identificado el último proceso
         * directamente desde Oracle, se reutiliza
         * la recuperación integral existente.
         */
        return recuperarPorRegistroInternoProceso(
                procesoOptional
                        .get()
                        .getRegistroInternoProceso()
        );
    }

    private RecuperarAvanceProcesoResponse
    construirResponseDesdeProcesoOracle(
            ProcesoVida proceso
    ) {

        RecuperarAvanceProcesoResponse response =
                new RecuperarAvanceProcesoResponse();

        response.setProcesoEncontrado(true);

        /*
         * Al construir inicialmente desde Oracle
         * todavía no sabemos si existe un
         * ExpedienteDigital cargado en memoria.
         */
        response.setExpedienteEncontrado(false);

        response.setMensajeConsulta(
                "Proceso +Vida recuperado correctamente desde Oracle."
        );

        response.setRegistroInternoProceso(
                proceso.getRegistroInternoProceso()
        );

        response.setTipoDocumentoTrabajador(
                proceso.getCodigoDocumentoTitular()
        );

        response.setNumeroDocumentoTrabajador(
                proceso.getNumeroDocumentoTitular()
        );

        response.setNombresApellidosTrabajador(
                construirNombreCompletoTitular(
                        proceso
                )
        );

        response.setCodigoEstadoProceso(
                proceso.getCodigoEstadoProceso()
        );

        response.setRutaFrontend(
                proceso.getRutaFrontend()
        );

        response.setCodigoEstadoNavegacion(
                proceso.getCodigoEstadoNavegacion()
        );

        response.setRutaFrontendNavegacion(
                proceso.getRutaFrontendNavegacion()
        );

        response.setEstadoOperativo(
                proceso.getEstadoOperativo()
        );

        response.setTipoFlujo(
                proceso.getTipoFlujo()
        );

        response.setFechaRegistroProceso(
                proceso.getFechaRegistro()
        );

        response.setFechaActualizacionProceso(
                proceso.getFechaActualizacion()
        );

        List<BeneficiarioVida> beneficiarios =
                beneficiarioVidaRepository
                        .listarPorIdSecomasvida(
                                proceso.getIdSecomasvida()
                        );

        Optional<AceptacionLegal> aceptacion =
                aceptacionRepository
                        .buscarPorRegistroInternoProceso(
                                proceso
                                        .getRegistroInternoProceso()
                        );

        response.setFormularioVida(
                construirFormularioVidaRecuperado(
                        proceso,
                        beneficiarios,
                        aceptacion.orElse(null)
                )
        );

        boolean observado =
                "OBSERVADO".equalsIgnoreCase(
                        proceso.getEstadoOperativo()
                );

        response.setPuedeContinuarEnModulo(
                !observado
        );

        response.setPermiteNuevaCargaTrabajador(
                false
        );

        response.setRequiereIntervencionInterna(
                observado
        );

        response.setDocumentosDisponiblesParaConsulta(
                false
        );

        if (observado) {
            response.setMensajeUsuario(
                    "El proceso fue recuperado, pero se encuentra en atención interna."
            );
        } else {
            response.setMensajeUsuario(
                    "El proceso fue recuperado correctamente. Puede continuar desde la etapa guardada."
            );
        }

        return response;
    }

    private FormularioVidaRecuperadoResponse
    construirFormularioVidaRecuperado(

            ProcesoVida proceso,

            List<BeneficiarioVida>
                    beneficiarios,

            AceptacionLegal aceptacion
    ) {

        FormularioVidaRecuperadoResponse
                .TitularRecuperado titular =

                new FormularioVidaRecuperadoResponse
                        .TitularRecuperado(

                        proceso
                                .getCodigoDocumentoTitular(),

                        proceso
                                .getDescripcionOtroDocumentoTitular(),

                        proceso
                                .getNumeroDocumentoTitular(),

                        proceso
                                .getApellidoPaternoTitular(),

                        proceso
                                .getApellidoMaternoTitular(),

                        proceso
                                .getPrimerNombreTitular(),

                        proceso
                                .getSegundoNombreTitular(),

                        proceso.getCorreo(),

                        proceso.getNumeroTelefono(),

                        proceso.getTipoAsegurado(),

                        proceso
                                .getNotificacionesCorreo()
                );

        FormularioVidaRecuperadoResponse
                .DatosComplementariosRecuperados
                datosComplementarios =

                new FormularioVidaRecuperadoResponse
                        .DatosComplementariosRecuperados(

                        proceso.getCodigoPlanilla(),

                        proceso.getDecretoLegislativo(),

                        proceso.getConvenioCgbvp(),

                        proceso.getRucEmpleador(),

                        proceso
                                .getRazonSocialEntidad()
                );

        FormularioVidaRecuperadoResponse
                .ConyugeRecuperado conyuge = null;

        if (existeConyuge(proceso)) {

            conyuge =
                    new FormularioVidaRecuperadoResponse
                            .ConyugeRecuperado(

                            proceso
                                    .getCodigoDocumentoConyuge(),

                            proceso
                                    .getDescripcionOtroDocumentoConyuge(),

                            proceso
                                    .getNumeroDocumentoConyuge(),

                            proceso
                                    .getApellidoPaternoConyuge(),

                            proceso
                                    .getApellidoMaternoConyuge(),

                            proceso
                                    .getPrimerNombreConyuge(),

                            proceso
                                    .getSegundoNombreConyuge(),

                            proceso
                                    .getTipoRelacion()
                    );
        }

        List<FormularioVidaRecuperadoResponse
                .BeneficiarioRecuperado>
                beneficiariosRecuperados =
                new ArrayList<>();

        if (beneficiarios != null) {

            for (
                    BeneficiarioVida beneficiario :
                    beneficiarios
            ) {

                beneficiariosRecuperados.add(

                        new FormularioVidaRecuperadoResponse
                                .BeneficiarioRecuperado(

                                beneficiario
                                        .getOrdenBeneficiario(),

                                beneficiario
                                        .getCodigoDocumentoBeneficiario(),

                                beneficiario
                                        .getDescripcionOtroDocumentoBeneficiario(),

                                beneficiario
                                        .getNumeroDocumentoBeneficiario(),

                                beneficiario
                                        .getApellidoPaterno(),

                                beneficiario
                                        .getApellidoMaterno(),

                                beneficiario
                                        .getPrimerNombre(),

                                beneficiario
                                        .getSegundoNombre(),

                                beneficiario
                                        .getPorcentajeBeneficio()
                        )
                );
            }
        }

        FormularioVidaRecuperadoResponse
                .AceptacionLegalRecuperada
                aceptacionRecuperada = null;

        if (aceptacion != null) {

            aceptacionRecuperada =
                    new FormularioVidaRecuperadoResponse
                            .AceptacionLegalRecuperada(

                            aceptacion
                                    .getIdAceptacion(),

                            aceptacion
                                    .isAceptaDeclaracionJurada(),

                            aceptacion
                                    .getFechaHoraAceptacionDeclaracionJurada(),

                            aceptacion
                                    .isAceptaTratamientoDatosPersonales(),

                            aceptacion
                                    .getFechaHoraAceptacionTratamientoDatosPersonales(),

                            aceptacion
                                    .getVersionTextoDeclaracionJurada(),

                            aceptacion
                                    .getVersionTextoTratamientoDatos(),

                            aceptacion
                                    .getReferenciaPoliticaPrivacidad()
                    );
        }

        return new FormularioVidaRecuperadoResponse(

                titular,

                datosComplementarios,

                conyuge,

                beneficiariosRecuperados,

                aceptacionRecuperada,

                proceso.isBeneficiarioBorradorAbierto()
        );
    }

    private boolean existeConyuge(
            ProcesoVida proceso
    ) {
        return !campoVacio(
                proceso.getCodigoDocumentoConyuge()
        )
                || !campoVacio(
                proceso
                        .getNumeroDocumentoConyuge()
        )
                || !campoVacio(
                proceso
                        .getTipoRelacion()
        );
    }

    private String construirNombreCompletoTitular(
            ProcesoVida proceso
    ) {

        List<String> partes =
                new ArrayList<>();

        agregarParteNombre(
                partes,
                proceso.getApellidoPaternoTitular()
        );

        agregarParteNombre(
                partes,
                proceso.getApellidoMaternoTitular()
        );

        agregarParteNombre(
                partes,
                proceso.getPrimerNombreTitular()
        );

        agregarParteNombre(
                partes,
                proceso.getSegundoNombreTitular()
        );

        return String.join(
                " ",
                partes
        );
    }

    private void agregarParteNombre(
            List<String> partes,
            String valor
    ) {
        if (!campoVacio(valor)) {
            partes.add(
                    valor.trim()
            );
        }
    }

    private void complementarConExpedienteSiExiste(

            RecuperarAvanceProcesoResponse response,

            String registroInternoProceso
    ) {

        try {

            ExpedienteDigitalResponse expediente =
                    expedienteDigitalService
                            .obtenerPorRegistroInternoProceso(
                                    registroInternoProceso
                            );

            response.setExpedienteEncontrado(
                    true
            );

            response.setCanalAcceso(
                    expediente.getCanalAcceso()
            );

            response.setEstadoActual(
                    expediente.getEstadoActual()
            );

            response.setFechaHoraCreacion(
                    expediente
                            .getFechaHoraCreacion()
            );

            response
                    .setFechaHoraUltimaActualizacion(
                            expediente
                                    .getFechaHoraUltimaActualizacion()
                    );

            response.setCantidadEventos(
                    expediente.getCantidadEventos()
            );

            response.setDocumentosGenerados(
                    copiarLista(
                            expediente
                                    .getDocumentosGenerados()
                    )
            );

            response.setDocumentosCargados(
                    copiarLista(
                            expediente
                                    .getDocumentosCargados()
                    )
            );

            response.setDocumentosSellados(
                    copiarLista(
                            expediente
                                    .getDocumentosSellados()
                    )
            );

            response.setDocumentosPublicados(
                    copiarLista(
                            expediente
                                    .getDocumentosPublicados()
                    )
            );

            response.setRechazosDocumentales(
                    copiarLista(
                            expediente
                                    .getRechazosDocumentales()
                    )
            );

            response.setUrlsDocumentosPublicados(
                    construirUrlsDocumentosPublicados(
                            expediente
                                    .getDocumentosPublicados()
                    )
            );

            aplicarDecisionDeReanudacion(
                    response
            );

        } catch (IllegalArgumentException e) {

            /*
             * No existe ExpedienteDigital en memoria.
             *
             * Esto NO invalida el proceso recuperado
             * desde Oracle.
             */
            response.setExpedienteEncontrado(
                    false
            );

            response.setCantidadEventos(
                    0
            );
        }
    }

    private RecuperarAvanceProcesoResponse
    construirResponseSinProcesoOracle(
            String registroInternoProceso
    ) {

        RecuperarAvanceProcesoResponse response =
                new RecuperarAvanceProcesoResponse();

        response.setProcesoEncontrado(false);
        response.setExpedienteEncontrado(false);

        response.setMensajeConsulta(
                "No se encontró un proceso +Vida persistido para el registro interno informado."
        );

        response.setRegistroInternoProceso(
                registroInternoProceso
        );

        response.setEstadoActual(
                EstadoProcesoConstants.SIN_EXPEDIENTE
        );

        response.setAccionPendiente(
                EstadoProcesoConstants
                        .ACCION_INICIAR_PROCESO
        );

        response.setAccionFrontendSugerida(
                EstadoProcesoConstants
                        .FRONTEND_MOSTRAR_INICIO_AFILIACION
        );

        response.setMensajeUsuario(
                "No se encontró un proceso en curso. Puede iniciar la afiliación digital al +Vida Seguro de Accidentes."
        );

        response.setPuedeContinuarEnModulo(true);
        response.setPermiteNuevaCargaTrabajador(false);
        response.setRequiereIntervencionInterna(false);
        response.setDocumentosDisponiblesParaConsulta(false);
        response.setCantidadEventos(0);

        return response;
    }

    private RecuperarAvanceProcesoResponse construirResponseDesdeExpediente(
            ExpedienteDigitalResponse expediente
    ) {
        RecuperarAvanceProcesoResponse response = new RecuperarAvanceProcesoResponse();

        response.setExpedienteEncontrado(true);
        response.setMensajeConsulta("Avance del proceso recuperado correctamente.");

        response.setRegistroInternoProceso(expediente.getRegistroInternoProceso());
        response.setTipoDocumentoTrabajador(expediente.getTipoDocumentoTrabajador());
        response.setNumeroDocumentoTrabajador(expediente.getNumeroDocumentoTrabajador());
        response.setNombresApellidosTrabajador(expediente.getNombresApellidosTrabajador());

        response.setCanalAcceso(expediente.getCanalAcceso());
        response.setEstadoActual(expediente.getEstadoActual());

        response.setFechaHoraCreacion(expediente.getFechaHoraCreacion());
        response.setFechaHoraUltimaActualizacion(expediente.getFechaHoraUltimaActualizacion());
        response.setCantidadEventos(expediente.getCantidadEventos());

        response.setDocumentosGenerados(copiarLista(expediente.getDocumentosGenerados()));
        response.setDocumentosCargados(copiarLista(expediente.getDocumentosCargados()));
        response.setDocumentosSellados(copiarLista(expediente.getDocumentosSellados()));
        response.setDocumentosPublicados(copiarLista(expediente.getDocumentosPublicados()));
        response.setRechazosDocumentales(copiarLista(expediente.getRechazosDocumentales()));

        response.setUrlsDocumentosPublicados(
                construirUrlsDocumentosPublicados(expediente.getDocumentosPublicados())
        );

        aplicarDecisionDeReanudacion(response);

        return response;
    }

    private void aplicarDecisionDeReanudacion(
            RecuperarAvanceProcesoResponse response
    ) {
        String estadoActual = normalizar(response.getEstadoActual());

        if (!response.getDocumentosPublicados().isEmpty()
                && (EstadoProcesoConstants.DOCUMENTO_PUBLICADO.equals(estadoActual)
                || EstadoProcesoConstants.SUBSANADO_OPERATIVAMENTE.equals(estadoActual))) {

            response.setAccionPendiente(EstadoProcesoConstants.ACCION_CONSULTAR_DOCUMENTOS_PUBLICADOS);
            response.setAccionFrontendSugerida(EstadoProcesoConstants.FRONTEND_MOSTRAR_DOCUMENTOS_PUBLICADOS);
            response.setMensajeUsuario("Sus documentos validados y sellados se encuentran disponibles para consulta.");
            response.setPuedeContinuarEnModulo(true);
            response.setPermiteNuevaCargaTrabajador(false);
            response.setRequiereIntervencionInterna(false);
            response.setDocumentosDisponiblesParaConsulta(true);
            return;
        }

        if (EstadoProcesoConstants.OBSERVADO_OPERATIVO.equals(estadoActual)
                || EstadoProcesoConstants.REINTENTO_INTERNO_REGISTRADO.equals(estadoActual)
                || EstadoProcesoConstants.ERROR_PUBLICACION_POR_SELLO_NO_VALIDADO.equals(estadoActual)) {

            response.setAccionPendiente(EstadoProcesoConstants.ACCION_ESPERAR_SUBSANACION_INTERNA);
            response.setAccionFrontendSugerida(EstadoProcesoConstants.FRONTEND_MOSTRAR_MENSAJE_ATENCION_INTERNA);
            response.setMensajeUsuario("Su proceso se encuentra en atención interna. No necesita cargar nuevamente documentos en este momento.");
            response.setPuedeContinuarEnModulo(false);
            response.setPermiteNuevaCargaTrabajador(false);
            response.setRequiereIntervencionInterna(true);
            response.setDocumentosDisponiblesParaConsulta(false);
            return;
        }

        if (estadoActual.contains(EstadoProcesoConstants.PREFIJO_RECHAZADO)) {
            response.setAccionPendiente(EstadoProcesoConstants.ACCION_CARGAR_NUEVAMENTE_DOCUMENTO);
            response.setAccionFrontendSugerida(EstadoProcesoConstants.FRONTEND_MOSTRAR_CARGA_DOCUMENTO_CORREGIDO);
            response.setMensajeUsuario("El documento cargado no superó las validaciones. Cargue nuevamente el documento corregido.");
            response.setPuedeContinuarEnModulo(true);
            response.setPermiteNuevaCargaTrabajador(true);
            response.setRequiereIntervencionInterna(false);
            response.setDocumentosDisponiblesParaConsulta(false);
            return;
        }

        if (EstadoProcesoConstants.DOCUMENTO_GENERADO.equals(estadoActual)
                || EstadoProcesoConstants.DOCUMENTOS_GENERADOS.equals(estadoActual)) {

            response.setAccionPendiente(EstadoProcesoConstants.ACCION_CARGAR_DOCUMENTOS_FIRMADOS);
            response.setAccionFrontendSugerida(EstadoProcesoConstants.FRONTEND_MOSTRAR_DESCARGA_Y_CARGA_DOCUMENTOS);
            response.setMensajeUsuario("Tiene documentos generados pendientes de firma y carga en el módulo.");
            response.setPuedeContinuarEnModulo(true);
            response.setPermiteNuevaCargaTrabajador(true);
            response.setRequiereIntervencionInterna(false);
            response.setDocumentosDisponiblesParaConsulta(false);
            return;
        }

        if (EstadoProcesoConstants.DOCUMENTO_CARGADO.equals(estadoActual)
                || EstadoProcesoConstants.VALIDACION_DOCUMENTAL_EN_PROCESO.equals(estadoActual)) {

            response.setAccionPendiente(EstadoProcesoConstants.ACCION_ESPERAR_VALIDACION_DOCUMENTAL);
            response.setAccionFrontendSugerida(EstadoProcesoConstants.FRONTEND_MOSTRAR_MENSAJE_VALIDACION_EN_PROCESO);
            response.setMensajeUsuario("Sus documentos fueron cargados y se encuentran en proceso de validación.");
            response.setPuedeContinuarEnModulo(false);
            response.setPermiteNuevaCargaTrabajador(false);
            response.setRequiereIntervencionInterna(false);
            response.setDocumentosDisponiblesParaConsulta(false);
            return;
        }

        if (EstadoProcesoConstants.DOCUMENTO_SELLADO.equals(estadoActual)
                || EstadoProcesoConstants.VALIDACION_DOCUMENTAL_APROBADA.equals(estadoActual)
                || EstadoProcesoConstants.SELLO_ESSALUD_VALIDADO.equals(estadoActual)) {

            response.setAccionPendiente(EstadoProcesoConstants.ACCION_ESPERAR_SELLADO_PUBLICACION);
            response.setAccionFrontendSugerida(EstadoProcesoConstants.FRONTEND_MOSTRAR_MENSAJE_PUBLICACION_PENDIENTE);
            response.setMensajeUsuario("Su documento fue validado. Se encuentra pendiente la publicación para consulta.");
            response.setPuedeContinuarEnModulo(false);
            response.setPermiteNuevaCargaTrabajador(false);
            response.setRequiereIntervencionInterna(false);
            response.setDocumentosDisponiblesParaConsulta(false);
            return;
        }

        response.setAccionPendiente(EstadoProcesoConstants.ACCION_COMPLETAR_DATOS_AFILIACION);
        response.setAccionFrontendSugerida(EstadoProcesoConstants.FRONTEND_MOSTRAR_FORMULARIO_AFILIACION);
        response.setMensajeUsuario("Puede continuar con el registro de afiliación digital al +Vida Seguro de Accidentes.");
        response.setPuedeContinuarEnModulo(true);
        response.setPermiteNuevaCargaTrabajador(false);
        response.setRequiereIntervencionInterna(false);
        response.setDocumentosDisponiblesParaConsulta(false);
    }

    private RecuperarAvanceProcesoResponse construirResponseSinExpediente(
            String registroInternoProceso,
            String numeroDocumentoTrabajador,
            String mensaje
    ) {
        RecuperarAvanceProcesoResponse response = new RecuperarAvanceProcesoResponse();

        response.setExpedienteEncontrado(false);
        response.setMensajeConsulta(mensaje);
        response.setRegistroInternoProceso(registroInternoProceso);
        response.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);

        response.setEstadoActual(EstadoProcesoConstants.SIN_EXPEDIENTE);
        response.setAccionPendiente(EstadoProcesoConstants.ACCION_INICIAR_PROCESO);
        response.setAccionFrontendSugerida(EstadoProcesoConstants.FRONTEND_MOSTRAR_INICIO_AFILIACION);
        response.setMensajeUsuario("No se encontró un proceso en curso. Puede iniciar la afiliación digital al +Vida Seguro de Accidentes.");

        response.setPuedeContinuarEnModulo(true);
        response.setPermiteNuevaCargaTrabajador(false);
        response.setRequiereIntervencionInterna(false);
        response.setDocumentosDisponiblesParaConsulta(false);
        response.setCantidadEventos(0);

        return response;
    }

    private List<String> construirUrlsDocumentosPublicados(
            List<String> documentosPublicados
    ) {
        List<String> urls = new ArrayList<>();

        if (documentosPublicados == null) {
            return urls;
        }

        for (String idDocumentoPublicado : documentosPublicados) {
            if (!campoVacio(idDocumentoPublicado)) {
                urls.add("/api/v1/documentos/publicacion/" + idDocumentoPublicado + "/archivo");
            }
        }

        return urls;
    }

    private List<String> copiarLista(List<String> origen) {
        if (origen == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(origen);
    }

    private String normalizar(String valor) {
        return campoVacio(valor) ? "" : valor.trim().toUpperCase();
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}