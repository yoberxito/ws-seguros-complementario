package essalud.gob.pe.wsseguroscomplementario.documento.service;
import org.springframework.transaction.annotation.Transactional;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import essalud.gob.pe.wsseguroscomplementario.common.util.ByteArrayMultipartFile;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.PublicarDocumentoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.PublicarDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidarSelloEssaludResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoSellado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.TipoDocumentoDigital;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoPublicadoRepository;
import org.springframework.stereotype.Service;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoSustentoRepository;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import essalud.gob.pe.seguroshijomenormayor.dto.response.CargaArchivoRes;

@Service
public class PublicacionDocumentoService {

    private static final String ESTADO_DOCUMENTO_PUBLICADO = "DOCUMENTO_PUBLICADO";
    private static final String ESTADO_ERROR_PUBLICACION = "ERROR_PUBLICACION_DOCUMENTO";
    private static final String ESTADO_ERROR_SELLO_NO_VALIDADO = "ERROR_PUBLICACION_POR_SELLO_NO_VALIDADO";

    private static final ZoneId ZONA_HORARIA_LIMA = ZoneId.of("America/Lima");

    private final DocumentoPublicadoRepository documentoPublicadoRepository;
    private final ValidacionSelloEssaludService validacionSelloEssaludService;
    private final DocumentoSustentoRepository documentoSustentoRepository;
    private final GoogleDriveService googleDriveService;


    public PublicacionDocumentoService(
            DocumentoPublicadoRepository documentoPublicadoRepository,
            ValidacionSelloEssaludService validacionSelloEssaludService,
            DocumentoSustentoRepository documentoSustentoRepository,
            GoogleDriveService googleDriveService
    ) {
        this.documentoPublicadoRepository =
                documentoPublicadoRepository;

        this.validacionSelloEssaludService =
                validacionSelloEssaludService;

        this.documentoSustentoRepository =
                documentoSustentoRepository;

        this.googleDriveService =
                googleDriveService;
    }

    @Transactional
    public PublicarDocumentoResponse publicarDocumento(
            PublicarDocumentoRequest request,
            DocumentoSellado documentoSellado
    ) {
        validarRequest(request);

        if (documentoSellado == null) {
            throw new IllegalArgumentException(
                    "El documento sellado temporal es obligatorio para publicar."
            );
        }

        List<String> observacionesMetadata = validarMetadataDocumentoSellado(request, documentoSellado);

        if (!observacionesMetadata.isEmpty()) {
            return construirRespuestaNoPublicada(
                    request,
                    documentoSellado,
                    ESTADO_ERROR_PUBLICACION,
                    "No se pudo publicar el documento porque la metadata informada no coincide con el documento sellado.",
                    true,
                    false,
                    observacionesMetadata
            );
        }

        ValidarSelloEssaludResponse validacionSello =
                validarSelloAntesDePublicar(documentoSellado);

        if (!validacionSello.isSelloValido()) {
            return construirRespuestaNoPublicada(
                    request,
                    documentoSellado,
                    ESTADO_ERROR_SELLO_NO_VALIDADO,
                    "No se publica el documento porque no se pudo verificar el sello institucional.",
                    true,
                    false,
                    validacionSello.getObservaciones()
            );
        }

        if (
                documentoSustentoRepository.estaPublicado(
                        request.getRegistroInternoProceso(),
                        request.getTipoDocumento()
                )
        ) {

            DocumentoPublicado existente =
                    documentoPublicadoRepository
                            .buscarPorProcesoYTrabajador(
                                    request.getRegistroInternoProceso(),
                                    request.getNumeroDocumentoTrabajador()
                            )
                            .stream()
                            .filter(
                                    documento ->
                                            documento != null
                                                    && documento.getTipoDocumento() != null
                                                    && normalizar(
                                                            documento.getTipoDocumento()
                                                    ).equals(
                                                            normalizar(
                                                                    request.getTipoDocumento()
                                                            )
                                                    )
                            )
                            .findFirst()
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "Oracle indica que el documento ya esta PUBLICADO, pero no fue posible recuperar su registro documental."
                                    )
                            );

