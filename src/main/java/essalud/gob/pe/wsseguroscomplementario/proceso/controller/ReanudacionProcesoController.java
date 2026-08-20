package essalud.gob.pe.wsseguroscomplementario.proceso.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.RecuperarAvanceProcesoResponse;
import essalud.gob.pe.wsseguroscomplementario.proceso.service.ReanudacionProcesoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/procesos/avance")
public class ReanudacionProcesoController {

    private final ReanudacionProcesoService reanudacionProcesoService;

    public ReanudacionProcesoController(
            ReanudacionProcesoService reanudacionProcesoService
    ) {
        this.reanudacionProcesoService = reanudacionProcesoService;
    }

    @GetMapping("/registro/{registroInternoProceso}")
    public ResponseEntity<ApiResponse<RecuperarAvanceProcesoResponse>> recuperarPorRegistroInternoProceso(
            @PathVariable String registroInternoProceso
    ) {
        try {
            RecuperarAvanceProcesoResponse response =
                    reanudacionProcesoService.recuperarPorRegistroInternoProceso(registroInternoProceso);

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeConsulta(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @GetMapping(
            "/trabajador/{tipoDocumentoTrabajador}/{numeroDocumentoTrabajador}/ultimo"
    )
    public ResponseEntity<
            ApiResponse<RecuperarAvanceProcesoResponse>
            >
    recuperarUltimoPorTrabajador(

            @PathVariable
            String tipoDocumentoTrabajador,

            @PathVariable
            String numeroDocumentoTrabajador
    ) {

        try {

            RecuperarAvanceProcesoResponse response =
                    reanudacionProcesoService
                            .recuperarUltimoPorTrabajador(
                                    tipoDocumentoTrabajador,
                                    numeroDocumentoTrabajador
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensajeConsulta(),
                            response
                    )
            );

        } catch (IllegalArgumentException e) {

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