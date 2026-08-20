package essalud.gob.pe.wsseguroscomplementario.aceptacion.service;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.BeneficiarioVidaRepository;
import essalud.gob.pe.wsseguroscomplementario.aceptacion.dto.RegistrarAceptacionRequest;
import essalud.gob.pe.wsseguroscomplementario.aceptacion.dto.RegistrarAceptacionResponse;
import essalud.gob.pe.wsseguroscomplementario.aceptacion.model.AceptacionLegal;
import essalud.gob.pe.wsseguroscomplementario.aceptacion.repository.AceptacionRepository;
import org.springframework.stereotype.Service;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.ProcesoVidaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AceptacionService {

    private static final String
            ESTADO_DESPUES_DECLARACION =
            "DOCUMENTOS";
    private static final String
            TIPO_FLUJO_COMPLETO =
            "COMPLETO";

    private static final String
            TIPO_FLUJO_SOLO_AUTORIZACION =
            "SOLO_AUTORIZACION";
    private final AceptacionRepository
            aceptacionRepository;

    private final ProcesoVidaRepository
            procesoVidaRepository;

    private final BeneficiarioVidaRepository
            beneficiarioVidaRepository;

    public AceptacionService(
            AceptacionRepository aceptacionRepository,
            ProcesoVidaRepository procesoVidaRepository,
            BeneficiarioVidaRepository beneficiarioVidaRepository
    ) {
        this.aceptacionRepository =
                aceptacionRepository;

        this.procesoVidaRepository =
                procesoVidaRepository;

        this.beneficiarioVidaRepository =
                beneficiarioVidaRepository;
    }

    @Transactional
    public RegistrarAceptacionResponse registrarAceptaciones(
            RegistrarAceptacionRequest request,
            String ipOrigen
    ) {
        validarRequest(request);

        String registroInternoProceso =
                request
                        .getRegistroInternoProceso()
                        .trim();

        /*
         * Primero se valida que el proceso al cual
         * se pretende asociar la aceptación exista.
         */
        ProcesoVida procesoVida =
                procesoVidaRepository
                        .buscarPorRegistroInternoProceso(
                                registroInternoProceso
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No se encontró el proceso +Vida indicado."
                                )
                        );

        /*
         * La identidad que acepta debe corresponder
         * al titular del trámite persistido.
         */
        if (
                !request
                        .getTipoDocumentoTrabajador()
                        .trim()
                        .equals(
                                procesoVida
                                        .getCodigoDocumentoTitular()
                        )
                        ||
                        !request
                                .getNumeroDocumentoTrabajador()
                                .trim()
                                .equals(
                                        procesoVida
                                                .getNumeroDocumentoTitular()
                                )
        ) {
            throw new IllegalArgumentException(
                    "Los datos del trabajador no corresponden al titular del proceso +Vida."
            );
        }

        LocalDateTime fechaHoraServidor =
                LocalDateTime.now();

        AceptacionLegal aceptacionLegal =
                new AceptacionLegal();

        aceptacionLegal.setIdAceptacion(
                generarIdAceptacion()
        );

        aceptacionLegal.setRegistroInternoProceso(
                registroInternoProceso
        );

        aceptacionLegal.setTipoDocumentoTrabajador(
                request
                        .getTipoDocumentoTrabajador()
                        .trim()
        );

        aceptacionLegal.setNumeroDocumentoTrabajador(
                request
                        .getNumeroDocumentoTrabajador()
                        .trim()
        );

        aceptacionLegal.setNombresApellidosTrabajador(
                request
                        .getNombresApellidosTrabajador()
                        .trim()
        );

        aceptacionLegal.setAceptaDeclaracionJurada(
                request.isAceptaDeclaracionJurada()
        );

        aceptacionLegal
                .setFechaHoraAceptacionDeclaracionJurada(
                        fechaHoraServidor
                );

        aceptacionLegal
                .setAceptaTratamientoDatosPersonales(
                        request
                                .isAceptaTratamientoDatosPersonales()
                );

        aceptacionLegal
                .setFechaHoraAceptacionTratamientoDatosPersonales(
                        fechaHoraServidor
                );

        aceptacionLegal.setIpOrigen(
                ipOrigen
        );

        aceptacionLegal.setCanalAcceso(
                valorPorDefecto(
                        request.getCanalAcceso(),
                        "SOMOS EsSalud"
                )
        );

        aceptacionLegal.setDatosSesionDispositivo(
                valorPorDefecto(
                        request.getDatosSesionDispositivo(),
                        "Sesión local / dispositivo no informado"
                )
        );

        aceptacionLegal.setVersionTextoDeclaracionJurada(
                request.getVersionTextoDeclaracionJurada()
        );

        aceptacionLegal.setVersionTextoTratamientoDatos(
                request.getVersionTextoTratamientoDatos()
        );

        aceptacionLegal.setReferenciaPoliticaPrivacidad(
                request.getReferenciaPoliticaPrivacidad()
        );

        /*
         * JdbcAceptacionRepository es idempotente:
         * si ya existe aceptación para este
         * ID_SECOMASVIDA, devuelve la existente.
         */
        AceptacionLegal aceptacionGuardada =
                aceptacionRepository.guardar(
                        aceptacionLegal
                );

        /*
         * Solo después de tener la aceptación
         * correctamente persistida se permite
         * avanzar a DOCUMENTOS.
         *
         * actualizarEstado() ya tiene protección
         * contra retroceso.
         */
        String tipoFlujoInicial =
                beneficiarioVidaRepository
                        .listarPorIdSecomasvida(
                                procesoVida.getIdSecomasvida()
                        )
                        .isEmpty()
                        ? TIPO_FLUJO_SOLO_AUTORIZACION
                        : TIPO_FLUJO_COMPLETO;

        /*
         * El flujo inicial se determina utilizando
         * los beneficiarios ya persistidos.
         *
         * El repositorio solamente lo escribe cuando
         * TIPO_FLUJO todavía es NULL, de modo que un
         * reintento de aceptación no sobrescriba un
         * flujo previamente definido.
         */
        procesoVidaRepository
                .actualizarTipoFlujoInicialYEstado(
                        registroInternoProceso,
                        tipoFlujoInicial,
                        ESTADO_DESPUES_DECLARACION
                );

        return convertirAResponse(
                aceptacionGuardada
        );
    }

    private void validarRequest(RegistrarAceptacionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de aceptación no puede estar vacía.");
        }

        if (campoVacio(request.getRegistroInternoProceso())) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(request.getTipoDocumentoTrabajador())) {
            throw new IllegalArgumentException("El tipo de documento del trabajador es obligatorio.");
        }

        if (campoVacio(request.getNumeroDocumentoTrabajador())) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        if (campoVacio(request.getNombresApellidosTrabajador())) {
            throw new IllegalArgumentException("Los nombres y apellidos del trabajador son obligatorios.");
        }

        if (!request.isAceptaDeclaracionJurada()) {
            throw new IllegalArgumentException("Debe aceptar los términos de la Declaración Jurada.");
        }

        if (!request.isAceptaTratamientoDatosPersonales()) {
            throw new IllegalArgumentException("Debe otorgar el consentimiento para el tratamiento de datos personales.");
        }

        if (campoVacio(request.getVersionTextoDeclaracionJurada())) {
            throw new IllegalArgumentException("La versión del texto de Declaración Jurada es obligatoria.");
        }

        if (campoVacio(request.getVersionTextoTratamientoDatos())) {
            throw new IllegalArgumentException("La versión del texto de tratamiento de datos personales es obligatoria.");
        }

        if (campoVacio(request.getReferenciaPoliticaPrivacidad())) {
            throw new IllegalArgumentException("La referencia de la Política de Privacidad es obligatoria.");
        }
    }

    private RegistrarAceptacionResponse convertirAResponse(AceptacionLegal aceptacionLegal) {
        RegistrarAceptacionResponse response = new RegistrarAceptacionResponse();

        response.setIdAceptacion(aceptacionLegal.getIdAceptacion());
        response.setRegistroInternoProceso(aceptacionLegal.getRegistroInternoProceso());

        response.setTipoDocumentoTrabajador(aceptacionLegal.getTipoDocumentoTrabajador());
        response.setNumeroDocumentoTrabajador(aceptacionLegal.getNumeroDocumentoTrabajador());
        response.setNombresApellidosTrabajador(aceptacionLegal.getNombresApellidosTrabajador());

        response.setAceptaDeclaracionJurada(aceptacionLegal.isAceptaDeclaracionJurada());
        response.setFechaHoraAceptacionDeclaracionJurada(
                aceptacionLegal.getFechaHoraAceptacionDeclaracionJurada()
        );

        response.setAceptaTratamientoDatosPersonales(
                aceptacionLegal.isAceptaTratamientoDatosPersonales()
        );
        response.setFechaHoraAceptacionTratamientoDatosPersonales(
                aceptacionLegal.getFechaHoraAceptacionTratamientoDatosPersonales()
        );

        response.setIpOrigen(aceptacionLegal.getIpOrigen());
        response.setCanalAcceso(aceptacionLegal.getCanalAcceso());
        response.setDatosSesionDispositivo(aceptacionLegal.getDatosSesionDispositivo());

        response.setVersionTextoDeclaracionJurada(
                aceptacionLegal.getVersionTextoDeclaracionJurada()
        );
        response.setVersionTextoTratamientoDatos(
                aceptacionLegal.getVersionTextoTratamientoDatos()
        );
        response.setReferenciaPoliticaPrivacidad(
                aceptacionLegal.getReferenciaPoliticaPrivacidad()
        );

        return response;
    }

    private String generarIdAceptacion() {
        return "ACEP-" + UUID.randomUUID();
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String valorPorDefecto(String valor, String valorDefecto) {
        if (campoVacio(valor)) {
            return valorDefecto;
        }

        return valor;
    }
}