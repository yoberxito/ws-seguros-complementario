package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FormularioVidaRecuperadoResponse(

        TitularRecuperado titular,

        DatosComplementariosRecuperados
        datosComplementarios,

        ConyugeRecuperado conyuge,

        List<BeneficiarioRecuperado>
        beneficiarios,

        AceptacionLegalRecuperada
        aceptacionLegal,

        boolean beneficiarioBorradorAbierto
) {

    public record TitularRecuperado(

            String tipoDocumento,
            String descripcionOtroDocumento,
            String numeroDocumento,

            String apellidoPaterno,
            String apellidoMaterno,
            String primerNombre,
            String segundoNombre,

            String correo,
            String celular,
            String tipoAsegurado,

            String notificacionesCorreo
    ) {
    }

    public record DatosComplementariosRecuperados(

            String codigoPlanilla,
            String decretoLegislativo,
            String convenioCgbvp,

            String rucEmpleador,
            String razonSocial
    ) {
    }

    public record ConyugeRecuperado(

            String tipoDocumento,
            String descripcionOtroDocumento,
            String numeroDocumento,

            String apellidoPaterno,
            String apellidoMaterno,
            String primerNombre,
            String segundoNombre,

            String tipoRelacion
    ) {
    }

    public record BeneficiarioRecuperado(

            Integer orden,

            String tipoDocumento,
            String descripcionOtroDocumento,
            String numeroDocumento,

            String apellidoPaterno,
            String apellidoMaterno,
            String primerNombre,
            String segundoNombre,

            BigDecimal porcentaje
    ) {
    }

    public record AceptacionLegalRecuperada(

            String idAceptacion,

            boolean aceptaDeclaracionJurada,

            LocalDateTime
            fechaHoraAceptacionDeclaracionJurada,

            boolean aceptaTratamientoDatosPersonales,

            LocalDateTime
            fechaHoraAceptacionTratamientoDatosPersonales,

            String versionTextoDeclaracionJurada,
            String versionTextoTratamientoDatos,
            String referenciaPoliticaPrivacidad
    ) {
    }
}