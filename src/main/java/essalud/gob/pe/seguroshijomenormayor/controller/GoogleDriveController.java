package essalud.gob.pe.seguroshijomenormayor.controller;

import essalud.gob.pe.seguroshijomenormayor.dto.response.CargaArchivoRes;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/seguro-complementario/google-drive")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class GoogleDriveController {

    private final GoogleDriveService googleDriveService;

    public GoogleDriveController(
            GoogleDriveService googleDriveService
    ) {
        this.googleDriveService = googleDriveService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CargaArchivoRes subirArchivo(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("idTpDoc") String idTpDoc,
            @RequestParam("tpDocument") String tpDocument,
            @RequestParam("numDocument") String numDocument
    ) throws IOException {

        String nombreOriginal = archivo.getOriginalFilename();

        return googleDriveService.guardarArchivo(
                archivo,
                idTpDoc,
                tpDocument,
                numDocument,
                nombreOriginal
        );
    }
}
