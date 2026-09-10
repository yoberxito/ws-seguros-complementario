package essalud.gob.pe.seguroshijomenormayor.lote.repository;

import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteLoteVidaItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("masVidaJdbcReporteLoteVidaRepository")
public class JdbcReporteLoteVidaRepository
        implements ReporteLoteVidaRepository {

    private static final String
            TIPO_AUTORIZACION =
            "AUTORIZACION_DESCUENTO";

    private final JdbcTemplate jdbcTemplate;

    public JdbcReporteLoteVidaRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                jdbcTemplate;
    }

    @Override
    public Optional<ReporteLoteVidaItem>
    buscarDocumentoPublicado(
            String tipoDocumentoTitular,
            String numeroDocumentoTitular,
            String tipoDocumentoLogico,
            LocalDate fechaInicioPeriodo,
            LocalDate fechaFinPeriodo
    ) {

        validar(
                tipoDocumentoTitular,
                numeroDocumentoTitular,
                tipoDocumentoLogico,
                fechaInicioPeriodo,
                fechaFinPeriodo
        );

        /*
         * D:
         * documento que pertenece al lote que se está
         * procesando.
         *
         * MAPFRE   -> FORMULARIO_6012
         * PERSONAL -> AUTORIZACION_DESCUENTO
         *
         * A:
         * autorización del mismo proceso.
         *
         * A.FECHA_PUBLICACION es la fecha operativa
         * de afiliación definida para el reporte.
         *
         * El período se aplica únicamente a D.
         *
         * Esto es importante para FORMULARIO_6012_POSTERIOR:
         * la autorización puede haberse publicado en
         * una quincena anterior.
         */
        String sql = """
            SELECT
                T.COD_EDOCUMENT_TITULAR,
                T.NUM_DOCUMENT_TITULAR,

                T.APE_PATERNO_TITULAR,
                T.APE_MATERNO_TITULAR,
                T.PRIMER_NOMBRE_TITULAR,
                T.SEGUNDO_NOMBRE_TITULAR,

                T.FECHA_REGISTRO
                    AS FECHA_REGISTRO_PROCESO,

                T.REGISTRO_INTERNO_PROCESO,

                D.TIPO_DOCUMENTO_LOGICO,

                NVL(
                    D.CANT_BENEFICIARIOS_GENERACION,
                    0
                ) AS CANTIDAD_BENEFICIARIOS,

                D.ID_DOCUMENTO_PUBLICADO,
                D.NOMBRE_ARCHIVO_FINAL,

                D.FECHA_PUBLICACION
                    AS FECHA_PUBLICACION_DOCUMENTO,

                A.FECHA_PUBLICACION
                    AS FECHA_AFILIACION

            FROM TEMP_SECOMASVIDA T

            INNER JOIN DOCUMENTOS_SUSTENTO D
                ON D.ID_SECOMASVIDA =
                   T.ID_SECOMASVIDA

            INNER JOIN ESTADO_SUSTENTO E
                ON E.ID_ESTADO_SUSTENTO =
                   D.ID_ESTADO_SUSTENTO

            INNER JOIN DOCUMENTOS_SUSTENTO A
                ON A.ID_SECOMASVIDA =
                   T.ID_SECOMASVIDA
               AND A.TIPO_DOCUMENTO_LOGICO =
                   'AUTORIZACION_DESCUENTO'

            INNER JOIN ESTADO_SUSTENTO EA
                ON EA.ID_ESTADO_SUSTENTO =
                   A.ID_ESTADO_SUSTENTO

            WHERE
                T.COD_EDOCUMENT_TITULAR = ?

                AND T.NUM_DOCUMENT_TITULAR = ?

                AND D.TIPO_DOCUMENTO_LOGICO = ?

                AND E.COD_ESTADO_DOCUMENTO =
                    'PUBLICADO'

                AND D.ID_DOCUMENTO_PUBLICADO
                    IS NOT NULL

                AND D.HASH_DOCUMENT_FINAL
                    IS NOT NULL

                AND D.FECHA_PUBLICACION
                    IS NOT NULL

                AND D.FECHA_PUBLICACION >= ?

                AND D.FECHA_PUBLICACION < ?

                AND EA.COD_ESTADO_DOCUMENTO =
                    'PUBLICADO'

                AND A.ID_DOCUMENTO_PUBLICADO
                    IS NOT NULL

                AND A.HASH_DOCUMENT_FINAL
                    IS NOT NULL

                AND A.FECHA_PUBLICACION
                    IS NOT NULL
            """;

        Timestamp inicio =
                Timestamp.valueOf(
                        fechaInicioPeriodo
                                .atStartOfDay()
                );

        /*
         * Intervalo [inicio, fin + 1 día).
         *
         * Así incluimos cualquier hora del último día
         * sin depender de 23:59:59.
         */
        Timestamp finExclusivo =
                Timestamp.valueOf(
                        fechaFinPeriodo
                                .plusDays(1)
                                .atStartOfDay()
                );

        List<ReporteLoteVidaItem> resultados =
                jdbcTemplate.query(
                        sql,

                        (rs, rowNum) -> {

                            ReporteLoteVidaItem item =
                                    new ReporteLoteVidaItem();

                            item.setTipoDocumentoTitular(
                                    rs.getString(
                                            "COD_EDOCUMENT_TITULAR"
                                    )
                            );

                            item.setNumeroDocumentoTitular(
                                    rs.getString(
                                            "NUM_DOCUMENT_TITULAR"
                                    )
                            );

                            item.setApellidoPaternoTitular(
                                    rs.getString(
                                            "APE_PATERNO_TITULAR"
                                    )
                            );

                            item.setApellidoMaternoTitular(
                                    rs.getString(
                                            "APE_MATERNO_TITULAR"
                                    )
                            );

                            item.setPrimerNombreTitular(
                                    rs.getString(
                                            "PRIMER_NOMBRE_TITULAR"
                                    )
                            );

                            item.setSegundoNombreTitular(
                                    rs.getString(
                                            "SEGUNDO_NOMBRE_TITULAR"
                                    )
                            );

                            Timestamp fechaRegistro =
                                    rs.getTimestamp(
                                            "FECHA_REGISTRO_PROCESO"
                                    );

                            if (fechaRegistro != null) {

                                item.setFechaRegistroProceso(
                                        fechaRegistro
                                                .toLocalDateTime()
                                );
                            }

                            item.setRegistroInternoProceso(
                                    rs.getString(
                                            "REGISTRO_INTERNO_PROCESO"
                                    )
                            );

                            item.setTipoDocumentoLogico(
                                    rs.getString(
                                            "TIPO_DOCUMENTO_LOGICO"
                                    )
                            );

                            item.setCantidadBeneficiarios(
                                    rs.getInt(
                                            "CANTIDAD_BENEFICIARIOS"
                                    )
                            );

                            item.setIdDocumentoPublicado(
                                    rs.getString(
                                            "ID_DOCUMENTO_PUBLICADO"
                                    )
                            );

                            item.setNombreArchivoFinal(
                                    rs.getString(
                                            "NOMBRE_ARCHIVO_FINAL"
                                    )
                            );

                            Timestamp fechaDocumento =
                                    rs.getTimestamp(
                                            "FECHA_PUBLICACION_DOCUMENTO"
                                    );

                            if (fechaDocumento != null) {

                                item.setFechaPublicacionDocumento(
                                        fechaDocumento
                                                .toLocalDateTime()
                                );
                            }

                            Timestamp fechaAfiliacion =
                                    rs.getTimestamp(
                                            "FECHA_AFILIACION"
                                    );

                            if (fechaAfiliacion != null) {

                                item.setFechaAfiliacion(
                                        fechaAfiliacion
                                                .toLocalDateTime()
                                );
                            }

                            return item;
                        },

                        tipoDocumentoTitular.trim(),
                        numeroDocumentoTitular.trim(),

                        tipoDocumentoLogico
                                .trim()
                                .toUpperCase(),

                        inicio,
                        finExclusivo
                );

        if (resultados.isEmpty()) {

            return Optional.empty();
        }

        /*
         * Nunca elegir silenciosamente un proceso.
         *
         * Si un mismo trabajador tiene dos documentos
         * publicados del mismo tipo dentro del mismo
         * período, el lote requiere revisión.
         */
        if (resultados.size() > 1) {

            throw new IllegalStateException(
                    "Se encontró más de un documento "
                            + "publicado para el trabajador "
                            + tipoDocumentoTitular
                            + "-"
                            + numeroDocumentoTitular
                            + ", tipo "
                            + tipoDocumentoLogico
                            + ", dentro del período "
                            + fechaInicioPeriodo
                            + " a "
                            + fechaFinPeriodo
                            + "."
            );
        }

        return Optional.of(
                resultados.get(0)
        );
    }

    private void validar(
            String tipo,
            String numero,
            String tipoLogico,
            LocalDate inicio,
            LocalDate fin
    ) {

        if (
                tipo == null
                        || tipo.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El tipo de documento del titular "
                            + "es obligatorio."
            );
        }

        if (
                numero == null
                        || numero.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El número de documento del titular "
                            + "es obligatorio."
            );
        }

        if (
                tipoLogico == null
                        || tipoLogico.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El tipo documental +Vida "
                            + "es obligatorio."
            );
        }

        if (
                inicio == null
                        || fin == null
        ) {

            throw new IllegalArgumentException(
                    "El período del lote es obligatorio."
            );
        }

        if (inicio.isAfter(fin)) {

            throw new IllegalArgumentException(
                    "El inicio del período no puede "
                            + "ser posterior al fin."
            );
        }
    }
}