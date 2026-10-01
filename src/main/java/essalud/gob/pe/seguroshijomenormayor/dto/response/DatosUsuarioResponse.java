package essalud.gob.pe.seguroshijomenormayor.dto.response;

public record DatosUsuarioResponse(String tipoDocumento,
                                   String numeroDocumento,
                                   String codigoPlanilla,
                                   String regimenLaboral,
                                   String codigoRed,
                                   String correo) {
}
