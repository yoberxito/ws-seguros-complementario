package essalud.gob.pe.wsseguroscomplementario.notificacion.service;

import essalud.gob.pe.wsseguroscomplementario.notificacion.dto.RegistrarNotificacionRequest;
import essalud.gob.pe.wsseguroscomplementario.notificacion.dto.RegistrarNotificacionResponse;
import essalud.gob.pe.wsseguroscomplementario.notificacion.model.NotificacionTrabajador;
import essalud.gob.pe.wsseguroscomplementario.notificacion.repository.NotificacionTrabajadorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificacionTrabajadorService {

    private static final ZoneId ZONA_HORARIA_LIMA = ZoneId.of("America/Lima");

    private static final String ESTADO_NOTIFICACION_SIMULADA = "NOTIFICACION_REGISTRADA_SIMULADA";
    private static final String RESULTADO_ENVIO_SIMULADO = "ENVIO_SIMULADO_PENDIENTE_INTEGRACION_CORREO";

    private final NotificacionTrabajadorRepository notificacionTrabajadorRepository;

    public NotificacionTrabajadorService(
            NotificacionTrabajadorRepository notificacionTrabajadorRepository
    ) {
        this.notificacionTrabajadorRepository = notificacionTrabajadorRepository;
    }

    public RegistrarNotificacionResponse registrarNotificacion(
            RegistrarNotificacionRequest request
    ) {
        validarRequest(request);

        NotificacionTrabajador notificacion = new NotificacionTrabajador();

        notificacion.setIdNotificacion(generarIdNotificacion());
        notificacion.setRegistroInternoProceso(request.getRegistroInternoProceso());
        notificacion.setTipoDocumentoTrabajador(valorPorDefecto(request.getTipoDocumentoTrabajador(), "01"));
        notificacion.setNumeroDocumentoTrabajador(request.getNumeroDocumentoTrabajador());
        notificacion.setDestinatario(request.getDestinatario());
        notificacion.setCanalNotificacion(valorPorDefecto(request.getCanalNotificacion(), "CORREO"));
        notificacion.setTipoNotificacion(normalizar(request.getTipoNotificacion()));
        notificacion.setAsunto(request.getAsunto());
        notificacion.setMensaje(request.getMensaje());
        notificacion.setEnlaceConsulta(request.getEnlaceConsulta());
        notificacion.setFechaHoraRegistro(LocalDateTime.now(ZONA_HORARIA_LIMA));
        notificacion.setEstadoNotificacion(ESTADO_NOTIFICACION_SIMULADA);
        notificacion.setResultadoEnvio(RESULTADO_ENVIO_SIMULADO);

        notificacionTrabajadorRepository.guardar(notificacion);

        return convertirAResponse(
                notificacion,
                true,
                "Notificación registrada correctamente en modo simulado."
        );
    }

    public List<RegistrarNotificacionResponse> listarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {
        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(numeroDocumentoTrabajador)) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        return notificacionTrabajadorRepository
                .buscarPorProcesoYTrabajador(registroInternoProceso, numeroDocumentoTrabajador)
                .stream()
                .map(notificacion ->
                        convertirAResponse(
                                notificacion,
                                true,
                                "Notificación registrada."
                        )
                )
                .collect(Collectors.toList());
    }

    private RegistrarNotificacionResponse convertirAResponse(
            NotificacionTrabajador notificacion,
            boolean registrada,
            String mensajeRegistro
    ) {
        RegistrarNotificacionResponse response = new RegistrarNotificacionResponse();

        response.setNotificacionRegistrada(registrada);
        response.setMensajeRegistro(mensajeRegistro);

        response.setIdNotificacion(notificacion.getIdNotificacion());
        response.setRegistroInternoProceso(notificacion.getRegistroInternoProceso());
        response.setTipoDocumentoTrabajador(notificacion.getTipoDocumentoTrabajador());
        response.setNumeroDocumentoTrabajador(notificacion.getNumeroDocumentoTrabajador());

        response.setDestinatario(notificacion.getDestinatario());
        response.setCanalNotificacion(notificacion.getCanalNotificacion());
        response.setTipoNotificacion(notificacion.getTipoNotificacion());

        response.setAsunto(notificacion.getAsunto());
        response.setMensaje(notificacion.getMensaje());
        response.setEnlaceConsulta(notificacion.getEnlaceConsulta());

        response.setFechaHoraRegistro(notificacion.getFechaHoraRegistro());
        response.setEstadoNotificacion(notificacion.getEstadoNotificacion());
        response.setResultadoEnvio(notificacion.getResultadoEnvio());

        return response;
    }

    private void validarRequest(RegistrarNotificacionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de notificación no puede estar vacía.");
        }

        if (campoVacio(request.getRegistroInternoProceso())) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(request.getNumeroDocumentoTrabajador())) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        if (campoVacio(request.getDestinatario())) {
            throw new IllegalArgumentException("El destinatario de la notificación es obligatorio.");
        }

        if (campoVacio(request.getTipoNotificacion())) {
            throw new IllegalArgumentException("El tipo de notificación es obligatorio.");
        }

        if (campoVacio(request.getAsunto())) {
            throw new IllegalArgumentException("El asunto de la notificación es obligatorio.");
        }

        if (campoVacio(request.getMensaje())) {
            throw new IllegalArgumentException("El mensaje de la notificación es obligatorio.");
        }
    }

    private String generarIdNotificacion() {
        return "NOTIF-" + UUID.randomUUID();
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase();
    }

    private String valorPorDefecto(String valor, String valorDefecto) {
        return campoVacio(valor) ? valorDefecto : valor;
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}