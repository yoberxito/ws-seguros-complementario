package essalud.gob.pe.wsseguroscomplementario.proceso.service;


import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.BeneficiarioProgresoRequest;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarBeneficiariosRequest;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.BeneficiarioVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.BeneficiarioVidaRepository;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.ActualizarNavegacionRequest;
import org.springframework.transaction.annotation.Transactional;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoSustentoRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarProgresoVidaResponse;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarTitularRequest;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.IniciarProcesoVidaRequest;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.IniciarProcesoVidaResponse;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.ProcesoVidaRepository;
import org.springframework.stereotype.Service;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarDatosComplementariosRequest;
import essalud.gob.pe.wsseguroscomplementario.proceso.dto.GuardarConyugeRequest;
import java.util.Optional;

@Service
public class ProgresoVidaService {

    private static final String ESTADO_INICIAL = "DATOS_TITULAR";
    private static final String ESTADO_OPERATIVO_INICIAL = "REGISTRO_PARCIAL";
    private static final String ESTADO_DESPUES_TITULAR = "DATOS_COMPLEMENTARIOS";
    private static final String ESTADO_DESPUES_DATOS_COMPLEMENTARIOS =
            "CONYUGE_CONCUBINO";
    private static final String ESTADO_DESPUES_CONYUGE =
            "BENEFICIARIOS";
    private static final String ESTADO_DESPUES_BENEFICIARIOS =
            "DECLARACION_JURADA";
    private static final String ESTADO_DOCUMENTOS =
            "DOCUMENTOS";
    private static final String
            TIPO_FLUJO_SOLO_AUTORIZACION =
            "SOLO_AUTORIZACION";

    private static final String
            TIPO_FLUJO_FORMULARIO_6012_POSTERIOR =
            "FORMULARIO_6012_POSTERIOR";

    private static final String
            TIPO_DOCUMENTO_AUTORIZACION =
            "AUTORIZACION_DESCUENTO";
    private static final ZoneId ZONA_HORARIA_LIMA =
            ZoneId.of(EstadoProcesoConstants.ZONA_HORARIA_LIMA);
    private final ProcesoVidaRepository procesoVidaRepository;
    private final BeneficiarioVidaRepository
            beneficiarioVidaRepository;
    private final DocumentoSustentoRepository
            documentoSustentoRepository;
    public ProgresoVidaService(
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

    public IniciarProcesoVidaResponse iniciarProceso(
            IniciarProcesoVidaRequest request
    ) {
        validarRequest(request);

        String registroInternoSolicitado =
                limpiarValor(
                        request.getRegistroInternoProceso()
                );

        if (registroInternoSolicitado != null) {

            Optional<ProcesoVida> procesoPorRegistro =
                    procesoVidaRepository
                            .buscarPorRegistroInternoProceso(
                                    registroInternoSolicitado
                            );

            if (procesoPorRegistro.isPresent()) {
                return convertirAResponse(
                        procesoPorRegistro.get(),
                        false,
                        "El proceso ya se encontraba registrado."
                );
            }
        }

        /*
         * Si el frontend todavía no conoce el identificador,
         * se intenta recuperar el último proceso persistido
         * para el trabajador autenticado.
         *
         * Esto evita crear un proceso nuevo después de un F5
         * o de volver a ingresar al módulo.
         */
        if (registroInternoSolicitado == null) {

            Optional<ProcesoVida> procesoPorTrabajador =
                    procesoVidaRepository
                            .buscarUltimoPorTrabajador(
                                    request
                                            .getTipoDocumentoTitular()
                                            .trim(),
                                    request
                                            .getNumeroDocumentoTitular()
                                            .trim()
                            );

            if (procesoPorTrabajador.isPresent()) {
                return convertirAResponse(
                        procesoPorTrabajador.get(),
                        false,
                        "Se recuperó el proceso +Vida existente del trabajador."
                );
            }
        }

        String registroInternoProceso =
                registroInternoSolicitado != null
                        ? registroInternoSolicitado
                        : generarRegistroInternoProceso();

        ProcesoVida procesoVida = new ProcesoVida();

        procesoVida.setRegistroInternoProceso(
                registroInternoProceso
        );

        procesoVida.setCodigoEstadoProceso(ESTADO_INICIAL);
        procesoVida.setEstadoOperativo(ESTADO_OPERATIVO_INICIAL);

        procesoVida.setCodigoDocumentoTitular(
                request.getTipoDocumentoTitular().trim()
        );

        procesoVida.setDescripcionOtroDocumentoTitular(
                limpiarValor(request.getDescripcionOtroDocumentoTitular())
        );

        procesoVida.setNumeroDocumentoTitular(
                request.getNumeroDocumentoTitular().trim()
        );

        procesoVida.setApellidoPaternoTitular(
                limpiarValor(request.getApellidoPaternoTitular())
        );

        procesoVida.setApellidoMaternoTitular(
                limpiarValor(request.getApellidoMaternoTitular())
        );

        procesoVida.setPrimerNombreTitular(
                limpiarValor(request.getPrimerNombreTitular())
        );

        procesoVida.setSegundoNombreTitular(
                limpiarValor(request.getSegundoNombreTitular())
        );

        procesoVida.setCorreo(
                limpiarValor(request.getCorreo())
        );

        procesoVida.setNumeroTelefono(
                limpiarValor(request.getCelular())
        );

        ProcesoVida procesoGuardado =
                procesoVidaRepository.crear(procesoVida);

        return convertirAResponse(
                procesoGuardado,
                true,
                "Proceso +Vida registrado correctamente."
        );
    }

    public GuardarProgresoVidaResponse guardarBorradorTitular(
            String registroInternoProceso,
            GuardarTitularRequest request
    ) {

        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "El borrador del titular no puede estar vacío."
            );
        }

