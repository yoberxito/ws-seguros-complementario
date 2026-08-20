package essalud.gob.pe.wsseguroscomplementario.documento.controller;

import essalud.gob.pe.wsseguroscomplementario.common.dto.ApiResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.CargarDocumentoFirmadoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.CargarDocumentoFirmadoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.service.CargaDocumentoFirmadoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documentos/cargados")
public class CargaDocumentoFirmadoController {

    private final CargaDocumentoFirmadoService cargaDocumentoFirmadoService;

    public CargaDocumentoFirmadoController(CargaDocumentoFirmadoService cargaDocumentoFirmadoService) {
        this.cargaDocumentoFirmadoService = cargaDocumentoFirmadoService;
    }

    @PostMapping(
            value = "/firmados",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CargarDocumentoFirmadoResponse>> cargarDocumentoFirmado(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("registroInternoProceso") String registroInternoProceso,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("tipoDocumentoTrabajador") String tipoDocumentoTrabajador,
            @RequestParam("numeroDocumentoTrabajador") String numeroDocumentoTrabajador,
            @RequestParam("nombresApellidosTrabajador") String nombresApellidosTrabajador,
            @RequestParam(value = "datosSesionDispositivo", required = false) String datosSesionDispositivo,
            HttpServletRequest httpServletRequest
    ) {
        try {
            CargarDocumentoFirmadoRequest request = new CargarDocumentoFirmadoRequest();
            request.setRegistroInternoProceso(registroInternoProceso);
            request.setTipoDocumento(tipoDocumento);
            request.setTipoDocumentoTrabajador(tipoDocumentoTrabajador);
            request.setNumeroDocumentoTrabajador(numeroDocumentoTrabajador);
            request.setNombresApellidosTrabajador(nombresApellidosTrabajador);
            request.setDatosSesionDispositivo(datosSesionDispositivo);

            String ipOrigen = obtenerIpOrigen(httpServletRequest);

            CargarDocumentoFirmadoResponse response =
                    cargaDocumentoFirmadoService.cargarDocumentoFirmado(request, archivo, ipOrigen);

            /*
             * Un rechazo técnico del PDF es un resultado
             * funcional válido de la operación.
             *
             * El archivo no fue aceptado, pero el backend
             * pudo procesar correctamente la solicitud,
             * registrar el rechazo y devolver el motivo.
             *
             * Por eso se responde HTTP 200 conservando
             * codResultado = "0" y el body del rechazo.
             */
            if (!response.isCargado()) {
                return ResponseEntity.ok(
                        ApiResponse.error(
                                response.getMensajeCarga(),
                                response
                        )
                );
            }

            return ResponseEntity.ok(
                    ApiResponse.exito(response.getMensajeCarga(), response)
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