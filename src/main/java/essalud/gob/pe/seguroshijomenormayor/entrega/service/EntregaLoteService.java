package essalud.gob.pe.seguroshijomenormayor.entrega.service;

import essalud.gob.pe.seguroshijomenormayor.common.util.HashUtil;
import essalud.gob.pe.seguroshijomenormayor.entrega.dto.ConfirmarAcuseEntregaResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.dto.ConsultarEntregaPublicaResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.exception.EstadoEntregaException;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.EntregaLote;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.PreparacionEntregaLote;
import essalud.gob.pe.seguroshijomenormayor.entrega.repository.EntregaLoteRepository;
import essalud.gob.pe.seguroshijomenormayor.lote.service.CierreDriveLoteVidaService;
import essalud.gob.pe.seguroshijomenormayor.lote.service.PublicacionDrivePersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

@Service("masVidaEntregaLoteService")
public class EntregaLoteService {

    private static final String
            ESTADO_LOTE_NO_PUBLICADO =
            "LOTE_NO_PUBLICADO";

    private static final String
            ESTADO_PENDIENTE_RECEPCION =
            "PENDIENTE_RECEPCION";

    private static final String
            ESTADO_ACUSE_REGISTRADO =
            "ACUSE_REGISTRADO";

    private static final String
            DESTINATARIO_PERSONAL =
            "PERSONAL";

    private static final String
            DESTINATARIO_MAPFRE =
            "MAPFRE";

    private static final String
            ESTADO_NOTIFICACION_PENDIENTE =
            "PENDIENTE";

    private static final String
            ESTADO_NOTIFICACION_ENVIANDO =
            "ENVIANDO";

    private static final String
            ESTADO_NOTIFICACION_ENVIADO =
            "ENVIADO";

    private static final String
            TEXTO_ACUSE_V1 =
            "Confirmo que el lote +Vida fue descargado y recibido correctamente.";

    private static final String
            VERSION_TEXTO_ACUSE_V1 =
            "V1";

    private final EntregaLoteRepository
            entregaLoteRepository;

    private final EntregaLoteTransicionService
            entregaLoteTransicionService;

    private final PublicacionDrivePersonalService
            publicacionDrivePersonalService;

    private final CierreDriveLoteVidaService
            cierreDriveLoteVidaService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Autowired
    public EntregaLoteService(
            EntregaLoteRepository entregaLoteRepository,
            EntregaLoteTransicionService
                    entregaLoteTransicionService,
            PublicacionDrivePersonalService
                    publicacionDrivePersonalService,
            CierreDriveLoteVidaService
                    cierreDriveLoteVidaService
    ) {
        this.entregaLoteRepository =
                entregaLoteRepository;

        this.entregaLoteTransicionService =
                entregaLoteTransicionService;

        this.publicacionDrivePersonalService =
                publicacionDrivePersonalService;

        this.cierreDriveLoteVidaService =
                cierreDriveLoteVidaService;
    }


    public EntregaLoteService(
            EntregaLoteRepository entregaLoteRepository,
            EntregaLoteTransicionService
                    entregaLoteTransicionService,
            PublicacionDrivePersonalService
                    publicacionDrivePersonalService
    ) {
        this(
                entregaLoteRepository,
                entregaLoteTransicionService,
                publicacionDrivePersonalService,
                null
        );
    }

