package essalud.gob.pe.wsseguroscomplementario.entrega.service;

import essalud.gob.pe.wsseguroscomplementario.common.util.HashUtil;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.ConsultarEntregaPublicaResponse;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.SolicitarOtpEntregaResponse;
import essalud.gob.pe.wsseguroscomplementario.entrega.exception.EstadoEntregaException;
import essalud.gob.pe.wsseguroscomplementario.entrega.model.EntregaLote;
import essalud.gob.pe.wsseguroscomplementario.entrega.repository.EntregaLoteRepository;
import essalud.gob.pe.wsseguroscomplementario.otp.dto.SolicitarOtpResponse;
import essalud.gob.pe.wsseguroscomplementario.otp.service.OtpIntegracionService;
import org.springframework.stereotype.Service;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.ValidarOtpEntregaRequest;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.ValidarOtpEntregaResponse;
import essalud.gob.pe.wsseguroscomplementario.otp.dto.ValidarOtpExternoResponse;
import java.nio.charset.StandardCharsets;
import essalud.gob.pe.wsseguroscomplementario.entrega.dto.ConfirmarAcuseEntregaResponse;
@Service
public class EntregaLoteService {

    private static final String
            ESTADO_LOTE_NO_PUBLICADO =
            "LOTE_NO_PUBLICADO";

    private static final String
            ESTADO_PENDIENTE_RECEPCION =
            "PENDIENTE_RECEPCION";

    private static final String
            ESTADO_OTP_VALIDADO =
            "OTP_VALIDADO";

    private static final String
            ESTADO_ACUSE_REGISTRADO =
            "ACUSE_REGISTRADO";
    private static final String
            TEXTO_ACUSE_V1 =
            "Confirmo la recepción de la notificación y que el lote +Vida correspondiente al período indicado se encuentra disponible para su consulta.";

    private static final String
            VERSION_TEXTO_ACUSE_V1 =
            "V1";

    /*
     * Contrato institucional OTP ya comprobado
     * por la integración existente:
     *
     * 0 = OTP nuevo generado.
     * 1 = ya existe OTP activo.
     */
    private static final String
            CODIGO_OTP_NUEVO =
            "0";

    private static final String
            CODIGO_OTP_ACTIVO =
            "1";

    private final EntregaLoteRepository
            entregaLoteRepository;

    private final OtpIntegracionService
            otpIntegracionService;

    private final EntregaLoteTransicionService
            entregaLoteTransicionService;

    public EntregaLoteService(
            EntregaLoteRepository entregaLoteRepository,
            OtpIntegracionService otpIntegracionService,
            EntregaLoteTransicionService
                    entregaLoteTransicionService
    ) {
        this.entregaLoteRepository =
                entregaLoteRepository;

        this.otpIntegracionService =
                otpIntegracionService;

        this.entregaLoteTransicionService =
                entregaLoteTransicionService;
    }

    public ConsultarEntregaPublicaResponse
    consultarPorToken(
            String token
    ) {

        EntregaLote entrega =
                resolverEntregaPorToken(
                        token
                );

        return convertirAResponse(
                entrega
        );
    }

