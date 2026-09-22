package essalud.gob.pe.seguroshijomenormayor.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.dto.response.CargaArchivoRes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoogleDriveService {

    CargaArchivoRes guardarArchivo(
            MultipartFile archivo,
            String idTpDoc,
            String tpDocument,
            String numDocument,
            String nombreOriginal
    ) throws IOException;


    /*
     * Subida canonica de un documento ya PUBLICADO.
     *
     * Usa el ID y la fecha real de publicacion para que
     * los reintentos sean idempotentes y no cambien de periodo.
     */
    CargaArchivoRes guardarArchivoPublicado(
            MultipartFile archivo,
            String idTpDoc,
            String tpDocument,
            String numDocument,
            String nombreOriginal,
            String idDocumentoPublicado,
            LocalDate fechaPublicacion
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

    /*
     * Busqueda no destructiva del periodo de preparacion.
     * Nunca crea Preparacion_* ni la carpeta del periodo.
     */
    Optional<File> buscarCarpetaPeriodo(
            String idTpDoc,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException;

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
     * Copia no destructiva de un archivo hacia otra carpeta.
     *
     * Se utiliza en PERSONAL multidestino para conservar
     * intacta la carpeta fuente de preparacion.
     *
     * La implementacion debe ser idempotente.
     */
    File copiarArchivoEnCarpeta(
            String fileId,
            String folderId
    ) throws IOException;

    /*
     * OperaciÃ³n genÃ©rica.
     *
     * La decisiÃ³n de quÃ© carpeta corresponde a MAPFRE
     * o PERSONAL queda fuera de este mÃ©todo.
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
