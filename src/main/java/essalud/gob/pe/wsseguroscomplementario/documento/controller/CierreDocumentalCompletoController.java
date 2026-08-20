package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.CierreDocumentalCompletoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.CierreDocumentalCompletoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos/cierre")
public class CierreDocumentalCompletoController {

    private final CierreDocumentalCompletoService cierreDocumentalCompletoService;

    public CierreDocumentalCompletoController(
            CierreDocumentalCompletoService cierreDocumentalCompletoService
    ) {
        this.cierreDocumentalCompletoService = cierreDocumentalCompletoService;
    }

    @PostMapping("/procesar")
    public ResponseEntity<ApiResponse<CierreDocumentalCompletoResponse>> procesarCierreDocumental(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("registroInternoProceso") String registroInternoProceso,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam(value = "tipoDocumentoTrabajador", required = false) String tipoDocumentoTrabajador,
            @RequestParam("numeroDocumentoTrabajador") String numeroDocumentoTrabajador,
            @RequestParam(value = "nombresApellidosTrabajador", required = false) String nombresApellidosTrabajador,
            @RequestParam(value = "canalPublicacion", required = false) String canalPublicacion,
            @RequestParam(value = "publicadoPor", required = false) String publicadoPor,
            @RequestParam(value = "usuarioResponsable", required = false) String usuarioResponsable,
            @RequestParam(value = "ipOrigen", required = false) String ipOrigen,
            @RequestParam(value = "datosSesionDispositivo", required = false) String datosSesionDispositivo
    ) {
        try {
            CierreDocumentalCompletoResponse response =
                    cierreDocumentalCompletoService.procesarCierreDocumental(
                            archivo,
                            registroInternoProceso,
                            tipoDocumento,
                            tipoDocumentoTrabajador,
                            numeroDocumentoTrabajador,
                            nombresApellidosTrabajador,
                            canalPublicacion,
                            publicadoPor,
                            usuarioResponsable,
                            ipOrigen,
                            datosSesionDispositivo
                    );

            /*
             * Un cierre no completado por incidencia
             * operativa es un resultado funcional del
             * orquestador.
             *
             * Se conserva HTTP 200 para que el frontend
             * pueda interpretar el body y distinguirlo
             * de una caída real del backend.
             */
            if (!response.isCierreCompletado()) {
                return ResponseEntity.ok(
                        ApiResponse.error(
                                response.getMensajeCierre(),
                                response
                        )
                );
            }

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeCierre(), response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }
}