    /*
     * Prepara una sola entrega por lote/destinatario.
     *
     * En un reintento previo al envÃ­o:
     * - conserva la misma fila;
     * - renueva TOKEN_HASH;
     * - devuelve un nuevo token pÃºblico.
     *
     * De esta forma un rerun no crea otra ENTREGA_LOTE.
     */
    @Transactional
    public PreparacionEntregaLote prepararEntrega(
            Long idLote,
            String tipoDestinatario,
            String correoDestinatario,
            int cantidadDocumentos,
            String urlAcceso
    ) {

        validarPreparacion(
                idLote,
                tipoDestinatario,
                correoDestinatario,
                cantidadDocumentos
        );

        String destino =
                normalizarDestinatario(
                        tipoDestinatario
                );

        Optional<EntregaLote> existente =
                entregaLoteRepository
                        .buscarPorLoteYDestinatario(
                                idLote,
                                destino
                        );

        if (existente.isPresent()) {

            EntregaLote entrega =
                    existente.get();

            /*
             * Si el flujo ya terminÃ³ o el enlace
             * ya fue notificado, no regeneramos token.
             */
            if (
                    entrega.getFechaAcuse() != null
                            ||
                            ESTADO_NOTIFICACION_ENVIADO
                                    .equals(
                                            entrega
                                                    .getEstadoNotificacion()
                                    )
            ) {

                return new PreparacionEntregaLote(
                        entrega,
                        null,
                        false,
                        false
                );
            }

            /*
             * ENVIANDO representa una operaciÃ³n que ya
             * comenzÃ³. No la pisamos silenciosamente.
             */
            if (
                    ESTADO_NOTIFICACION_ENVIANDO
                            .equals(
                                    entrega
                                            .getEstadoNotificacion()
                            )
            ) {

                throw new EstadoEntregaException(
                        "La entrega se encuentra actualmente en proceso de notificaciÃ³n."
                );
            }

            String tokenPublico =
                    generarTokenPublico();

            String tokenHash =
                    calcularTokenHash(
                            tokenPublico
                    );

            boolean actualizada =
                    entregaLoteRepository
                            .actualizarPreparacionPendiente(
                                    entrega.getIdEntrega(),
                                    correoDestinatario,
                                    tokenHash,
                                    cantidadDocumentos,
                                    urlAcceso
                            );

            if (!actualizada) {
                throw new IllegalStateException(
                        "No fue posible actualizar la preparaciÃ³n de la entrega existente."
                );
            }

            EntregaLote actualizadaOracle =
                    entregaLoteRepository
                            .buscarPorLoteYDestinatario(
                                    idLote,
                                    destino
                            )
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "La entrega actualizada no pudo recuperarse desde Oracle."
                                    )
                            );

