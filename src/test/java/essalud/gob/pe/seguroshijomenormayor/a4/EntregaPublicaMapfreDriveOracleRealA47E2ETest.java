package essalud.gob.pe.seguroshijomenormayor.a4;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import essalud.gob.pe.seguroshijomenormayor.config.GoogleDriveConfig;
import essalud.gob.pe.seguroshijomenormayor.config.GoogleDriveProperties;

import essalud.gob.pe.seguroshijomenormayor.entrega.dto.ConfirmarAcuseEntregaResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.exception.EstadoEntregaException;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.CierreLotePreparado;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.DescargaLotePreparada;

import essalud.gob.pe.seguroshijomenormayor.entrega.repository.JdbcEntregaLoteRepository;
import essalud.gob.pe.seguroshijomenormayor.entrega.repository.JdbcHistorialEntregaLoteRepository;
import essalud.gob.pe.seguroshijomenormayor.entrega.repository.JdbcLoteDistribucionRepository;

import essalud.gob.pe.seguroshijomenormayor.entrega.service.CierreLoteDistribucionService;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.DescargaLoteService;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.EntregaLoteService;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.EntregaLoteTransicionService;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.LoteDistribucionService;

import essalud.gob.pe.seguroshijomenormayor.lote.service.CierreDriveLoteVidaService;
import essalud.gob.pe.seguroshijomenormayor.lote.service.PublicacionDrivePersonalService;

import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import essalud.gob.pe.seguroshijomenormayor.service.impl.GoogleDriveServiceImpl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.time.LocalDate;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;


class EntregaPublicaMapfreDriveOracleRealA47E2ETest {

    private static final String DESTINO =
            "MAPFRE";

    private static final String ID_TP_DOC =
            "244";

    private static final String FINAL_MAPFRE =
            "MAPFRE_HISTORICAL";

    private static final String MIME_FOLDER =
            "application/vnd.google-apps.folder";

    private static final String MIME_PDF =
            "application/pdf";

    private static final String MIME_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";


