package essalud.gob.pe.wsseguroscomplementario.proceso.service;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.ExpedienteDigitalResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.service.ExpedienteDigitalService;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.RecuperarAvanceProcesoResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReanudacionProcesoService {

    private final ExpedienteDigitalService expedienteDigitalService;

    public ReanudacionProcesoService(
            ExpedienteDigitalService expedienteDigitalService
    ) {
        this.expedienteDigitalService = expedienteDigitalService;
    }

    public RecuperarAvanceProcesoResponse recuperarPorRegistroInternoProceso(
            String registroInternoProceso
    ) {
        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        try {
            ExpedienteDigitalResponse expediente =
                    expedienteDigitalService.obtenerPorRegistroInternoProceso(registroInternoProceso);

            return construirResponseDesdeExpediente(expediente);

        } catch (IllegalArgumentException e) {
            return construirResponseSinExpediente(
                    registroInternoProceso,
                    null,
                    "No se encontró expediente digital asociado al registro interno informado."
            );
        }
    }

    public RecuperarAvanceProcesoResponse recuperarUltimoPorTrabajador(
            String numeroDocumentoTrabajador
    ) {
        if (campoVacio(numeroDocumentoTrabajador)) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        List<ExpedienteDigitalResponse> expedientes =
                expedienteDigitalService.listarPorTrabajador(numeroDocumentoTrabajador);

        if (expedientes == null || expedientes.isEmpty()) {
            return construirResponseSinExpediente(
                    null,
                    numeroDocumentoTrabajador,
                    "No se encontró un proceso de afiliación digital en curso para el trabajador."
            );
        }

        return construirResponseDesdeExpediente(expedientes.get(0));
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