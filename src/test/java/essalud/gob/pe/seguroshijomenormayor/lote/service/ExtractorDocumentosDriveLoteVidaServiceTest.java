package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtractorDocumentosDriveLoteVidaServiceTest {

    private final ExtractorDocumentosDriveLoteVidaService service =
            new ExtractorDocumentosDriveLoteVidaService();

    @Test
    void extraeDocumento244ConMetadataCompleta() {

        File archivo =
                crearPdf(
                        "FILE-244-1",
                        "Formulario6012.pdf",
                        "244",
                        "1",
                        "12345678"
                );

        List<DocumentoDriveLoteVida> resultado =
                service.extraer(
                        List.of(archivo),
                        "244"
                );

        assertEquals(
                1,
                resultado.size()
        );

        DocumentoDriveLoteVida documento =
                resultado.get(0);

        assertEquals(
                "FILE-244-1",
                documento.getFileId()
        );

        assertEquals(
                "244",
                documento.getIdTpDoc()
        );

        assertEquals(
                "1",
                documento.getTipoDocumentoTitular()
        );

        assertEquals(
                "12345678",
                documento.getNumeroDocumentoTitular()
        );

        assertEquals(
                "FORMULARIO_6012",
                documento.getTipoDocumentoLogico()
        );

        assertEquals(
                "https://drive.google.com/file/d/FILE-244-1/view",
                documento.getWebViewLink()
        );
    }

    @Test
    void extraeDocumento247() {

        File archivo =
                crearPdf(
                        "FILE-247-1",
                        "AutorizacionDescuento.pdf",
                        "247",
                        "1",
                        "87654321"
                );

        List<DocumentoDriveLoteVida> resultado =
                service.extraer(
                        List.of(archivo),
                        "247"
                );

        assertEquals(
                1,
                resultado.size()
        );

        assertEquals(
                "AUTORIZACION_DESCUENTO",
                resultado
                        .get(0)
                        .getTipoDocumentoLogico()
        );
    }

    @Test
    void ignoraArchivosQueNoSonPdf() {

        File excel =
                new File();

        excel.setId(
                "EXCEL-1"
        );

        excel.setName(
                "Reporte_MAPFRE.xlsx"
        );

        excel.setMimeType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        List<DocumentoDriveLoteVida> resultado =
                service.extraer(
                        List.of(excel),
                        "244"
                );

        assertTrue(
                resultado.isEmpty()
        );
    }

    @Test
    void rechazaPdfSinMetadataVida() {

        File archivo =
                new File();

        archivo.setId(
                "FILE-SIN-META"
        );

        archivo.setName(
                "Documento.pdf"
        );

        archivo.setMimeType(
                "application/pdf"
        );

        archivo.setWebViewLink(
                "https://drive.google.com/file/d/FILE-SIN-META/view"
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.extraer(
                                        List.of(archivo),
                                        "244"
                                )
                );

        assertTrue(
                error.getMessage()
                        .contains(
                                "appProperties"
                        )
        );
    }

    @Test
    void rechazaDocumento247DentroDeLote244() {

        File archivo =
                crearPdf(
                        "FILE-MEZCLADO",
                        "Autorizacion.pdf",
                        "247",
                        "1",
                        "12345678"
                );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.extraer(
                                List.of(archivo),
                                "244"
                        )
        );
    }

    @Test
    void rechazaDosPdfDelMismoTrabajador() {

        File archivo1 =
                crearPdf(
                        "FILE-1",
                        "Formulario1.pdf",
                        "244",
                        "1",
                        "12345678"
                );

        File archivo2 =
                crearPdf(
                        "FILE-2",
                        "Formulario2.pdf",
                        "244",
                        "1",
                        "12345678"
                );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.extraer(
                                List.of(
                                        archivo1,
                                        archivo2
                                ),
                                "244"
                        )
        );
    }

    private File crearPdf(
            String id,
            String nombre,
            String idTpDoc,
            String tipoDocumento,
            String numeroDocumento
    ) {

        File archivo =
                new File();

        archivo.setId(
                id
        );

        archivo.setName(
                nombre
        );

        archivo.setMimeType(
                "application/pdf"
        );

        archivo.setWebViewLink(
                "https://drive.google.com/file/d/"
                        + id
                        + "/view"
        );

        archivo.setAppProperties(
                Map.of(
                        "idTpDoc",
                        idTpDoc,

                        "tpDocument",
                        tipoDocumento,

                        "numDocument",
                        numeroDocumento,

                        "nombreOriginal",
                        nombre
                )
        );

        return archivo;
    }
}