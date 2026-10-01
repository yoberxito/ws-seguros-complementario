package essalud.gob.pe.seguroshijomenormayor.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GenerarTockenReq(

        @NotBlank(message = "El tipo de documento es obligatorio")
        String tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        String numeroDocumento,

        @NotBlank(message = "El código de planilla es obligatorio")
        String codigoPlanilla,

        @NotBlank(message = "El régimen laboral es obligatorio")
        String regimenLaboral,

        @NotBlank(message = "El código de red es obligatorio")
        String codigoRed,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo
) {
}
