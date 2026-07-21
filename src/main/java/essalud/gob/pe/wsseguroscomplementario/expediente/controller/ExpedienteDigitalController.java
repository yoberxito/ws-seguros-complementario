package essalud.gob.pe.wsseguroscomplementario.expediente.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.ExpedienteDigitalResponse;
import essalud.gob.pe.wsseguroscomplementario.expediente.dto.RegistrarAvanceExpedienteRequest;
import essalud.gob.pe.wsseguroscomplementario.expediente.service.ExpedienteDigitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expedientes")
public class ExpedienteDigitalController {

    private final ExpedienteDigitalService expedienteDigitalService;

    public ExpedienteDigitalController(
            ExpedienteDigitalService expedienteDigitalService
    ) {
        this.expedienteDigitalService = expedienteDigitalService;
    }

    @PostMapping("/registrar-avance")
    public ResponseEntity<ApiResponse<ExpedienteDigitalResponse>> registrarAvance(
            @RequestBody RegistrarAvanceExpedienteRequest request
    ) {
        try {
            ExpedienteDigitalResponse response =
                    expedienteDigitalService.registrarAvance(request);

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeOperacion(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @GetMapping("/proceso/{registroInternoProceso}")
    public ResponseEntity<ApiResponse<ExpedienteDigitalResponse>> obtenerPorRegistroInternoProceso(
            @PathVariable String registroInternoProceso
    ) {
        try {
            ExpedienteDigitalResponse response =
                    expedienteDigitalService.obtenerPorRegistroInternoProceso(registroInternoProceso);

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeOperacion(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @GetMapping("/trabajador/{numeroDocumentoTrabajador}")
    public ResponseEntity<ApiResponse<List<ExpedienteDigitalResponse>>> listarPorTrabajador(
            @PathVariable String numeroDocumentoTrabajador
    ) {
        try {
            List<ExpedienteDigitalResponse> response =
                    expedienteDigitalService.listarPorTrabajador(numeroDocumentoTrabajador);

            return ResponseEntity.ok(
                    ApiResponse.exito("Expedientes digitales obtenidos correctamente.", response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }
}