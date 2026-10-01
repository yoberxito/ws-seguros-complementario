package essalud.gob.pe.seguroshijomenormayor.controller;


import essalud.gob.pe.seguroshijomenormayor.dto.request.GenerarTockenReq;
import essalud.gob.pe.seguroshijomenormayor.dto.response.DatosUsuarioResponse;
import essalud.gob.pe.seguroshijomenormayor.dto.response.GenerarTokenResponse;
import essalud.gob.pe.seguroshijomenormayor.service.IntegracionSomosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/integracion")
@RequiredArgsConstructor
public class IntegracionSomosSeguroMasVida {
    private final IntegracionSomosService tokenService;

    @PostMapping("/generar-token")
    public ResponseEntity<GenerarTokenResponse> generarToken(
            @Valid @RequestBody GenerarTockenReq request
    ) {
        GenerarTokenResponse response =
                tokenService.generarToken(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validar-token/{token}")
    public ResponseEntity<DatosUsuarioResponse> validarToken(
            @PathVariable String token
    ) {
        DatosUsuarioResponse response =
                tokenService.validarToken(token);

        return ResponseEntity.ok(response);
    }
}
