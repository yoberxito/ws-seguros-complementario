package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.documento.dto.CargarDocumentoFirmadoRequest;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.RechazoDocumentoResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionPdfResponse;
import essalud.gob.pe.wsseguroscomplementario.documento.model.RechazoDocumento;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.RechazoDocumentoRepository;
import org.springframework.stereotype.Service;
import essalud.gob.pe.wsseguroscomplementario.documento.dto.ValidacionDocumentalCompletaResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RechazoDocumentoService {

    public static final String ESTADO_RECHAZADO_VALIDACION = "RECHAZADO_VALIDACION";

    private final RechazoDocumentoRepository rechazoDocumentoRepository;

    public RechazoDocumentoService(RechazoDocumentoRepository rechazoDocumentoRepository) {
        this.rechazoDocumentoRepository = rechazoDocumentoRepository;
    }

    public RechazoDocumento registrarRechazoDesdeCarga(
            CargarDocumentoFirmadoRequest request,
            ValidacionPdfResponse validacionPdf,
            String ipOrigen
    ) {
        RechazoDocumento rechazoDocumento = new RechazoDocumento();

        rechazoDocumento.setIdRechazoDocumental(generarIdRechazo());
        rechazoDocumento.setRegistroInternoProceso(request.getRegistroInternoProceso());

        rechazoDocumento.setTipoDocumento(request.getTipoDocumento());
        rechazoDocumento.setTipoDocumentoTrabajador(request.getTipoDocumentoTrabajador());
        rechazoDocumento.setNumeroDocumentoTrabajador(request.getNumeroDocumentoTrabajador());
        rechazoDocumento.setNombresApellidosTrabajador(request.getNombresApellidosTrabajador());

        rechazoDocumento.setNombreArchivoOriginal(validacionPdf.getNombreArchivo());
        rechazoDocumento.setTamanioBytes(validacionPdf.getTamanioBytes());
        rechazoDocumento.setNumeroPaginas(validacionPdf.getNumeroPaginas());

        rechazoDocumento.setEstadoValidacionDocumental(ESTADO_RECHAZADO_VALIDACION);
        rechazoDocumento.setPermiteNuevaCarga(true);

        rechazoDocumento.setMotivosRechazo(validacionPdf.getObservaciones());
        rechazoDocumento.setFechaHoraRechazo(LocalDateTime.now());
        rechazoDocumento.setIpOrigen(ipOrigen);
        rechazoDocumento.setDatosSesionDispositivo(
                valorPorDefecto(request.getDatosSesionDispositivo(), "Sesión local / dispositivo no informado")
        );

        return rechazoDocumentoRepository.guardar(rechazoDocumento);
    }

    public List<RechazoDocumentoResponse> listarRechazos() {
        return rechazoDocumentoRepository.listar()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public RechazoDocumentoResponse buscarPorId(String idRechazoDocumental) {
        RechazoDocumento rechazoDocumento = rechazoDocumentoRepository.buscarPorId(idRechazoDocumental)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el rechazo documental solicitado."));

        return convertirAResponse(rechazoDocumento);
    }

    public RechazoDocumentoResponse convertirAResponse(RechazoDocumento rechazoDocumento) {
        RechazoDocumentoResponse response = new RechazoDocumentoResponse();

        response.setIdRechazoDocumental(rechazoDocumento.getIdRechazoDocumental());
        response.setRegistroInternoProceso(rechazoDocumento.getRegistroInternoProceso());
        response.setTipoDocumento(rechazoDocumento.getTipoDocumento());

        response.setNombreArchivoOriginal(rechazoDocumento.getNombreArchivoOriginal());
        response.setTamanioBytes(rechazoDocumento.getTamanioBytes());
        response.setNumeroPaginas(rechazoDocumento.getNumeroPaginas());

        response.setEstadoValidacionDocumental(rechazoDocumento.getEstadoValidacionDocumental());
        response.setPermiteNuevaCarga(rechazoDocumento.isPermiteNuevaCarga());
        response.setMotivosRechazo(rechazoDocumento.getMotivosRechazo());

        response.setFechaHoraRechazo(rechazoDocumento.getFechaHoraRechazo());
        response.setIpOrigen(rechazoDocumento.getIpOrigen());
        response.setDatosSesionDispositivo(rechazoDocumento.getDatosSesionDispositivo());

        return response;
    }

    private String generarIdRechazo() {
        return "RECH-DOC-" + UUID.randomUUID();
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

    public RechazoDocumento registrarRechazoDesdeValidacionCompleta(
            ValidacionDocumentalCompletaResponse response,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        RechazoDocumento rechazoDocumento =
                new RechazoDocumento();

        rechazoDocumento.setIdRechazoDocumental(
                generarIdRechazo()
        );

        rechazoDocumento.setRegistroInternoProceso(
                response.getRegistroInternoProceso()
        );

        rechazoDocumento.setIdDocumentoCargado(
                response.getIdDocumentoCargado()
        );

        rechazoDocumento.setTipoDocumento(
                response.getTipoDocumento()
        );

        rechazoDocumento.setTipoDocumentoTrabajador(
                response.getTipoDocumentoTrabajador()
        );

        rechazoDocumento.setNumeroDocumentoTrabajador(
                response.getNumeroDocumentoTrabajador()
        );

        rechazoDocumento.setEstadoValidacionDocumental(
                ESTADO_RECHAZADO_VALIDACION
        );

        rechazoDocumento.setPermiteNuevaCarga(
                true
        );

        rechazoDocumento.setMotivosRechazo(
                List.of(
                        response.getMensajeValidacion()
                )
        );

        rechazoDocumento.setFechaHoraRechazo(
                response.getFechaHoraValidacion()
        );

        rechazoDocumento.setIpOrigen(
                ipOrigen
        );

        rechazoDocumento.setDatosSesionDispositivo(
                valorPorDefecto(
                        datosSesionDispositivo,
                        "Sesión local / dispositivo no informado"
                )
        );

        return rechazoDocumentoRepository
                .guardar(
                        rechazoDocumento
                );
    }
}