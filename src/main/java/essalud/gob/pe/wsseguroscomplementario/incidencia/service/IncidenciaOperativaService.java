package essalud.gob.pe.wsseguroscomplementario.incidencia.service;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.RegistrarAvanceExpedienteRequest;
import essalud.gob.pe.wsseguroscomplementario.expediente.service.ExpedienteDigitalService;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.CerrarIncidenciaOperativaRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.IncidenciaOperativaResponse;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.RegistrarIncidenciaOperativaRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.RegistrarReintentoIncidenciaRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.ReintentoIncidenciaResponse;
import essalud.gob.pe.wsseguroscomplementario.incidencia.model.IncidenciaOperativa;
import essalud.gob.pe.wsseguroscomplementario.incidencia.model.ReintentoIncidenciaOperativa;
import essalud.gob.pe.wsseguroscomplementario.incidencia.repository.IncidenciaOperativaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IncidenciaOperativaService {

    private static final ZoneId ZONA_HORARIA_LIMA =
            ZoneId.of(EstadoProcesoConstants.ZONA_HORARIA_LIMA);

    private final IncidenciaOperativaRepository incidenciaOperativaRepository;
    private final ExpedienteDigitalService expedienteDigitalService;

    public IncidenciaOperativaService(
            IncidenciaOperativaRepository incidenciaOperativaRepository,
            ExpedienteDigitalService expedienteDigitalService
    ) {
        this.incidenciaOperativaRepository = incidenciaOperativaRepository;
        this.expedienteDigitalService = expedienteDigitalService;
    }

    public IncidenciaOperativaResponse registrarObservadoOperativo(
            RegistrarIncidenciaOperativaRequest request
    ) {
        validarRegistroIncidencia(request);
        validarNoExisteIncidenciaAbiertaDuplicada(request);

        LocalDateTime fechaHoraRegistro = LocalDateTime.now(ZONA_HORARIA_LIMA);

        IncidenciaOperativa incidencia = new IncidenciaOperativa();

        incidencia.setIdIncidenciaOperativa(generarIdIncidencia());
        incidencia.setRegistroInternoProceso(request.getRegistroInternoProceso());

        incidencia.setTipoDocumentoTrabajador(
                valorPorDefecto(
                        request.getTipoDocumentoTrabajador(),
                        EstadoProcesoConstants.TIPO_DOCUMENTO_TRABAJADOR_DNI
                )
        );
        incidencia.setNumeroDocumentoTrabajador(request.getNumeroDocumentoTrabajador());

        incidencia.setTipoDocumentoProceso(request.getTipoDocumentoProceso());
        incidencia.setIdDocumentoGenerado(request.getIdDocumentoGenerado());
        incidencia.setIdDocumentoCargado(request.getIdDocumentoCargado());
        incidencia.setIdDocumentoSellado(request.getIdDocumentoSellado());
        incidencia.setIdDocumentoPublicado(request.getIdDocumentoPublicado());

        incidencia.setSistemaInvolucrado(normalizar(request.getSistemaInvolucrado()));
        incidencia.setEtapaProceso(normalizar(request.getEtapaProceso()));
        incidencia.setTipoIncidenciaOperativa(normalizar(request.getTipoIncidenciaOperativa()));

        incidencia.setMotivoObservado(request.getMotivoObservado());
        incidencia.setDetalleIncidencia(request.getDetalleIncidencia());

        incidencia.setEstadoIncidencia(EstadoProcesoConstants.OBSERVADO_OPERATIVO);
        incidencia.setObservadoOperativo(true);
        incidencia.setPermiteNuevaCargaTrabajador(false);
        incidencia.setRequiereIntervencionInterna(true);

        incidencia.setNumeroReintentosInternos(0);
        incidencia.setFechaHoraRegistro(fechaHoraRegistro);

        incidencia.setUsuarioResponsable(
                valorPorDefecto(request.getUsuarioResponsable(), EstadoProcesoConstants.USUARIO_SISTEMA)
        );
        incidencia.setIpOrigen(request.getIpOrigen());
        incidencia.setDatosSesionDispositivo(request.getDatosSesionDispositivo());

        incidenciaOperativaRepository.guardar(incidencia);

        registrarAvanceEnExpediente(
                incidencia,
                EstadoProcesoConstants.OBSERVADO_OPERATIVO,
                "Se registró incidencia operativa posterior a la validación documental: "
                        + incidencia.getMotivoObservado()
        );

        return convertirAResponse(
                incidencia,
                "Incidencia operativa registrada correctamente como OBSERVADO."
        );
    }

    public IncidenciaOperativaResponse registrarReintentoInterno(
            String idIncidenciaOperativa,
            RegistrarReintentoIncidenciaRequest request
    ) {
        validarReintento(idIncidenciaOperativa, request);

        IncidenciaOperativa incidencia = obtenerIncidencia(idIncidenciaOperativa);

        if (sonIguales(incidencia.getEstadoIncidencia(), EstadoProcesoConstants.SUBSANADO_OPERATIVAMENTE)) {
            throw new IllegalArgumentException("La incidencia operativa ya se encuentra cerrada o subsanada.");
        }

        LocalDateTime fechaHoraReintento = LocalDateTime.now(ZONA_HORARIA_LIMA);

        ReintentoIncidenciaOperativa reintento = new ReintentoIncidenciaOperativa();

        reintento.setIdReintento(generarIdReintento());
        reintento.setFechaHoraReintento(fechaHoraReintento);
        reintento.setDescripcionReintento(request.getDescripcionReintento());
        reintento.setResultadoReintento(request.getResultadoReintento());
        reintento.setUsuarioResponsable(
                valorPorDefecto(request.getUsuarioResponsable(), EstadoProcesoConstants.USUARIO_SISTEMA)
        );
        reintento.setIpOrigen(request.getIpOrigen());
        reintento.setDatosSesionDispositivo(request.getDatosSesionDispositivo());
        reintento.setIdDocumentoSellado(request.getIdDocumentoSellado());
        reintento.setIdDocumentoPublicado(request.getIdDocumentoPublicado());

        incidencia.getReintentos().add(reintento);
        incidencia.setNumeroReintentosInternos(incidencia.getNumeroReintentosInternos() + 1);
        incidencia.setFechaHoraUltimoReintento(fechaHoraReintento);
        incidencia.setEstadoIncidencia(EstadoProcesoConstants.REINTENTO_INTERNO_REGISTRADO);
        incidencia.setRequiereIntervencionInterna(true);
        incidencia.setPermiteNuevaCargaTrabajador(false);

        if (!campoVacio(request.getIdDocumentoSellado())) {
            incidencia.setIdDocumentoSellado(request.getIdDocumentoSellado());
        }

        if (!campoVacio(request.getIdDocumentoPublicado())) {
            incidencia.setIdDocumentoPublicado(request.getIdDocumentoPublicado());
        }

        incidenciaOperativaRepository.guardar(incidencia);

        registrarAvanceEnExpediente(
                incidencia,
                EstadoProcesoConstants.REINTENTO_INTERNO_REGISTRADO,
                "Se registró reintento interno para subsanar incidencia operativa."
        );

        return convertirAResponse(
                incidencia,
                "Reintento interno registrado correctamente."
        );
    }

    public IncidenciaOperativaResponse cerrarIncidencia(
            String idIncidenciaOperativa,
            CerrarIncidenciaOperativaRequest request
    ) {
        validarCierre(idIncidenciaOperativa, request);

        IncidenciaOperativa incidencia = obtenerIncidencia(idIncidenciaOperativa);

        LocalDateTime fechaHoraCierre = LocalDateTime.now(ZONA_HORARIA_LIMA);

        incidencia.setEstadoIncidencia(EstadoProcesoConstants.SUBSANADO_OPERATIVAMENTE);
        incidencia.setObservadoOperativo(false);
        incidencia.setRequiereIntervencionInterna(false);
        incidencia.setPermiteNuevaCargaTrabajador(false);
        incidencia.setFechaHoraCierre(fechaHoraCierre);
        incidencia.setResultadoCierre(request.getResultadoCierre());

        incidencia.setUsuarioResponsable(
                valorPorDefecto(request.getUsuarioResponsable(), incidencia.getUsuarioResponsable())
        );
        incidencia.setIpOrigen(valorPorDefecto(request.getIpOrigen(), incidencia.getIpOrigen()));
        incidencia.setDatosSesionDispositivo(
                valorPorDefecto(request.getDatosSesionDispositivo(), incidencia.getDatosSesionDispositivo())
        );

        if (!campoVacio(request.getIdDocumentoSellado())) {
            incidencia.setIdDocumentoSellado(request.getIdDocumentoSellado());
        }

        if (!campoVacio(request.getIdDocumentoPublicado())) {
            incidencia.setIdDocumentoPublicado(request.getIdDocumentoPublicado());
        }

        incidenciaOperativaRepository.guardar(incidencia);

        registrarAvanceEnExpediente(
                incidencia,
                EstadoProcesoConstants.SUBSANADO_OPERATIVAMENTE,
                "Se cerró la incidencia operativa: " + incidencia.getResultadoCierre()
        );

        return convertirAResponse(
                incidencia,
                "Incidencia operativa cerrada correctamente."
        );
    }

    public List<IncidenciaOperativaResponse> listarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {
        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(numeroDocumentoTrabajador)) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        return incidenciaOperativaRepository
                .buscarPorProcesoYTrabajador(registroInternoProceso, numeroDocumentoTrabajador)
                .stream()
                .sorted(Comparator.comparing(IncidenciaOperativa::getFechaHoraRegistro).reversed())
                .map(incidencia ->
                        convertirAResponse(
                                incidencia,
                                "Incidencia operativa obtenida correctamente."
                        )
                )
                .collect(Collectors.toList());
    }

    private void validarNoExisteIncidenciaAbiertaDuplicada(
            RegistrarIncidenciaOperativaRequest request
    ) {
        List<IncidenciaOperativa> incidenciasDelProceso =
                incidenciaOperativaRepository.buscarPorProcesoYTrabajador(
                        request.getRegistroInternoProceso(),
                        request.getNumeroDocumentoTrabajador()
                );

        for (IncidenciaOperativa incidencia : incidenciasDelProceso) {
            if (esIncidenciaAbierta(incidencia)
                    && sonIguales(incidencia.getTipoDocumentoProceso(), request.getTipoDocumentoProceso())
                    && sonIguales(incidencia.getSistemaInvolucrado(), request.getSistemaInvolucrado())
                    && sonIguales(incidencia.getEtapaProceso(), request.getEtapaProceso())
                    && sonIguales(incidencia.getTipoIncidenciaOperativa(), request.getTipoIncidenciaOperativa())) {

                throw new IllegalStateException(
                        "Ya existe una incidencia operativa abierta para el mismo proceso, documento, sistema, etapa y tipo de incidencia. "
                                + "Debe atenderse la incidencia existente antes de registrar una nueva. "
                                + "ID incidencia existente: " + incidencia.getIdIncidenciaOperativa()
                );
            }
        }
    }

    private boolean esIncidenciaAbierta(IncidenciaOperativa incidencia) {
        return sonIguales(incidencia.getEstadoIncidencia(), EstadoProcesoConstants.OBSERVADO_OPERATIVO)
                || sonIguales(incidencia.getEstadoIncidencia(), EstadoProcesoConstants.REINTENTO_INTERNO_REGISTRADO);
    }

    private void registrarAvanceEnExpediente(
            IncidenciaOperativa incidencia,
            String estadoOperativo,
            String descripcionAvance
    ) {
        RegistrarAvanceExpedienteRequest request = new RegistrarAvanceExpedienteRequest();

        request.setRegistroInternoProceso(incidencia.getRegistroInternoProceso());
        request.setTipoDocumentoTrabajador(incidencia.getTipoDocumentoTrabajador());
        request.setNumeroDocumentoTrabajador(incidencia.getNumeroDocumentoTrabajador());
        request.setCanalAcceso(EstadoProcesoConstants.CANAL_SOMOS_ESSALUD);
        request.setEstadoOperativo(estadoOperativo);
        request.setDescripcionAvance(descripcionAvance);
        request.setUsuarioAutenticado(incidencia.getUsuarioResponsable());
        request.setIpOrigen(incidencia.getIpOrigen());
        request.setDatosSesionDispositivo(incidencia.getDatosSesionDispositivo());
        request.setTipoDocumentoProceso(incidencia.getTipoDocumentoProceso());

        request.setIdDocumentoGenerado(incidencia.getIdDocumentoGenerado());
        request.setIdDocumentoCargado(incidencia.getIdDocumentoCargado());
        request.setIdDocumentoSellado(incidencia.getIdDocumentoSellado());
        request.setIdDocumentoPublicado(incidencia.getIdDocumentoPublicado());

        expedienteDigitalService.registrarAvance(request);
    }

    private IncidenciaOperativa obtenerIncidencia(String idIncidenciaOperativa) {
        return incidenciaOperativaRepository.buscarPorId(idIncidenciaOperativa)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la incidencia operativa informada."));
    }

    private IncidenciaOperativaResponse convertirAResponse(
            IncidenciaOperativa incidencia,
            String mensajeOperacion
    ) {
        IncidenciaOperativaResponse response = new IncidenciaOperativaResponse();

        response.setIncidenciaRegistrada(true);
        response.setMensajeOperacion(mensajeOperacion);

        response.setIdIncidenciaOperativa(incidencia.getIdIncidenciaOperativa());
        response.setRegistroInternoProceso(incidencia.getRegistroInternoProceso());

        response.setTipoDocumentoTrabajador(incidencia.getTipoDocumentoTrabajador());
        response.setNumeroDocumentoTrabajador(incidencia.getNumeroDocumentoTrabajador());

        response.setTipoDocumentoProceso(incidencia.getTipoDocumentoProceso());
        response.setIdDocumentoGenerado(incidencia.getIdDocumentoGenerado());
        response.setIdDocumentoCargado(incidencia.getIdDocumentoCargado());
        response.setIdDocumentoSellado(incidencia.getIdDocumentoSellado());
        response.setIdDocumentoPublicado(incidencia.getIdDocumentoPublicado());

        response.setSistemaInvolucrado(incidencia.getSistemaInvolucrado());
        response.setEtapaProceso(incidencia.getEtapaProceso());
        response.setTipoIncidenciaOperativa(incidencia.getTipoIncidenciaOperativa());

        response.setMotivoObservado(incidencia.getMotivoObservado());
        response.setDetalleIncidencia(incidencia.getDetalleIncidencia());

        response.setEstadoIncidencia(incidencia.getEstadoIncidencia());
        response.setObservadoOperativo(incidencia.isObservadoOperativo());
        response.setPermiteNuevaCargaTrabajador(incidencia.isPermiteNuevaCargaTrabajador());
        response.setRequiereIntervencionInterna(incidencia.isRequiereIntervencionInterna());

        response.setNumeroReintentosInternos(incidencia.getNumeroReintentosInternos());

        response.setFechaHoraRegistro(incidencia.getFechaHoraRegistro());
        response.setFechaHoraUltimoReintento(incidencia.getFechaHoraUltimoReintento());
        response.setFechaHoraCierre(incidencia.getFechaHoraCierre());

        response.setUsuarioResponsable(incidencia.getUsuarioResponsable());
        response.setIpOrigen(incidencia.getIpOrigen());
        response.setDatosSesionDispositivo(incidencia.getDatosSesionDispositivo());

        response.setResultadoCierre(incidencia.getResultadoCierre());
        response.setReintentos(convertirReintentosAResponse(incidencia.getReintentos()));

        return response;
    }

    private List<ReintentoIncidenciaResponse> convertirReintentosAResponse(
            List<ReintentoIncidenciaOperativa> reintentos
    ) {
        return reintentos
                .stream()
                .map(this::convertirReintentoAResponse)
                .collect(Collectors.toList());
    }

    private ReintentoIncidenciaResponse convertirReintentoAResponse(
            ReintentoIncidenciaOperativa reintento
    ) {
        ReintentoIncidenciaResponse response = new ReintentoIncidenciaResponse();

        response.setIdReintento(reintento.getIdReintento());
        response.setFechaHoraReintento(reintento.getFechaHoraReintento());
        response.setDescripcionReintento(reintento.getDescripcionReintento());
        response.setResultadoReintento(reintento.getResultadoReintento());
        response.setUsuarioResponsable(reintento.getUsuarioResponsable());
        response.setIpOrigen(reintento.getIpOrigen());
        response.setDatosSesionDispositivo(reintento.getDatosSesionDispositivo());
        response.setIdDocumentoSellado(reintento.getIdDocumentoSellado());
        response.setIdDocumentoPublicado(reintento.getIdDocumentoPublicado());

        return response;
    }

    private void validarRegistroIncidencia(RegistrarIncidenciaOperativaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de incidencia operativa no puede estar vacía.");
        }

        if (campoVacio(request.getRegistroInternoProceso())) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(request.getNumeroDocumentoTrabajador())) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        if (campoVacio(request.getSistemaInvolucrado())) {
            throw new IllegalArgumentException("El sistema involucrado es obligatorio.");
        }

        if (campoVacio(request.getEtapaProceso())) {
            throw new IllegalArgumentException("La etapa del proceso es obligatoria.");
        }

        if (campoVacio(request.getTipoIncidenciaOperativa())) {
            throw new IllegalArgumentException("El tipo de incidencia operativa es obligatorio.");
        }

        if (campoVacio(request.getMotivoObservado())) {
            throw new IllegalArgumentException("El motivo observado es obligatorio.");
        }
    }

    private void validarReintento(
            String idIncidenciaOperativa,
            RegistrarReintentoIncidenciaRequest request
    ) {
        if (campoVacio(idIncidenciaOperativa)) {
            throw new IllegalArgumentException("El ID de incidencia operativa es obligatorio.");
        }

        if (request == null) {
            throw new IllegalArgumentException("La solicitud de reintento no puede estar vacía.");
        }

        if (campoVacio(request.getDescripcionReintento())) {
            throw new IllegalArgumentException("La descripción del reintento interno es obligatoria.");
        }

        if (campoVacio(request.getResultadoReintento())) {
            throw new IllegalArgumentException("El resultado del reintento interno es obligatorio.");
        }
    }

    private void validarCierre(
            String idIncidenciaOperativa,
            CerrarIncidenciaOperativaRequest request
    ) {
        if (campoVacio(idIncidenciaOperativa)) {
            throw new IllegalArgumentException("El ID de incidencia operativa es obligatorio.");
        }

        if (request == null) {
            throw new IllegalArgumentException("La solicitud de cierre no puede estar vacía.");
        }

        if (campoVacio(request.getResultadoCierre())) {
            throw new IllegalArgumentException("El resultado de cierre es obligatorio.");
        }
    }

    private String generarIdIncidencia() {
        return "INC-OP-" + UUID.randomUUID();
    }

    private String generarIdReintento() {
        return "REINT-OP-" + UUID.randomUUID();
    }

    private String valorPorDefecto(String valor, String valorDefecto) {
        return campoVacio(valor) ? valorDefecto : valor;
    }

    private boolean sonIguales(String valorA, String valorB) {
        return normalizar(valorA).equals(normalizar(valorB));
    }

    private String normalizar(String valor) {
        return campoVacio(valor) ? "" : valor.trim().toUpperCase();
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}