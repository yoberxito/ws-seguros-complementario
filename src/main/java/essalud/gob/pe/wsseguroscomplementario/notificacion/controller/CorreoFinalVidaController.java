package essalud.gob.pe.wsseguroscomplementario.notificacion.controller;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.notificacion.service.ReintentoCorreoFinalVidaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/v1/notificaciones/correo-final"
)
public class CorreoFinalVidaController {

    private final ReintentoCorreoFinalVidaService
            reintentoCorreoFinalVidaService;

    public CorreoFinalVidaController(
            ReintentoCorreoFinalVidaService
                    reintentoCorreoFinalVidaService
    ) {
        this.reintentoCorreoFinalVidaService =
                reintentoCorreoFinalVidaService;
    }

    @PostMapping(
            "/{registroInternoProceso}/reintentar/{cicloDocumental}"
    )
    public ResponseEntity<ApiResponse<String>>
    reintentar(
            @PathVariable
            String registroInternoProceso,

            @PathVariable
            String cicloDocumental,

            HttpServletRequest httpRequest
    ) {

        try {

            boolean depuracionEjecutada =
                    reintentoCorreoFinalVidaService
                            .reintentar(
                                    registroInternoProceso,
                                    cicloDocumental,

                                    EstadoProcesoConstants
                                            .USUARIO_SISTEMA,

                                    httpRequest
                                            .getRemoteAddr(),

                                    null
                            );

            String resultado =
                    depuracionEjecutada
                            ? "CORREO_REENVIADO_Y_DEPURACION_COMPLETADA"
                            : "CORREO_REENVIADO";

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            "El correo final fue procesado correctamente.",
                            resultado
                    )
            );

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            ApiResponse.error(
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }
}