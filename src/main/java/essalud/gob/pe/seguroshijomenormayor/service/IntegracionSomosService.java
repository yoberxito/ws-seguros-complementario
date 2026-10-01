package essalud.gob.pe.seguroshijomenormayor.service;


import essalud.gob.pe.seguroshijomenormayor.dto.response.DatosUsuarioResponse;
import essalud.gob.pe.seguroshijomenormayor.dto.response.GenerarTokenResponse;
import essalud.gob.pe.seguroshijomenormayor.dto.request.GenerarTockenReq;

public interface IntegracionSomosService {

    GenerarTokenResponse generarToken(
            GenerarTockenReq request
    );

    DatosUsuarioResponse validarToken(String token);
}
