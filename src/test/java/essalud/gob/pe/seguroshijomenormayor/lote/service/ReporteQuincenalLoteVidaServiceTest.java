package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;

import essalud.gob.pe.seguroshijomenormayor.config.GoogleDriveProperties;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteLoteVidaItem;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoReporteLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.repository.ReporteLoteVidaRepository;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

class ReporteQuincenalLoteVidaServiceTest {

    @Test
    void generaPipelineCompletoMapfre()
            throws Exception {

        /*
         * ======================================================
         * BORDES EXTERNOS
         * ======================================================
         */

        GoogleDriveService googleDriveService =
                mock(
                        GoogleDriveService.class
                );

        GoogleDriveProperties googleDriveProperties =
                mock(
                        GoogleDriveProperties.class
                );

        when(
                googleDriveProperties.getFolderId()
        ).thenReturn(
                "ROOT-MAS-VIDA"
        );

        ReporteLoteVidaRepository repository =
                mock(
                        ReporteLoteVidaRepository.class
                );

        /*
         * ======================================================
         * SERVICIOS PRODUCTIVOS REALES
         * ======================================================
         */

        ExtractorDocumentosDriveLoteVidaService extractor =
                new ExtractorDocumentosDriveLoteVidaService();

        GeneradorExcelLoteVidaService generador =
                new GeneradorExcelLoteVidaService();

        CierreDriveLoteVidaService cierreDrive =
                new CierreDriveLoteVidaService(
                        googleDriveService,
                        googleDriveProperties
                );

        ReporteQuincenalLoteVidaService service =
                new ReporteQuincenalLoteVidaService(
                        googleDriveService,
                        extractor,
                        repository,
                        generador,
                        cierreDrive
                );

        LocalDate inicio =
                LocalDate.of(
                        2096,
                        9,
                        1
                );

        LocalDate fin =
                LocalDate.of(
                        2096,
                        9,
                        15
                );

        /*
         * ======================================================
         * CARPETA DRIVE
         * ======================================================
         */

        File carpeta =
                new File();

        carpeta.setId(
                "FOLDER-MAPFRE-2096"
        );

        carpeta.setName(
                "2096-09-01_2096-09-15"
        );

        carpeta.setMimeType(
                "application/vnd.google-apps.folder"
        );

        File carpetaDestinoFinal =
                new File();

        carpetaDestinoFinal.setId(
                "FINAL-MAPFRE"
        );

        carpetaDestinoFinal.setName(
                "Seguro +Vida - Afiliaciones MAPFRE"
        );

        carpetaDestinoFinal.setMimeType(
                "application/vnd.google-apps.folder"
        );

        when(
                googleDriveService
                        .obtenerOCrearCarpeta(
                                "Seguro +Vida - Afiliaciones MAPFRE",
                                "ROOT-MAS-VIDA"
                        )
        ).thenReturn(
                carpetaDestinoFinal
        );

        when(
                googleDriveService
                        .buscarCarpetas(
                                "2096-09-01_2096-09-15",
                                "FINAL-MAPFRE"
                        )
        ).thenReturn(
                List.of()
        );

        when(
                googleDriveService
                        .obtenerCarpetaPeriodo(
                                "244",
                                inicio,
                                fin
                        )
        ).thenReturn(
                carpeta
        );

        /*
         * ======================================================
         * PDF 6012 EN DRIVE
         * ======================================================
         */

        File pdf =
                new File();

        pdf.setId(
                "PDF-6012-12345678"
        );

        pdf.setName(
                "Formulario6012Sellado.pdf"
        );

        pdf.setMimeType(
                "application/pdf"
        );

        pdf.setWebViewLink(
                "https://drive.google.com/file/d/PDF-6012-12345678/view"
        );

        pdf.setAppProperties(
                Map.of(
                        "idTpDoc",
                        "244",

                        "tpDocument",
                        "01",

                        "numDocument",
                        "12345678",

                        "nombreOriginal",
                        "Formulario6012Sellado.pdf"
                )
        );

        /*
         * Simula un Excel generado por una ejecución previa.
         *
         * El extractor real debe ignorarlo.
         */
        File excelPrevio =
                new File();

        excelPrevio.setId(
                "EXCEL-PREVIO"
        );

        excelPrevio.setName(
                "Reporte_MAPFRE_0109_1509.xlsx"
        );

        excelPrevio.setMimeType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        when(
                googleDriveService
                        .listarArchivosCarpeta(
                                "FOLDER-MAPFRE-2096"
                        )
        ).thenReturn(
                List.of(
                        pdf,
                        excelPrevio
                )
        );

        /*
         * ======================================================
         * ORACLE +VIDA
         * ======================================================
         *
         * La fecha de afiliación proviene de la publicación
         * de AUTORIZACION_DESCUENTO del mismo proceso.
         */

        ReporteLoteVidaItem item =
                new ReporteLoteVidaItem();

        item.setTipoDocumentoTitular(
                "01"
        );

        item.setNumeroDocumentoTitular(
                "12345678"
        );

        item.setPrimerNombreTitular(
                "JUAN"
        );

        item.setSegundoNombreTitular(
                "CARLOS"
        );

        item.setApellidoPaternoTitular(
                "PEREZ"
        );

        item.setApellidoMaternoTitular(
                "QUISPE"
        );

        item.setRegistroInternoProceso(
                "VIDA-2096-PRUEBA-MAPFRE"
        );

        item.setTipoDocumentoLogico(
                "FORMULARIO_6012"
        );

        item.setCantidadBeneficiarios(
                3
        );

        item.setIdDocumentoPublicado(
                "DOC-PUB-6012"
        );

        item.setNombreArchivoFinal(
                "6012-publicado.pdf"
        );

        item.setFechaPublicacionDocumento(
                LocalDateTime.of(
                        2096,
                        9,
                        10,
                        10,
                        30
                )
        );

        /*
         * Regla funcional confirmada:
         *
         * fecha afiliación =
         * fecha publicación AUTORIZACION_DESCUENTO.
         */
        item.setFechaAfiliacion(
                LocalDateTime.of(
                        2096,
                        9,
                        8,
                        16,
                        45
                )
        );

        when(
                repository
                        .buscarDocumentoPublicado(
                                "01",
                                "12345678",
                                "FORMULARIO_6012",
                                inicio,
                                fin
                        )
        ).thenReturn(
                Optional.of(
                        item
                )
        );

        /*
         * ======================================================
         * RESULTADO DE SUBIDA DEL REPORTE A DRIVE
         * ======================================================
         */

        File reporteDrive =
                new File();

        reporteDrive.setId(
                "EXCEL-MAPFRE-NUEVO"
        );

        reporteDrive.setName(
                "Reporte_MAPFRE_0109_1509.xlsx"
        );

        reporteDrive.setMimeType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        reporteDrive.setWebViewLink(
                "https://drive.google.com/file/d/EXCEL-MAPFRE-NUEVO/view"
        );

        when(
                googleDriveService
                        .guardarOActualizarArchivoEnCarpeta(
                                any(byte[].class),
                                eq(
                                        "Reporte_MAPFRE_0109_1509.xlsx"
                                ),
                                eq(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                ),
                                eq(
                                        "FOLDER-MAPFRE-2096"
                                )
                        )
        ).thenReturn(
                reporteDrive
        );

        when(
                googleDriveService
                        .obtenerInformacionArchivo(
                                "FOLDER-MAPFRE-2096"
                        )
        ).thenReturn(
                carpeta
        );

        File carpetaMovida =
                new File();

        carpetaMovida.setId(
                "FOLDER-MAPFRE-2096"
        );

        carpetaMovida.setName(
                "2096-09-01_2096-09-15"
        );

        carpetaMovida.setMimeType(
                "application/vnd.google-apps.folder"
        );

        carpetaMovida.setWebViewLink(
                "https://drive.google.com/drive/folders/FOLDER-MAPFRE-2096"
        );

        when(
                googleDriveService
                        .moverArchivo(
                                "FOLDER-MAPFRE-2096",
                                "FINAL-MAPFRE"
                        )
        ).thenReturn(
                carpetaMovida
        );

        /*
         * ======================================================
         * EJECUCIÓN DEL PIPELINE COMPLETO
         * ======================================================
         */

        ResultadoReporteLoteVida resultado =
                service.generar(
                        "MAPFRE",
                        inicio,
                        fin
                );

        /*
         * ======================================================
         * RESULTADO FUNCIONAL
         * ======================================================
         */

        assertEquals(
                "MAPFRE",
                resultado.getDestinatario()
        );

        assertEquals(
                inicio,
                resultado.getFechaInicio()
        );

        assertEquals(
                fin,
                resultado.getFechaFin()
        );

        assertEquals(
                1,
                resultado.getCantidadDocumentos()
        );

        assertEquals(
                "FOLDER-MAPFRE-2096",
                resultado.getCarpetaDriveId()
        );

        assertEquals(
                "EXCEL-MAPFRE-NUEVO",
                resultado.getReporteDriveId()
        );

        assertEquals(
                "Reporte_MAPFRE_0109_1509.xlsx",
                resultado.getNombreReporte()
        );

        assertEquals(
                "https://drive.google.com/file/d/EXCEL-MAPFRE-NUEVO/view",
                resultado.getUrlReporteDrive()
        );

        /*
         * ======================================================
         * CONTRATOS INVOCADOS
         * ======================================================
         */

        verify(
                googleDriveService,
                times(1)
        ).obtenerCarpetaPeriodo(
                "244",
                inicio,
                fin
        );

        verify(
                googleDriveService,
                times(2)
        ).obtenerOCrearCarpeta(
                "Seguro +Vida - Afiliaciones MAPFRE",
                "ROOT-MAS-VIDA"
        );

        verify(
                googleDriveService,
                times(1)
        ).moverArchivo(
                "FOLDER-MAPFRE-2096",
                "FINAL-MAPFRE"
        );

        verify(
                repository,
                times(1)
        ).buscarDocumentoPublicado(
                "01",
                "12345678",
                "FORMULARIO_6012",
                inicio,
                fin
        );

        /*
         * ======================================================
         * CAPTURAR XLSX REAL QUE EL PIPELINE INTENTÓ SUBIR
         * ======================================================
         */

        ArgumentCaptor<byte[]> contenidoCaptor =
                ArgumentCaptor.forClass(
                        byte[].class
                );

        verify(
                googleDriveService,
                times(1)
        ).guardarOActualizarArchivoEnCarpeta(
                contenidoCaptor.capture(),
                eq(
                        "Reporte_MAPFRE_0109_1509.xlsx"
                ),
                eq(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ),
                eq(
                        "FOLDER-MAPFRE-2096"
                )
        );

        byte[] contenidoExcel =
                contenidoCaptor.getValue();

        assertNotNull(
                contenidoExcel
        );

        assertTrue(
                contenidoExcel.length > 0
        );

        /*
         * ======================================================
         * ABRIR EL XLSX GENERADO REALMENTE
         * ======================================================
         */

        try (
                Workbook workbook =
                        WorkbookFactory.create(
                                new ByteArrayInputStream(
                                        contenidoExcel
                                )
                        )
        ) {

            Sheet hoja =
                    workbook.getSheet(
                            "MAPFRE"
                    );

            assertNotNull(
                    hoja
            );

            /*
             * título + cabecera + una fila
             */
            assertEquals(
                    3,
                    hoja.getPhysicalNumberOfRows()
            );

            Row cabecera =
                    hoja.getRow(1);

            assertEquals(
                    "DNI titular",
                    cabecera
                            .getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    "Titular: Nombres y apellidos",
                    cabecera
                            .getCell(1)
                            .getStringCellValue()
            );

            assertEquals(
                    "Fecha de afiliación",
                    cabecera
                            .getCell(2)
                            .getStringCellValue()
            );

            assertEquals(
                    "N.º beneficiarios",
                    cabecera
                            .getCell(3)
                            .getStringCellValue()
            );

            assertEquals(
                    "Registro interno +Vida",
                    cabecera
                            .getCell(4)
                            .getStringCellValue()
            );

            assertEquals(
                    "Ver PDF",
                    cabecera
                            .getCell(5)
                            .getStringCellValue()
            );

            Row datos =
                    hoja.getRow(2);

            assertEquals(
                    "12345678",
                    datos
                            .getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    "JUAN CARLOS PEREZ QUISPE",
                    datos
                            .getCell(1)
                            .getStringCellValue()
            );

            /*
             * Debe usar fecha de publicación
             * de la AUTORIZACIÓN, no del 6012.
             */
            Cell fechaAfiliacion =
                    datos.getCell(2);

            assertTrue(
                    DateUtil.isCellDateFormatted(
                            fechaAfiliacion
                    )
            );

            assertEquals(
                    LocalDate.of(
                            2096,
                            9,
                            8
                    ),
                    fechaAfiliacion
                            .getLocalDateTimeCellValue()
                            .toLocalDate()
            );

            assertNotEquals(
                    LocalDate.of(
                            2096,
                            9,
                            10
                    ),
                    fechaAfiliacion
                            .getLocalDateTimeCellValue()
                            .toLocalDate()
            );

            assertEquals(
                    3,
                    (int) datos
                            .getCell(3)
                            .getNumericCellValue()
            );

            assertEquals(
                    "VIDA-2096-PRUEBA-MAPFRE",
                    datos
                            .getCell(4)
                            .getStringCellValue()
            );

            Cell enlace =
                    datos.getCell(5);

            assertEquals(
                    "Ver PDF",
                    enlace.getStringCellValue()
            );

            assertNotNull(
                    enlace.getHyperlink()
            );

            assertEquals(
                    "https://drive.google.com/file/d/PDF-6012-12345678/view",
                    enlace
                            .getHyperlink()
                            .getAddress()
            );
        }

        /*
         * Solamente debe haberse consultado
         * un PDF válido.
         *
         * El Excel previo listado en la carpeta
         * fue ignorado por el extractor.
         */
        verifyNoMoreInteractions(
                repository
        );
    }
}