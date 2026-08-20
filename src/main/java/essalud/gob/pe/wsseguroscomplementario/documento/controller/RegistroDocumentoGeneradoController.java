package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RegistrarDocumentoGeneradoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RegistrarDocumentoGeneradoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.RegistroDocumentoGeneradoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos/generados")
public class RegistroDocumentoGeneradoController {

    private final RegistroDocumentoGeneradoService registroDocumentoGeneradoService;

    public RegistroDocumentoGeneradoController(RegistroDocumentoGeneradoService registroDocumentoGeneradoService) {
        this.registroDocumentoGeneradoService = registroDocumentoGeneradoService;
    }

    @PostMapping(
            value = "/registrar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<RegistrarDocumentoGeneradoResponse>> registrarDocumentoGenerado(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("registroInternoProceso") String registroInternoProceso,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("versionFormato") String versionFormato,
            @RequestParam("tipoDocumentoTrabajador") String tipoDocumentoTrabajador,
            @RequestParam("numeroDocumentoTrabajador") String numeroDocumentoTrabajador,
            @RequestParam("nombresApellidosTrabajador") String nombresApellidosTrabajador,
            @RequestParam(value = "cantidadBeneficiariosRegistrados", defaultValue = "0") Integer cantidadBeneficiariosRegistrados,
            @RequestParam(value = "generadoPor", required = false) String generadoPor,
            @RequestParam(value = "canalGeneracion", required = false) String canalGeneracion
    ) {
        try {
            RegistrarDocumentoGeneradoRequest request = new RegistrarDocumentoGeneradoRequest();

            request.setRegistroInternoProceso(registroInternoProceso);
            request.setTipoDocumento(tipoDocumento);
            request.setVersionFormato(versionFormato);
            request.setTipoDocumentoTrabajador(tipoDocumentoTrabajador);
            request.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);
            request.setNombresApellidosTrabajador(nombresApellidosTrabajador);
            request.setCantidadBeneficiariosRegistrados(cantidadBeneficiariosRegistrados);
            request.setGeneradoPor(generadoPor);
            request.setCanalGeneracion(canalGeneracion);

            RegistrarDocumentoGeneradoResponse response =
                    registroDocumentoGeneradoService.registrarDocumentoGenerado(request, archivo);

            return ResponseEntity.ok(
                    ApiResponse.exito("Documento generado registrado correctamente.", response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RegistrarDocumentoGeneradoResponse>>> listarDocumentosGenerados() {
        List<RegistrarDocumentoGeneradoResponse> response =
                registroDocumentoGeneradoService.listarDocumentosGenerados();

        return ResponseEntity.ok(
                ApiResponse.exito("Consulta de documentos generados realizada correctamente.", response)
        );
    }

    @GetMapping("/{idDocumentoGenerado}")
    public ResponseEntity<ApiResponse<RegistrarDocumentoGeneradoResponse>> buscarDocumentoGeneradoPorId(
            @PathVariable String idDocumentoGenerado
    ) {
        try {
            RegistrarDocumentoGeneradoResponse response =
                    registroDocumentoGeneradoService.buscarPorId(idDocumentoGenerado);

            return ResponseEntity.ok(
                    ApiResponse.exito("Documento generado encontrado.", response)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    @GetMapping("/proceso/{registroInternoProceso}")
    public ResponseEntity<ApiResponse<List<RegistrarDocumentoGeneradoResponse>>> buscarPorRegistroInternoProceso(
            @PathVariable String registroInternoProceso
    ) {
        List<RegistrarDocumentoGeneradoResponse> response =
                registroDocumentoGeneradoService.buscarPorRegistroInternoProceso(registroInternoProceso);

        return ResponseEntity.ok(
                ApiResponse.exito("Consulta de documentos generados por proceso realizada correctamente.", response)
        );
    }

}