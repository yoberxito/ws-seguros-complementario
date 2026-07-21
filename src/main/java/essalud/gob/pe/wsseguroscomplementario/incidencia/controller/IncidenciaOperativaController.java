package essalud.gob.pe.wsseguroscomplementario.incidencia.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.CerrarIncidenciaOperativaRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.IncidenciaOperativaResponse;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.RegistrarIncidenciaOperativaRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.dto.RegistrarReintentoIncidenciaRequest;
import essalud.gob.pe.wsseguroscomplementario.incidencia.service.IncidenciaOperativaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidencias-operativas")
public class IncidenciaOperativaController {

    private final IncidenciaOperativaService incidenciaOperativaService;

    public IncidenciaOperativaController(
            IncidenciaOperativaService incidenciaOperativaService
    ) {
        this.incidenciaOperativaService = incidenciaOperativaService;
    }

    @PostMapping("/registrar-observado")
    public ResponseEntity<ApiResponse<IncidenciaOperativaResponse>> registrarObservadoOperativo(
            @RequestBody RegistrarIncidenciaOperativaRequest request
    ) {
        try {
            IncidenciaOperativaResponse response =
                    incidenciaOperativaService.registrarObservadoOperativo(request);

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeOperacion(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @PostMapping("/{idIncidenciaOperativa}/registrar-reintento")
    public ResponseEntity<ApiResponse<IncidenciaOperativaResponse>> registrarReintentoInterno(
            @PathVariable String idIncidenciaOperativa,
            @RequestBody RegistrarReintentoIncidenciaRequest request
    ) {
        try {
            IncidenciaOperativaResponse response =
                    incidenciaOperativaService.registrarReintentoInterno(
                            idIncidenciaOperativa,
                            request
                    );

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeOperacion(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @PostMapping("/{idIncidenciaOperativa}/cerrar")
    public ResponseEntity<ApiResponse<IncidenciaOperativaResponse>> cerrarIncidencia(
            @PathVariable String idIncidenciaOperativa,
            @RequestBody CerrarIncidenciaOperativaRequest request
    ) {
        try {
            IncidenciaOperativaResponse response =
                    incidenciaOperativaService.cerrarIncidencia(
                            idIncidenciaOperativa,
                            request
                    );

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeOperacion(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @GetMapping("/proceso/{registroInternoProceso}/trabajador/{numeroDocumentoTrabajador}")
    public ResponseEntity<ApiResponse<List<IncidenciaOperativaResponse>>> listarPorProcesoYTrabajador(
            @PathVariable String registroInternoProceso,
            @PathVariable String numeroDocumentoTrabajador
    ) {
        try {
            List<IncidenciaOperativaResponse> response =
                    incidenciaOperativaService.listarPorProcesoYTrabajador(
                            registroInternoProceso,
                            numeroDocumentoTrabajador
                    );

            return ResponseEntity.ok(
                    ApiResponse.exito("Incidencias operativas obtenidas correctamente.", response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }
}