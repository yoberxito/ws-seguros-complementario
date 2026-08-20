package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.common.util.HashUtil;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoSustentoRepository;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.BeneficiarioVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.BeneficiarioVidaRepository;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.ProcesoVidaRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class RespaldoFormulario6012Service {

    private static final ZoneId ZONA_HORARIA_LIMA =
            ZoneId.of("America/Lima");

    private final ProcesoVidaRepository
            procesoVidaRepository;

    private final BeneficiarioVidaRepository
            beneficiarioVidaRepository;

    private final DocumentoSustentoRepository
            documentoSustentoRepository;

    public RespaldoFormulario6012Service(
            ProcesoVidaRepository procesoVidaRepository,
            BeneficiarioVidaRepository beneficiarioVidaRepository,
            DocumentoSustentoRepository documentoSustentoRepository
    ) {
        this.procesoVidaRepository =
                procesoVidaRepository;

        this.beneficiarioVidaRepository =
                beneficiarioVidaRepository;

        this.documentoSustentoRepository =
                documentoSustentoRepository;
    }

    public void generarYPersistir(
            String registroInternoProceso,
            String idDocumentoPublicado
    ) {

        ProcesoVida proceso =
                procesoVidaRepository
                        .buscarPorRegistroInternoProceso(
                                registroInternoProceso
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No se encontró el proceso +Vida para generar el respaldo del Formulario 6012."
                                )
                        );

        List<BeneficiarioVida> beneficiarios =
                beneficiarioVidaRepository
                        .listarPorIdSecomasvida(
                                proceso.getIdSecomasvida()
                        );

        if (beneficiarios.isEmpty()) {
            throw new IllegalStateException(
                    "No existen beneficiarios persistidos para generar el respaldo del Formulario 6012."
            );
        }

        String contenido =
                construirContenido(
                        proceso,
                        beneficiarios,
                        idDocumentoPublicado
                );

        String hash =
                HashUtil.calcularSha256(
                        contenido.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        documentoSustentoRepository
                .registrarRespaldoFormulario6012(
                        registroInternoProceso,
                        contenido,
                        hash,
                        LocalDateTime.now(
                                ZONA_HORARIA_LIMA
                        )
                );
    }

    public String obtenerRespaldo(
            String registroInternoProceso
    ) {
        return documentoSustentoRepository
                .obtenerRespaldoFormulario6012(
                        registroInternoProceso
                );
    }

    private String construirContenido(
            ProcesoVida proceso,
            List<BeneficiarioVida> beneficiarios,
            String idDocumentoPublicado
    ) {

        StringBuilder txt =
                new StringBuilder();

        txt.append(
                "AFILIACION DIGITAL +VIDA - RESPALDO FORMULARIO 6012\n"
        );

        txt.append(
                "REGISTRO_INTERNO_PROCESO="
        ).append(
                valor(
                        proceso
                                .getRegistroInternoProceso()
                )
        ).append('\n');

        txt.append(
                "TIPO_FLUJO="
        ).append(
                valor(
                        proceso.getTipoFlujo()
                )
        ).append('\n');

        txt.append(
                "ID_DOCUMENTO_PUBLICADO="
        ).append(
                valor(
                        idDocumentoPublicado
                )
        ).append('\n');

        txt.append("\n[TITULAR]\n");

        agregar(
                txt,
                "TIPO_DOCUMENTO",
                proceso
                        .getCodigoDocumentoTitular()
        );

        agregar(
                txt,
                "OTRO_TIPO_DOCUMENTO",
                proceso
                        .getDescripcionOtroDocumentoTitular()
        );

        agregar(
                txt,
                "NUMERO_DOCUMENTO",
                proceso
                        .getNumeroDocumentoTitular()
        );

        agregar(
                txt,
                "APELLIDO_PATERNO",
                proceso
                        .getApellidoPaternoTitular()
        );

        agregar(
                txt,
                "APELLIDO_MATERNO",
                proceso
                        .getApellidoMaternoTitular()
        );

        agregar(
                txt,
                "PRIMER_NOMBRE",
                proceso
                        .getPrimerNombreTitular()
        );

        agregar(
                txt,
                "SEGUNDO_NOMBRE",
                proceso
                        .getSegundoNombreTitular()
        );

        agregar(
                txt,
                "CORREO",
                proceso.getCorreo()
        );

        agregar(
                txt,
                "TELEFONO",
                proceso.getNumeroTelefono()
        );

        agregar(
                txt,
                "NOTIFICACION_CORREO",
                proceso.getNotificacionesCorreo()
        );

        txt.append(
                "\n[DATOS_ASEGURAMIENTO]\n"
        );

        agregar(
                txt,
                "TIPO_ASEGURADO",
                proceso.getTipoAsegurado()
        );

        agregar(
                txt,
                "CODIGO_PLANILLA",
                proceso.getCodigoPlanilla()
        );

        agregar(
                txt,
                "DECRETO_LEGISLATIVO",
                proceso.getDecretoLegislativo()
        );

        agregar(
                txt,
                "CONVENIO_CGBVP",
                proceso.getConvenioCgbvp()
        );

        agregar(
                txt,
                "RUC_EMPLEADOR",
                proceso.getRucEmpleador()
        );

        agregar(
                txt,
                "RAZON_SOCIAL",
                proceso.getRazonSocialEntidad()
        );

        txt.append(
                "\n[CONYUGE_CONCUBINO]\n"
        );

        agregar(
                txt,
                "TIPO_RELACION",
                proceso.getTipoRelacion()
        );

        agregar(
                txt,
                "TIPO_DOCUMENTO",
                proceso
                        .getCodigoDocumentoConyuge()
        );

        agregar(
                txt,
                "OTRO_TIPO_DOCUMENTO",
                proceso
                        .getDescripcionOtroDocumentoConyuge()
        );

        agregar(
                txt,
                "NUMERO_DOCUMENTO",
                proceso
                        .getNumeroDocumentoConyuge()
        );

        agregar(
                txt,
                "APELLIDO_PATERNO",
                proceso
                        .getApellidoPaternoConyuge()
        );

        agregar(
                txt,
                "APELLIDO_MATERNO",
                proceso
                        .getApellidoMaternoConyuge()
        );

        agregar(
                txt,
                "PRIMER_NOMBRE",
                proceso
                        .getPrimerNombreConyuge()
        );

        agregar(
                txt,
                "SEGUNDO_NOMBRE",
                proceso
                        .getSegundoNombreConyuge()
        );

        for (
                BeneficiarioVida beneficiario :
                beneficiarios
        ) {

            txt.append(
                    "\n[BENEFICIARIO_"
            ).append(
                    beneficiario
                            .getOrdenBeneficiario()
            ).append(
                    "]\n"
            );

            agregar(
                    txt,
                    "ORDEN",
                    beneficiario
                            .getOrdenBeneficiario()
            );

            agregar(
                    txt,
                    "TIPO_DOCUMENTO",
                    beneficiario
                            .getCodigoDocumentoBeneficiario()
            );

            agregar(
                    txt,
                    "OTRO_TIPO_DOCUMENTO",
                    beneficiario
                            .getDescripcionOtroDocumentoBeneficiario()
            );

            agregar(
                    txt,
                    "NUMERO_DOCUMENTO",
                    beneficiario
                            .getNumeroDocumentoBeneficiario()
            );

            agregar(
                    txt,
                    "APELLIDO_PATERNO",
                    beneficiario
                            .getApellidoPaterno()
            );

            agregar(
                    txt,
                    "APELLIDO_MATERNO",
                    beneficiario
                            .getApellidoMaterno()
            );

            agregar(
                    txt,
                    "PRIMER_NOMBRE",
                    beneficiario
                            .getPrimerNombre()
            );

            agregar(
                    txt,
                    "SEGUNDO_NOMBRE",
                    beneficiario
                            .getSegundoNombre()
            );

            agregar(
                    txt,
                    "PORCENTAJE",
                    beneficiario
                            .getPorcentajeBeneficio()
            );
        }

        return txt.toString();
    }

    private void agregar(
            StringBuilder txt,
            String clave,
            Object valor
    ) {
        txt.append(
                clave
        ).append(
                '='
        ).append(
                valor(valor)
        ).append(
                '\n'
        );
    }

    private String valor(
            Object valor
    ) {
        if (valor == null) {
            return "";
        }

        return valor
                .toString()
                .replace(
                        "\r",
                        " "
                )
                .replace(
                        "\n",
                        " "
                )
                .trim();
    }
}