            return new PreparacionEntregaLote(
                    actualizadaOracle,
                    tokenPublico,
                    false,
                    true
            );
        }

        String tokenPublico =
                generarTokenPublico();

        EntregaLote nueva =
                new EntregaLote();

        nueva.setCodEntrega(
                generarCodigoEntrega(
                        idLote,
                        destino
                )
        );

        nueva.setIdLote(idLote);
        nueva.setTipoDestinatario(destino);

        nueva.setCorreoDestinatario(
                correoDestinatario.trim()
        );

        nueva.setTokenHash(
                calcularTokenHash(
                        tokenPublico
                )
        );

        nueva.setCantidadDocumentos(
                cantidadDocumentos
        );

        nueva.setEstadoNotificacion(
                ESTADO_NOTIFICACION_PENDIENTE
        );

        nueva.setUrlAcceso(
                normalizarNullable(
                        urlAcceso
                )
        );

        EntregaLote creada =
                entregaLoteRepository
                        .crear(nueva);

        return new PreparacionEntregaLote(
                creada,
                tokenPublico,
                true,
                true
        );
    }

    public boolean marcarNotificacionEnviando(
            Long idEntrega
    ) {
        return entregaLoteRepository
                .marcarEnviando(idEntrega);
    }

    public boolean marcarNotificacionEnviada(
            Long idEntrega
    ) {
        return entregaLoteRepository
                .marcarEnviado(idEntrega);
    }

    public boolean marcarErrorNotificacion(
            Long idEntrega
    ) {
        return entregaLoteRepository
                .marcarErrorEnvio(idEntrega);
    }

    public ConsultarEntregaPublicaResponse
    consultarPorToken(
            String token
    ) {

        EntregaLote entrega =
                resolverEntregaPorToken(token);

        String tokenHash =
                calcularTokenHash(token);

        return convertirAResponse(
                entrega,
                tokenHash
        );
    }

    public void registrarDescargaCompletadaPorToken(
            String token,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        validarToken(token);

        String tokenHash =
                calcularTokenHash(token);

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
                    "El lote todavÃ­a no se encuentra publicado."
            );
        }

        if (
                entrega.getFechaAcuse()
                        != null
        ) {
            throw new EstadoEntregaException(
                    "La recepciÃ³n del lote ya se encuentra registrada."
            );
        }

        entregaLoteTransicionService
                .registrarDescargaLoteCompletada(
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
    }

    public ConfirmarAcuseEntregaResponse
    confirmarAcusePorToken(
            String token,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        validarToken(token);

        String tokenHash =
                calcularTokenHash(token);

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
                    "El lote todavÃ­a no se encuentra publicado."
            );
        }

        /*
         * Idempotencia del acuse.
         */
        if (
                entrega.getFechaAcuse()
                        != null
        ) {

            asegurarPublicacionDrivePostAcuse(
                    entrega,
                    tokenHash,
                    ipOrigen,
                    datosSesionDispositivo
            );

            return construirRespuestaAcuse(
                    entrega,
                    tokenHash,
                    true
            );
        }

        if (
                !entregaLoteTransicionService
                        .existeDescargaLoteCompletada(
                                tokenHash
                        )
        ) {
            throw new EstadoEntregaException(
                    "Debe descargar completamente el lote antes de confirmar la recepciÃ³n."
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

        if (!actualizado) {

            EntregaLote estadoActual =
                    entregaLoteRepository
                            .buscarPorTokenHash(
                                    tokenHash
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "La entrega dejÃ³ de estar disponible durante el registro del acuse."
                                            )
                            );

            if (
                    estadoActual.getFechaAcuse()
                            != null
            ) {
                asegurarPublicacionDrivePostAcuse(
                        estadoActual,
                        tokenHash,
                        ipOrigen,
                        datosSesionDispositivo
                );

                return construirRespuestaAcuse(
                        estadoActual,
                        tokenHash,
                        true
                );
            }

            if (
                    estadoActual
                            .getFechaPublicacion()
                            == null
            ) {
                throw new EstadoEntregaException(
                        "El lote todavÃ­a no se encuentra publicado."
                );
            }

            throw new IllegalStateException(
                    "No fue posible registrar el acuse de recepciÃ³n."
            );
        }

        EntregaLote registrada =
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
                registrada.getFechaAcuse()
                        == null
        ) {
            throw new IllegalStateException(
                    "Oracle no confirmÃ³ la persistencia del acuse de recepciÃ³n."
            );
        }

        /*
         * IMPORTANTE:
         *
         * El acuse ya fue persistido antes de tocar Drive.
         *
         * Si Drive falla desde aqui, el acuse se conserva.
         * Un nuevo POST /confirmar detectara el acuse existente
         * y reintentara solamente la publicacion.
         */
        asegurarPublicacionDrivePostAcuse(
                registrada,
                tokenHash,
                ipOrigen,
                datosSesionDispositivo
        );

        return construirRespuestaAcuse(
                registrada,
                tokenHash,
                false
        );
    }

    private ConfirmarAcuseEntregaResponse
    construirRespuestaAcuse(
            EntregaLote entrega,
            String tokenHash,
            boolean yaRegistrado
    ) {

        ConfirmarAcuseEntregaResponse response =
                new ConfirmarAcuseEntregaResponse();

        boolean accesoDisponible =
                entrega.getFechaAcuse() != null
                        &&
                        !campoVacio(
                                entrega.getUrlAcceso()
                        )
                        &&
                        publicacionDisponible(
                                entrega,
                                tokenHash
                        );

        response.setAcuseRegistrado(
                entrega.getFechaAcuse() != null
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

        validarToken(token);

        String tokenHash =
                calcularTokenHash(token);

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

    private ConsultarEntregaPublicaResponse
    convertirAResponse(
            EntregaLote entrega,
            String tokenHash
    ) {

        ConsultarEntregaPublicaResponse response =
                new ConsultarEntregaPublicaResponse();

        boolean lotePublicado =
                entrega.getFechaPublicacion()
                        != null;

        boolean acuseRegistrado =
                entrega.getFechaAcuse()
                        != null;

        boolean descargaRegistrada =
                entregaLoteTransicionService
                        .existeDescargaLoteCompletada(
                                tokenHash
                        );

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
                                entrega
                                        .getVersionTextoAcuse()
                        )
        ) {

            textoAcusePublico =
                    entrega.getTextoAcuse();

            versionTextoAcusePublico =
                    entrega.getVersionTextoAcuse();

        } else {

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
                        )
                        &&
                        publicacionDisponible(
                                entrega,
                                tokenHash
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

        response.setAcuseRegistrado(
                acuseRegistrado
        );

        response.setDescargaRegistrada(
                descargaRegistrada
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

    private void asegurarPublicacionDrivePostAcuse(
            EntregaLote entrega,
            String tokenHash,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        if (
                !esPersonal(entrega)
                        &&
                !esMapfre(entrega)
        ) {
            throw new IllegalStateException(
                    "El destinatario de la entrega no puede publicarse en Drive."
            );
        }

        if (
                entrega.getFechaAcuse()
                        == null
        ) {
            throw new IllegalStateException(
                    "No se puede publicar Drive antes del acuse."
            );
        }

        /*
         * Compatibilidad con MAPFRE historico.
         *
         * Las entregas antiguas no registraban
         * DESCARGA_LOTE_COMPLETADA y ya estaban derivadas.
         */
        if (
                esMapfre(entrega)
                        &&
                !entregaLoteTransicionService
                        .existeDescargaLoteCompletada(
                                tokenHash
                        )
        ) {
            return;
        }

        if (
                entregaLoteTransicionService
                        .existePublicacionDriveCompletada(
                                tokenHash
                        )
        ) {
            return;
        }

        if (esPersonal(entrega)) {

            publicacionDrivePersonalService
                    .publicarPeriodoPersonal(
                            entrega.getUrlAcceso(),
                            entrega.getFechaInicioPeriodo(),
                            entrega.getFechaFinPeriodo()
                    );

        } else {

            String folderId =
                    extraerFolderIdDrive(
                            entrega.getUrlAcceso()
                    );

            if (cierreDriveLoteVidaService == null) {
                throw new IllegalStateException(
                        "El servicio de derivacion MAPFRE no se encuentra disponible."
                );
            }

            try {

                cierreDriveLoteVidaService
                        .cerrarCarpetaPeriodo(
                                DESTINATARIO_MAPFRE,
                                entrega.getFechaInicioPeriodo(),
                                entrega.getFechaFinPeriodo(),
                                folderId,
                                entrega.getCantidadDocumentos()
                        );

            } catch (IOException e) {

                throw new IllegalStateException(
                        "El acuse MAPFRE fue registrado, pero no fue posible derivar el lote hacia su carpeta final.",
                        e
                );
            }
        }

        entregaLoteTransicionService
                .registrarPublicacionDriveCompletada(
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

        if (
                !entregaLoteTransicionService
                        .existePublicacionDriveCompletada(
                                tokenHash
                        )
        ) {
            throw new IllegalStateException(
                    "La publicacion Drive termino, pero no pudo confirmarse su trazabilidad."
            );
        }
    }



    private boolean publicacionDisponible(
            EntregaLote entrega,
            String tokenHash
    ) {

        if (
                entregaLoteTransicionService
                        .existePublicacionDriveCompletada(
                                tokenHash
                        )
        ) {
            return true;
        }

        /*
         * Compatibilidad con MAPFRE historico:
         * antes del nuevo flujo no existia evento de descarga
         * ni evento de publicacion post-acuse.
         */
        if (
                esMapfre(entrega)
                        &&
                entrega.getFechaAcuse() != null
                        &&
                !entregaLoteTransicionService
                        .existeDescargaLoteCompletada(
                                tokenHash
                        )
        ) {
            return true;
        }

        return false;
    }


    private boolean esMapfre(
            EntregaLote entrega
    ) {

        return entrega != null
                &&
                entrega.getTipoDestinatario() != null
                &&
                DESTINATARIO_MAPFRE.equalsIgnoreCase(
                        entrega
                                .getTipoDestinatario()
                                .trim()
                );
    }


    private String extraerFolderIdDrive(
            String urlAcceso
    ) {

        if (campoVacio(urlAcceso)) {
            throw new IllegalStateException(
                    "La entrega no cuenta con URL_ACCESO Drive."
            );
        }

        final URI uri;

        try {

            uri =
                    URI.create(
                            urlAcceso.trim()
                    );

        } catch (IllegalArgumentException e) {

            throw new IllegalStateException(
                    "URL_ACCESO no contiene una URL Drive valida.",
                    e
            );
        }

        if (
                uri.getHost() == null
                        ||
                !"drive.google.com"
                        .equalsIgnoreCase(
                                uri.getHost()
                        )
        ) {
            throw new IllegalStateException(
                    "URL_ACCESO no pertenece a Google Drive."
            );
        }

        String path =
                uri.getPath();

        if (campoVacio(path)) {
            throw new IllegalStateException(
                    "URL_ACCESO no contiene una ruta Drive valida."
            );
        }

        String[] segmentos =
                path.split("/");

        for (
                int i = 0;
                i < segmentos.length - 1;
                i++
        ) {

            if (
                    "folders".equals(
                            segmentos[i]
                    )
            ) {

                String folderId =
                        segmentos[i + 1];

                if (
                        folderId != null
                                &&
                        folderId.matches(
                                "[A-Za-z0-9_-]+"
                        )
                ) {
                    return folderId;
                }
            }
        }

        throw new IllegalStateException(
                "No fue posible obtener el identificador de carpeta desde URL_ACCESO."
        );
    }



    private boolean esPersonal(
            EntregaLote entrega
    ) {

        return entrega != null
                &&
                entrega.getTipoDestinatario() != null
                &&
                DESTINATARIO_PERSONAL.equalsIgnoreCase(
                        entrega
                                .getTipoDestinatario()
                                .trim()
                );
    }

    private String determinarEstadoEntrega(
            boolean lotePublicado,
            boolean acuseRegistrado
    ) {

        if (!lotePublicado) {
            return ESTADO_LOTE_NO_PUBLICADO;
        }

        if (acuseRegistrado) {
            return ESTADO_ACUSE_REGISTRADO;
        }

        return ESTADO_PENDIENTE_RECEPCION;
    }

    private String generarCodigoEntrega(
            Long idLote,
            String destinatario
    ) {

        return "ENT-VIDA-"
                + idLote
                + "-"
                + destinatario;
    }

    private String generarTokenPublico() {

        byte[] bytes =
                new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
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

    private void validarPreparacion(
            Long idLote,
            String tipoDestinatario,
            String correoDestinatario,
            int cantidadDocumentos
    ) {

        if (idLote == null) {
            throw new IllegalArgumentException(
                    "El ID del lote es obligatorio."
            );
        }

        normalizarDestinatario(
                tipoDestinatario
        );

        if (campoVacio(correoDestinatario)) {
            throw new IllegalArgumentException(
                    "El correo destinatario es obligatorio."
            );
        }

        if (cantidadDocumentos < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de documentos no puede ser negativa."
            );
        }
    }

    private String normalizarDestinatario(
            String destinatario
    ) {

        if (campoVacio(destinatario)) {
            throw new IllegalArgumentException(
                    "El destinatario es obligatorio."
            );
        }

        String destino =
                destinatario
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                !"MAPFRE".equals(destino)
                        &&
                        !"PERSONAL".equals(destino)
        ) {
            throw new IllegalArgumentException(
                    "El destinatario debe ser MAPFRE o PERSONAL."
            );
        }

        return destino;
    }

    private String enmascararCorreo(
            String correo
    ) {

        if (campoVacio(correo)) {
            return "***";
        }

        String normalizado =
                correo.trim();

        int arroba =
                normalizado
                        .lastIndexOf('@');

        if (
                arroba <= 0
                        ||
                        arroba
                                == normalizado.length() - 1
        ) {
            return "***";
        }

        String usuario =
                normalizado.substring(
                        0,
                        arroba
                );

        String dominio =
                normalizado.substring(
                        arroba + 1
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

    private String normalizarNullable(
            String valor
    ) {

        return campoVacio(valor)
                ? null
                : valor.trim();
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

    private boolean campoVacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }
}
