package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.common.util.HashUtil;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.CargarDocumentoFirmadoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.CargarDocumentoFirmadoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionPdfResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoCargado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.TipoDocumentoDigital;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoCargadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import essalud.gob.pe.wsseguroscomplementario.documento.model.RechazoDocumento;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CargaDocumentoFirmadoService {

    private final ValidacionPdfService validacionPdfService;
    private final DocumentoCargadoRepository documentoCargadoRepository;
    private final RechazoDocumentoService rechazoDocumentoService;

    public CargaDocumentoFirmadoService(
            ValidacionPdfService validacionPdfService,
            DocumentoCargadoRepository documentoCargadoRepository,
            RechazoDocumentoService rechazoDocumentoService
    ) {
        this.validacionPdfService = validacionPdfService;
        this.documentoCargadoRepository = documentoCargadoRepository;
        this.rechazoDocumentoService = rechazoDocumentoService;
    }

    public CargarDocumentoFirmadoResponse cargarDocumentoFirmado(
            CargarDocumentoFirmadoRequest request,
            MultipartFile archivo,
            String ipOrigen
    ) {
        validarRequest(request);

        ValidacionPdfResponse validacionPdf = validacionPdfService.validarEstructuraTecnica(archivo);

        if (!validacionPdf.isValido()) {
            return construirRespuestaRechazada(request, validacionPdf, ipOrigen);
        }

        byte[] contenidoArchivo = obtenerContenidoArchivo(archivo);

        DocumentoCargado documentoCargado = new DocumentoCargado();
        documentoCargado.setIdDocumentoCargado(generarIdDocumentoCargado());
        documentoCargado.setRegistroInternoProceso(request.getRegistroInternoProceso());

        documentoCargado.setTipoDocumento(request.getTipoDocumento().trim().toUpperCase());
        documentoCargado.setTipoDocumentoTrabajador(request.getTipoDocumentoTrabajador());
        documentoCargado.setNumeroDocumentoTrabajador(request.getNumeroDocumentoTrabajador());
        documentoCargado.setNombresApellidosTrabajador(request.getNombresApellidosTrabajador());

        documentoCargado.setNombreArchivoOriginal(archivo.getOriginalFilename());
        documentoCargado.setTamanioBytes(archivo.getSize());
        documentoCargado.setNumeroPaginas(validacionPdf.getNumeroPaginas());
        documentoCargado.setHashSha256ArchivoCargado(HashUtil.calcularSha256(contenidoArchivo));

        documentoCargado.setFechaHoraCarga(LocalDateTime.now());
        documentoCargado.setIpOrigen(ipOrigen);
        documentoCargado.setDatosSesionDispositivo(
                valorPorDefecto(request.getDatosSesionDispositivo(), "Sesión local / dispositivo no informado")
        );

        documentoCargado.setValidacionTecnicaPdf(true);
        documentoCargado.setObservaciones(validacionPdf.getObservaciones());
        documentoCargado.setContenidoArchivo(contenidoArchivo);

        DocumentoCargado documentoGuardado = documentoCargadoRepository.guardar(documentoCargado);

        return convertirAResponse(documentoGuardado);
    }

    private void validarRequest(CargarDocumentoFirmadoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de carga de documento no puede estar vacía.");
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

        if (campoVacio(request.getTipoDocumentoTrabajador())) {
            throw new IllegalArgumentException("El tipo de documento del trabajador es obligatorio.");
        }

        if (campoVacio(request.getNumeroDocumentoTrabajador())) {
            throw new IllegalArgumentException("El número de documento del trabajador es obligatorio.");
        }

        if (campoVacio(request.getNombresApellidosTrabajador())) {
            throw new IllegalArgumentException("Los nombres y apellidos del trabajador son obligatorios.");
        }
    }

    private byte[] obtenerContenidoArchivo(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el contenido del documento cargado.");
        }
    }

    private CargarDocumentoFirmadoResponse construirRespuestaRechazada(
            CargarDocumentoFirmadoRequest request,
            ValidacionPdfResponse validacionPdf,
            String ipOrigen
    ) {
        RechazoDocumento rechazoDocumento = rechazoDocumentoService.registrarRechazoDesdeCarga(
                request,
                validacionPdf,
                ipOrigen
        );

        CargarDocumentoFirmadoResponse response = new CargarDocumentoFirmadoResponse();

        response.setCargado(false);
        response.setMensajeCarga("El documento cargado no supera las validaciones técnicas.");
        response.setRegistroInternoProceso(request.getRegistroInternoProceso());
        response.setTipoDocumento(request.getTipoDocumento());

        response.setNombreArchivoOriginal(validacionPdf.getNombreArchivo());
        response.setTamanioBytes(validacionPdf.getTamanioBytes());
        response.setNumeroPaginas(validacionPdf.getNumeroPaginas());

        response.setValidacionTecnicaPdf(false);
        response.setObservaciones(validacionPdf.getObservaciones());

        response.setEstadoValidacionDocumental(rechazoDocumento.getEstadoValidacionDocumental());
        response.setIdRechazoDocumental(rechazoDocumento.getIdRechazoDocumental());
        response.setPermiteNuevaCarga(rechazoDocumento.isPermiteNuevaCarga());

        response.setFechaHoraCarga(rechazoDocumento.getFechaHoraRechazo());
        response.setIpOrigen(rechazoDocumento.getIpOrigen());
        response.setDatosSesionDispositivo(rechazoDocumento.getDatosSesionDispositivo());

        return response;
    }

    private CargarDocumentoFirmadoResponse convertirAResponse(DocumentoCargado documentoCargado) {
        CargarDocumentoFirmadoResponse response = new CargarDocumentoFirmadoResponse();

        response.setCargado(true);
        response.setMensajeCarga("Documento firmado cargado y registrado correctamente.");
        response.setIdDocumentoCargado(documentoCargado.getIdDocumentoCargado());
        response.setRegistroInternoProceso(documentoCargado.getRegistroInternoProceso());
        response.setTipoDocumento(documentoCargado.getTipoDocumento());

        response.setNombreArchivoOriginal(documentoCargado.getNombreArchivoOriginal());
        response.setTamanioBytes(documentoCargado.getTamanioBytes());
        response.setNumeroPaginas(documentoCargado.getNumeroPaginas());
        response.setHashSha256ArchivoCargado(documentoCargado.getHashSha256ArchivoCargado());

        response.setFechaHoraCarga(documentoCargado.getFechaHoraCarga());
        response.setIpOrigen(documentoCargado.getIpOrigen());
        response.setDatosSesionDispositivo(documentoCargado.getDatosSesionDispositivo());

        response.setValidacionTecnicaPdf(documentoCargado.isValidacionTecnicaPdf());
        response.setObservaciones(documentoCargado.getObservaciones());

        response.setEstadoValidacionDocumental("VALIDACION_TECNICA_APROBADA");
        response.setPermiteNuevaCarga(false);

        return response;
    }

    private String generarIdDocumentoCargado() {
        return "DOC-CARG-" + UUID.randomUUID();
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