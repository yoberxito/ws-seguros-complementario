package essalud.gob.pe.wsseguroscomplementario.entrega.repository;

import essalud.gob.pe.wsseguroscomplementario.entrega.model.ReporteLoteVidaItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReporteLoteVidaRepository
        implements ReporteLoteVidaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReporteLoteVidaRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ReporteLoteVidaItem>
    buscarDocumentoPublicado(
            String tipoDocumentoTitular,
            String numeroDocumentoTitular,
            String tipoDocumentoLogico
    ) {

        validar(
                tipoDocumentoTitular,
                numeroDocumentoTitular,
                tipoDocumentoLogico
        );

        /*
         * Oracle 11g compatible.
         *
         * No se usa FETCH FIRST.
         *
         * UK_DOC_TIPO_TRAMITE garantiza una única
         * combinación:
         *
         * ID_SECOMASVIDA + TIPO_DOCUMENTO_LOGICO.
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

            FROM TEMP_SECOMASVIDA T

            INNER JOIN DOCUMENTOS_SUSTENTO D
                ON D.ID_SECOMASVIDA =
                   T.ID_SECOMASVIDA

            INNER JOIN ESTADO_SUSTENTO E
                ON E.ID_ESTADO_SUSTENTO =
                   D.ID_ESTADO_SUSTENTO

            WHERE
                T.COD_EDOCUMENT_TITULAR = ?

                AND T.NUM_DOCUMENT_TITULAR = ?

                AND D.TIPO_DOCUMENTO_LOGICO = ?

                AND E.COD_ESTADO_DOCUMENTO =
                    'PUBLICADO'

                AND D.ID_DOCUMENTO_PUBLICADO
                    IS NOT NULL

                AND D.FECHA_PUBLICACION
                    IS NOT NULL
            """;

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

                            item.setFechaRegistroProceso(
                                    fechaRegistro == null
                                            ? null
                                            : fechaRegistro
                                                    .toLocalDateTime()
                            );

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

                            Timestamp fechaPublicacion =
                                    rs.getTimestamp(
                                            "FECHA_PUBLICACION"
                                    );

                            item.setFechaPublicacionDocumento(
                                    fechaPublicacion == null
                                            ? null
                                            : fechaPublicacion
                                                    .toLocalDateTime()
                            );

                            return item;
                        },
                        tipoDocumentoTitular.trim(),
                        numeroDocumentoTitular.trim(),
                        tipoDocumentoLogico
                                .trim()
                                .toUpperCase()
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        if (resultados.size() > 1) {

            throw new IllegalStateException(
                    "Oracle devolvió más de un documento publicado "
                            + "para el trabajador, trámite y tipo "
                            + "documental indicados."
            );
        }

        return Optional.of(
                resultados.get(0)
        );
    }

    private void validar(
            String tipoDocumentoTitular,
            String numeroDocumentoTitular,
            String tipoDocumentoLogico
    ) {

        if (
                tipoDocumentoTitular == null
                        || tipoDocumentoTitular
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El tipo de documento del titular es obligatorio."
            );
        }

        if (
                numeroDocumentoTitular == null
                        || numeroDocumentoTitular
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El número de documento del titular es obligatorio."
            );
        }

        if (
                tipoDocumentoLogico == null
                        || tipoDocumentoLogico
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El tipo documental +Vida es obligatorio."
            );
        }
    }
}