    public SolicitarOtpEntregaResponse
    solicitarOtpPorToken(
            String token
    ) {

        /*
         * El correo JAMAS viene del cliente.
         *
         * El token resuelve la entrega y la entrega
         * determina el correo autorizado persistido
         * en Oracle.
         */
        EntregaLote entrega =
                resolverEntregaPorToken(
                        token
                );

        if (
                entrega.getFechaPublicacion()
                        == null
        ) {

            throw new EstadoEntregaException(
                    "El lote todavía no se encuentra publicado."
            );
        }

        if (
                entrega.getFechaAcuse()
                        != null
        ) {

            throw new EstadoEntregaException(
                    "La recepción de esta entrega ya fue registrada."
            );
        }

        if (
                campoVacio(
                        entrega.getCorreoDestinatario()
                )
        ) {

            throw new IllegalStateException(
                    "La entrega no tiene un correo autorizado disponible para OTP."
            );
        }

        SolicitarOtpEntregaResponse response =
                new SolicitarOtpEntregaResponse();

        response.setCorreoEnmascarado(
                enmascararCorreo(
                        entrega.getCorreoDestinatario()
                )
        );

        /*
         * Si el OTP ya fue validado anteriormente,
         * no corresponde generar ni enviar otro.
         *
         * Esto permite conservar el estado después
         * de F5 sin depender del frontend.
         */
        if (
                entrega.getFechaOtpValidado()
                        != null
        ) {

            response.setOtpDisponible(
                    true
            );

            response.setNuevoOtpGenerado(
                    false
            );

            response.setCorreoEnviado(
                    false
            );

            response.setOtpYaValidado(
                    true
            );

            response.setMensaje(
                    "La identidad ya fue validada mediante OTP para esta entrega."
            );

            return response;
        }

        SolicitarOtpResponse resultadoOtp =
                otpIntegracionService
                        .solicitarOtp(
                                entrega
                                        .getCorreoDestinatario()
                                        .trim()
                        );

        if (resultadoOtp == null) {

            throw new IllegalStateException(
                    "La integración OTP respondió sin resultado."
            );
        }

        String codigoGeneracion =
                resultadoOtp
                        .getCodResultadoGeneracion();

        /*
         * Caso 0:
         * se creó un OTP nuevo y la integración
         * existente ya se encargó de enviarlo.
         */
        if (
                CODIGO_OTP_NUEVO
                        .equals(
                                codigoGeneracion
                        )
        ) {

            if (
                    !resultadoOtp.isOtpGenerado()
                            ||
                            !resultadoOtp.isCorreoEnviado()
            ) {

                throw new IllegalStateException(
                        "La integración OTP no confirmó correctamente la generación y envío."
                );
            }

            response.setOtpDisponible(
                    true
            );

            response.setNuevoOtpGenerado(
                    true
            );

            response.setCorreoEnviado(
                    true
            );

            response.setOtpYaValidado(
                    false
            );

            response.setMensaje(
                    "Se envió un código OTP al correo autorizado."
            );

            return response;
        }

        /*
         * Caso 1:
         * el servicio institucional informa que
         * todavía existe un OTP activo.
         *
         * No generamos uno nuevo y tampoco
         * duplicamos el envío.
         */
        if (
                CODIGO_OTP_ACTIVO
                        .equals(
                                codigoGeneracion
                        )
        ) {

            response.setOtpDisponible(
                    true
            );

            response.setNuevoOtpGenerado(
                    false
            );

            response.setCorreoEnviado(
                    false
            );

            response.setOtpYaValidado(
                    false
            );

            response.setMensaje(
                    "Ya existe un código OTP activo. Utilice el código enviado previamente."
            );

            return response;
        }

        throw new IllegalStateException(
                "El servicio institucional no permitió generar un código OTP."
        );
    }