            return convertirDocumentoPublicadoAResponse(
                    existente,
                    true,
                    "El documento ya se encontraba publicado.",
                    false
            );
        }

        String idTpDoc =
                resolverIdTpDocDrive(
                        request.getTipoDocumento()
                );

        if (idTpDoc == null) {
            return construirRespuestaNoPublicada(
                    request,
                    documentoSellado,
                    ESTADO_ERROR_PUBLICACION,
                    "El tipo documental no cuenta con repositorio Drive configurado.",
                    true,
                    true,
                    List.of(
                            "No fue posible resolver idTpDoc para la publicacion oficial en Drive."
                    )
            );
        }

        String idDocumentoPublicado =
                generarIdDocumentoPublicado(
                        request
                );

        LocalDateTime fechaHoraPublicacion =
                LocalDateTime.now(
                        ZONA_HORARIA_LIMA
                );

        DocumentoPublicado documentoPublicado =
                new DocumentoPublicado();

        documentoPublicado.setIdDocumentoPublicado(
                idDocumentoPublicado
        );
        documentoPublicado.setIdDocumentoSellado(
                documentoSellado.getIdDocumentoSellado()
        );
        documentoPublicado.setRegistroInternoProceso(
                documentoSellado.getRegistroInternoProceso()
        );
        documentoPublicado.setTipoDocumento(
                documentoSellado.getTipoDocumento()
        );
        documentoPublicado.setNumeroDocumentoTrabajador(
                documentoSellado.getNumeroDocumentoTrabajador()
        );
        documentoPublicado.setNombreArchivo(
                documentoSellado.getNombreArchivo()
        );
        documentoPublicado.setContentType(
                documentoSellado.getContentType()
        );
        documentoPublicado.setContenidoArchivo(
                documentoSellado.getContenidoArchivo()
        );
        documentoPublicado.setHashSha256DocumentoPublicado(
                calcularSha256(
                        documentoSellado.getContenidoArchivo()
                )
        );
        documentoPublicado.setFechaHoraPublicacion(
                fechaHoraPublicacion
        );
        documentoPublicado.setCanalPublicacion(
                valorPorDefecto(
                        request.getCanalPublicacion(),
                        "SOMOS_ESSALUD"
                )
        );
        documentoPublicado.setPublicadoPor(
                valorPorDefecto(
                        request.getPublicadoPor(),
                        "SISTEMA"
                )
        );
        documentoPublicado.setEstadoPublicacionDocumental(
                ESTADO_DOCUMENTO_PUBLICADO
        );
        documentoPublicado.setDisponibleParaUsuario(
                true
        );

        /*
         * Drive es el repositorio documental oficial.
         *
         * Regla de consistencia:
         * primero Drive confirma la persistencia fisica;
         * solo despues Oracle puede registrar PUBLICADO.
         *
         * Si Oracle falla despues de Drive, el reintento es
         * idempotente porque guardarArchivoPublicado reutiliza
         * el archivo por identidad/nombre deterministico.
         */
        try {

            ByteArrayMultipartFile archivoDrive =
                    new ByteArrayMultipartFile(
                            "archivo",
                            documentoSellado.getNombreArchivo(),
                            documentoSellado.getContentType(),
                            documentoSellado.getContenidoArchivo()
                    );

            CargaArchivoRes resultadoDrive =
                    googleDriveService
                            .guardarArchivoPublicado(
                                    archivoDrive,
                                    idTpDoc,
                                    request.getTipoDocumentoTrabajador(),
                                    request.getNumeroDocumentoTrabajador(),
                                    documentoSellado.getNombreArchivo(),
                                    idDocumentoPublicado,
                                    fechaHoraPublicacion.toLocalDate()
                            );

            if (
                    resultadoDrive == null
                            || resultadoDrive.getArchivo() == null
                            || campoVacio(
                                    resultadoDrive
                                            .getArchivo()
                                            .nombreArchivo()
                            )
                            || campoVacio(
                                    resultadoDrive
                                            .getArchivo()
                                            .rutaArchivo()
                            )
            ) {
                throw new IllegalStateException(
                        "Google Drive no confirmo la persistencia del documento oficial."
                );
            }

            documentoPublicado.setNombreArchivo(
                    resultadoDrive
                            .getArchivo()
                            .nombreArchivo()
            );

            documentoPublicado.setRutaArchivo(
                    resultadoDrive
                            .getArchivo()
                            .rutaArchivo()
            );

        } catch (Exception e) {

            String detalle =
                    campoVacio(
                            e.getMessage()
                    )
                            ? "Error no especificado por Google Drive."
                            : e.getMessage();

            return construirRespuestaNoPublicada(
                    request,
                    documentoSellado,
                    ESTADO_ERROR_PUBLICACION,
                    "El documento fue sellado correctamente, pero no pudo ser almacenado en el repositorio oficial de Google Drive.",
                    true,
                    true,
                    List.of(
                            "No se pudo almacenar el documento en Google Drive: "
                                    + detalle
                    )
            );
        }

        DocumentoPublicado documentoGuardado =
                documentoPublicadoRepository
                        .guardar(
                                documentoPublicado
                        );

        /*
         * El repositorio simulado no persiste RUTA_ARCHIVO.
         * La URL oficial Drive se conserva para actualizar
         * DOCUMENTOS_SUSTENTO y construir la respuesta.
         */
        documentoGuardado.setRutaArchivo(
                documentoPublicado.getRutaArchivo()
        );

        documentoSustentoRepository
                .registrarPublicacion(
                        documentoGuardado
                );

        return convertirDocumentoPublicadoAResponse(
                documentoGuardado,
                true,
                "Documento almacenado en Google Drive y publicado correctamente.",
                false
        );
    }


    private String resolverIdTpDocDrive(
            String tipoDocumento
    ) {

        String tipo =
                normalizar(
                        tipoDocumento
                );

        if ("FORMULARIO_6012".equals(tipo)) {
            return "244";
        }

        if ("AUTORIZACION_DESCUENTO".equals(tipo)) {
            return "247";
        }

        return null;
    }

    public DocumentoPublicado obtenerDocumentoPublicado(String idDocumentoPublicado) {
        return documentoPublicadoRepository.buscarPorId(idDocumentoPublicado)
                .orElseThrow(() -> new IllegalArgumentException("No se encontrÃ³ el documento publicado solicitado."));
    }

    public List<PublicarDocumentoResponse> listarDocumentosPublicadosPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {
        return documentoPublicadoRepository
                .buscarPorProcesoYTrabajador(registroInternoProceso, numeroDocumentoTrabajador)
                .stream()
                .map(documentoPublicado ->
                        convertirDocumentoPublicadoAResponse(
                                documentoPublicado,
                                true,
                                "Documento publicado.",
                                false
                        )
                )
                .collect(Collectors.toList());
    }

    private ValidarSelloEssaludResponse validarSelloAntesDePublicar(
            DocumentoSellado documentoSellado
    ) {
        ByteArrayMultipartFile archivoSellado = new ByteArrayMultipartFile(
                "archivo",
                documentoSellado.getNombreArchivo(),
                documentoSellado.getContentType(),
                documentoSellado.getContenidoArchivo()
        );

        return validacionSelloEssaludService.validarSelloEssalud(
                archivoSellado,
                documentoSellado.getTipoDocumento()
        );
    }

    private List<String> validarMetadataDocumentoSellado(
            PublicarDocumentoRequest request,
            DocumentoSellado documentoSellado
    ) {

        List<String> observaciones =
                new ArrayList<>();

        if (
                !documentoSellado
                        .getIdDocumentoSellado()
                        .equalsIgnoreCase(
                                request
                                        .getIdDocumentoSellado()
                        )
        ) {
            observaciones.add(
                    "El identificador del documento sellado no coincide."
            );
        }

        if (
                !documentoSellado
                        .getRegistroInternoProceso()
                        .equalsIgnoreCase(
                                request
                                        .getRegistroInternoProceso()
                        )
        ) {
            observaciones.add(
                    "El registro interno del proceso no coincide con el documento sellado."
            );
        }

        if (
                !documentoSellado
                        .getTipoDocumento()
                        .equalsIgnoreCase(
                                request
                                        .getTipoDocumento()
                        )
        ) {
            observaciones.add(
                    "El tipo de documento no coincide con el documento sellado."
            );
        }

        if (
                !documentoSellado
                        .getNumeroDocumentoTrabajador()
                        .equalsIgnoreCase(
                                request
                                        .getNumeroDocumentoTrabajador()
                        )
        ) {
            observaciones.add(
                    "El nÃºmero de documento del trabajador no coincide con el documento sellado."
            );
        }

        return observaciones;
    }

    private PublicarDocumentoResponse construirRespuestaNoPublicada(
            PublicarDocumentoRequest request,
            DocumentoSellado documentoSellado,
            String estado,
            String mensaje,
            boolean requiereIntervencionInterna,
            boolean selloValidado,
            List<String> observaciones
    ) {
        PublicarDocumentoResponse response = new PublicarDocumentoResponse();

        response.setPublicado(false);
        response.setMensajePublicacion(mensaje);
        response.setEstadoPublicacionDocumental(estado);
        response.setDisponibleParaUsuario(false);
        response.setSelloValidadoAntesPublicacion(selloValidado);
        response.setRequiereIntervencionInterna(requiereIntervencionInterna);

        response.setIdDocumentoSellado(documentoSellado.getIdDocumentoSellado());
        response.setRegistroInternoProceso(request.getRegistroInternoProceso());
        response.setTipoDocumento(normalizar(request.getTipoDocumento()));
        response.setNumeroDocumentoTrabajador(request.getNumeroDocumentoTrabajador());

        response.setNombreArchivo(documentoSellado.getNombreArchivo());
        response.setContentType(documentoSellado.getContentType());
        response.setHashSha256DocumentoPublicado(null);

        response.setCanalPublicacion(valorPorDefecto(request.getCanalPublicacion(), "SOMOS_ESSALUD"));
        response.setPublicadoPor(valorPorDefecto(request.getPublicadoPor(), "SISTEMA"));
        response.setObservaciones(observaciones == null ? new ArrayList<>() : observaciones);

        return response;
    }

    private PublicarDocumentoResponse convertirDocumentoPublicadoAResponse(
            DocumentoPublicado documentoPublicado,
            boolean selloValidado,
            String mensaje,
            boolean requiereIntervencionInterna
    ) {
        PublicarDocumentoResponse response = new PublicarDocumentoResponse();

        response.setPublicado(true);
        response.setMensajePublicacion(mensaje);
        response.setEstadoPublicacionDocumental(documentoPublicado.getEstadoPublicacionDocumental());
        response.setDisponibleParaUsuario(documentoPublicado.isDisponibleParaUsuario());
        response.setSelloValidadoAntesPublicacion(selloValidado);
        response.setRequiereIntervencionInterna(requiereIntervencionInterna);

        response.setIdDocumentoPublicado(documentoPublicado.getIdDocumentoPublicado());
        response.setIdDocumentoSellado(documentoPublicado.getIdDocumentoSellado());
        response.setRegistroInternoProceso(documentoPublicado.getRegistroInternoProceso());
        response.setTipoDocumento(documentoPublicado.getTipoDocumento());
        response.setNumeroDocumentoTrabajador(documentoPublicado.getNumeroDocumentoTrabajador());

        response.setNombreArchivo(documentoPublicado.getNombreArchivo());
        response.setRutaArchivo(
                documentoPublicado.getRutaArchivo()
        );
        response.setContentType(documentoPublicado.getContentType());
        response.setHashSha256DocumentoPublicado(documentoPublicado.getHashSha256DocumentoPublicado());

        response.setFechaHoraPublicacion(documentoPublicado.getFechaHoraPublicacion());
        response.setCanalPublicacion(documentoPublicado.getCanalPublicacion());
        response.setPublicadoPor(documentoPublicado.getPublicadoPor());

        response.setUrlVisualizacionSimulada(
                "/api/v1/documentos/publicacion/"
                        + documentoPublicado.getIdDocumentoPublicado()
                        + "/archivo"
        );

        response.setObservaciones(new ArrayList<>());

        return response;
    }

    private void validarRequest(PublicarDocumentoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de publicaciÃ³n no puede estar vacÃ­a.");
        }

        if (campoVacio(request.getIdDocumentoSellado())) {
            throw new IllegalArgumentException("El ID del documento sellado es obligatorio.");
        }

        if (campoVacio(request.getRegistroInternoProceso())) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(request.getTipoDocumento())) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }

        if (!TipoDocumentoDigital.esValido(request.getTipoDocumento())) {
            throw new IllegalArgumentException(
                    "El tipo de documento no es vÃ¡lido. Valores permitidos: "
                            + TipoDocumentoDigital.valoresPermitidos()
            );
        }

        if (campoVacio(request.getTipoDocumentoTrabajador())) {
            throw new IllegalArgumentException(
                    "El tipo de documento del trabajador es obligatorio."
            );
        }

        if (campoVacio(request.getNumeroDocumentoTrabajador())) {
            throw new IllegalArgumentException("El nÃºmero de documento del trabajador es obligatorio.");
        }
    }

    private String calcularSha256(byte[] contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido);

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo calcular el hash SHA-256 del documento publicado.", e);
        }
    }

    private String generarIdDocumentoPublicado(
            PublicarDocumentoRequest request
    ) {

        String identidad =
                normalizar(
                        request.getRegistroInternoProceso()
                )
                        + "|"
                        + normalizar(
                                request.getTipoDocumento()
                        );

        return "DOC-PUB-"
                + UUID.nameUUIDFromBytes(
                        identidad.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    private String valorPorDefecto(String valor, String valorDefecto) {
        return campoVacio(valor) ? valorDefecto : valor;
    }

    private String normalizar(String valor) {
        return valor.trim().toUpperCase();
    }

    private boolean campoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
