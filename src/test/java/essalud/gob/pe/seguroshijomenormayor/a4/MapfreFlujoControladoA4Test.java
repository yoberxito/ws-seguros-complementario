package essalud.gob.pe.seguroshijomenormayor.a4;

import com.google.api.services.drive.model.File;

import essalud.gob.pe.seguroshijomenormayor.entrega.dto.ConfirmarAcuseEntregaResponse;
import essalud.gob.pe.seguroshijomenormayor.entrega.exception.EstadoEntregaException;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.DescargaLotePreparada;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.EntregaLote;
import essalud.gob.pe.seguroshijomenormayor.entrega.repository.EntregaLoteRepository;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.DescargaLoteService;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.EntregaLoteService;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.EntregaLoteTransicionService;

import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoCierreDrive;
import essalud.gob.pe.seguroshijomenormayor.lote.service.CierreDriveLoteVidaService;
import essalud.gob.pe.seguroshijomenormayor.lote.service.PublicacionDrivePersonalService;

import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;


class MapfreFlujoControladoA4Test {

    private static final String TOKEN =
            "TOKEN-A4-MAPFRE";

    private static final String FOLDER_ID =
            "FOLDER_MAPFRE_A4";

    private static final String URL_ACCESO =
            "https://drive.google.com/drive/folders/"
                    + FOLDER_ID;

    private static final LocalDate INICIO =
            LocalDate.of(
                    2099,
                    3,
                    4
            );

    private static final LocalDate FIN =
            LocalDate.of(
                    2099,
                    3,
                    18
            );


    @Test
    void descargaMapfreGeneraZipCon244YExcel()
            throws Exception {

        EntregaLoteRepository repository =
                mock(
                        EntregaLoteRepository.class
                );

        GoogleDriveService drive =
                mock(
                        GoogleDriveService.class
                );

        DescargaLoteService service =
                new DescargaLoteService(
                        repository,
                        drive
                );


        EntregaLote entrega =
                crearEntregaMapfre();

        when(
                repository
                        .buscarPorTokenHash(
                                anyString()
                        )
        ).thenReturn(
                Optional.of(
                        entrega
                )
        );


        File carpeta =
                new File();

        carpeta.setId(
                FOLDER_ID
        );

        carpeta.setName(
                "2099-03-04_2099-03-18"
        );

        carpeta.setMimeType(
                "application/vnd.google-apps.folder"
        );


        File pdf244 =
                new File();

        pdf244.setId(
                "PDF-244-A4"
        );

        pdf244.setName(
                "Formulario6012Sellado.pdf"
        );

        pdf244.setMimeType(
                "application/pdf"
        );

        pdf244.setAppProperties(
                Map.of(
                        "idTpDoc",
                        "244"
                )
        );


        File excel =
                new File();

        excel.setId(
                "EXCEL-A4"
        );

        excel.setName(
                "Reporte_MAPFRE_0403_1803.xlsx"
        );

        excel.setMimeType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );


        when(
                drive
                        .obtenerInformacionArchivo(
                                FOLDER_ID
                        )
        ).thenReturn(
                carpeta
        );


        when(
                drive
                        .listarArchivosCarpeta(
                                FOLDER_ID
                        )
        ).thenReturn(
                List.of(
                        pdf244,
                        excel
                )
        );


        byte[] pdf =
                "%PDF-1.4\nA4 MAPFRE\n%%EOF"
                        .getBytes(
                                StandardCharsets.US_ASCII
                        );

        byte[] xlsx =
                crearXlsxReal();


        when(
                drive
                        .descargarArchivo(
                                "PDF-244-A4"
                        )
        ).thenReturn(
                pdf
        );


        when(
                drive
                        .descargarArchivo(
                                "EXCEL-A4"
                        )
        ).thenReturn(
                xlsx
        );


        DescargaLotePreparada resultado =
                service
                        .prepararDescargaPorToken(
                                TOKEN
                        );


        assertNotNull(
                resultado
        );

