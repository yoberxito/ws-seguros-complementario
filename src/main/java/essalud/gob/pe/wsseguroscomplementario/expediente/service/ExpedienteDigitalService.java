package essalud.gob.pe.wsseguroscomplementario.expediente.service;

import essalud.gob.pe.wsseguroscomplementario.expediente.dto.EventoExpedienteResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.ExpedienteDigitalResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.RegistrarAvanceExpedienteRequest;
import essalud.gob.pe.wsseguroscomplementario.expediente.model.EventoExpedienteDigital;
import essalud.gob.pe.wsseguroscomplementario.expediente.model.ExpedienteDigital;
import essalud.gob.pe.wsseguroscomplementario.expediente.repository.ExpedienteDigitalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpedienteDigitalService {

    private static final ZoneId ZONA_HORARIA_LIMA = ZoneId.of("America/Lima");

    private static final String ESTADO_INICIADO = "PROCESO_INICIADO";

    private final ExpedienteDigitalRepository expedienteDigitalRepository;

    public ExpedienteDigitalService(
            ExpedienteDigitalRepository expedienteDigitalRepository
    ) {
        this.expedienteDigitalRepository = expedienteDigitalRepository;
    }

    public ExpedienteDigitalResponse registrarAvance(
            RegistrarAvanceExpedienteRequest request
    ) {
        validarRequest(request);

        LocalDateTime fechaHoraEvento = LocalDateTime.now(ZONA_HORARIA_LIMA);

        String registroInternoProceso = obtenerOGenerarRegistroInternoProceso(request);

        ExpedienteDigital expediente = expedienteDigitalRepository
                .buscarPorRegistroInternoProceso(registroInternoProceso)
                .orElseGet(() -> crearExpedienteNuevo(request, registroInternoProceso, fechaHoraEvento));

        String estadoAnterior = expediente.getEstadoActual();
        String estadoNuevo = valorPorDefecto(request.getEstadoOperativo(), ESTADO_INICIADO);

        actualizarDatosPrincipales(expediente, request, fechaHoraEvento, estadoNuevo);

        EventoExpedienteDigital evento = construirEvento(
                request,
                fechaHoraEvento,
                estadoAnterior,
                estadoNuevo
        );

        expediente.getEventos().add(evento);

        asociarDocumentosAlExpediente(expediente, request);

        expedienteDigitalRepository.guardar(expediente);

        return convertirAResponse(
                expediente,
                true,
                "Avance del expediente digital registrado correctamente."
        );
    }

    public ExpedienteDigitalResponse obtenerPorRegistroInternoProceso(
            String registroInternoProceso
    ) {
        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        ExpedienteDigital expediente = expedienteDigitalRepository
                .buscarPorRegistroInternoProceso(registroInternoProceso)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró expediente digital para el registro interno informado."));

        return convertirAResponse(
                expediente,
                true,
                "Expediente digital obtenido correctamente."
        );
    }

    public List<ExpedienteDigitalResponse> listarPorTrabajador(
            String numeroDocumentoTrabajador
    ) {
        if (campoVacio(numeroDocumentoTrabajador)) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        return expedienteDigitalRepository
                .buscarPorNumeroDocumentoTrabajador(numeroDocumentoTrabajador)
                .stream()
                .sorted(Comparator.comparing(ExpedienteDigital::getFechaHoraUltimaActualizacion).reversed())
                .map(expediente ->
                        convertirAResponse(
                                expediente,
                                true,
                                "Expediente digital obtenido correctamente."
                        )
                )
                .collect(Collectors.toList());
    }

    private ExpedienteDigital crearExpedienteNuevo(
            RegistrarAvanceExpedienteRequest request,
            String registroInternoProceso,
            LocalDateTime fechaHoraCreacion
    ) {
        ExpedienteDigital expediente = new ExpedienteDigital();

        expediente.setRegistroInternoProceso(registroInternoProceso);
        expediente.setTipoDocumentoTrabajador(valorPorDefecto(request.getTipoDocumentoTrabajador(), "01"));
        expediente.setNumeroDocumentoTrabajador(request.getNumeroDocumentoTrabajador());
        expediente.setNombresApellidosTrabajador(request.getNombresApellidosTrabajador());
        expediente.setCanalAcceso(valorPorDefecto(request.getCanalAcceso(), "SOMOS_ESSALUD"));
        expediente.setEstadoActual(ESTADO_INICIADO);
        expediente.setFechaHoraCreacion(fechaHoraCreacion);
        expediente.setFechaHoraUltimaActualizacion(fechaHoraCreacion);
        expediente.setUsuarioAutenticado(valorPorDefecto(request.getUsuarioAutenticado(), "SISTEMA"));
        expediente.setIpOrigen(request.getIpOrigen());
        expediente.setDatosSesionDispositivo(request.getDatosSesionDispositivo());

        return expediente;
    }

    private void actualizarDatosPrincipales(
            ExpedienteDigital expediente,
            RegistrarAvanceExpedienteRequest request,
            LocalDateTime fechaHoraActualizacion,
            String estadoNuevo
    ) {
        expediente.setTipoDocumentoTrabajador(
                valorPorDefecto(request.getTipoDocumentoTrabajador(), expediente.getTipoDocumentoTrabajador())
        );

        expediente.setNumeroDocumentoTrabajador(
                valorPorDefecto(request.getNumeroDocumentoTrabajador(), expediente.getNumeroDocumentoTrabajador())
        );

        expediente.setNombresApellidosTrabajador(
                valorPorDefecto(request.getNombresApellidosTrabajador(), expediente.getNombresApellidosTrabajador())
        );

        expediente.setCanalAcceso(
                valorPorDefecto(request.getCanalAcceso(), expediente.getCanalAcceso())
        );

        expediente.setEstadoActual(estadoNuevo);
        expediente.setFechaHoraUltimaActualizacion(fechaHoraActualizacion);

        expediente.setUsuarioAutenticado(
                valorPorDefecto(request.getUsuarioAutenticado(), expediente.getUsuarioAutenticado())
        );

        expediente.setIpOrigen(
                valorPorDefecto(request.getIpOrigen(), expediente.getIpOrigen())
        );

        expediente.setDatosSesionDispositivo(
                valorPorDefecto(request.getDatosSesionDispositivo(), expediente.getDatosSesionDispositivo())
        );
    }

    private EventoExpedienteDigital construirEvento(
            RegistrarAvanceExpedienteRequest request,
            LocalDateTime fechaHoraEvento,
            String estadoAnterior,
            String estadoNuevo
    ) {
        EventoExpedienteDigital evento = new EventoExpedienteDigital();

        evento.setIdEvento(generarIdEvento());
        evento.setFechaHoraEvento(fechaHoraEvento);
        evento.setEstadoAnterior(estadoAnterior);
        evento.setEstadoNuevo(estadoNuevo);
        evento.setDescripcionEvento(request.getDescripcionAvance());

        evento.setUsuarioAutenticado(valorPorDefecto(request.getUsuarioAutenticado(), "SISTEMA"));
        evento.setIpOrigen(request.getIpOrigen());
        evento.setDatosSesionDispositivo(request.getDatosSesionDispositivo());

        evento.setTipoDocumentoProceso(request.getTipoDocumentoProceso());

        evento.setIdDocumentoGenerado(request.getIdDocumentoGenerado());
        evento.setIdDocumentoCargado(request.getIdDocumentoCargado());
        evento.setIdDocumentoSellado(request.getIdDocumentoSellado());
        evento.setIdDocumentoPublicado(request.getIdDocumentoPublicado());
        evento.setIdRechazoDocumental(request.getIdRechazoDocumental());

        return evento;
    }

    private void asociarDocumentosAlExpediente(
            ExpedienteDigital expediente,
            RegistrarAvanceExpedienteRequest request
    ) {
        agregarSiNoExiste(expediente.getDocumentosGenerados(), request.getIdDocumentoGenerado());
        agregarSiNoExiste(expediente.getDocumentosCargados(), request.getIdDocumentoCargado());
        agregarSiNoExiste(expediente.getDocumentosSellados(), request.getIdDocumentoSellado());
        agregarSiNoExiste(expediente.getDocumentosPublicados(), request.getIdDocumentoPublicado());
        agregarSiNoExiste(expediente.getRechazosDocumentales(), request.getIdRechazoDocumental());
    }

    private void agregarSiNoExiste(List<String> lista, String valor) {
        if (campoVacio(valor)) {
            return;
        }

        if (!lista.contains(valor)) {
            lista.add(valor);
        }
    }

    private ExpedienteDigitalResponse convertirAResponse(
            ExpedienteDigital expediente,
            boolean registrado,
            String mensaje
    ) {
        ExpedienteDigitalResponse response = new ExpedienteDigitalResponse();

        response.setExpedienteRegistrado(registrado);
        response.setMensajeOperacion(mensaje);

        response.setRegistroInternoProceso(expediente.getRegistroInternoProceso());
        response.setTipoDocumentoTrabajador(expediente.getTipoDocumentoTrabajador());
        response.setNumeroDocumentoTrabajador(expediente.getNumeroDocumentoTrabajador());
        response.setNombresApellidosTrabajador(expediente.getNombresApellidosTrabajador());

        response.setCanalAcceso(expediente.getCanalAcceso());
        response.setEstadoActual(expediente.getEstadoActual());

        response.setFechaHoraCreacion(expediente.getFechaHoraCreacion());
        response.setFechaHoraUltimaActualizacion(expediente.getFechaHoraUltimaActualizacion());

        response.setUsuarioAutenticado(expediente.getUsuarioAutenticado());
        response.setIpOrigen(expediente.getIpOrigen());
        response.setDatosSesionDispositivo(expediente.getDatosSesionDispositivo());

        response.setDocumentosGenerados(new ArrayList<>(expediente.getDocumentosGenerados()));
        response.setDocumentosCargados(new ArrayList<>(expediente.getDocumentosCargados()));
        response.setDocumentosSellados(new ArrayList<>(expediente.getDocumentosSellados()));
        response.setDocumentosPublicados(new ArrayList<>(expediente.getDocumentosPublicados()));
        response.setRechazosDocumentales(new ArrayList<>(expediente.getRechazosDocumentales()));

        response.setCantidadEventos(expediente.getEventos().size());
        response.setEventos(convertirEventosAResponse(expediente.getEventos()));

        return response;
    }

    private List<EventoExpedienteResponse> convertirEventosAResponse(
            List<EventoExpedienteDigital> eventos
    ) {
        return eventos
                .stream()
                .map(this::convertirEventoAResponse)
                .collect(Collectors.toList());
    }

    private EventoExpedienteResponse convertirEventoAResponse(
            EventoExpedienteDigital evento
    ) {
        EventoExpedienteResponse response = new EventoExpedienteResponse();

        response.setIdEvento(evento.getIdEvento());
        response.setFechaHoraEvento(evento.getFechaHoraEvento());

        response.setEstadoAnterior(evento.getEstadoAnterior());
        response.setEstadoNuevo(evento.getEstadoNuevo());
        response.setDescripcionEvento(evento.getDescripcionEvento());

        response.setUsuarioAutenticado(evento.getUsuarioAutenticado());
        response.setIpOrigen(evento.getIpOrigen());
        response.setDatosSesionDispositivo(evento.getDatosSesionDispositivo());

        response.setTipoDocumentoProceso(evento.getTipoDocumentoProceso());

        response.setIdDocumentoGenerado(evento.getIdDocumentoGenerado());
        response.setIdDocumentoCargado(evento.getIdDocumentoCargado());
        response.setIdDocumentoSellado(evento.getIdDocumentoSellado());
        response.setIdDocumentoPublicado(evento.getIdDocumentoPublicado());
        response.setIdRechazoDocumental(evento.getIdRechazoDocumental());

        return response;
    }

    private void validarRequest(RegistrarAvanceExpedienteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de registro de avance no puede estar vacía.");
        }

        if (campoVacio(request.getNumeroDocumentoTrabajador())) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        if (campoVacio(request.getEstadoOperativo())) {
            throw new IllegalArgumentException("El estado operativo del proceso es obligatorio.");
        }
    }

    private String obtenerOGenerarRegistroInternoProceso(
            RegistrarAvanceExpedienteRequest request
    ) {
        if (!campoVacio(request.getRegistroInternoProceso())) {
            return request.getRegistroInternoProceso();
        }

        return generarRegistroInternoProceso();
    }

    private String generarRegistroInternoProceso() {
        int anio = LocalDate.now(ZONA_HORARIA_LIMA).getYear();
        String correlativoTecnico = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return "VIDA-" + anio + "-" + correlativoTecnico;
    }

    private String generarIdEvento() {
        return "EVT-EXP-" + UUID.randomUUID();
    }

    private String valorPorDefecto(String valor, String valorDefecto) {
        return campoVacio(valor) ? valorDefecto : valor;
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}