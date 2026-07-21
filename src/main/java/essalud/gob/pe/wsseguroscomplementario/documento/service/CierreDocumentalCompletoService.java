package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.CierreDocumentalCompletoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.DetalleEtapaCierreDocumentalResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.GenerarDocumentoSelladoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.PublicarDocumentoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.PublicarDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.RegistrarAvanceExpedienteRequest;
import essalud.gob.pe.wsseguroscomplementario.expediente.service.ExpedienteDigitalService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class CierreDocumentalCompletoService {

    private static final ZoneId ZONA_HORARIA_LIMA =
            ZoneId.of(EstadoProcesoConstants.ZONA_HORARIA_LIMA);

    private final GeneracionDocumentoSelladoService generacionDocumentoSelladoService;
    private final PublicacionDocumentoService publicacionDocumentoService;
    private final ExpedienteDigitalService expedienteDigitalService;

    public CierreDocumentalCompletoService(
            GeneracionDocumentoSelladoService generacionDocumentoSelladoService,
            PublicacionDocumentoService publicacionDocumentoService,
            ExpedienteDigitalService expedienteDigitalService
    ) {
        this.generacionDocumentoSelladoService = generacionDocumentoSelladoService;
        this.publicacionDocumentoService = publicacionDocumentoService;
        this.expedienteDigitalService = expedienteDigitalService;
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
                registroInternoProceso,
                tipoDocumento,
                numeroDocumentoTrabajador,
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

    private void ejecutarSellado(
            CierreDocumentalCompletoResponse response,
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        try {
            GenerarDocumentoSelladoResponse sellado =
                    generacionDocumentoSelladoService.generarDocumentoSellado(
                            archivo,
                            registroInternoProceso,
                            tipoDocumento,
                            numeroDocumentoTrabajador
                    );

            response.setIdDocumentoSellado(sellado.getIdDocumentoSellado());

            boolean selladoCorrecto = !campoVacio(sellado.getIdDocumentoSellado());

            registrarEtapa(
                    response,
                    "23",
                    "Generar documento validado y sellado",
                    selladoCorrecto,
                    selladoCorrecto ? EstadoProcesoConstants.DOCUMENTO_SELLADO : "ERROR_SELLADO_DOCUMENTO",
                    selladoCorrecto
                            ? "Documento sellado generado correctamente."
                            : "No se pudo generar el documento sellado."
            );

        } catch (Exception e) {
            registrarEtapa(
                    response,
                    "23",
                    "Generar documento validado y sellado",
                    false,
                    "ERROR_SELLADO_DOCUMENTO",
                    "No se pudo generar el documento sellado: " + e.getMessage()
            );
        }
    }

    private void ejecutarPublicacion(
            CierreDocumentalCompletoResponse response,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador,
            String canalPublicacion,
            String publicadoPor
    ) {
        try {
            PublicarDocumentoRequest request = new PublicarDocumentoRequest();

            request.setIdDocumentoSellado(response.getIdDocumentoSellado());
            request.setRegistroInternoProceso(registroInternoProceso);
            request.setTipoDocumento(tipoDocumento);
            request.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);
            request.setCanalPublicacion(valorPorDefecto(canalPublicacion, EstadoProcesoConstants.CANAL_SOMOS_ESSALUD));
            request.setPublicadoPor(valorPorDefecto(publicadoPor, EstadoProcesoConstants.USUARIO_SISTEMA));

            PublicarDocumentoResponse publicacion =
                    publicacionDocumentoService.publicarDocumento(request);

            response.setIdDocumentoPublicado(publicacion.getIdDocumentoPublicado());
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
            response.setEstadoCierreDocumental(EstadoProcesoConstants.DOCUMENTO_PUBLICADO);
            response.setMensajeCierre("El documento fue sellado y publicado correctamente.");
        } else {
            response.setEstadoCierreDocumental(EstadoProcesoConstants.ERROR_CIERRE_DOCUMENTAL);
            response.setMensajeCierre("No se pudo completar el cierre documental. Se requiere intervención interna.");
            response.getObservaciones().add("Debe registrarse o atenderse la incidencia operativa correspondiente.");
        }

        registrarAvanceExpediente(
                response,
                usuarioResponsable,
                ipOrigen,
                datosSesionDispositivo
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
                valorPorDefecto(tipoDocumentoTrabajador, EstadoProcesoConstants.TIPO_DOCUMENTO_TRABAJADOR_DNI)
        );
        response.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);
        response.setNombresApellidosTrabajador(nombresApellidosTrabajador);
        response.setCanalPublicacion(valorPorDefecto(canalPublicacion, EstadoProcesoConstants.CANAL_SOMOS_ESSALUD));
        response.setFechaHoraCierre(LocalDateTime.now(ZONA_HORARIA_LIMA));

        return response;
    }

    private void validarParametros(
            MultipartFile archivo,
            String registroInternoProceso,
            String tipoDocumento,
            String numeroDocumentoTrabajador
    ) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF firmado es obligatorio.");
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