        String registro =
                registroInternoProceso.trim();

        validarProcesoEditableParaBorrador(
                registro
        );

        ProcesoVida procesoVida =
                new ProcesoVida();

        procesoVida.setCorreo(
                limpiarValor(
                        request.getCorreo()
                )
        );

        procesoVida.setNumeroTelefono(
                limpiarValor(
                        request.getCelular()
                )
        );

        ProcesoVida procesoActualizado =
                procesoVidaRepository
                        .actualizarTitularBorrador(
                                registro,
                                procesoVida
                        );

        return convertirAGuardarProgresoResponse(
                procesoActualizado,
                "Borrador del titular guardado correctamente."
        );
    }

    public GuardarProgresoVidaResponse
    guardarBorradorDatosComplementarios(
            String registroInternoProceso,
            GuardarDatosComplementariosRequest request
    ) {

        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "El borrador de datos complementarios no puede estar vacío."
            );
        }

        String registro =
                registroInternoProceso.trim();

        validarProcesoEditableParaBorrador(
                registro
        );

        String convenio =
                limpiarValor(
                        request.getConvenioCgbvp()
                );

        if (
                convenio != null
                        && !"SI".equalsIgnoreCase(convenio)
                        && !"NO".equalsIgnoreCase(convenio)
        ) {
            throw new IllegalArgumentException(
                    "El valor del convenio CGBVP no es válido."
            );
        }

        ProcesoVida procesoVida =
                new ProcesoVida();

        procesoVida.setCodigoPlanilla(
                limpiarValor(
                        request.getCodigoPlanilla()
                )
        );

        procesoVida.setDecretoLegislativo(
                limpiarValor(
                        request.getDecretoLegislativo()
                )
        );

        procesoVida.setConvenioCgbvp(
                convenio == null
                        ? null
                        : convenio.toUpperCase()
        );

        procesoVida.setRucEmpleador(
                limpiarValor(
                        request.getRucEmpleador()
                )
        );

        procesoVida.setRazonSocialEntidad(
                limpiarValor(
                        request.getRazonSocial()
                )
        );

        ProcesoVida procesoActualizado =
                procesoVidaRepository
                        .actualizarDatosComplementariosBorrador(
                                registro,
                                procesoVida
                        );

        return convertirAGuardarProgresoResponse(
                procesoActualizado,
                "Borrador de datos complementarios guardado correctamente."
        );
    }

    @Transactional
    public GuardarProgresoVidaResponse
    guardarBorradorBeneficiarios(
            String registroInternoProceso,
            GuardarBeneficiariosRequest request
    ) {

        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        if (
                request == null
                        || request.getBeneficiarios() == null
        ) {
            throw new IllegalArgumentException(
                    "La lista de beneficiarios del borrador es obligatoria."
            );
        }

        String registro =
                registroInternoProceso.trim();

        ProcesoVida procesoActual =
                validarProcesoEditableParaBorrador(
                        registro
                );

        Long idSecomasvida =
                procesoActual.getIdSecomasvida();

        if (idSecomasvida == null) {
            throw new IllegalStateException(
                    "El proceso +Vida no cuenta con identificador interno en Oracle."
            );
        }

        if (
                request.getBeneficiarios().size()
                        > 999
        ) {
            throw new IllegalArgumentException(
                    "La cantidad de beneficiarios supera el límite técnico permitido."
            );
        }

        List<BeneficiarioProgresoRequest>
                beneficiariosIdentificados =
                obtenerBeneficiariosIdentificadosBorrador(
                        request.getBeneficiarios()
                );

        validarBeneficiariosBorrador(
                beneficiariosIdentificados
        );

        List<BeneficiarioVida>
                beneficiariosPersistir =
                convertirBeneficiarios(
                        idSecomasvida,
                        beneficiariosIdentificados
                );

        /*
         * La colección recibida representa el
         * borrador vigente completo.
         */
        beneficiarioVidaRepository
                .eliminarPorIdSecomasvida(
                        idSecomasvida
                );

        beneficiarioVidaRepository
                .guardarTodos(
                        beneficiariosPersistir
                );

        /*
         * Importante:
         * solamente actualizamos la fecha.
         *
         * No cambia ID_ESTADO.
         * No cambia ID_ESTADO_NAVEGACION.
         */
        boolean borradorAbierto =
                Boolean.TRUE.equals(
                        request
                                .getBeneficiarioBorradorAbierto()
                );

        ProcesoVida procesoActualizado =
                procesoVidaRepository
                        .actualizarBorradorBeneficiarios(
                                registro,
                                borradorAbierto
                        );

        return convertirAGuardarProgresoResponse(
                procesoActualizado,
                "Borrador de beneficiarios guardado correctamente."
        );
    }

    private List<BeneficiarioProgresoRequest>
    obtenerBeneficiariosIdentificadosBorrador(
            List<BeneficiarioProgresoRequest> beneficiarios
    ) {

        List<BeneficiarioProgresoRequest> resultado =
                new ArrayList<>();

        for (
                BeneficiarioProgresoRequest beneficiario :
                beneficiarios
        ) {

            if (beneficiario == null) {
                continue;
            }

            boolean identidadCompleta =
                    !campoVacio(
                            beneficiario.getTipoDocumento()
                    )
                            && !campoVacio(
                            beneficiario.getNumeroDocumento()
                    )
                            && !campoVacio(
                            beneficiario.getApellidoPaterno()
                    )
                            && !campoVacio(
                            beneficiario.getApellidoMaterno()
                    )
                            && !campoVacio(
                            beneficiario.getPrimerNombre()
                    );

            /*
             * Una fila visual completamente vacía
             * o una persona todavía no identificada
             * no se persiste como beneficiario.
             */
            if (!identidadCompleta) {
                continue;
            }

            resultado.add(
                    beneficiario
            );
        }

        return resultado;
    }

    private void validarBeneficiariosBorrador(
            List<BeneficiarioProgresoRequest> beneficiarios
    ) {

        Set<String> documentosRegistrados =
                new HashSet<>();

        for (
                int i = 0;
                i < beneficiarios.size();
                i++
        ) {

            BeneficiarioProgresoRequest beneficiario =
                    beneficiarios.get(i);

            int numeroBeneficiario =
                    i + 1;

            String tipoDocumento =
                    beneficiario
                            .getTipoDocumento()
                            .trim();

            String numeroDocumento =
                    beneficiario
                            .getNumeroDocumento()
                            .trim();

            if (numeroDocumento.length() > 15) {
                throw new IllegalArgumentException(
                        "El número de documento del Beneficiario #"
                                + numeroBeneficiario
                                + " supera los 15 caracteres permitidos."
                );
            }

            if (
                    "01".equals(tipoDocumento)
                            && !numeroDocumento.matches("^\\d{8}$")
            ) {
                throw new IllegalArgumentException(
                        "El DNI del Beneficiario #"
                                + numeroBeneficiario
                                + " debe contener 8 dígitos."
                );
            }

            if (
                    "04".equals(tipoDocumento)
                            && !numeroDocumento.matches("^\\d{9}$")
            ) {
                throw new IllegalArgumentException(
                        "El C.E. del Beneficiario #"
                                + numeroBeneficiario
                                + " debe contener 9 dígitos."
                );
            }

            if (
                    !"01".equals(tipoDocumento)
                            && !"04".equals(tipoDocumento)
                            && campoVacio(
                            beneficiario
                                    .getDescripcionOtroDocumento()
                    )
            ) {
                throw new IllegalArgumentException(
                        "La descripción del documento del Beneficiario #"
                                + numeroBeneficiario
                                + " es obligatoria para tipos distintos de DNI o C.E."
                );
            }

            BigDecimal porcentaje =
                    beneficiario.getPorcentaje();

            /*
             * En borrador el porcentaje puede
             * todavía estar pendiente.
             */
            if (porcentaje != null) {

                if (
                        porcentaje.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
                                || porcentaje.compareTo(
                                new BigDecimal("100")
                        ) > 0
                ) {
                    throw new IllegalArgumentException(
                            "El porcentaje del Beneficiario #"
                                    + numeroBeneficiario
                                    + " debe ser mayor que 0 y menor o igual a 100."
                    );
                }

                if (
                        porcentaje
                                .stripTrailingZeros()
                                .scale() > 2
                ) {
                    throw new IllegalArgumentException(
                            "El porcentaje del Beneficiario #"
                                    + numeroBeneficiario
                                    + " admite como máximo 2 decimales."
                    );
                }
            }

            String claveDocumento =
                    tipoDocumento
                            + "|"
                            + numeroDocumento;

            if (
                    !documentosRegistrados.add(
                            claveDocumento
                    )
            ) {
                throw new IllegalArgumentException(
                        "No se puede registrar dos veces el mismo documento como beneficiario."
                );
            }
        }
    }

    private ProcesoVida validarProcesoEditableParaBorrador(
            String registroInternoProceso
    ) {

        ProcesoVida proceso =
                procesoVidaRepository
                        .buscarPorRegistroInternoProceso(
                                registroInternoProceso
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No se encontró el proceso +Vida indicado."
                                )
                        );

        String estadoMaximo =
                proceso.getCodigoEstadoProceso();

        if (
                "DOCUMENTOS".equalsIgnoreCase(
                        estadoMaximo
                )
                        || "FINALIZACION".equalsIgnoreCase(
                        estadoMaximo
                )
        ) {
            throw new IllegalArgumentException(
                    "La solicitud ya fue confirmada para generación documental y sus datos no pueden modificarse."
            );
        }

        return proceso;
    }

    public GuardarProgresoVidaResponse guardarTitular(
            String registroInternoProceso,
            GuardarTitularRequest request
    ) {
        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        validarTitularRequest(request);

        ProcesoVida procesoVida = new ProcesoVida();

        procesoVida.setCodigoDocumentoTitular(
                request.getTipoDocumentoTitular().trim()
        );

        procesoVida.setDescripcionOtroDocumentoTitular(
                limpiarValor(request.getDescripcionOtroDocumentoTitular())
        );

        procesoVida.setNumeroDocumentoTitular(
                request.getNumeroDocumentoTitular().trim()
        );

        procesoVida.setApellidoPaternoTitular(
                limpiarValor(request.getApellidoPaternoTitular())
        );

        procesoVida.setApellidoMaternoTitular(
                limpiarValor(request.getApellidoMaternoTitular())
        );

        procesoVida.setPrimerNombreTitular(
                limpiarValor(request.getPrimerNombreTitular())
        );

        procesoVida.setSegundoNombreTitular(
                limpiarValor(request.getSegundoNombreTitular())
        );

        procesoVida.setCorreo(
                request.getCorreo().trim()
        );

        procesoVida.setNumeroTelefono(
                request.getCelular().trim()
        );

        procesoVida.setTipoAsegurado(
                request.getTipoAsegurado()
                        .trim()
                        .toUpperCase()
        );

        ProcesoVida procesoActualizado =
                procesoVidaRepository.actualizarTitularYEstado(
                        registroInternoProceso.trim(),
                        procesoVida,
                        ESTADO_DESPUES_TITULAR
                );

        return convertirAGuardarProgresoResponse(
                procesoActualizado,
                "Datos del titular guardados correctamente."
        );
    }

    public GuardarProgresoVidaResponse guardarDatosComplementarios(
            String registroInternoProceso,
            GuardarDatosComplementariosRequest request
    ) {
        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        validarDatosComplementariosRequest(request);

        ProcesoVida procesoVida =
                new ProcesoVida();

        procesoVida.setCodigoPlanilla(
                request.getCodigoPlanilla().trim()
        );

        procesoVida.setDecretoLegislativo(
                request.getDecretoLegislativo().trim()
        );

        procesoVida.setConvenioCgbvp(
                request.getConvenioCgbvp()
                        .trim()
                        .toUpperCase()
        );

        procesoVida.setRucEmpleador(
                limpiarValor(
                        request.getRucEmpleador()
                )
        );

        procesoVida.setRazonSocialEntidad(
                limpiarValor(
                        request.getRazonSocial()
                )
        );

        ProcesoVida procesoActualizado =
                procesoVidaRepository
                        .actualizarDatosComplementariosYEstado(
                                registroInternoProceso.trim(),
                                procesoVida,
                                ESTADO_DESPUES_DATOS_COMPLEMENTARIOS
                        );

        return convertirAGuardarProgresoResponse(
                procesoActualizado,
                "Datos complementarios guardados correctamente."
        );
    }

    public GuardarProgresoVidaResponse guardarConyuge(
            String registroInternoProceso,
            GuardarConyugeRequest request
    ) {
        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        validarConyugeRequest(request);

        ProcesoVida procesoVida =
                new ProcesoVida();

        procesoVida.setCodigoDocumentoConyuge(
                limpiarValor(
                        request.getTipoDocumentoConyuge()
                )
        );

        procesoVida.setDescripcionOtroDocumentoConyuge(
                limpiarValor(
                        request.getDescripcionOtroDocumentoConyuge()
                )
        );

        procesoVida.setNumeroDocumentoConyuge(
                limpiarValor(
                        request.getNumeroDocumentoConyuge()
                )
        );

        procesoVida.setApellidoPaternoConyuge(
                limpiarValor(
                        request.getApellidoPaternoConyuge()
                )
        );

        procesoVida.setApellidoMaternoConyuge(
                limpiarValor(
                        request.getApellidoMaternoConyuge()
                )
        );

        procesoVida.setPrimerNombreConyuge(
                limpiarValor(
                        request.getPrimerNombreConyuge()
                )
        );

        procesoVida.setSegundoNombreConyuge(
                limpiarValor(
                        request.getSegundoNombreConyuge()
                )
        );

        procesoVida.setTipoRelacion(
                limpiarValor(
                        request.getTipoRelacion()
                )
        );

        ProcesoVida procesoActualizado =
                procesoVidaRepository
                        .actualizarConyugeYEstado(
                                registroInternoProceso.trim(),
                                procesoVida,
                                ESTADO_DESPUES_CONYUGE
                        );

        return convertirAGuardarProgresoResponse(
                procesoActualizado,
                "Datos del cónyuge o concubino guardados correctamente."
        );
    }

    @Transactional
    public GuardarProgresoVidaResponse guardarBeneficiarios(
            String registroInternoProceso,
            GuardarBeneficiariosRequest request
    ) {
        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        validarBeneficiariosRequest(
                request
        );

        String registro =
                registroInternoProceso.trim();

        ProcesoVida procesoVida =
                procesoVidaRepository
                        .buscarPorRegistroInternoProceso(
                                registro
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No se encontró el proceso +Vida indicado."
                                )
                        );

        Long idSecomasvida =
                procesoVida.getIdSecomasvida();

        if (idSecomasvida == null) {
            throw new IllegalStateException(
                    "El proceso +Vida no cuenta con identificador interno en Oracle."
            );
        }

        List<BeneficiarioVida> beneficiariosPersistir =
                convertirBeneficiarios(
                        idSecomasvida,
                        request.getBeneficiarios()
                );

        boolean iniciaFormulario6012Posterior =
                !beneficiariosPersistir.isEmpty()

                        && TIPO_FLUJO_SOLO_AUTORIZACION
                        .equalsIgnoreCase(
                                procesoVida
                                        .getTipoFlujo()
                        );

        if (iniciaFormulario6012Posterior) {

            /*
             * Un ciclo 6012 posterior únicamente puede
             * nacer después de existir una Autorización
             * de Descuento final publicada.
             *
             * No confiamos en un indicador del frontend.
             * La condición se comprueba contra la
             * persistencia documental.
             */
            boolean autorizacionPublicada =
                    documentoSustentoRepository
                            .estaPublicado(
                                    registro,
                                    TIPO_DOCUMENTO_AUTORIZACION
                            );

            if (!autorizacionPublicada) {

                throw new IllegalStateException(
                        "No se puede iniciar el Formulario 6012 posterior "
                                + "porque la Autorización de Descuento "
                                + "todavía no se encuentra publicada."
                );
            }
        }

        /*
         * La lista recibida representa la colección
         * completa vigente del trámite.
         */
        beneficiarioVidaRepository
                .eliminarPorIdSecomasvida(
                        idSecomasvida
                );

        beneficiarioVidaRepository
                .guardarTodos(
                        beneficiariosPersistir
                );

        /*
         * Al confirmar definitivamente la sección,
         * ya no queda un formulario vacío pendiente.
         */
        procesoVidaRepository
                .actualizarBorradorBeneficiarios(
                        registro,
                        false
                );

        if (iniciaFormulario6012Posterior) {

            procesoVidaRepository
                    .activarFormulario6012Posterior(
                            registro
                    );
        }

        String estadoSiguiente =
                iniciaFormulario6012Posterior
                        ? ESTADO_DOCUMENTOS
                        : ESTADO_DESPUES_BENEFICIARIOS;

        ProcesoVida procesoActualizado =
                procesoVidaRepository
                        .actualizarEstado(
                                registro,
                                estadoSiguiente
                        );

        String mensaje =
                beneficiariosPersistir.isEmpty()
                        ? "Se registró correctamente la decisión de continuar sin beneficiarios."
                        : "Beneficiarios guardados correctamente.";

        return convertirAGuardarProgresoResponse(
                procesoActualizado,
                mensaje
        );
    }

    private List<BeneficiarioVida> convertirBeneficiarios(
            Long idSecomasvida,
            List<BeneficiarioProgresoRequest> beneficiarios
    ) {
        List<BeneficiarioVida> resultado =
                new ArrayList<>();

        for (int i = 0; i < beneficiarios.size(); i++) {

            BeneficiarioProgresoRequest origen =
                    beneficiarios.get(i);

            BeneficiarioVida destino =
                    new BeneficiarioVida();

            destino.setIdSecomasvida(
                    idSecomasvida
            );

            destino.setOrdenBeneficiario(
                    i + 1
            );

            destino.setCodigoDocumentoBeneficiario(
                    origen.getTipoDocumento().trim()
            );

            destino.setDescripcionOtroDocumentoBeneficiario(
                    limpiarValor(
                            origen.getDescripcionOtroDocumento()
                    )
            );

            destino.setNumeroDocumentoBeneficiario(
                    origen.getNumeroDocumento().trim()
            );

            destino.setApellidoPaterno(
                    limpiarValor(
                            origen.getApellidoPaterno()
                    )
            );

            destino.setApellidoMaterno(
                    limpiarValor(
                            origen.getApellidoMaterno()
                    )
            );

            destino.setPrimerNombre(
                    limpiarValor(
                            origen.getPrimerNombre()
                    )
            );

            destino.setSegundoNombre(
                    limpiarValor(
                            origen.getSegundoNombre()
                    )
            );

            destino.setPorcentajeBeneficio(
                    origen.getPorcentaje()
            );

            resultado.add(
                    destino
            );
        }

        return resultado;
    }

    private void validarBeneficiariosRequest(
            GuardarBeneficiariosRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "La información de beneficiarios no puede estar vacía."
            );
        }

        if (request.getBeneficiarios() == null) {
            throw new IllegalArgumentException(
                    "La lista de beneficiarios es obligatoria."
            );
        }

        List<BeneficiarioProgresoRequest> beneficiarios =
                request.getBeneficiarios();

        /*
         * Lista vacía es válida:
         * continuar sin beneficiarios.
         */
        if (beneficiarios.isEmpty()) {
            return;
        }

        /*
         * Único límite técnico proveniente de
         * ORDEN_BENEFICIARIO NUMBER(3).
         */
        if (beneficiarios.size() > 999) {
            throw new IllegalArgumentException(
                    "La cantidad de beneficiarios supera el límite técnico permitido."
            );
        }

        BigDecimal sumaPorcentajes =
                BigDecimal.ZERO;

        Set<String> documentosRegistrados =
                new HashSet<>();

        for (int i = 0; i < beneficiarios.size(); i++) {

            BeneficiarioProgresoRequest beneficiario =
                    beneficiarios.get(i);

            int numeroBeneficiario =
                    i + 1;

            if (beneficiario == null) {
                throw new IllegalArgumentException(
                        "El Beneficiario #"
                                + numeroBeneficiario
                                + " no puede estar vacío."
                );
            }

            if (campoVacio(
                    beneficiario.getTipoDocumento()
            )) {
                throw new IllegalArgumentException(
                        "El tipo de documento del Beneficiario #"
                                + numeroBeneficiario
                                + " es obligatorio."
                );
            }

            if (campoVacio(
                    beneficiario.getNumeroDocumento()
            )) {
                throw new IllegalArgumentException(
                        "El número de documento del Beneficiario #"
                                + numeroBeneficiario
                                + " es obligatorio."
                );
            }

            String tipoDocumento =
                    beneficiario
                            .getTipoDocumento()
                            .trim();

            String numeroDocumento =
                    beneficiario
                            .getNumeroDocumento()
                            .trim();

            if (numeroDocumento.length() > 15) {
                throw new IllegalArgumentException(
                        "El número de documento del Beneficiario #"
                                + numeroBeneficiario
                                + " supera los 15 caracteres permitidos."
                );
            }

            if (
                    "01".equals(tipoDocumento)
                            && !numeroDocumento.matches("^\\d{8}$")
            ) {
                throw new IllegalArgumentException(
                        "El DNI del Beneficiario #"
                                + numeroBeneficiario
                                + " debe contener 8 dígitos."
                );
            }

            if (
                    "04".equals(tipoDocumento)
                            && !numeroDocumento.matches("^\\d{9}$")
            ) {
                throw new IllegalArgumentException(
                        "El C.E. del Beneficiario #"
                                + numeroBeneficiario
                                + " debe contener 9 dígitos."
                );
            }

            if (
                    !"01".equals(tipoDocumento)
                            && !"04".equals(tipoDocumento)
                            && campoVacio(
                            beneficiario
                                    .getDescripcionOtroDocumento()
                    )
            ) {
                throw new IllegalArgumentException(
                        "La descripción del documento del Beneficiario #"
                                + numeroBeneficiario
                                + " es obligatoria para tipos distintos de DNI o C.E."
                );
            }

            if (campoVacio(
                    beneficiario.getApellidoPaterno()
            )) {
                throw new IllegalArgumentException(
                        "El apellido paterno del Beneficiario #"
                                + numeroBeneficiario
                                + " es obligatorio."
                );
            }

            if (campoVacio(
                    beneficiario.getApellidoMaterno()
            )) {
                throw new IllegalArgumentException(
                        "El apellido materno del Beneficiario #"
                                + numeroBeneficiario
                                + " es obligatorio."
                );
            }

            if (campoVacio(
                    beneficiario.getPrimerNombre()
            )) {
                throw new IllegalArgumentException(
                        "El primer nombre del Beneficiario #"
                                + numeroBeneficiario
                                + " es obligatorio."
                );
            }

            BigDecimal porcentaje =
                    beneficiario.getPorcentaje();

            if (porcentaje == null) {
                throw new IllegalArgumentException(
                        "El porcentaje del Beneficiario #"
                                + numeroBeneficiario
                                + " es obligatorio."
                );
            }

            if (
                    porcentaje.compareTo(BigDecimal.ZERO) <= 0
                            || porcentaje.compareTo(
                            new BigDecimal("100")
                    ) > 0
            ) {
                throw new IllegalArgumentException(
                        "El porcentaje del Beneficiario #"
                                + numeroBeneficiario
                                + " debe ser mayor que 0 y menor o igual a 100."
                );
            }

            if (
                    porcentaje
                            .stripTrailingZeros()
                            .scale() > 2
            ) {
                throw new IllegalArgumentException(
                        "El porcentaje del Beneficiario #"
                                + numeroBeneficiario
                                + " admite como máximo 2 decimales."
                );
            }

            String claveDocumento =
                    tipoDocumento
                            + "|"
                            + numeroDocumento;

            if (
                    !documentosRegistrados.add(
                            claveDocumento
                    )
            ) {
                throw new IllegalArgumentException(
                        "No se puede registrar dos veces el mismo documento como beneficiario."
                );
            }

            sumaPorcentajes =
                    sumaPorcentajes.add(
                            porcentaje
                    );
        }

        if (
                sumaPorcentajes.compareTo(
                        new BigDecimal("100")
                ) != 0
        ) {
            throw new IllegalArgumentException(
                    "La suma de los porcentajes de los beneficiarios debe ser igual a 100."
            );
        }
    }

    private void validarRequest(
            IniciarProcesoVidaRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de inicio del proceso no puede estar vacía."
            );
        }

        if (campoVacio(request.getTipoDocumentoTitular())) {
            throw new IllegalArgumentException(
                    "El tipo de documento del titular es obligatorio."
            );
        }

        if (campoVacio(request.getNumeroDocumentoTitular())) {
            throw new IllegalArgumentException(
                    "El número de documento del titular es obligatorio."
            );
        }

        String tipoDocumento =
                request.getTipoDocumentoTitular().trim();

        boolean documentoDniOCe =
                "01".equals(tipoDocumento)
                        || "04".equals(tipoDocumento);

        if (
                !documentoDniOCe
                        && campoVacio(request.getDescripcionOtroDocumentoTitular())
        ) {
            throw new IllegalArgumentException(
                    "La descripción del documento del titular es obligatoria para tipos distintos de DNI o C.E."
            );
        }
    }

    private IniciarProcesoVidaResponse convertirAResponse(
            ProcesoVida procesoVida,
            boolean procesoCreado,
            String mensajeOperacion
    ) {
        IniciarProcesoVidaResponse response =
                new IniciarProcesoVidaResponse();

        response.setProcesoCreado(procesoCreado);
        response.setMensajeOperacion(mensajeOperacion);

        response.setIdSecomasvida(
                procesoVida.getIdSecomasvida()
        );

        response.setRegistroInternoProceso(
                procesoVida.getRegistroInternoProceso()
        );

        response.setTipoDocumentoTitular(
                procesoVida.getCodigoDocumentoTitular()
        );

        response.setNumeroDocumentoTitular(
                procesoVida.getNumeroDocumentoTitular()
        );

        response.setCodigoEstadoProceso(
                procesoVida.getCodigoEstadoProceso()
        );

        response.setRutaFrontend(
                procesoVida.getRutaFrontend()
        );

        response.setEstadoOperativo(
                procesoVida.getEstadoOperativo()
        );

        response.setFechaRegistro(
                procesoVida.getFechaRegistro()
        );

        response.setFechaActualizacion(
                procesoVida.getFechaActualizacion()
        );

        return response;
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String limpiarValor(String valor) {
        return campoVacio(valor)
                ? null
                : valor.trim();
    }

    private void validarTitularRequest(
            GuardarTitularRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos del titular no pueden estar vacíos."
            );
        }

        if (campoVacio(request.getTipoDocumentoTitular())) {
            throw new IllegalArgumentException(
                    "El tipo de documento del titular es obligatorio."
            );
        }

        if (campoVacio(request.getNumeroDocumentoTitular())) {
            throw new IllegalArgumentException(
                    "El número de documento del titular es obligatorio."
            );
        }

        if (campoVacio(request.getCorreo())) {
            throw new IllegalArgumentException(
                    "El correo del titular es obligatorio."
            );
        }

        if (!request.getCorreo().trim().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
        )) {
            throw new IllegalArgumentException(
                    "El correo del titular no tiene un formato válido."
            );
        }

        if (campoVacio(request.getCelular())) {
            throw new IllegalArgumentException(
                    "El celular del titular es obligatorio."
            );
        }

        if (campoVacio(request.getTipoAsegurado())) {
            throw new IllegalArgumentException(
                    "El tipo de asegurado es obligatorio."
            );
        }

        String tipoAsegurado =
                request.getTipoAsegurado()
                        .trim()
                        .toUpperCase();

        if (
                !"REGULAR".equals(tipoAsegurado)
                        && !"AGRARIO".equals(tipoAsegurado)
                        && !"POTESTATIVO".equals(tipoAsegurado)
        ) {
            throw new IllegalArgumentException(
                    "El tipo de asegurado no es válido."
            );
        }

        String tipoDocumento =
                request.getTipoDocumentoTitular().trim();

        boolean documentoDniOCe =
                "01".equals(tipoDocumento)
                        || "04".equals(tipoDocumento);

        if (
                !documentoDniOCe
                        && campoVacio(request.getDescripcionOtroDocumentoTitular())
        ) {
            throw new IllegalArgumentException(
                    "La descripción del documento del titular es obligatoria para tipos distintos de DNI o C.E."
            );
        }
    }

    private GuardarProgresoVidaResponse convertirAGuardarProgresoResponse(
            ProcesoVida procesoVida,
            String mensajeOperacion
    ) {
        GuardarProgresoVidaResponse response =
                new GuardarProgresoVidaResponse();

        response.setRegistroInternoProceso(
                procesoVida.getRegistroInternoProceso()
        );

        response.setCodigoEstadoProceso(
                procesoVida.getCodigoEstadoProceso()
        );

        response.setRutaFrontend(
                procesoVida.getRutaFrontend()
        );

        response.setEstadoOperativo(
                procesoVida.getEstadoOperativo()
        );

        response.setFechaActualizacion(
                procesoVida.getFechaActualizacion()
        );

        response.setMensajeOperacion(
                mensajeOperacion
        );
        response.setCodigoEstadoNavegacion(
                procesoVida.getCodigoEstadoNavegacion()
        );

        response.setRutaFrontendNavegacion(
                procesoVida.getRutaFrontendNavegacion()
        );

        return response;
    }

    private String generarRegistroInternoProceso() {
        int anio =
                LocalDate.now(ZONA_HORARIA_LIMA)
                        .getYear();

        String correlativoTecnico =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        return "VIDA-"
                + anio
                + "-"
                + correlativoTecnico;
    }
    private void validarDatosComplementariosRequest(
            GuardarDatosComplementariosRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos complementarios no pueden estar vacíos."
            );
        }

        if (campoVacio(request.getCodigoPlanilla())) {
            throw new IllegalArgumentException(
                    "El código de planilla es obligatorio."
            );
        }

        if (campoVacio(request.getDecretoLegislativo())) {
            throw new IllegalArgumentException(
                    "El decreto legislativo es obligatorio."
            );
        }

        if (campoVacio(request.getConvenioCgbvp())) {
            throw new IllegalArgumentException(
                    "El convenio CGBVP es obligatorio."
            );
        }

        String convenio =
                request.getConvenioCgbvp()
                        .trim()
                        .toUpperCase();

        if (
                !"SI".equals(convenio)
                        && !"NO".equals(convenio)
        ) {
            throw new IllegalArgumentException(
                    "El valor del convenio CGBVP no es válido."
            );
        }

        if (!campoVacio(request.getRucEmpleador())) {
            String ruc =
                    request.getRucEmpleador().trim();

            if (!ruc.matches("^\\d{11}$")) {
                throw new IllegalArgumentException(
                        "El RUC del empleador debe contener 11 dígitos."
                );
            }
        }
    }
    private void validarConyugeRequest(
            GuardarConyugeRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "La información del cónyuge no puede estar vacía."
            );
        }

        boolean sinConyuge =
                campoVacio(request.getTipoDocumentoConyuge())
                        && campoVacio(request.getNumeroDocumentoConyuge())
                        && campoVacio(request.getApellidoPaternoConyuge())
                        && campoVacio(request.getApellidoMaternoConyuge())
                        && campoVacio(request.getPrimerNombreConyuge())
                        && campoVacio(request.getSegundoNombreConyuge())
                        && campoVacio(request.getTipoRelacion());

        /*
         * No tener cónyuge o concubino es un resultado
         * funcional válido del servicio institucional.
         */
        if (sinConyuge) {
            return;
        }

        if (campoVacio(request.getTipoDocumentoConyuge())) {
            throw new IllegalArgumentException(
                    "El tipo de documento del cónyuge es obligatorio."
            );
        }

        if (campoVacio(request.getNumeroDocumentoConyuge())) {
            throw new IllegalArgumentException(
                    "El número de documento del cónyuge es obligatorio."
            );
        }

        if (campoVacio(request.getApellidoPaternoConyuge())) {
            throw new IllegalArgumentException(
                    "El apellido paterno del cónyuge es obligatorio."
            );
        }

        if (campoVacio(request.getApellidoMaternoConyuge())) {
            throw new IllegalArgumentException(
                    "El apellido materno del cónyuge es obligatorio."
            );
        }

        if (campoVacio(request.getPrimerNombreConyuge())) {
            throw new IllegalArgumentException(
                    "El primer nombre del cónyuge es obligatorio."
            );
        }

        if (campoVacio(request.getTipoRelacion())) {
            throw new IllegalArgumentException(
                    "El tipo de relación del cónyuge es obligatorio."
            );
        }

        String tipoDocumento =
                request.getTipoDocumentoConyuge()
                        .trim();

        boolean documentoDniOCe =
                "01".equals(tipoDocumento)
                        || "04".equals(tipoDocumento);

        if (
                !documentoDniOCe
                        && campoVacio(
                        request.getDescripcionOtroDocumentoConyuge()
                )
        ) {
            throw new IllegalArgumentException(
                    "La descripción del documento del cónyuge es obligatoria para tipos distintos de DNI o C.E."
            );
        }
    }
    public GuardarProgresoVidaResponse
    actualizarNavegacion(
            String registroInternoProceso,
            ActualizarNavegacionRequest request
    ) {

        if (campoVacio(registroInternoProceso)) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }

        if (
                request == null
                        || campoVacio(
                        request
                                .getCodigoEstadoNavegacion()
                )
        ) {
            throw new IllegalArgumentException(
                    "La sección de navegación es obligatoria."
            );
        }

        ProcesoVida procesoActual =
                procesoVidaRepository
                        .buscarPorRegistroInternoProceso(
                                registroInternoProceso.trim()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No se encontró el proceso +Vida indicado."
                                )
                        );

        String estadoMaximo =
                procesoActual
                        .getCodigoEstadoProceso();

        /*
         * Una vez iniciada la fase documental,
         * los datos que forman los documentos
         * quedan bloqueados.
         */
        if (
                "DOCUMENTOS".equalsIgnoreCase(
                        estadoMaximo
                )
                        || "FINALIZACION".equalsIgnoreCase(
                        estadoMaximo
                )
        ) {
            throw new IllegalArgumentException(
                    "La navegación hacia secciones editables ya no está permitida porque la solicitud documental fue confirmada."
            );
        }

        ProcesoVida procesoActualizado =
                procesoVidaRepository
                        .actualizarEstadoNavegacion(
                                registroInternoProceso.trim(),
                                request
                                        .getCodigoEstadoNavegacion()
                                        .trim()
                                        .toUpperCase()
                        );

        return convertirAGuardarProgresoResponse(
                procesoActualizado,
                "Sección actual del trámite actualizada correctamente."
        );
    }
}