package essalud.gob.pe.wsseguroscomplementario.entrega.service;

import essalud.gob.pe.wsseguroscomplementario.entrega.model.FilaReporteLoteVida;
import essalud.gob.pe.wsseguroscomplementario.entrega.model.ReporteExcelLoteVida;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GeneradorExcelLoteVidaService {

    public static final String DESTINO_MAPFRE =
            "MAPFRE";

    public static final String DESTINO_PERSONAL =
            "PERSONAL";

    public static final String CONTENT_TYPE_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final DateTimeFormatter
            FORMATO_NOMBRE_FECHA =
            DateTimeFormatter.ofPattern("ddMM");

    public ReporteExcelLoteVida generar(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<FilaReporteLoteVida> filas
    ) {

        String destino =
                normalizarDestino(
                        destinatario
                );

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        validarFilas(
                destino,
                filas
        );

        boolean esMapfre =
                DESTINO_MAPFRE.equals(
                        destino
                );

        int cantidadColumnas =
                esMapfre
                        ? 6
                        : 5;

        try (
                Workbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream salida =
                        new ByteArrayOutputStream()
        ) {

            Sheet hoja =
                    workbook.createSheet(
                            esMapfre
                                    ? "MAPFRE"
                                    : "PERSONAL"
                    );

            CellStyle estiloTitulo =
                    crearEstiloTitulo(
                            workbook
                    );

            CellStyle estiloCabecera =
                    crearEstiloCabecera(
                            workbook
                    );

            CellStyle estiloTexto =
                    crearEstiloTexto(
                            workbook
                    );

            CellStyle estiloFecha =
                    crearEstiloFecha(
                            workbook
                    );

            CellStyle estiloEnlace =
                    crearEstiloEnlace(
                            workbook
                    );

            crearTitulo(
                    hoja,
                    estiloTitulo,
                    destino,
                    fechaInicio,
                    fechaFin,
                    cantidadColumnas
            );

            crearCabecera(
                    hoja,
                    estiloCabecera,
                    esMapfre
            );

            CreationHelper creationHelper =
                    workbook
                            .getCreationHelper();

            int filaExcel = 2;

            for (
                    FilaReporteLoteVida fila
                    : filas
            ) {

                crearFila(
                        hoja,
                        filaExcel,
                        fila,
                        esMapfre,
                        estiloTexto,
                        estiloFecha,
                        estiloEnlace,
                        creationHelper
                );

                filaExcel++;
            }

            /*
             * Mantener visibles título y cabecera.
             */
            hoja.createFreezePane(
                    0,
                    2
            );

            /*
             * El filtro se aplica únicamente a la tabla.
             */
            if (!filas.isEmpty()) {

                hoja.setAutoFilter(
                        new CellRangeAddress(
                                1,
                                filaExcel - 1,
                                0,
                                cantidadColumnas - 1
                        )
                );
            }

            ajustarColumnas(
                    hoja,
                    cantidadColumnas
            );

            workbook.write(
                    salida
            );

            String nombreArchivo =
                    construirNombreArchivo(
                            destino,
                            fechaInicio,
                            fechaFin
                    );

            return new ReporteExcelLoteVida(
                    nombreArchivo,
                    CONTENT_TYPE_XLSX,
                    salida.toByteArray()
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No fue posible generar el reporte Excel del lote +Vida.",
                    e
            );
        }
    }

    private void crearTitulo(
            Sheet hoja,
            CellStyle estilo,
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            int cantidadColumnas
    ) {

        Row fila =
                hoja.createRow(0);

        Cell celda =
                fila.createCell(0);

        celda.setCellValue(
                "Reporte quincenal +Vida - "
                        + destinatario
                        + " - "
                        + fechaInicio
                        + " al "
                        + fechaFin
        );

        celda.setCellStyle(
                estilo
        );

        hoja.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        cantidadColumnas - 1
                )
        );

        fila.setHeightInPoints(
                24
        );
    }

    private void crearCabecera(
            Sheet hoja,
            CellStyle estilo,
            boolean esMapfre
    ) {

        Row fila =
                hoja.createRow(1);

        String[] columnas =
                esMapfre
                        ? new String[]{
                            "DNI titular",
                            "Titular: Nombres y apellidos",
                            "Fecha de afiliación",
                            "N.º beneficiarios",
                            "Registro interno +Vida",
                            "Ver PDF"
                        }
                        : new String[]{
                            "DNI titular",
                            "Titular: Nombres y apellidos",
                            "Fecha de afiliación",
                            "Registro interno +Vida",
                            "Ver PDF"
                        };

        for (
                int i = 0;
                i < columnas.length;
                i++
        ) {

            Cell celda =
                    fila.createCell(i);

            celda.setCellValue(
                    columnas[i]
            );

            celda.setCellStyle(
                    estilo
            );
        }

        fila.setHeightInPoints(
                30
        );
    }

    private void crearFila(
            Sheet hoja,
            int indiceFila,
            FilaReporteLoteVida dato,
            boolean esMapfre,
            CellStyle estiloTexto,
            CellStyle estiloFecha,
            CellStyle estiloEnlace,
            CreationHelper creationHelper
    ) {

        Row fila =
                hoja.createRow(
                        indiceFila
                );

        int columna = 0;

        Cell dni =
                fila.createCell(
                        columna++
                );

        /*
         * DNI se escribe como texto para evitar
         * pérdida de ceros iniciales.
         */
        dni.setCellValue(
                dato
                        .getNumeroDocumentoTitular()
                        .trim()
        );

        dni.setCellStyle(
                estiloTexto
        );


        Cell nombres =
                fila.createCell(
                        columna++
                );

        nombres.setCellValue(
                dato
                        .getNombresApellidosTitular()
                        .trim()
        );

        nombres.setCellStyle(
                estiloTexto
        );


        Cell fecha =
                fila.createCell(
                        columna++
                );

        fecha.setCellValue(
                Date.valueOf(
                        dato.getFechaAfiliacion()
                )
        );

        fecha.setCellStyle(
                estiloFecha
        );


        if (esMapfre) {

            Cell beneficiarios =
                    fila.createCell(
                            columna++
                    );

            beneficiarios.setCellValue(
                    dato
                            .getCantidadBeneficiarios()
            );

            beneficiarios.setCellStyle(
                    estiloTexto
            );
        }


        Cell registro =
                fila.createCell(
                        columna++
                );

        registro.setCellValue(
                dato
                        .getRegistroInternoProceso()
                        .trim()
        );

        registro.setCellStyle(
                estiloTexto
        );


        Cell enlace =
                fila.createCell(
                        columna
                );

        enlace.setCellValue(
                "Ver PDF"
        );

        var hyperlink =
                creationHelper.createHyperlink(
                        HyperlinkType.URL
                );

        hyperlink.setAddress(
                dato
                        .getUrlPdf()
                        .trim()
        );

        enlace.setHyperlink(
                hyperlink
        );

        enlace.setCellStyle(
                estiloEnlace
        );
    }

    private CellStyle crearEstiloTitulo(
            Workbook workbook
    ) {

        CellStyle estilo =
                workbook.createCellStyle();

        Font fuente =
                workbook.createFont();

        fuente.setBold(true);
        fuente.setFontHeightInPoints(
                (short) 14
        );

        estilo.setFont(
                fuente
        );

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        estilo.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        return estilo;
    }

    private CellStyle crearEstiloCabecera(
            Workbook workbook
    ) {

        CellStyle estilo =
                workbook.createCellStyle();

        Font fuente =
                workbook.createFont();

        fuente.setBold(true);
        fuente.setColor(
                IndexedColors.WHITE
                        .getIndex()
        );

        estilo.setFont(
                fuente
        );

        estilo.setFillForegroundColor(
                IndexedColors.DARK_BLUE
                        .getIndex()
        );

        estilo.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        estilo.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        estilo.setWrapText(
                true
        );

        aplicarBordes(
                estilo
        );

        return estilo;
    }

    private CellStyle crearEstiloTexto(
            Workbook workbook
    ) {

        CellStyle estilo =
                workbook.createCellStyle();

        estilo.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        aplicarBordes(
                estilo
        );

        return estilo;
    }

    private CellStyle crearEstiloFecha(
            Workbook workbook
    ) {

        CellStyle estilo =
                crearEstiloTexto(
                        workbook
                );

        estilo.setDataFormat(
                workbook
                        .createDataFormat()
                        .getFormat(
                                "dd/mm/yyyy"
                        )
        );

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        return estilo;
    }

    private CellStyle crearEstiloEnlace(
            Workbook workbook
    ) {

        CellStyle estilo =
                crearEstiloTexto(
                        workbook
                );

        Font fuente =
                workbook.createFont();

        fuente.setColor(
                IndexedColors.BLUE
                        .getIndex()
        );

        fuente.setUnderline(
                Font.U_SINGLE
        );

        estilo.setFont(
                fuente
        );

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        return estilo;
    }

    private void aplicarBordes(
            CellStyle estilo
    ) {

        estilo.setBorderTop(
                BorderStyle.THIN
        );

        estilo.setBorderBottom(
                BorderStyle.THIN
        );

        estilo.setBorderLeft(
                BorderStyle.THIN
        );

        estilo.setBorderRight(
                BorderStyle.THIN
        );
    }

    private void ajustarColumnas(
            Sheet hoja,
            int cantidadColumnas
    ) {

        for (
                int i = 0;
                i < cantidadColumnas;
                i++
        ) {

            hoja.autoSizeColumn(i);

            int anchoActual =
                    hoja.getColumnWidth(i);

            int anchoMaximo =
                    50 * 256;

            hoja.setColumnWidth(
                    i,
                    Math.min(
                            anchoActual + 512,
                            anchoMaximo
                    )
            );
        }
    }

    private String construirNombreArchivo(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        return "Reporte_"
                + destinatario
                + "_"
                + fechaInicio.format(
                        FORMATO_NOMBRE_FECHA
                )
                + "_"
                + fechaFin.format(
                        FORMATO_NOMBRE_FECHA
                )
                + ".xlsx";
    }

    private void validarFilas(
            String destinatario,
            List<FilaReporteLoteVida> filas
    ) {

        if (filas == null) {

            throw new IllegalArgumentException(
                    "La colección de filas del reporte es obligatoria."
            );
        }

        for (
                int i = 0;
                i < filas.size();
                i++
        ) {

            FilaReporteLoteVida fila =
                    filas.get(i);

            if (fila == null) {

                throw new IllegalArgumentException(
                        "Existe una fila vacía en el reporte."
                );
            }

            validarTexto(
                    fila.getNumeroDocumentoTitular(),
                    "DNI titular",
                    i
            );

            validarTexto(
                    fila.getNombresApellidosTitular(),
                    "Nombres y apellidos",
                    i
            );

            if (
                    fila.getFechaAfiliacion()
                            == null
            ) {

                throw new IllegalArgumentException(
                        "La fecha de afiliación es obligatoria en la fila "
                                + (i + 1)
                                + "."
                );
            }

            validarTexto(
                    fila.getRegistroInternoProceso(),
                    "Registro interno +Vida",
                    i
            );

            validarTexto(
                    fila.getUrlPdf(),
                    "URL del PDF",
                    i
            );

            String url =
                    fila.getUrlPdf()
                            .trim()
                            .toLowerCase();

            if (
                    !url.startsWith("https://")
                            && !url.startsWith("http://")
            ) {

                throw new IllegalArgumentException(
                        "La URL del PDF no es válida en la fila "
                                + (i + 1)
                                + "."
                );
            }

            if (
                    DESTINO_MAPFRE.equals(
                            destinatario
                    )
            ) {

                if (
                        fila.getCantidadBeneficiarios()
                                == null
                                || fila
                                .getCantidadBeneficiarios()
                                < 0
                ) {

                    throw new IllegalArgumentException(
                            "La cantidad de beneficiarios es obligatoria "
                                    + "para MAPFRE en la fila "
                                    + (i + 1)
                                    + "."
                    );
                }
            }
        }
    }

    private void validarTexto(
            String valor,
            String campo,
            int indice
    ) {

        if (
                valor == null
                        || valor.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    campo
                            + " es obligatorio en la fila "
                            + (indice + 1)
                            + "."
            );
        }
    }

    private void validarPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        if (
                fechaInicio == null
                        || fechaFin == null
        ) {

            throw new IllegalArgumentException(
                    "El período del reporte es obligatorio."
            );
        }

        if (
                fechaInicio.isAfter(
                        fechaFin
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }
    }

    private String normalizarDestino(
            String destinatario
    ) {

        if (
                destinatario == null
                        || destinatario
                        .trim()
                        .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El destinatario del reporte es obligatorio."
            );
        }

        String valor =
                destinatario
                        .trim()
                        .toUpperCase();

        if (
                !DESTINO_MAPFRE.equals(valor)
                        && !DESTINO_PERSONAL.equals(valor)
        ) {

            throw new IllegalArgumentException(
                    "El destinatario solamente puede ser MAPFRE o PERSONAL."
            );
        }

        return valor;
    }
}