        assertEquals(
                "Lote_Mas_Vida_MAPFRE_2099-03-04_2099-03-18.zip",
                resultado.getNombreArchivo()
        );


        Set<String> entradas =
                leerEntradasZip(
                        resultado.getContenido()
                );


        assertEquals(
                2,
                entradas.size()
        );

        assertTrue(
                entradas.contains(
                        "Formulario6012Sellado.pdf"
                )
        );

        assertTrue(
                entradas.contains(
                        "Reporte_MAPFRE_0403_1803.xlsx"
                )
        );


        verify(
                drive,
                times(1)
        ).descargarArchivo(
                "PDF-244-A4"
        );


        verify(
                drive,
                times(1)
        ).descargarArchivo(
                "EXCEL-A4"
        );
    }


    @Test
    void descargaMapfreRechazaDocumento247()
            throws Exception {

        EntregaLoteRepository repository =
                mock(
                        EntregaLoteRepository.class
                );

        GoogleDriveService drive =
                mock(
                        GoogleDriveService.class
                );

        DescargaLoteService service =
                new DescargaLoteService(
                        repository,
                        drive
                );


        when(
                repository
                        .buscarPorTokenHash(
                                anyString()
                        )
        ).thenReturn(
                Optional.of(
                        crearEntregaMapfre()
                )
        );


        File carpeta =
                new File();

        carpeta.setId(
                FOLDER_ID
        );

        carpeta.setMimeType(
                "application/vnd.google-apps.folder"
        );


        File documento247 =
                new File();

        documento247.setId(
                "PDF-247-A4"
        );

        documento247.setName(
                "AutorizacionDescuento.pdf"
        );

        documento247.setAppProperties(
                Map.of(
                        "idTpDoc",
                        "247"
                )
        );


        File excel =
                new File();

        excel.setId(
                "EXCEL-A4"
        );

        excel.setName(
                "Reporte_MAPFRE.xlsx"
        );


        when(
                drive
                        .obtenerInformacionArchivo(
                                FOLDER_ID
                        )
        ).thenReturn(
                carpeta
        );


        when(
                drive
                        .listarArchivosCarpeta(
                                FOLDER_ID
                        )
        ).thenReturn(
                List.of(
                        documento247,
                        excel
                )
        );


        assertThrows(
                IllegalStateException.class,
                () ->
                        service
                                .prepararDescargaPorToken(
                                        TOKEN
                                )
        );
    }


    @Test
    void acuseMapfreSeBloqueaAntesDeDescarga() {

        EntregaLoteRepository repository =
                mock(
                        EntregaLoteRepository.class
                );

        EntregaLoteTransicionService transicion =
                mock(
                        EntregaLoteTransicionService.class
                );

        PublicacionDrivePersonalService publicacionPersonal =
                mock(
                        PublicacionDrivePersonalService.class
                );

        CierreDriveLoteVidaService cierreDrive =
                mock(
                        CierreDriveLoteVidaService.class
                );


        EntregaLoteService service =
                new EntregaLoteService(
                        repository,
                        transicion,
                        publicacionPersonal,
                        cierreDrive
                );


        EntregaLote entrega =
                crearEntregaMapfre();


        when(
                repository
                        .buscarPorTokenHash(
                                anyString()
                        )
        ).thenReturn(
                Optional.of(
                        entrega
                )
        );


        when(
                transicion
                        .existeDescargaLoteCompletada(
                                anyString()
                        )
        ).thenReturn(
                false
        );


        assertThrows(
                EstadoEntregaException.class,
                () ->
                        service
                                .confirmarAcusePorToken(
                                        TOKEN,
                                        "127.0.0.1",
                                        "A4"
                                )
        );


        verify(
                transicion,
                never()
        ).registrarAcuseConHistorial(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );


        verifyNoInteractions(
                cierreDrive
        );
    }


    @Test
    void acuseMapfrePersisteAntesDeDerivarDrive()
            throws Exception {

        EntregaLoteRepository repository =
                mock(
                        EntregaLoteRepository.class
                );

        EntregaLoteTransicionService transicion =
                mock(
                        EntregaLoteTransicionService.class
                );

        PublicacionDrivePersonalService publicacionPersonal =
                mock(
                        PublicacionDrivePersonalService.class
                );

        CierreDriveLoteVidaService cierreDrive =
                mock(
                        CierreDriveLoteVidaService.class
                );


        EntregaLoteService service =
                new EntregaLoteService(
                        repository,
                        transicion,
                        publicacionPersonal,
                        cierreDrive
                );


        EntregaLote pendiente =
                crearEntregaMapfre();


        EntregaLote registrada =
                crearEntregaMapfre();

        registrada.setFechaAcuse(
                LocalDateTime.of(
                        2099,
                        3,
                        19,
                        8,
                        30
                )
        );

        registrada.setTextoAcuse(
                "Acuse MAPFRE A4"
        );

        registrada.setVersionTextoAcuse(
                "V1"
        );


        when(
                repository
                        .buscarPorTokenHash(
                                anyString()
                        )
        ).thenReturn(
                Optional.of(
                        pendiente
                ),
                Optional.of(
                        registrada
                )
        );


        when(
                transicion
                        .existeDescargaLoteCompletada(
                                anyString()
                        )
        ).thenReturn(
                true
        );


        when(
                transicion
                        .registrarAcuseConHistorial(
                                anyString(),
                                anyString(),
                                anyString(),
                                anyString(),
                                anyString()
                        )
        ).thenReturn(
                true
        );


        when(
                transicion
                        .existePublicacionDriveCompletada(
                                anyString()
                        )
        ).thenReturn(
                false,
                true,
                true
        );


        when(
                cierreDrive
                        .cerrarCarpetaPeriodo(
                                eq("MAPFRE"),
                                eq(INICIO),
                                eq(FIN),
                                eq(FOLDER_ID),
                                eq(1)
                        )
        ).thenReturn(
                new ResultadoCierreDrive(
                        FOLDER_ID,
                        "2099-03-04_2099-03-18",
                        URL_ACCESO,
                        1
                )
        );


        ConfirmarAcuseEntregaResponse response =
                service
                        .confirmarAcusePorToken(
                                TOKEN,
                                "127.0.0.1",
                                "A4"
                        );


        assertNotNull(
                response
        );

        assertTrue(
                response.isAcuseRegistrado()
        );

        assertTrue(
                response.isAccesoDisponible()
        );


        InOrder orden =
                inOrder(
                        transicion,
                        cierreDrive
                );


        orden.verify(
                transicion
        ).registrarAcuseConHistorial(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );


        orden.verify(
                cierreDrive
        ).cerrarCarpetaPeriodo(
                "MAPFRE",
                INICIO,
                FIN,
                FOLDER_ID,
                1
        );


        orden.verify(
                transicion
        ).registrarPublicacionDriveCompletada(
                anyString(),
                anyString(),
                anyString()
        );


        verify(
                publicacionPersonal,
                never()
        ).publicarPeriodoPersonal(
                anyString(),
                eq(INICIO),
                eq(FIN)
        );
    }


    private EntregaLote crearEntregaMapfre() {

        EntregaLote entrega =
                new EntregaLote();

        entrega.setTipoDestinatario(
                "MAPFRE"
        );

        entrega.setCantidadDocumentos(
                1
        );

        entrega.setUrlAcceso(
                URL_ACCESO
        );

        entrega.setFechaInicioPeriodo(
                INICIO
        );

        entrega.setFechaFinPeriodo(
                FIN
        );

        entrega.setFechaPublicacion(
                LocalDateTime.of(
                        2099,
                        3,
                        19,
                        8,
                        0
                )
        );

        return entrega;
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
                            "A4"
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

        Set<String> nombres =
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

                nombres.add(
                        entry.getName()
                );
            }
        }

        return nombres;
    }
}