    public ValidarOtpEntregaResponse validarOtpPorToken(
            String token,
            ValidarOtpEntregaRequest request,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La solicitud de validación OTP es obligatoria."
            );
        }

        if (
                campoVacio(
                        request.getCodigo()
                )
        ) {

            throw new IllegalArgumentException(
                    "El código OTP es obligatorio."
            );
        }

        validarToken(
                token
        );

        String tokenHash =
                calcularTokenHash(
                        token
                );

        EntregaLote entrega =
                entregaLoteRepository
                        .buscarPorTokenHash(
                                tokenHash
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La entrega solicitada no existe."
                                        )
                        );

        if (
                entrega.getFechaPublicacion()
                        == null
        ) {

            throw new EstadoEntregaException(
                    "El lote todavía no se encuentra publicado."
            );
        }

        /*
         * Si el acuse ya existe, el flujo terminó.
         */
        if (
                entrega.getFechaAcuse()
                        != null
        ) {

            throw new EstadoEntregaException(
                    "La recepción de esta entrega ya fue registrada."
            );
        }

        ValidarOtpEntregaResponse response =
                new ValidarOtpEntregaResponse();

        response.setCorreoEnmascarado(
                enmascararCorreo(
                        entrega.getCorreoDestinatario()
                )
        );

        /*
         * Idempotencia funcional:
         *
         * si esta entrega ya fue validada antes,
         * Oracle es la fuente de verdad.
         *
         * No volvemos a consumir el OTP institucional.
         */
        if (
                entrega.getFechaOtpValidado()
                        != null
        ) {

            response.setOtpValidado(
                    true
            );

            response.setYaValidado(
                    true
            );

            response.setMensaje(
                    "La identidad ya fue validada mediante OTP para esta entrega."
            );

            return response;
        }

        if (
                campoVacio(
                        entrega.getCorreoDestinatario()
                )
        ) {

            throw new IllegalStateException(
                    "La entrega no tiene un correo autorizado disponible para validar OTP."
            );
        }

        ValidarOtpExternoResponse resultadoOtp;

        try {

            resultadoOtp =
                    otpIntegracionService
                            .validarOtp(
                                    entrega
                                            .getCorreoDestinatario()
                                            .trim(),

                                    request
                                            .getCodigo()
                                            .trim()
                            );

        } catch (
                IllegalArgumentException e
        ) {

            /*
             * El código ya fue validado localmente
             * como no vacío.
             *
             * Un IllegalArgumentException en este punto
             * corresponde a un problema con los datos
             * usados para la integración y no debe
             * hacerse pasar por "entrega inexistente".
             */
            throw new IllegalStateException(
                    "No fue posible ejecutar la validación institucional del OTP.",
                    e
            );
        }

        if (resultadoOtp == null) {

            throw new IllegalStateException(
                    "La integración OTP respondió sin resultado."
            );
        }

        /*
         * OTP inválido es un resultado funcional.
         *
         * NO se escribe nada en Oracle.
         */
        if (
                !resultadoOtp.isValido()
        ) {

            /*
             * Revisión defensiva para un eventual
             * reintento concurrente que sí hubiera
             * alcanzado a persistir la validación.
             */
            EntregaLote estadoActual =
                    entregaLoteRepository
                            .buscarPorTokenHash(
                                    tokenHash
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "La entrega dejó de estar disponible durante la validación OTP."
                                            )
                            );

            if (
                    estadoActual
                            .getFechaOtpValidado()
                            != null
            ) {

                response.setOtpValidado(
                        true
                );

                response.setYaValidado(
                        true
                );

                response.setMensaje(
                        "La identidad ya fue validada mediante OTP para esta entrega."
                );

                return response;
            }

            response.setOtpValidado(
                    false
            );

            response.setYaValidado(
                    false
            );

            response.setMensaje(
                    "El código OTP ingresado no es válido."
            );

            return response;
        }

        /*
         * OTP institucional válido.
         *
         * Persistimos únicamente la evidencia temporal.
         * Nunca el código OTP.
         */
        boolean actualizado =
                entregaLoteTransicionService
                        .marcarOtpValidadoConHistorial(
                                tokenHash,

                                limitarNullable(
                                        ipOrigen,
                                        64
                                ),

                                limitarNullable(
                                        datosSesionDispositivo,
                                        1000
                                )
                        );

        if (!actualizado) {

            /*
             * Puede ser un reintento concurrente.
             *
             * Se consulta Oracle antes de concluir
             * que ocurrió un problema.
             */
            EntregaLote estadoActual =
                    entregaLoteRepository
                            .buscarPorTokenHash(
                                    tokenHash
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "La entrega no pudo recuperarse después de validar el OTP."
                                            )
                            );

            if (
                    estadoActual
                            .getFechaOtpValidado()
                            != null
            ) {

                response.setOtpValidado(
                        true
                );

                response.setYaValidado(
                        true
                );

                response.setMensaje(
                        "La identidad ya fue validada mediante OTP para esta entrega."
                );

                return response;
            }

            throw new IllegalStateException(
                    "El OTP fue validado, pero no fue posible registrar la evidencia en Oracle."
            );
        }

        response.setOtpValidado(
                true
        );

        response.setYaValidado(
                false
        );

        response.setMensaje(
                "Código OTP validado correctamente."
        );

        return response;
    }

    public ConfirmarAcuseEntregaResponse
    confirmarAcusePorToken(
            String token,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        validarToken(
                token
        );

        String tokenHash =
                calcularTokenHash(
                        token
                );

        EntregaLote entrega =
                entregaLoteRepository
                        .buscarPorTokenHash(
                                tokenHash
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La entrega solicitada no existe."
                                        )
                        );

        if (
                entrega.getFechaPublicacion()
                        == null
        ) {

            throw new EstadoEntregaException(
                    "El lote todavía no se encuentra publicado."
            );
        }

        /*
         * Idempotencia funcional:
         *
         * si el acuse ya existe, no modificamos nada.
         * Devolvemos exactamente la evidencia
         * previamente registrada.
         */
        if (
                entrega.getFechaAcuse()
                        != null
        ) {

            return construirRespuestaAcuse(
                    entrega,
                    true
            );
        }

        /*
         * El acuse JAMÁS puede preceder
         * a la validación OTP.
         */
        if (
                entrega.getFechaOtpValidado()
                        == null
        ) {

            throw new EstadoEntregaException(
                    "Debe validar el código OTP antes de confirmar la recepción."
            );
        }

        boolean actualizado =
                entregaLoteTransicionService
                        .registrarAcuseConHistorial(
                                tokenHash,

                                TEXTO_ACUSE_V1,

                                VERSION_TEXTO_ACUSE_V1,

                                limitarNullable(
                                        ipOrigen,
                                        64
                                ),

                                limitarNullable(
                                        datosSesionDispositivo,
                                        1000
                                )
                        );

        /*
         * Si otro request ganó la carrera,
         * recuperamos Oracle y devolvemos
         * el acuse existente.
         */
        if (!actualizado) {

            EntregaLote estadoActual =
                    entregaLoteRepository
                            .buscarPorTokenHash(
                                    tokenHash
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "La entrega dejó de estar disponible durante el registro del acuse."
                                            )
                            );

            if (
                    estadoActual
                            .getFechaAcuse()
                            != null
            ) {

                return construirRespuestaAcuse(
                        estadoActual,
                        true
                );
            }

            if (
                    estadoActual
                            .getFechaOtpValidado()
                            == null
            ) {

                throw new EstadoEntregaException(
                        "Debe validar el código OTP antes de confirmar la recepción."
                );
            }

            if (
                    estadoActual
                            .getFechaPublicacion()
                            == null
            ) {

                throw new EstadoEntregaException(
                        "El lote todavía no se encuentra publicado."
                );
            }

            throw new IllegalStateException(
                    "No fue posible registrar el acuse de recepción."
            );
        }

        /*
         * Recuperamos el valor escrito por Oracle
         * para devolver la fecha real SYSTIMESTAMP,
         * no una fecha fabricada desde Java.
         */
        EntregaLote entregaRegistrada =
                entregaLoteRepository
                        .buscarPorTokenHash(
                                tokenHash
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "El acuse fue registrado, pero la entrega no pudo recuperarse desde Oracle."
                                        )
                        );

        if (
                entregaRegistrada
                        .getFechaAcuse()
                        == null
        ) {

            throw new IllegalStateException(
                    "Oracle no confirmó la persistencia del acuse de recepción."
            );
        }

        return construirRespuestaAcuse(
                entregaRegistrada,
                false
        );
    }

    private ConfirmarAcuseEntregaResponse
    construirRespuestaAcuse(
            EntregaLote entrega,
            boolean yaRegistrado
    ) {

        ConfirmarAcuseEntregaResponse response =
                new ConfirmarAcuseEntregaResponse();

        boolean accesoDisponible =
                entrega.getFechaAcuse()
                        != null
                        &&
                        !campoVacio(
                                entrega.getUrlAcceso()
                        );

        response.setAcuseRegistrado(
                entrega.getFechaAcuse()
                        != null
        );

        response.setYaRegistrado(
                yaRegistrado
        );

        response.setFechaAcuse(
                entrega.getFechaAcuse()
        );

        response.setTextoAcuse(
                entrega.getTextoAcuse()
        );

        response.setVersionTextoAcuse(
                entrega.getVersionTextoAcuse()
        );

        response.setAccesoDisponible(
                accesoDisponible
        );

        response.setUrlAcceso(
                accesoDisponible
                        ? entrega
                        .getUrlAcceso()
                        .trim()
                        : null
        );

        return response;
    }

    private EntregaLote resolverEntregaPorToken(
            String token
    ) {

        validarToken(
                token
        );

        String tokenHash =
                calcularTokenHash(
                        token
                );

        return entregaLoteRepository
                .buscarPorTokenHash(
                        tokenHash
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "La entrega solicitada no existe."
                                )
                );
    }

    private String calcularTokenHash(
            String token
    ) {

        return HashUtil.calcularSha256(
                token
                        .trim()
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        );
    }

    private ConsultarEntregaPublicaResponse
    convertirAResponse(
            EntregaLote entrega
    ) {

        ConsultarEntregaPublicaResponse response =
                new ConsultarEntregaPublicaResponse();

        boolean lotePublicado =
                entrega.getFechaPublicacion()
                        != null;

        boolean otpValidado =
                entrega.getFechaOtpValidado()
                        != null;

        boolean acuseRegistrado =
                entrega.getFechaAcuse()
                        != null;

        String textoAcusePublico;
        String versionTextoAcusePublico;

        if (
                acuseRegistrado
                        &&
                        !campoVacio(
                                entrega.getTextoAcuse()
                        )
                        &&
                        !campoVacio(
                                entrega.getVersionTextoAcuse()
                        )
        ) {

            /*
             * Si ya existe acuse, devolvemos exactamente
             * la evidencia histórica persistida.
             */
            textoAcusePublico =
                    entrega.getTextoAcuse();

            versionTextoAcusePublico =
                    entrega.getVersionTextoAcuse();

        } else {

            /*
             * Si todavía no existe acuse, exponemos
             * el texto vigente que será registrado
             * al confirmar.
             */
            textoAcusePublico =
                    TEXTO_ACUSE_V1;

            versionTextoAcusePublico =
                    VERSION_TEXTO_ACUSE_V1;
        }

        boolean accesoDisponible =
                acuseRegistrado
                        &&
                        !campoVacio(
                                entrega.getUrlAcceso()
                        );

        response.setDestinatario(
                entrega.getTipoDestinatario()
        );

        response.setFechaInicioPeriodo(
                entrega.getFechaInicioPeriodo()
        );

        response.setFechaFinPeriodo(
                entrega.getFechaFinPeriodo()
        );

        response.setFechaPublicacion(
                entrega.getFechaPublicacion()
        );

        response.setCantidadDocumentos(
                entrega.getCantidadDocumentos()
        );

        response.setCorreoEnmascarado(
                enmascararCorreo(
                        entrega.getCorreoDestinatario()
                )
        );

        response.setOtpValidado(
                otpValidado
        );

        response.setAcuseRegistrado(
                acuseRegistrado
        );

        response.setAccesoDisponible(
                accesoDisponible
        );

        response.setUrlAcceso(
                accesoDisponible
                        ? entrega
                        .getUrlAcceso()
                        .trim()
                        : null
        );

        response.setEstadoEntrega(
                determinarEstadoEntrega(
                        lotePublicado,
                        otpValidado,
                        acuseRegistrado
                )
        );

        response.setTextoAcuse(
                textoAcusePublico
        );

        response.setVersionTextoAcuse(
                versionTextoAcusePublico
        );

        response.setFechaAcuse(
                entrega.getFechaAcuse()
        );

        return response;
    }




    private String determinarEstadoEntrega(
            boolean lotePublicado,
            boolean otpValidado,
            boolean acuseRegistrado
    ) {

        if (!lotePublicado) {

            return ESTADO_LOTE_NO_PUBLICADO;
        }

        if (acuseRegistrado) {

            return ESTADO_ACUSE_REGISTRADO;
        }

        if (otpValidado) {

            return ESTADO_OTP_VALIDADO;
        }

        return ESTADO_PENDIENTE_RECEPCION;
    }

    private String enmascararCorreo(
            String correo
    ) {

        if (campoVacio(correo)) {

            return "***";
        }

        String correoNormalizado =
                correo.trim();

        int posicionArroba =
                correoNormalizado
                        .lastIndexOf('@');

        if (
                posicionArroba <= 0
                        ||
                        posicionArroba
                                == correoNormalizado.length() - 1
        ) {

            return "***";
        }

        String usuario =
                correoNormalizado.substring(
                        0,
                        posicionArroba
                );

        String dominio =
                correoNormalizado.substring(
                        posicionArroba + 1
                );

        return usuario.substring(
                0,
                1
        )
                + "*****@"
                + dominio;
    }

    private void validarToken(
            String token
    ) {

        if (campoVacio(token)) {

            throw new IllegalArgumentException(
                    "El token de entrega es obligatorio."
            );
        }
    }

    private boolean campoVacio(
            String valor
    ) {

        return valor == null
                ||
                valor.trim().isEmpty();
    }

    private String limitarNullable(
            String valor,
            int maximo
    ) {

        if (campoVacio(valor)) {

            return null;
        }

        String normalizado =
                valor.trim();

        if (
                normalizado.length()
                        <= maximo
        ) {

            return normalizado;
        }

        return normalizado.substring(
                0,
                maximo
        );
    }
}