    @Test
    @EnabledIfEnvironmentVariable(
            named = "VIDA_E2E_DRIVE_ENABLED",
            matches = "true"
    )
    void debeEjecutarFlujoRealMapfreDescargaAcuseYDerivacion()
            throws Exception {

        Assumptions.assumeTrue(
                "true".equalsIgnoreCase(
                        System.getenv(
                                "VIDA_E2E_WRITE_ENABLED"
                        )
                ),
                "E2E real de escritura no habilitado."
        );


        /*
         * ======================================================
         * INFRA REAL
         * ======================================================
         */

        GoogleDriveProperties properties =
                construirDriveProperties();

        Drive drive =
                new GoogleDriveConfig(
                        properties
                )
                        .googleDrive();

        GoogleDriveService googleDriveService =
                new GoogleDriveServiceImpl(
                        drive,
                        properties
                );

        JdbcTemplate jdbc =
                construirJdbcTemplate();


        JdbcLoteDistribucionRepository loteRepository =
                new JdbcLoteDistribucionRepository(
                        jdbc
                );

        JdbcEntregaLoteRepository entregaRepository =
                new JdbcEntregaLoteRepository(
                        jdbc
                );

        JdbcHistorialEntregaLoteRepository historialRepository =
                new JdbcHistorialEntregaLoteRepository(
                        jdbc
                );


        LoteDistribucionService loteService =
                new LoteDistribucionService(
                        loteRepository
                );

        EntregaLoteTransicionService transicionService =
                new EntregaLoteTransicionService(
                        entregaRepository,
                        historialRepository
                );

        CierreDriveLoteVidaService cierreDrive =
                new CierreDriveLoteVidaService(
                        googleDriveService,
                        properties
                );

        PublicacionDrivePersonalService publicacionPersonal =
                new PublicacionDrivePersonalService(
                        googleDriveService,
                        properties
                );

        EntregaLoteService entregaService =
                new EntregaLoteService(
                        entregaRepository,
                        transicionService,
                        publicacionPersonal,
                        cierreDrive
                );

        CierreLoteDistribucionService cierreLoteService =
                new CierreLoteDistribucionService(
                        loteService,
                        entregaService
                );

        DescargaLoteService descargaService =
                new DescargaLoteService(
                        entregaRepository,
                        googleDriveService
                );


        /*
         * ======================================================
         * PERIODO E2E UNICO
         * ======================================================
         */

        String correlativo =
                UUID.randomUUID()
                        .toString()
                        .replace(
                                "-",
                                ""
                        )
                        .substring(
                                0,
                                8
                        )
                        .toUpperCase();

        int semilla =
                Math.abs(
                        correlativo.hashCode()
                );

        int year =
                2200
                        +
                (semilla % 5000);

        int month =
                1
                        +
                (semilla % 12);

        LocalDate inicio =
                LocalDate.of(
                        year,
                        month,
                        1
                );

        LocalDate fin =
                LocalDate.of(
                        year,
                        month,
                        2
                );

        String nombrePeriodo =
                inicio
                        + "_"
                        + fin;


        /*
         * ======================================================
         * DRIVE - PREPARACION REAL
         * ======================================================
         *
         * Todo se crea bajo VIDA_E2E_DRIVE_ROOT_ID.
         */

        File carpetaPreparacion =
                googleDriveService
                        .obtenerOCrearCarpeta(
                                "_A47_MAPFRE_"
                                        + correlativo,
                                properties
                                        .getFolderId()
                        );

        assertNotNull(
                carpetaPreparacion
        );

        assertNotNull(
                carpetaPreparacion.getId()
        );


        File carpetaPeriodo =
                googleDriveService
                        .obtenerOCrearCarpeta(
                                nombrePeriodo,
                                carpetaPreparacion
                                        .getId()
                        );

        assertNotNull(
                carpetaPeriodo
        );

        assertNotNull(
                carpetaPeriodo.getId()
        );


        /*
         * PDF 6012 real con appProperties idTpDoc=244.
         */

        byte[] pdf =
                crearPdfReal();

        File metadataPdf =
                new File();

        metadataPdf.setName(
                "Formulario_6012_A47_"
                        + correlativo
                        + ".pdf"
        );

        metadataPdf.setParents(
                Collections.singletonList(
                        carpetaPeriodo.getId()
                )
        );

        metadataPdf.setAppProperties(
                Map.of(
                        "idTpDoc",
                        ID_TP_DOC
                )
        );


        File pdfDrive =
                drive.files()
                        .create(
                                metadataPdf,
                                new ByteArrayContent(
                                        MIME_PDF,
                                        pdf
                                )
                        )
                        .setSupportsAllDrives(
                                true
                        )
                        .setFields(
                                "id,name,mimeType,size,"
                                        + "webViewLink,parents,"
                                        + "appProperties"
                        )
                        .execute();

        assertNotNull(
                pdfDrive
        );

        assertEquals(
                ID_TP_DOC,
                pdfDrive
                        .getAppProperties()
                        .get(
                                "idTpDoc"
                        )
        );


        /*
         * XLSX real del lote.
         */

        byte[] xlsx =
                crearXlsxReal();

        File metadataExcel =
                new File();

        metadataExcel.setName(
                "Reporte_MAPFRE_A47_"
                        + correlativo
                        + ".xlsx"
        );

        metadataExcel.setParents(
                Collections.singletonList(
                        carpetaPeriodo.getId()
                )
        );


        File excelDrive =
                drive.files()
                        .create(
                                metadataExcel,
                                new ByteArrayContent(
                                        MIME_XLSX,
                                        xlsx
                                )
                        )
                        .setSupportsAllDrives(
                                true
                        )
                        .setFields(
                                "id,name,mimeType,size,"
                                        + "webViewLink,parents,"
                                        + "appProperties"
                        )
                        .execute();

        assertNotNull(
                excelDrive
        );


        String urlPreparacion =
                "https://drive.google.com/drive/folders/"
                        + carpetaPeriodo
                                .getId();


        /*
         * ======================================================
         * ORACLE - PREPARACION DE LOTE Y ENTREGA
         * ======================================================
         */

        CierreLotePreparado cierre =
                cierreLoteService
                        .prepararLotePublicado(
                                DESTINO,
                                inicio,
                                fin,
                                "mapfre-e2e@example.invalid",
                                1,
                                urlPreparacion
                        );

        assertNotNull(
                cierre
        );

        assertNotNull(
                cierre.getLote()
        );

        assertNotNull(
                cierre.getPreparacionEntrega()
        );

        assertNotNull(
                cierre
                        .getPreparacionEntrega()
                        .getEntrega()
        );


        String token =
                cierre.getTokenPublico();

        assertNotNull(
                token
        );

        assertFalse(
                token.isBlank()
        );


        Long idEntrega =
                cierre
                        .getPreparacionEntrega()
                        .getEntrega()
                        .getIdEntrega();

        assertNotNull(
                idEntrega
        );


        assertTrue(
                cierreLoteService
                        .marcarNotificacionEnviando(
                                cierre
                        )
        );

        assertTrue(
                cierreLoteService
                        .marcarNotificacionEnviada(
                                cierre
                        )
        );


        /*
         * ======================================================
         * OTP BACKEND TRACE
         * ======================================================
         *
         * NO representa validacion OTP institucional.
         */



        /*
         * ======================================================
         * GATE: ACUSE IMPOSIBLE ANTES DE DESCARGA
         * ======================================================
         */

        assertThrows(
                EstadoEntregaException.class,
                () ->
                        entregaService
                                .confirmarAcusePorToken(
                                        token,
                                        "127.0.0.1",
                                        "A47-MAPFRE-E2E"
                                )
        );


        /*
         * ======================================================
         * DESCARGA ZIP REAL
         * ======================================================
         */

        DescargaLotePreparada descarga =
                descargaService
                        .prepararDescargaPorToken(
                                token
                        );

        assertNotNull(
                descarga
        );

        assertNotNull(
                descarga.getContenido()
        );

        assertTrue(
                descarga
                        .getContenido()
                        .length
                        > 0
        );

        assertTrue(
                descarga
                        .getNombreArchivo()
                        .contains(
                                "MAPFRE"
                        )
        );


        Set<String> entradas =
                leerEntradasZip(
                        descarga.getContenido()
                );

        assertEquals(
                2,
                entradas.size()
        );

        assertTrue(
                contieneExtension(
                        entradas,
                        ".pdf"
                )
        );

        assertTrue(
                contieneExtension(
                        entradas,
                        ".xlsx"
                )
        );


        /*
         * Antes del registro del acuse la carpeta sigue
         * en Preparacion.
         */

        File periodoAntesAcuse =
                googleDriveService
                        .obtenerInformacionArchivo(
                                carpetaPeriodo
                                        .getId()
                        );

        assertNotNull(
                periodoAntesAcuse
        );

        assertNotNull(
                periodoAntesAcuse
                        .getParents()
        );

        assertTrue(
                periodoAntesAcuse
                        .getParents()
                        .contains(
                                carpetaPreparacion
                                        .getId()
                        )
        );


        /*
         * ======================================================
         * REGISTRAR DESCARGA
         * ======================================================
         */

        entregaService
                .registrarDescargaCompletadaPorToken(
                        token,
                        "127.0.0.1",
                        "A47-MAPFRE-E2E"
                );


        /*
         * ======================================================
         * ACUSE REAL + DERIVACION MAPFRE
         * ======================================================
         */

        ConfirmarAcuseEntregaResponse acuse =
                entregaService
                        .confirmarAcusePorToken(
                                token,
                                "127.0.0.1",
                                "A47-MAPFRE-E2E"
                        );

        assertNotNull(
                acuse
        );

        assertTrue(
                acuse.isAcuseRegistrado()
        );

        assertTrue(
                acuse.isAccesoDisponible()
        );


        /*
         * ======================================================
         * DRIVE POST-ACUSE
         * ======================================================
         */

        File periodoFinal =
                googleDriveService
                        .obtenerInformacionArchivo(
                                carpetaPeriodo
                                        .getId()
                        );

        assertNotNull(
                periodoFinal
        );

        assertEquals(
                carpetaPeriodo.getId(),
                periodoFinal.getId()
        );

        assertNotNull(
                periodoFinal.getParents()
        );

        assertEquals(
                1,
                periodoFinal
                        .getParents()
                        .size()
        );


        String parentFinalId =
                periodoFinal
                        .getParents()
                        .get(0);

        File parentFinal =
                googleDriveService
                        .obtenerInformacionArchivo(
                                parentFinalId
                        );

        assertNotNull(
                parentFinal
        );

        assertEquals(
                MIME_FOLDER,
                parentFinal.getMimeType()
        );

        assertEquals(
                FINAL_MAPFRE,
                parentFinal.getName()
        );

        assertNotNull(
                parentFinal.getParents()
        );

        assertTrue(
                parentFinal
                        .getParents()
                        .contains(
                                properties
                                        .getFolderId()
                        )
        );


        /*
         * ======================================================
         * ORACLE - ACUSE Y TRAZA
         * ======================================================
         */

        Integer acusePersistido =
                jdbc.queryForObject(
                        "SELECT COUNT(*) "
                                + "FROM ENTREGA_LOTE "
                                + "WHERE ID_ENTREGA = ? "
                                + "AND FECHA_ACUSE IS NOT NULL",
                        Integer.class,
                        idEntrega
                );

        assertNotNull(
                acusePersistido
        );

        assertEquals(
                1,
                acusePersistido
        );


        List<String> eventos =
                jdbc.query(
                        "SELECT TIPO_EVENTO "
                                + "FROM HISTORIAL_ENTREGA_LOTE "
                                + "WHERE ID_ENTREGA = ? "
                                + "ORDER BY FECHA_EVENTO, "
                                + "ID_HISTORIAL_ENTREGA",
                        (rs, rowNum) ->
                                rs.getString(
                                        "TIPO_EVENTO"
                                ),
                        idEntrega
                );


        int descargaEvento =
                eventos.indexOf(
                        "DESCARGA_LOTE_COMPLETADA"
                );

        int acuseEvento =
                eventos.indexOf(
                        "ACUSE_REGISTRADO"
                );

        int publicacionEvento =
                eventos.indexOf(
                        "PUBLICACION_DRIVE_COMPLETADA"
                );
        assertTrue(
                descargaEvento >= 0
        );

        assertTrue(
                acuseEvento > descargaEvento
        );

        assertTrue(
                publicacionEvento > acuseEvento
        );


        /*
         * ======================================================
         * REINTENTO / IDEMPOTENCIA
         * ======================================================
         */

        ConfirmarAcuseEntregaResponse reintento =
                entregaService
                        .confirmarAcusePorToken(
                                token,
                                "127.0.0.1",
                                "A47-MAPFRE-E2E-RETRY"
                        );

        assertNotNull(
                reintento
        );

        assertTrue(
                reintento.isAcuseRegistrado()
        );

        assertTrue(
                reintento.isAccesoDisponible()
        );


        File periodoReintento =
                googleDriveService
                        .obtenerInformacionArchivo(
                                carpetaPeriodo
                                        .getId()
                        );

        assertEquals(
                carpetaPeriodo.getId(),
                periodoReintento.getId()
        );

        assertEquals(
                parentFinalId,
                periodoReintento
                        .getParents()
                        .get(0)
        );


        Integer publicaciones =
                jdbc.queryForObject(
                        "SELECT COUNT(*) "
                                + "FROM HISTORIAL_ENTREGA_LOTE "
                                + "WHERE ID_ENTREGA = ? "
                                + "AND TIPO_EVENTO = "
                                + "'PUBLICACION_DRIVE_COMPLETADA'",
                        Integer.class,
                        idEntrega
                );

        assertNotNull(
                publicaciones
        );

        assertEquals(
                1,
                publicaciones
        );


        /*
         * ======================================================
         * MARCADORES SEGUROS
         * ======================================================
         *
         * Nunca imprimir token, IDs Drive ni credenciales.
         */

        System.out.println(
                "A47_E2E_REAL_OK=true"
        );

        System.out.println(
                "A47_MAPFRE_244_OK=true"
        );

        System.out.println(
                "A47_ZIP_REAL_OK=true"
        );

        System.out.println(
                "A47_DOWNLOAD_GATE_OK=true"
        );

        System.out.println(
                "A47_ACUSE_ORACLE_OK=true"
        );

        System.out.println(
                "A47_DRIVE_MOVE_OK=true"
        );

        System.out.println(
                "A47_SAME_DRIVE_ID_OK=true"
        );

        System.out.println(
                "A47_TRACE_ORDER_OK=true"
        );

        System.out.println(
                "A47_IDEMPOTENCIA_OK=true"
        );
    }


