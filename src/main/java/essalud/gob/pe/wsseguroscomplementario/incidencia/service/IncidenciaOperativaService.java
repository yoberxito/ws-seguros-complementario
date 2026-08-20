package essalud.gob.pe.wsseguroscomplementario.incidencia.service;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.CerrarIncidenciaOperativaRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.IncidenciaOperativaResponse;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
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

    public IncidenciaOperativaService(
            IncidenciaOperativaRepository incidenciaOperativaRepository
    ) {
        this.incidenciaOperativaRepository =
                incidenciaOperativaRepository;
    }
    @Transactional
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
        incidenciaOperativaRepository
                .registrarEventoIncidencia(
                        incidencia.getIdIncidenciaOperativa(),
                        "HIST-"
                                + incidencia
                                .getIdIncidenciaOperativa(),
                        EVENTO_INCIDENCIA_REGISTRADA,
                        null,
                        EstadoProcesoConstants
                                .OBSERVADO_OPERATIVO,
                        "OBSERVADO",
                        incidencia.getMotivoObservado(),
                        incidencia.getUsuarioResponsable(),
                        incidencia.getIpOrigen(),
                        incidencia.getDatosSesionDispositivo(),
                        incidencia.getIdDocumentoSellado(),
                        incidencia.getIdDocumentoPublicado(),
                        fechaHoraRegistro
                );
        return convertirAResponse(
                obtenerIncidencia(
                        incidencia
                                .getIdIncidenciaOperativa()
                ),
                "Incidencia operativa registrada correctamente como OBSERVADO."
        );
    }
    @Transactional
    public IncidenciaOperativaResponse registrarReintentoInterno(
            String idIncidenciaOperativa,
            RegistrarReintentoIncidenciaRequest request
    ) {
        validarReintento(
                idIncidenciaOperativa,
                request
        );

        IncidenciaOperativa incidencia =
                obtenerIncidencia(
                        idIncidenciaOperativa
                );

        /*
         * Una incidencia ya subsanada no debe admitir
         * nuevos reintentos.
         */
        if (
                sonIguales(
                        incidencia.getEstadoIncidencia(),
                        EstadoProcesoConstants
                                .SUBSANADO_OPERATIVAMENTE
                )
        ) {
            throw new IllegalArgumentException(
                    "La incidencia operativa ya se encuentra cerrada o subsanada."
            );
        }

        /*
         * Conservamos el estado existente antes
         * de actualizarlo para registrarlo en
         * HISTORIAL_PROC_REGMASVIDA.
         */
        String estadoAnterior =
                incidencia.getEstadoIncidencia();

        LocalDateTime fechaHoraReintento =
                LocalDateTime.now(
                        ZONA_HORARIA_LIMA
                );

        ReintentoIncidenciaOperativa reintento =
                new ReintentoIncidenciaOperativa();

        reintento.setIdReintento(
                generarIdReintento()
        );

        reintento.setFechaHoraReintento(
                fechaHoraReintento
        );

        reintento.setDescripcionReintento(
                request.getDescripcionReintento()
        );

        reintento.setResultadoReintento(
                request.getResultadoReintento()
        );

        reintento.setUsuarioResponsable(
                valorPorDefecto(
                        request.getUsuarioResponsable(),
                        EstadoProcesoConstants
                                .USUARIO_SISTEMA
                )
        );

        reintento.setIpOrigen(
                request.getIpOrigen()
        );

        reintento.setDatosSesionDispositivo(
                request.getDatosSesionDispositivo()
        );

        reintento.setIdDocumentoSellado(
                request.getIdDocumentoSellado()
        );

        reintento.setIdDocumentoPublicado(
                request.getIdDocumentoPublicado()
        );

        incidencia
                .getReintentos()
                .add(
                        reintento
                );

        incidencia.setNumeroReintentosInternos(
                incidencia.getNumeroReintentosInternos()
                        + 1
        );

        incidencia.setFechaHoraUltimoReintento(
                fechaHoraReintento
        );

        incidencia.setEstadoIncidencia(
                EstadoProcesoConstants
                        .REINTENTO_INTERNO_REGISTRADO
        );

        incidencia.setObservadoOperativo(
                true
        );

        incidencia.setRequiereIntervencionInterna(
                true
        );

        incidencia.setPermiteNuevaCargaTrabajador(
                false
        );

        if (
                !campoVacio(
                        request.getIdDocumentoSellado()
                )
        ) {
            incidencia.setIdDocumentoSellado(
                    request.getIdDocumentoSellado()
            );
        }

        if (
                !campoVacio(
                        request.getIdDocumentoPublicado()
                )
        ) {
            incidencia.setIdDocumentoPublicado(
                    request.getIdDocumentoPublicado()
            );
        }

        incidenciaOperativaRepository
                .guardar(
                        incidencia
                );

        incidenciaOperativaRepository
                .registrarEventoIncidencia(
                        incidencia.getIdIncidenciaOperativa(),

                        "HIST-"
                                + reintento.getIdReintento(),

                        EVENTO_REINTENTO,

                        estadoAnterior,

                        EstadoProcesoConstants
                                .REINTENTO_INTERNO_REGISTRADO,

                        request.getResultadoReintento(),

                        request.getDescripcionReintento(),

                        reintento.getUsuarioResponsable(),

                        reintento.getIpOrigen(),

                        reintento.getDatosSesionDispositivo(),

                        reintento.getIdDocumentoSellado(),

                        reintento.getIdDocumentoPublicado(),

                        fechaHoraReintento
                );

        /*
         * Volvemos a leer desde Oracle para que
         * la respuesta use la información durable,
         * incluida la lista histórica de reintentos.
         */
        return convertirAResponse(
                obtenerIncidencia(
                        incidencia
                                .getIdIncidenciaOperativa()
                ),
                "Reintento interno registrado correctamente."
        );
    }

    @Transactional
    public IncidenciaOperativaResponse cerrarIncidencia(
            String idIncidenciaOperativa,
            CerrarIncidenciaOperativaRequest request
    ) {
        validarCierre(idIncidenciaOperativa, request);

        IncidenciaOperativa incidencia =
                obtenerIncidencia(idIncidenciaOperativa);

        /*
         * Si ya fue subsanada, no volvemos a cerrarla
         * ni generamos otro evento de cierre.
         */
        if (
                sonIguales(
                        incidencia.getEstadoIncidencia(),
                        EstadoProcesoConstants
                                .SUBSANADO_OPERATIVAMENTE
                )
        ) {
            return convertirAResponse(
                    incidencia,
                    "La incidencia operativa ya se encontraba subsanada."
            );
        }

        String estadoAnterior =
                incidencia.getEstadoIncidencia();

        LocalDateTime fechaHoraCierre =
                LocalDateTime.now(ZONA_HORARIA_LIMA);

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


        incidenciaOperativaRepository
                .registrarEventoIncidencia(
                        incidencia.getIdIncidenciaOperativa(),
                        "HIST-CIERRE-"
                                + UUID.randomUUID(),
                        EVENTO_INCIDENCIA_SUBSANADA,
                        estadoAnterior,
                        EstadoProcesoConstants
                                .SUBSANADO_OPERATIVAMENTE,
                        "SUBSANADO",
                        incidencia.getResultadoCierre(),
                        incidencia.getUsuarioResponsable(),
                        incidencia.getIpOrigen(),
                        incidencia.getDatosSesionDispositivo(),
                        incidencia.getIdDocumentoSellado(),
                        incidencia.getIdDocumentoPublicado(),
                        fechaHoraCierre
                );

        return convertirAResponse(
                obtenerIncidencia(
                        incidencia
                                .getIdIncidenciaOperativa()
                ),
                "Incidencia operativa cerrada correctamente."
        );
    }

    @Transactional
    public IncidenciaOperativaResponse
    registrarOReintentarObservadoOperativo(
            RegistrarIncidenciaOperativaRequest request
    ) {

        validarRegistroIncidencia(
                request
        );

        Optional<IncidenciaOperativa> existente =
                buscarIncidenciaAbiertaDuplicada(
                        request
                );

        if (existente.isEmpty()) {
            return registrarObservadoOperativo(
                    request
            );
        }

        RegistrarReintentoIncidenciaRequest reintento =
                new RegistrarReintentoIncidenciaRequest();

        String detalle =
                campoVacio(
                        request.getDetalleIncidencia()
                )
                        ? ""
                        : " Detalle: "
                          + request.getDetalleIncidencia();

        reintento.setDescripcionReintento(
                "Reintento automático de la operación. "
                        + request.getMotivoObservado()
                        + detalle
        );

        reintento.setResultadoReintento(
                "FALLIDO"
        );

        reintento.setUsuarioResponsable(
                request.getUsuarioResponsable()
        );

        reintento.setIpOrigen(
                request.getIpOrigen()
        );

        reintento.setDatosSesionDispositivo(
                request.getDatosSesionDispositivo()
        );

        reintento.setIdDocumentoSellado(
                request.getIdDocumentoSellado()
        );

        reintento.setIdDocumentoPublicado(
                request.getIdDocumentoPublicado()
        );

        return registrarReintentoInterno(
                existente
                        .get()
                        .getIdIncidenciaOperativa(),
                reintento
        );
    }

    @Transactional
    public void cerrarIncidenciasAbiertasPorSistema(
            String registroInternoProceso,
            String numeroDocumentoTrabajador,
            String tipoDocumentoProceso,
            String sistemaInvolucrado,
            String resultadoCierre,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo,
            String idDocumentoSellado,
            String idDocumentoPublicado
    ) {

        List<IncidenciaOperativa> incidencias =
                incidenciaOperativaRepository
                        .buscarPorProcesoYTrabajador(
                                registroInternoProceso,
                                numeroDocumentoTrabajador
                        );

        for (
                IncidenciaOperativa incidencia
                : incidencias
        ) {

            if (!esIncidenciaAbierta(incidencia)) {
                continue;
            }

            if (
                    !sonIguales(
                            incidencia
                                    .getTipoDocumentoProceso(),
                            tipoDocumentoProceso
                    )
            ) {
                continue;
            }

            if (
                    !sonIguales(
                            incidencia
                                    .getSistemaInvolucrado(),
                            sistemaInvolucrado
                    )
            ) {
                continue;
            }

            CerrarIncidenciaOperativaRequest request =
                    new CerrarIncidenciaOperativaRequest();

            request.setResultadoCierre(
                    resultadoCierre
            );

            request.setUsuarioResponsable(
                    usuarioResponsable
            );

            request.setIpOrigen(
                    ipOrigen
            );

            request.setDatosSesionDispositivo(
                    datosSesionDispositivo
            );

            request.setIdDocumentoSellado(
                    idDocumentoSellado
            );

            request.setIdDocumentoPublicado(
                    idDocumentoPublicado
            );

            cerrarIncidencia(
                    incidencia
                            .getIdIncidenciaOperativa(),
                    request
            );
        }
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

        Optional<IncidenciaOperativa> existente =
                buscarIncidenciaAbiertaDuplicada(
                        request
                );

        if (existente.isPresent()) {
            throw new IllegalStateException(
                    "Ya existe una incidencia operativa abierta "
                            + "para el mismo proceso, documento, "
                            + "sistema, etapa y tipo de incidencia. "
                            + "ID incidencia existente: "
                            + existente
                            .get()
                            .getIdIncidenciaOperativa()
            );
        }
    }

    private boolean esIncidenciaAbierta(IncidenciaOperativa incidencia) {
        return sonIguales(incidencia.getEstadoIncidencia(), EstadoProcesoConstants.OBSERVADO_OPERATIVO)
                || sonIguales(incidencia.getEstadoIncidencia(), EstadoProcesoConstants.REINTENTO_INTERNO_REGISTRADO);
    }

    private Optional<IncidenciaOperativa>
    buscarIncidenciaAbiertaDuplicada(
            RegistrarIncidenciaOperativaRequest request
    ) {

        return incidenciaOperativaRepository
                .buscarPorProcesoYTrabajador(
                        request.getRegistroInternoProceso(),
                        request.getNumeroDocumentoTrabajador()
                )
                .stream()
                .filter(this::esIncidenciaAbierta)
                .filter(
                        incidencia ->
                                sonIguales(
                                        incidencia
                                                .getTipoDocumentoProceso(),
                                        request
                                                .getTipoDocumentoProceso()
                                )
                )
                .filter(
                        incidencia ->
                                sonIguales(
                                        incidencia
                                                .getSistemaInvolucrado(),
                                        request
                                                .getSistemaInvolucrado()
                                )
                )
                .filter(
                        incidencia ->
                                sonIguales(
                                        incidencia
                                                .getEtapaProceso(),
                                        request
                                                .getEtapaProceso()
                                )
                )
                .filter(
                        incidencia ->
                                sonIguales(
                                        incidencia
                                                .getTipoIncidenciaOperativa(),
                                        request
                                                .getTipoIncidenciaOperativa()
                                )
                )
                .findFirst();
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

    private static final String
            EVENTO_INCIDENCIA_REGISTRADA =
            "INCIDENCIA_OPERATIVA_REGISTRADA";

    private static final String
            EVENTO_REINTENTO =
            "REINTENTO_INCIDENCIA_OPERATIVA";

    private static final String
            EVENTO_INCIDENCIA_SUBSANADA =
            "INCIDENCIA_OPERATIVA_SUBSANADA";

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