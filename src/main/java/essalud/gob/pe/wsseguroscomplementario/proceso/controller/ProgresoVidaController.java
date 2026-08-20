package essalud.gob.pe.wsseguroscomplementario.proceso.controller;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarConyugeRequest;
import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.IniciarProcesoVidaRequest;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.IniciarProcesoVidaResponse;
import essalud.gob.pe.wsseguroscomplementario.proceso.service.ProgresoVidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarProgresoVidaResponse;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarTitularRequest;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarDatosComplementariosRequest;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarBeneficiariosRequest;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.ActualizarNavegacionRequest;
@RestController
@RequestMapping("/api/v1/procesos/progreso")
public class ProgresoVidaController {

    private final ProgresoVidaService progresoVidaService;

    public ProgresoVidaController(
            ProgresoVidaService progresoVidaService
    ) {
        this.progresoVidaService = progresoVidaService;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<ApiResponse<IniciarProcesoVidaResponse>> iniciarProceso(
            @RequestBody IniciarProcesoVidaRequest request
    ) {
        try {
            IniciarProcesoVidaResponse response =
                    progresoVidaService.iniciarProceso(request);

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensajeOperacion(),
                            response
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                            e.getMessage(),
                            null
                    )
            );
        }
    }
    @PutMapping("/{registroInternoProceso}/titular")
    public ResponseEntity<ApiResponse<GuardarProgresoVidaResponse>> guardarTitular(
            @PathVariable String registroInternoProceso,
            @RequestBody GuardarTitularRequest request
    ) {
        try {
            GuardarProgresoVidaResponse response =
                    progresoVidaService.guardarTitular(
                            registroInternoProceso,
                            request
                    );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensajeOperacion(),
                            response
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                            e.getMessage(),
                            null
                    )
            );
        }
    }
    @PutMapping("/{registroInternoProceso}/datos-complementarios")
    public ResponseEntity<ApiResponse<GuardarProgresoVidaResponse>>
    guardarDatosComplementarios(
            @PathVariable String registroInternoProceso,
            @RequestBody GuardarDatosComplementariosRequest request
    ) {
        try {
            GuardarProgresoVidaResponse response =
                    progresoVidaService
                            .guardarDatosComplementarios(
                                    registroInternoProceso,
                                    request
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensajeOperacion(),
                            response
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                            e.getMessage(),
                            null
                    )
            );
        }
    }
    @PutMapping("/{registroInternoProceso}/conyuge")
    public ResponseEntity<ApiResponse<GuardarProgresoVidaResponse>>
    guardarConyuge(
            @PathVariable String registroInternoProceso,
            @RequestBody GuardarConyugeRequest request
    ) {
        try {
            GuardarProgresoVidaResponse response =
                    progresoVidaService
                            .guardarConyuge(
                                    registroInternoProceso,
                                    request
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensajeOperacion(),
                            response
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                            e.getMessage(),
                            null
                    )
            );
        }
    }
    @PutMapping("/{registroInternoProceso}/beneficiarios")
    public ResponseEntity<ApiResponse<GuardarProgresoVidaResponse>>
    guardarBeneficiarios(
            @PathVariable String registroInternoProceso,
            @RequestBody GuardarBeneficiariosRequest request
    ) {
        try {
            GuardarProgresoVidaResponse response =
                    progresoVidaService
                            .guardarBeneficiarios(
                                    registroInternoProceso,
                                    request
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensajeOperacion(),
                            response
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                            e.getMessage(),
                            null
                    )
            );
        }
    }

    @PutMapping(
            "/{registroInternoProceso}/navegacion"
    )
    public ResponseEntity<
            ApiResponse<GuardarProgresoVidaResponse>
            >
    actualizarNavegacion(
            @PathVariable
            String registroInternoProceso,

            @RequestBody
            ActualizarNavegacionRequest request
    ) {
        try {

            GuardarProgresoVidaResponse response =
                    progresoVidaService
                            .actualizarNavegacion(
                                    registroInternoProceso,
                                    request
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response
                                    .getMensajeOperacion(),
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
    @PutMapping(
            "/{registroInternoProceso}/borrador/titular"
    )
    public ResponseEntity<
            ApiResponse<GuardarProgresoVidaResponse>
            >
    guardarBorradorTitular(
            @PathVariable
            String registroInternoProceso,

            @RequestBody
            GuardarTitularRequest request
    ) {
        try {

            GuardarProgresoVidaResponse response =
                    progresoVidaService
                            .guardarBorradorTitular(
                                    registroInternoProceso,
                                    request
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensajeOperacion(),
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

    @PutMapping(
            "/{registroInternoProceso}/borrador/datos-complementarios"
    )
    public ResponseEntity<
            ApiResponse<GuardarProgresoVidaResponse>
            >
    guardarBorradorDatosComplementarios(
            @PathVariable
            String registroInternoProceso,

            @RequestBody
            GuardarDatosComplementariosRequest request
    ) {
        try {

            GuardarProgresoVidaResponse response =
                    progresoVidaService
                            .guardarBorradorDatosComplementarios(
                                    registroInternoProceso,
                                    request
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensajeOperacion(),
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

    @PutMapping(
            "/{registroInternoProceso}/borrador/beneficiarios"
    )
    public ResponseEntity<
            ApiResponse<GuardarProgresoVidaResponse>
            >
    guardarBorradorBeneficiarios(
            @PathVariable
            String registroInternoProceso,

            @RequestBody
            GuardarBeneficiariosRequest request
    ) {
        try {

            GuardarProgresoVidaResponse response =
                    progresoVidaService
                            .guardarBorradorBeneficiarios(
                                    registroInternoProceso,
                                    request
                            );

            return ResponseEntity.ok(
                    ApiResponse.exito(
                            response.getMensajeOperacion(),
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