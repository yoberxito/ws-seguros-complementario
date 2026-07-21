package essalud.gob.pe.wsseguroscomplementario.aceptacion.controller;

import essalud.gob.pe.wsseguroscomplementario.aceptacion.dto.RegistrarAceptacionRequest;
import essalud.gob.pe.wsseguroscomplementario.aceptacion.dto.RegistrarAceptacionResponse;
import essalud.gob.pe.wsseguroscomplementario.aceptacion.service.AceptacionService;
import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/aceptaciones")
public class AceptacionController {

    private final AceptacionService aceptacionService;

    public AceptacionController(AceptacionService aceptacionService) {
        this.aceptacionService = aceptacionService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<RegistrarAceptacionResponse>> registrarAceptaciones(
            @RequestBody RegistrarAceptacionRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            String ipOrigen = obtenerIpOrigen(httpServletRequest);

            RegistrarAceptacionResponse response = aceptacionService.registrarAceptaciones(
                    request,
                    ipOrigen
            );

            return ResponseEntity.ok(
                    ApiResponse.exito("Aceptaciones registradas correctamente.", response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    private String obtenerIpOrigen(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}