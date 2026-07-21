package essalud.gob.pe.wsseguroscomplementario.notificacion.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.notificacion.dto.RegistrarNotificacionRequest;
import essalud.gob.pe.wsseguroscomplementario.notificacion.dto.RegistrarNotificacionResponse;
import essalud.gob.pe.wsseguroscomplementario.notificacion.service.NotificacionTrabajadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionTrabajadorController {

    private final NotificacionTrabajadorService notificacionTrabajadorService;

    public NotificacionTrabajadorController(
            NotificacionTrabajadorService notificacionTrabajadorService
    ) {
        this.notificacionTrabajadorService = notificacionTrabajadorService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<RegistrarNotificacionResponse>> registrarNotificacion(
            @RequestBody RegistrarNotificacionRequest request
    ) {
        try {
            RegistrarNotificacionResponse response =
                    notificacionTrabajadorService.registrarNotificacion(request);

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeRegistro(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @GetMapping("/proceso/{registroInternoProceso}/trabajador/{numeroDocumentoTrabajador}")
    public ResponseEntity<ApiResponse<List<RegistrarNotificacionResponse>>> listarNotificaciones(
            @PathVariable String registroInternoProceso,
            @PathVariable String numeroDocumentoTrabajador
    ) {
        try {
            List<RegistrarNotificacionResponse> response =
                    notificacionTrabajadorService.listarPorProcesoYTrabajador(
                            registroInternoProceso,
                            numeroDocumentoTrabajador
                    );

            return ResponseEntity.ok(
                    ApiResponse.exito("Notificaciones obtenidas correctamente.", response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }
}