    private GoogleDriveProperties construirDriveProperties() {

        GoogleDriveProperties properties =
                new GoogleDriveProperties();

        properties.setProjectId(
                env(
                        "GOOGLE_DRIVE_PROJECT_ID"
                )
        );

        properties.setClientEmail(
                env(
                        "GOOGLE_DRIVE_CLIENT_EMAIL"
                )
        );

        properties.setPrivateKey(
                env(
                        "GOOGLE_DRIVE_PRIVATE_KEY"
                )
        );

        properties.setClientId(
                env(
                        "GOOGLE_DRIVE_CLIENT_ID"
                )
        );

        properties.setFolderId(
                env(
                        "VIDA_E2E_DRIVE_ROOT_ID"
                )
        );

        return properties;
    }


    private JdbcTemplate construirJdbcTemplate() {

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setDriverClassName(
                "oracle.jdbc.OracleDriver"
        );

        dataSource.setUrl(
                env(
                        "DB_URL"
                )
        );

        dataSource.setUsername(
                env(
                        "DB_USERNAME"
                )
        );

        dataSource.setPassword(
                env(
                        "DB_PASSWORD"
                )
        );

        return new JdbcTemplate(
                dataSource
        );
    }


    private byte[] crearPdfReal()
            throws Exception {

        try (
                PDDocument document =
                        new PDDocument();

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            document.addPage(
                    new PDPage()
            );

            document.save(
                    out
            );

            return out.toByteArray();
        }
    }


