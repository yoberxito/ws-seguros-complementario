package essalud.gob.pe.seguroshijomenormayor.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.dto.response.CargaArchivoRes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface GoogleDriveService {

    CargaArchivoRes guardarArchivo(
            MultipartFile archivo,
            String idTpDoc,
            String tpDocument,
            String numDocument,
            String nombreOriginal
    ) throws IOException;

    byte[] descargarArchivo(
            String fileId
    ) throws IOException;

    File obtenerInformacionArchivo(
            String fileId
    ) throws IOException;

    /*
     * ==========================================================
     * OPERACIONES REQUERIDAS POR EL JOB QUINCENAL +VIDA
     * ==========================================================
     */

    File obtenerCarpetaPeriodo(
            String idTpDoc,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException;

    List<File> listarArchivosCarpeta(
            String folderId
    ) throws IOException;

    File guardarOActualizarArchivoEnCarpeta(
            byte[] contenido,
            String nombreArchivo,
            String contentType,
            String folderId
    ) throws IOException;

    File obtenerOCrearCarpeta(
            String nombreCarpeta,
            String parentId
    ) throws IOException;

    /*
     * Operación genérica.
     *
     * La decisión de qué carpeta corresponde a MAPFRE
     * o PERSONAL queda fuera de este método.
     */
    File moverArchivo(
            String fileId,
            String nuevoParentId
    ) throws IOException;

    /*
     * Busqueda exacta y no destructiva de carpetas
     * bajo un padre Drive.
     *
     * No crea carpetas.
     */
    List<File> buscarCarpetas(
            String nombreCarpeta,
            String parentId
    ) throws IOException;
}