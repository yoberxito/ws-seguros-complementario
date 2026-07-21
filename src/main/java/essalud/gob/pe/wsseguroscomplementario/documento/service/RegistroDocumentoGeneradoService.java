package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.common.util.HashUtil;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RegistrarDocumentoGeneradoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RegistrarDocumentoGeneradoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionPdfResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoGenerado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.PaginaDocumentoGenerado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.TipoDocumentoDigital;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoGeneradoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RegistroDocumentoGeneradoService {

    public static final String ESTADO_GENERADO_PENDIENTE_FIRMA = "GENERADO_PENDIENTE_FIRMA";

    private final ValidacionPdfService validacionPdfService;
    private final DocumentoGeneradoRepository documentoGeneradoRepository;

    public RegistroDocumentoGeneradoService(
            ValidacionPdfService validacionPdfService,
            DocumentoGeneradoRepository documentoGeneradoRepository
    ) {
        this.validacionPdfService = validacionPdfService;
        this.documentoGeneradoRepository = documentoGeneradoRepository;
    }

    public RegistrarDocumentoGeneradoResponse registrarDocumentoGenerado(
            RegistrarDocumentoGeneradoRequest request,
            MultipartFile archivo
    ) {
        validarRequest(request);

        ValidacionPdfResponse validacionPdf = validacionPdfService.validarEstructuraTecnica(archivo);

        if (!validacionPdf.isValido()) {
            throw new IllegalArgumentException("El documento generado no supera la validación técnica PDF.");
        }

        byte[] contenidoArchivo = obtenerContenidoArchivo(archivo);

        String idDocumentoGenerado = campoVacio(request.getIdDocumentoGenerado())
                ? generarIdDocumentoGenerado()
                : request.getIdDocumentoGenerado();
        int numeroPaginas = validacionPdf.getNumeroPaginas();

        DocumentoGenerado documentoGenerado = new DocumentoGenerado();

        documentoGenerado.setIdDocumentoGenerado(idDocumentoGenerado);
        documentoGenerado.setRegistroInternoProceso(request.getRegistroInternoProceso());

        documentoGenerado.setTipoDocumento(normalizarTipoDocumento(request.getTipoDocumento()));
        documentoGenerado.setVersionFormato(request.getVersionFormato());

        documentoGenerado.setTipoDocumentoTrabajador(request.getTipoDocumentoTrabajador());
        documentoGenerado.setNumeroDocumentoTrabajador(request.getNumeroDocumentoTrabajador());
        documentoGenerado.setNombresApellidosTrabajador(request.getNombresApellidosTrabajador());

        documentoGenerado.setNombreArchivoOriginal(archivo.getOriginalFilename());
        documentoGenerado.setTamanioBytes(archivo.getSize());
        documentoGenerado.setNumeroPaginasGeneradas(numeroPaginas);
        documentoGenerado.setHashSha256DocumentoOriginal(HashUtil.calcularSha256(contenidoArchivo));

        documentoGenerado.setCantidadBeneficiariosRegistrados(request.getCantidadBeneficiariosRegistrados());

        documentoGenerado.setEstadoDocumentoGenerado(ESTADO_GENERADO_PENDIENTE_FIRMA);
        documentoGenerado.setFechaHoraGeneracion(LocalDateTime.now());

        documentoGenerado.setGeneradoPor(valorPorDefecto(request.getGeneradoPor(), "SISTEMA"));
        documentoGenerado.setCanalGeneracion(valorPorDefecto(request.getCanalGeneracion(), "MODULO_AFILIACION_DIGITAL"));

        documentoGenerado.setPaginasEsperadas(
                construirPaginasEsperadas(
                        idDocumentoGenerado,
                        documentoGenerado.getTipoDocumento(),
                        numeroPaginas
                )
        );

        documentoGenerado.setContenidoArchivoOriginal(contenidoArchivo);

        DocumentoGenerado documentoGuardado = documentoGeneradoRepository.guardar(documentoGenerado);

        return convertirAResponse(documentoGuardado);
    }

    public List<RegistrarDocumentoGeneradoResponse> listarDocumentosGenerados() {
        return documentoGeneradoRepository.listar()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public RegistrarDocumentoGeneradoResponse buscarPorId(String idDocumentoGenerado) {
        DocumentoGenerado documentoGenerado = documentoGeneradoRepository.buscarPorId(idDocumentoGenerado)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el documento generado solicitado."));

        return convertirAResponse(documentoGenerado);
    }

    public DocumentoGenerado obtenerDocumentoGenerado(String idDocumentoGenerado) {
        return documentoGeneradoRepository.buscarPorId(idDocumentoGenerado)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el documento generado solicitado."));
    }

    public List<RegistrarDocumentoGeneradoResponse> buscarPorRegistroInternoProceso(String registroInternoProceso) {
        return documentoGeneradoRepository.buscarPorRegistroInternoProceso(registroInternoProceso)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    private void validarRequest(RegistrarDocumentoGeneradoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de registro documental no puede estar vacía.");
        }

        if (campoVacio(request.getRegistroInternoProceso())) {
            throw new IllegalArgumentException("El registro interno del proceso es obligatorio.");
        }

        if (campoVacio(request.getTipoDocumento())) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }

        if (!TipoDocumentoDigital.esValido(request.getTipoDocumento())) {
            throw new IllegalArgumentException(
                    "El tipo de documento no es válido. Valores permitidos: "
                            + TipoDocumentoDigital.valoresPermitidos()
            );
        }

        if (campoVacio(request.getVersionFormato())) {
            throw new IllegalArgumentException("La versión del formato es obligatoria.");
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

        if (request.getCantidadBeneficiariosRegistrados() < 0) {
            throw new IllegalArgumentException("La cantidad de beneficiarios registrados no puede ser negativa.");
        }
    }

    private byte[] obtenerContenidoArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo del documento generado es obligatorio.");
        }

        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el contenido del documento generado.");
        }
    }

    private List<PaginaDocumentoGenerado> construirPaginasEsperadas(
            String idDocumentoGenerado,
            String tipoDocumento,
            int totalPaginas
    ) {
        List<PaginaDocumentoGenerado> paginas = new ArrayList<>();

        for (int numeroPagina = 1; numeroPagina <= totalPaginas; numeroPagina++) {
            String identificadorPagina = idDocumentoGenerado + "-PAG-" + numeroPagina + "-DE-" + totalPaginas;

            String contenidoQrEsperado =
                    "ID_DOCUMENTO=" + idDocumentoGenerado
                            + "|TIPO_DOCUMENTO=" + tipoDocumento
                            + "|PAGINA=" + numeroPagina
                            + "|TOTAL_PAGINAS=" + totalPaginas;

            paginas.add(
                    new PaginaDocumentoGenerado(
                            numeroPagina,
                            totalPaginas,
                            identificadorPagina,
                            contenidoQrEsperado
                    )
            );
        }

        return paginas;
    }

    private RegistrarDocumentoGeneradoResponse convertirAResponse(DocumentoGenerado documentoGenerado) {
        RegistrarDocumentoGeneradoResponse response = new RegistrarDocumentoGeneradoResponse();

        response.setIdDocumentoGenerado(documentoGenerado.getIdDocumentoGenerado());
        response.setRegistroInternoProceso(documentoGenerado.getRegistroInternoProceso());

        response.setTipoDocumento(documentoGenerado.getTipoDocumento());
        response.setVersionFormato(documentoGenerado.getVersionFormato());

        response.setNombreArchivoOriginal(documentoGenerado.getNombreArchivoOriginal());
        response.setTamanioBytes(documentoGenerado.getTamanioBytes());
        response.setNumeroPaginasGeneradas(documentoGenerado.getNumeroPaginasGeneradas());
        response.setHashSha256DocumentoOriginal(documentoGenerado.getHashSha256DocumentoOriginal());

        response.setCantidadBeneficiariosRegistrados(documentoGenerado.getCantidadBeneficiariosRegistrados());

        response.setEstadoDocumentoGenerado(documentoGenerado.getEstadoDocumentoGenerado());
        response.setFechaHoraGeneracion(documentoGenerado.getFechaHoraGeneracion());

        response.setGeneradoPor(documentoGenerado.getGeneradoPor());
        response.setCanalGeneracion(documentoGenerado.getCanalGeneracion());

        response.setPaginasEsperadas(documentoGenerado.getPaginasEsperadas());

        return response;
    }

    private String generarIdDocumentoGenerado() {
        return "DOC-GEN-" + UUID.randomUUID();
    }

    private String normalizarTipoDocumento(String tipoDocumento) {
        return tipoDocumento.trim().toUpperCase();
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