    private byte[] crearXlsxReal()
            throws Exception {

        try (
                XSSFWorkbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            workbook
                    .createSheet(
                            "MAPFRE"
                    )
                    .createRow(
                            0
                    )
                    .createCell(
                            0
                    )
                    .setCellValue(
                            "E2E A4.7"
                    );

            workbook.write(
                    out
            );

            return out.toByteArray();
        }
    }


    private Set<String> leerEntradasZip(
            byte[] contenido
    ) throws Exception {

        Set<String> entradas =
                new HashSet<>();

        try (
                ZipInputStream zip =
                        new ZipInputStream(
                                new ByteArrayInputStream(
                                        contenido
                                )
                        )
        ) {

            ZipEntry entry;

            while (
                    (entry = zip.getNextEntry())
                            != null
            ) {

                entradas.add(
                        entry.getName()
                );
            }
        }

        return entradas;
    }


    private boolean contieneExtension(
            Set<String> nombres,
            String extension
    ) {

        for (String nombre : nombres) {

            if (
                    nombre != null
                            &&
                    nombre
                            .toLowerCase()
                            .endsWith(
                                    extension
                            )
            ) {
                return true;
            }
        }

        return false;
    }


    private String env(
            String nombre
    ) {

        String valor =
                System.getenv(
                        nombre
                );

        if (
                valor == null
                        ||
                valor.trim().isEmpty()
        ) {
            throw new IllegalStateException(
                    "Variable E2E requerida ausente: "
                            + nombre
            );
        }

        return valor.trim();
    }
}