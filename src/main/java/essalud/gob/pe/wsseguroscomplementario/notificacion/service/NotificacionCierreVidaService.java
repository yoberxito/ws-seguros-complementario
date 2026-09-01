package essalud.gob.pe.wsseguroscomplementario.notificacion.service;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoPublicadoRepository;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoSustentoRepository;
import essalud.gob.pe.wsseguroscomplementario.notificacion.dto.EnviarCorreoFinalExternoResponse;
import essalud.gob.pe.wsseguroscomplementario.notificacion.dto.EnviarCorreoFinalRequest;
import essalud.gob.pe.wsseguroscomplementario.notificacion.repository.CorreoFinalVidaRepository;
import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;
import essalud.gob.pe.wsseguroscomplementario.proceso.repository.ProcesoVidaRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificacionCierreVidaService {

    private static final String
            TIPO_FLUJO_COMPLETO =
            "COMPLETO";

    private static final String
            TIPO_FLUJO_SOLO_AUTORIZACION =
            "SOLO_AUTORIZACION";

    private static final String
            TIPO_FLUJO_FORMULARIO_6012_POSTERIOR =
            "FORMULARIO_6012_POSTERIOR";

    private static final String
            TIPO_DOCUMENTO_AUTORIZACION =
            "AUTORIZACION_DESCUENTO";

    private static final String
            TIPO_DOCUMENTO_FORMULARIO_6012 =
            "FORMULARIO_6012";

    private static final String
            DESCRIPCION_SEGURO =
            "Seguro Complementario +vida";

    private static final String
            NOMBRE_SEGURO =
            "5-Seguro Complementario +vida";

    private static final int
            MONTO_VIDA =
            5;

    private final ProcesoVidaRepository
            procesoVidaRepository;

    private final DocumentoPublicadoRepository
            documentoPublicadoRepository;

    private final DocumentoSustentoRepository
            documentoSustentoRepository;

    private final CorreoFinalVidaRepository
            correoFinalVidaRepository;

    private final CorreoFinalVidaService
            correoFinalVidaService;

    private final String
            destinatarioPrueba;

    public NotificacionCierreVidaService(
            ProcesoVidaRepository
                    procesoVidaRepository,

            DocumentoPublicadoRepository
                    documentoPublicadoRepository,

            DocumentoSustentoRepository
                    documentoSustentoRepository,

            CorreoFinalVidaRepository
                    correoFinalVidaRepository,

            CorreoFinalVidaService
                    correoFinalVidaService,

            @Value(
                    "${integraciones.correo-final.destinatario-prueba:}"
            )
            String destinatarioPrueba
    ) {

        this.procesoVidaRepository =
                procesoVidaRepository;

        this.documentoPublicadoRepository =
                documentoPublicadoRepository;

        this.documentoSustentoRepository =
                documentoSustentoRepository;

        this.correoFinalVidaRepository =
                correoFinalVidaRepository;

        this.correoFinalVidaService =
                correoFinalVidaService;

        this.destinatarioPrueba =
                destinatarioPrueba;
    }

    /**
     * Envía el correo correspondiente al ciclo
     * documental que acaba de completarse.
     *
     * true:
     * - el correo fue enviado ahora, o
     * - ya había sido enviado anteriormente.
     *
     * false:
     * - el ciclo todavía no tiene todos sus
     *   documentos publicados.
     */
    public boolean enviarSiCorresponde(
            String registroInternoProceso,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        validarRegistro(
                registroInternoProceso
        );

        String registro =
                registroInternoProceso.trim();

        ProcesoVida proceso =
                obtenerProceso(
                        registro
                );

        String cicloDocumental =
                normalizarTipoFlujo(
                        proceso.getTipoFlujo()
                );

        return procesarEnvioCiclo(
                proceso,
                cicloDocumental,
                usuarioResponsable,
                ipOrigen,
                datosSesionDispositivo
        );
    }

    public boolean reintentarCiclo(
            String registroInternoProceso,
            String cicloDocumental,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        validarRegistro(
                registroInternoProceso
        );

        String registro =
                registroInternoProceso.trim();

        String ciclo =
                normalizarTipoFlujo(
                        cicloDocumental
                );

        ProcesoVida proceso =
                obtenerProceso(
                        registro
                );

        if (
                correoFinalVidaRepository
                        .estaEnviado(
                                registro,
                                ciclo
                        )
        ) {
            return true;
        }

        if (
                !correoFinalVidaRepository
                        .estaEnError(
                                registro,
                                ciclo
                        )
        ) {

            throw new IllegalStateException(
                    "No existe un correo final en ERROR_ENVIO "
                            + "para el ciclo "
                            + ciclo
                            + "."
            );
        }

        return procesarEnvioCiclo(
                proceso,
                ciclo,
                usuarioResponsable,
                ipOrigen,
                datosSesionDispositivo
        );
    }

    private boolean procesarEnvioCiclo(
            ProcesoVida proceso,
            String cicloDocumental,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        String registro =
                requerido(
                        proceso
                                .getRegistroInternoProceso(),
                        "El registro interno del proceso es obligatorio."
                );

        if (
                !cicloDocumentalCompleto(
                        registro,
                        cicloDocumental
                )
        ) {
            return false;
        }

        if (
                correoFinalVidaRepository
                        .estaEnviado(
                                registro,
                                cicloDocumental
                        )
        ) {
            return true;
        }

        String numeroDocumentoTitular =
                requerido(
                        proceso
                                .getNumeroDocumentoTitular(),
                        "El número de documento del titular es obligatorio para el correo final."
                );

        String destinatario =
                resolverDestinatario(
                        proceso
                );

        boolean enviaFormulario =
                !TIPO_FLUJO_SOLO_AUTORIZACION
                        .equals(
                                cicloDocumental
                        );

        List<DocumentoPublicado>
                documentos =
                obtenerDocumentosDelCiclo(
                        registro,
                        numeroDocumentoTitular,
                        cicloDocumental
                );

        EnviarCorreoFinalRequest request =
                construirRequest(
                        proceso,
                        destinatario,
                        enviaFormulario
                );

        boolean reservado =
                correoFinalVidaRepository
                        .reservarEnvio(
                                registro,
                                cicloDocumental,
                                destinatario,
                                enviaFormulario,
                                documentos.size(),

                                limpiarAuditoria(
                                        usuarioResponsable
                                ),

                                limpiarAuditoria(
                                        ipOrigen
                                ),

                                limpiarAuditoria(
                                        datosSesionDispositivo
                                )
                        );

        if (!reservado) {

            if (
                    correoFinalVidaRepository
                            .estaEnviado(
                                    registro,
                                    cicloDocumental
                            )
            ) {
                return true;
            }

            throw new IllegalStateException(
                    "El correo final del ciclo "
                            + cicloDocumental
                            + " ya tiene un intento en curso y no puede duplicarse."
            );
        }

        EnviarCorreoFinalExternoResponse respuesta;

        try {

            respuesta =
                    correoFinalVidaService
                            .enviarCorreoFinal(
                                    request,
                                    documentos
                            );

        } catch (RuntimeException e) {

            /*
             * Aquí sabemos que el servicio externo
             * NO confirmó correctamente el envío.
             * Por eso sí es seguro permitir reintento.
             */
            try {

                correoFinalVidaRepository
                        .marcarError(
                                registro,
                                cicloDocumental,
                                obtenerMensajeError(e)
                        );

            } catch (RuntimeException errorPersistencia) {

                e.addSuppressed(
                        errorPersistencia
                );
            }

            throw e;
        }

        /*
         * Desde este punto el servicio externo ya
         * confirmó el envío.
         *
         * Si Oracle falla ahora, NO debemos marcar
         * ERROR_ENVIO porque eso permitiría duplicar
         * el correo.
         *
         * La fila permanece ENVIANDO para exigir
         * conciliación antes de cualquier reintento.
         */
        try {

            correoFinalVidaRepository
                    .marcarEnviado(
                            registro,
                            cicloDocumental,
                            respuesta
                                    .getCodigoResultado(),
                            respuesta
                                    .getMensaje()
                    );

        } catch (RuntimeException e) {

            throw new IllegalStateException(
                    "El servicio institucional confirmó el envío del correo, "
                            + "pero no fue posible registrar ENVIADO en Oracle. "
                            + "No debe reintentarse automáticamente.",
                    e
            );
        }

        return true;
    }

    private ProcesoVida obtenerProceso(
            String registroInternoProceso
    ) {

        return procesoVidaRepository
                .buscarPorRegistroInternoProceso(
                        registroInternoProceso
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "No se encontró el proceso +Vida para enviar el correo final."
                                )
                );
    }

    private boolean cicloDocumentalCompleto(
            String registroInternoProceso,
            String cicloDocumental
    ) {

        boolean autorizacionPublicada =
                documentoSustentoRepository
                        .estaPublicado(
                                registroInternoProceso,
                                TIPO_DOCUMENTO_AUTORIZACION
                        );

        boolean formulario6012Publicado =
                documentoSustentoRepository
                        .estaPublicado(
                                registroInternoProceso,
                                TIPO_DOCUMENTO_FORMULARIO_6012
                        );

        if (
                TIPO_FLUJO_COMPLETO.equals(
                        cicloDocumental
                )
        ) {

            return autorizacionPublicada
                    && formulario6012Publicado;
        }

        if (
                TIPO_FLUJO_SOLO_AUTORIZACION
                        .equals(
                                cicloDocumental
                        )
        ) {

            return autorizacionPublicada;
        }

        if (
                TIPO_FLUJO_FORMULARIO_6012_POSTERIOR
                        .equals(
                                cicloDocumental
                        )
        ) {

            return formulario6012Publicado;
        }

        throw new IllegalStateException(
                "Tipo de flujo +Vida no soportado para correo final: "
                        + cicloDocumental
        );
    }

    private List<DocumentoPublicado>
    obtenerDocumentosDelCiclo(
            String registroInternoProceso,
            String numeroDocumentoTitular,
            String cicloDocumental
    ) {

        List<DocumentoPublicado>
                publicados =
                documentoPublicadoRepository
                        .buscarPorProcesoYTrabajador(
                                registroInternoProceso,
                                numeroDocumentoTitular
                        );

        if (
                publicados == null
                        || publicados.isEmpty()
        ) {

            throw new IllegalStateException(
                    "No se encontraron PDFs publicados en el repositorio documental para enviar el correo final."
            );
        }

        List<DocumentoPublicado>
                seleccionados =
                new ArrayList<>();

        if (
                TIPO_FLUJO_COMPLETO.equals(
                        cicloDocumental
                )
        ) {

            seleccionados.add(
                    buscarDocumentoObligatorio(
                            publicados,
                            TIPO_DOCUMENTO_AUTORIZACION
                    )
            );

            seleccionados.add(
                    buscarDocumentoObligatorio(
                            publicados,
                            TIPO_DOCUMENTO_FORMULARIO_6012
                    )
            );

            return seleccionados;
        }

        if (
                TIPO_FLUJO_SOLO_AUTORIZACION
                        .equals(
                                cicloDocumental
                        )
        ) {

            seleccionados.add(
                    buscarDocumentoObligatorio(
                            publicados,
                            TIPO_DOCUMENTO_AUTORIZACION
                    )
            );

            return seleccionados;
        }

        if (
                TIPO_FLUJO_FORMULARIO_6012_POSTERIOR
                        .equals(
                                cicloDocumental
                        )
        ) {

            seleccionados.add(
                    buscarDocumentoObligatorio(
                            publicados,
                            TIPO_DOCUMENTO_FORMULARIO_6012
                    )
            );

            return seleccionados;
        }

        throw new IllegalStateException(
                "No existe una selección documental para el flujo "
                        + cicloDocumental
                        + "."
        );
    }

    private DocumentoPublicado
    buscarDocumentoObligatorio(
            List<DocumentoPublicado> documentos,
            String tipoDocumento
    ) {

        return documentos
                .stream()
                .filter(
                        documento ->
                                documento != null
                                        && documento
                                        .getTipoDocumento()
                                        != null
                                        && tipoDocumento
                                        .equalsIgnoreCase(
                                                documento
                                                        .getTipoDocumento()
                                                        .trim()
                                        )
                )
                .findFirst()
                .map(
                        documento -> {

                            validarDocumentoAdjunto(
                                    documento,
                                    tipoDocumento
                            );

                            return documento;
                        }
                )
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "No se encontró el PDF publicado obligatorio "
                                                + tipoDocumento
                                                + " para el correo final."
                                )
                );
    }

    private void validarDocumentoAdjunto(
            DocumentoPublicado documento,
            String tipoDocumento
    ) {

        if (
                documento
                        .getContenidoArchivo()
                        == null
                        || documento
                        .getContenidoArchivo()
                        .length == 0
        ) {

            throw new IllegalStateException(
                    "El PDF "
                            + tipoDocumento
                            + " existe como publicado, pero no contiene bytes recuperables para adjuntarlo al correo."
            );
        }

        if (
                campoVacio(
                        documento
                                .getNombreArchivo()
                )
        ) {

            throw new IllegalStateException(
                    "El PDF "
                            + tipoDocumento
                            + " no tiene nombre de archivo para adjuntarlo al correo."
            );
        }
    }

    private EnviarCorreoFinalRequest
    construirRequest(
            ProcesoVida proceso,
            String destinatario,
            boolean enviaFormulario
    ) {

        EnviarCorreoFinalRequest request =
                new EnviarCorreoFinalRequest();

        request.setCorreo(
                destinatario
        );

        request.setDescripcionSeguro(
                DESCRIPCION_SEGURO
        );

        request.setNombreSeguro(
                NOMBRE_SEGURO
        );

        request.setUsuario(
                requerido(
                        proceso
                                .getNumeroDocumentoTitular(),
                        "El documento del titular es obligatorio para construir el correo final."
                )
        );

        request.setNombreCompleto(
                construirNombreCompleto(
                        proceso
                )
        );

        request.setMontoVida(
                MONTO_VIDA
        );

        request.setEnviaFormulario(
                enviaFormulario
        );

        return request;
    }

    private String resolverDestinatario(
            ProcesoVida proceso
    ) {

        /*
         * QA/local:
         * si existe override, TODO correo
         * final va allí.
         *
         * Producción/integración:
         * dejando la propiedad vacía,
         * se utiliza el correo persistido
         * del titular.
         */
        if (
                !campoVacio(
                        destinatarioPrueba
                )
        ) {

            return destinatarioPrueba
                    .trim();
        }

        return requerido(
                proceso.getCorreo(),
                "El proceso no tiene correo del titular para enviar la notificación final."
        );
    }

    private String construirNombreCompleto(
            ProcesoVida proceso
    ) {

        List<String> partes =
                new ArrayList<>();

        agregarParte(
                partes,
                proceso
                        .getPrimerNombreTitular()
        );

        agregarParte(
                partes,
                proceso
                        .getSegundoNombreTitular()
        );

        agregarParte(
                partes,
                proceso
                        .getApellidoPaternoTitular()
        );

        agregarParte(
                partes,
                proceso
                        .getApellidoMaternoTitular()
        );

        if (partes.isEmpty()) {

            throw new IllegalStateException(
                    "El proceso no contiene el nombre del titular para construir el correo final."
            );
        }

        return String.join(
                " ",
                partes
        );
    }

    private void agregarParte(
            List<String> partes,
            String valor
    ) {

        if (!campoVacio(valor)) {
            partes.add(
                    valor.trim()
            );
        }
    }

    private String normalizarTipoFlujo(
            String tipoFlujo
    ) {

        String flujo =
                requerido(
                        tipoFlujo,
                        "El proceso +Vida no tiene TIPO_FLUJO definido."
                )
                        .toUpperCase();

        if (
                !TIPO_FLUJO_COMPLETO
                        .equals(flujo)
                        && !TIPO_FLUJO_SOLO_AUTORIZACION
                        .equals(flujo)
                        && !TIPO_FLUJO_FORMULARIO_6012_POSTERIOR
                        .equals(flujo)
        ) {

            throw new IllegalStateException(
                    "El tipo de flujo "
                            + flujo
                            + " no está soportado para el correo final."
            );
        }

        return flujo;
    }

    private void validarRegistro(
            String registroInternoProceso
    ) {

        if (
                campoVacio(
                        registroInternoProceso
                )
        ) {

            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio para enviar el correo final."
            );
        }
    }

    private String requerido(
            String valor,
            String mensaje
    ) {

        if (campoVacio(valor)) {
            throw new IllegalStateException(
                    mensaje
            );
        }

        return valor.trim();
    }

    private String limpiarAuditoria(
            String valor
    ) {

        return campoVacio(valor)
                ? null
                : valor.trim();
    }

    private String obtenerMensajeError(
            RuntimeException e
    ) {

        String mensaje =
                e.getMessage();

        if (campoVacio(mensaje)) {
            mensaje =
                    e.getClass()
                            .getSimpleName();
        }

        if (mensaje.length() <= 2000) {
            return mensaje;
        }

        return mensaje.substring(
                0,
                2000
        );
    }

    private boolean campoVacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }
}