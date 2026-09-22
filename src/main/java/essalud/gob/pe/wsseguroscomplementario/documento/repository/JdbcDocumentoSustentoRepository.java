package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoCargado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.io.StringReader;
import org.springframework.dao.EmptyResultDataAccessException;

@Repository
public class JdbcDocumentoSustentoRepository
        implements DocumentoSustentoRepository {

    private static final String
            ESTADO_PENDIENTE_CARGA =
            "PENDIENTE_CARGA";

    private static final String
            ESTADO_RECHAZADO_VALIDACION =
            "RECHAZADO_VALIDACION";

    private static final String
            ESTADO_PUBLICADO =
            "PUBLICADO";

    private final JdbcTemplate jdbcTemplate;

    public JdbcDocumentoSustentoRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                jdbcTemplate;
    }

    @Override
    public void registrarCargaExitosa(
            DocumentoCargado documentoCargado
    ) {

        String sql = """
                UPDATE DOCUMENTOS_SUSTENTO D
                SET
                    D.ID_ESTADO_SUSTENTO = (
                        SELECT E.ID_ESTADO_SUSTENTO
                        FROM ESTADO_SUSTENTO E
                        WHERE E.COD_ESTADO_DOCUMENTO = ?
                    ),

                    D.ID_DOCUMENTO_CARGADO_ULTIMO = ?,
                    D.NOMBRE_ARCHIVO_CARGADO = ?,
                    D.TAMANIO_BYTES_CARGADO = ?,
                    D.NUMERO_PAGINAS_CARGADO = ?,
                    D.HASH_DOCUMENT_CARGADO = ?,
                    D.FECHA_CARGA = ?,

                    D.ID_RECHAZO_ULTIMO = NULL,
                    D.MOTIVO_RECHAZO = NULL,

                    D.FECHA_ACTUALIZACION =
                        SYSTIMESTAMP

                WHERE D.ID_SECOMASVIDA = (
                    SELECT T.ID_SECOMASVIDA
                    FROM TEMP_SECOMASVIDA T
                    WHERE T.REGISTRO_INTERNO_PROCESO = ?
                )

                AND D.TIPO_DOCUMENTO_LOGICO = ?
                
                AND D.ID_DOCUMENTO_PUBLICADO IS NULL
                """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,

                        ESTADO_PENDIENTE_CARGA,

                        documentoCargado
                                .getIdDocumentoCargado(),

                        documentoCargado
                                .getNombreArchivoOriginal(),

                        documentoCargado
                                .getTamanioBytes(),

                        documentoCargado
                                .getNumeroPaginas(),

                        documentoCargado
                                .getHashSha256ArchivoCargado(),

                        convertirTimestamp(
                                documentoCargado
                                        .getFechaHoraCarga()
                        ),

                        documentoCargado
                                .getRegistroInternoProceso(),

                        documentoCargado
                                .getTipoDocumento()
                );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontrÃ³ metadata documental persistida para registrar la carga del PDF firmado."
            );
        }
    }

    @Override
    public void registrarValidacionAprobada(
            String registroInternoProceso,
            String tipoDocumento,
            java.time.LocalDateTime fechaValidacion
    ) {

        String sql = """
            UPDATE DOCUMENTOS_SUSTENTO D
            SET
                D.ID_ESTADO_SUSTENTO = (
                    SELECT E.ID_ESTADO_SUSTENTO
                    FROM ESTADO_SUSTENTO E
                    WHERE E.COD_ESTADO_DOCUMENTO = ?
                ),

                D.FECHA_VALIDACION = ?,

                D.ID_RECHAZO_ULTIMO = NULL,
                D.MOTIVO_RECHAZO = NULL,

                D.FECHA_ACTUALIZACION =
                    SYSTIMESTAMP

            WHERE D.ID_SECOMASVIDA = (
                SELECT T.ID_SECOMASVIDA
                FROM TEMP_SECOMASVIDA T
                WHERE T.REGISTRO_INTERNO_PROCESO = ?
            )
            AND D.TIPO_DOCUMENTO_LOGICO = ?
            AND D.ID_DOCUMENTO_PUBLICADO IS NULL
            """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,

                        ESTADO_PENDIENTE_CARGA,

                        convertirTimestamp(
                                fechaValidacion
                        ),

                        registroInternoProceso.trim(),

                        normalizarTipoDocumento(
                                tipoDocumento
                        )
                );

        validarActualizacionDocumental(
                filasActualizadas,
                "registrar la validaciÃ³n aprobada"
        );
    }

    @Override
    public void registrarValidacionRechazada(
            String registroInternoProceso,
            String tipoDocumento,
            java.time.LocalDateTime fechaValidacion,
            String idRechazoDocumental,
            String motivoRechazo
    ) {

        String sql = """
            UPDATE DOCUMENTOS_SUSTENTO D
            SET
                D.ID_ESTADO_SUSTENTO = (
                    SELECT E.ID_ESTADO_SUSTENTO
                    FROM ESTADO_SUSTENTO E
                    WHERE E.COD_ESTADO_DOCUMENTO = ?
                ),

                D.FECHA_VALIDACION = ?,

                D.ID_RECHAZO_ULTIMO = ?,
                D.MOTIVO_RECHAZO = ?,

                D.FECHA_ACTUALIZACION =
                    SYSTIMESTAMP

            WHERE D.ID_SECOMASVIDA = (
                SELECT T.ID_SECOMASVIDA
                FROM TEMP_SECOMASVIDA T
                WHERE T.REGISTRO_INTERNO_PROCESO = ?
            )
            
            AND D.TIPO_DOCUMENTO_LOGICO = ?
            
            AND D.ID_DOCUMENTO_PUBLICADO IS NULL
            """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,

                        ESTADO_RECHAZADO_VALIDACION,

                        convertirTimestamp(
                                fechaValidacion
                        ),

                        idRechazoDocumental,

                        limitarMotivoRechazo(
                                motivoRechazo
                        ),

                        registroInternoProceso.trim(),

                        normalizarTipoDocumento(
                                tipoDocumento
                        )
                );

        validarActualizacionDocumental(
                filasActualizadas,
                "registrar la validaciÃ³n rechazada"
        );
    }

    @Override
    public void registrarPublicacion(
            DocumentoPublicado documentoPublicado
    ) {

        String sql = """
            UPDATE DOCUMENTOS_SUSTENTO D
            SET
                D.ID_ESTADO_SUSTENTO = (
                    SELECT E.ID_ESTADO_SUSTENTO
                    FROM ESTADO_SUSTENTO E
                    WHERE E.COD_ESTADO_DOCUMENTO = ?
                ),

                D.ID_DOCUMENTO_PUBLICADO = ?,
                D.NOMBRE_ARCHIVO_FINAL = ?,
                D.RUTA_ARCHIVO = ?,
                D.HASH_DOCUMENT_FINAL = ?,
                D.FECHA_PUBLICACION = ?,

                D.FECHA_ACTUALIZACION =
                    SYSTIMESTAMP

            WHERE D.ID_SECOMASVIDA = (
                SELECT T.ID_SECOMASVIDA
                FROM TEMP_SECOMASVIDA T
                WHERE T.REGISTRO_INTERNO_PROCESO = ?
            )

            AND D.TIPO_DOCUMENTO_LOGICO = ?
            """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,

                        ESTADO_PUBLICADO,

                        documentoPublicado
                                .getIdDocumentoPublicado(),

                        documentoPublicado
                                .getNombreArchivo(),

                        documentoPublicado
                                .getRutaArchivo(),

                        documentoPublicado
                                .getHashSha256DocumentoPublicado(),

                        convertirTimestamp(
                                documentoPublicado
                                        .getFechaHoraPublicacion()
                        ),

                        documentoPublicado
                                .getRegistroInternoProceso(),

                        normalizarTipoDocumento(
                                documentoPublicado
                                        .getTipoDocumento()
                        )
                );

        validarActualizacionDocumental(
                filasActualizadas,
                "registrar la publicaciÃ³n documental"
        );
    }

    @Override
    public void registrarRespaldoFormulario6012(
            String registroInternoProceso,
            String contenidoRespaldo,
            String hashRespaldo,
            java.time.LocalDateTime fechaRespaldo
    ) {

        String sql = """
    UPDATE DOCUMENTOS_SUSTENTO D
    SET
        D.CONTENIDO_RESPALDO_6012 = ?,
        D.HASH_RESPALDO_6012 = ?,
        D.FECHA_RESPALDO_6012 = ?,
        D.FECHA_ACTUALIZACION =
            SYSTIMESTAMP

    WHERE D.ID_SECOMASVIDA = (
        SELECT T.ID_SECOMASVIDA
        FROM TEMP_SECOMASVIDA T
        WHERE T.REGISTRO_INTERNO_PROCESO = ?
    )

    AND D.TIPO_DOCUMENTO_LOGICO =
        'FORMULARIO_6012'

    AND D.ID_ESTADO_SUSTENTO = (
        SELECT E.ID_ESTADO_SUSTENTO
        FROM ESTADO_SUSTENTO E
        WHERE E.COD_ESTADO_DOCUMENTO =
            'PUBLICADO'
    )

    AND (
        D.CONTENIDO_RESPALDO_6012 IS NULL
        OR D.HASH_RESPALDO_6012 IS NULL
        OR D.FECHA_RESPALDO_6012 IS NULL
    )
    """;

        int filasActualizadas =
                jdbcTemplate.update(
                        connection -> {

                            java.sql.PreparedStatement ps =
                                    connection.prepareStatement(
                                            sql
                                    );

                            ps.setCharacterStream(
                                    1,
                                    new StringReader(
                                            contenidoRespaldo
                                    )
                            );

                            ps.setString(
                                    2,
                                    hashRespaldo
                            );

                            ps.setTimestamp(
                                    3,
                                    convertirTimestamp(
                                            fechaRespaldo
                                    )
                            );

                            ps.setString(
                                    4,
                                    registroInternoProceso.trim()
                            );

                            return ps;
                        }
                );

        if (filasActualizadas > 0) {
            return;
        }

        /*
         * Si el respaldo ya existe completo,
         * el reintento es idempotente:
         * no se modifica contenido, hash ni fecha.
         */
        String sqlExiste = """
    SELECT COUNT(*)

    FROM DOCUMENTOS_SUSTENTO D

    INNER JOIN TEMP_SECOMASVIDA T
        ON T.ID_SECOMASVIDA =
           D.ID_SECOMASVIDA

    INNER JOIN ESTADO_SUSTENTO E
        ON E.ID_ESTADO_SUSTENTO =
           D.ID_ESTADO_SUSTENTO

    WHERE
        T.REGISTRO_INTERNO_PROCESO = ?

        AND D.TIPO_DOCUMENTO_LOGICO =
            'FORMULARIO_6012'

        AND E.COD_ESTADO_DOCUMENTO =
            'PUBLICADO'

        AND D.CONTENIDO_RESPALDO_6012
            IS NOT NULL

        AND D.HASH_RESPALDO_6012
            IS NOT NULL

        AND D.FECHA_RESPALDO_6012
            IS NOT NULL
    """;

        Integer cantidad =
                jdbcTemplate.queryForObject(
                        sqlExiste,
                        Integer.class,
                        registroInternoProceso.trim()
                );

        if (
                cantidad != null
                        && cantidad > 0
        ) {
            return;
        }

        throw new IllegalArgumentException(
                "No se encontrÃ³ un Formulario 6012 publicado para registrar su respaldo textual."
        );
    }

    @Override
    public String obtenerRespaldoFormulario6012(
            String registroInternoProceso
    ) {

        String sql = """
        SELECT
            D.CONTENIDO_RESPALDO_6012
        FROM DOCUMENTOS_SUSTENTO D
        INNER JOIN TEMP_SECOMASVIDA T
            ON T.ID_SECOMASVIDA =
               D.ID_SECOMASVIDA
        WHERE
            T.REGISTRO_INTERNO_PROCESO = ?
            AND D.TIPO_DOCUMENTO_LOGICO =
                'FORMULARIO_6012'
            AND D.CONTENIDO_RESPALDO_6012
                IS NOT NULL
        """;

        try {

            return jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) ->
                            rs.getString(
                                    "CONTENIDO_RESPALDO_6012"
                            ),
                    registroInternoProceso.trim()
            );

        } catch (EmptyResultDataAccessException e) {

            throw new IllegalArgumentException(
                    "No se encontrÃ³ un respaldo textual del Formulario 6012 para el trÃ¡mite indicado."
            );
        }
    }

    @Override
    public boolean estaPublicado(
            String registroInternoProceso,
            String tipoDocumento
    ) {

        String sql = """
            SELECT COUNT(*)
            FROM DOCUMENTOS_SUSTENTO D

            INNER JOIN TEMP_SECOMASVIDA T
                ON T.ID_SECOMASVIDA =
                   D.ID_SECOMASVIDA

            INNER JOIN ESTADO_SUSTENTO E
                ON E.ID_ESTADO_SUSTENTO =
                   D.ID_ESTADO_SUSTENTO

            WHERE
                T.REGISTRO_INTERNO_PROCESO = ?

                AND D.TIPO_DOCUMENTO_LOGICO = ?

                AND E.COD_ESTADO_DOCUMENTO =
                    'PUBLICADO'

                AND D.ID_DOCUMENTO_PUBLICADO
                    IS NOT NULL

                AND D.HASH_DOCUMENT_FINAL
                    IS NOT NULL

                AND D.FECHA_PUBLICACION
                    IS NOT NULL
            """;

        Integer cantidad =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,

                        registroInternoProceso.trim(),

                        normalizarTipoDocumento(
                                tipoDocumento
                        )
                );

        return cantidad != null
                && cantidad > 0;
    }

    @Override
    public void depurarMetadataOperativaPublicada(
            String registroInternoProceso
    ) {

        String sql = """
    UPDATE DOCUMENTOS_SUSTENTO D

    SET
        /*
         * Metadata utilizada Ãºnicamente durante
         * generaciÃ³n y validaciÃ³n.
         */
        D.ID_DOCUMENTO_GENERADO = NULL,
        D.NOMBRE_ARCHIVO_GENERADO = NULL,
        D.TAMANIO_BYTES_GENERADO = NULL,
        D.NUMERO_PAGINAS_GENERADAS = NULL,
        D.HASH_DOCUMENT_ORIGINAL = NULL,
        D.CANT_BENEFICIARIOS_GENERACION = NULL,
        D.FECHA_GENERACION = NULL,
        D.GENERADO_POR = NULL,
        D.CANAL_GENERACION = NULL,

        D.ID_DOCUMENTO_CARGADO_ULTIMO = NULL,
        D.NOMBRE_ARCHIVO_CARGADO = NULL,
        D.TAMANIO_BYTES_CARGADO = NULL,
        D.NUMERO_PAGINAS_CARGADO = NULL,
        D.HASH_DOCUMENT_CARGADO = NULL,
        D.FECHA_CARGA = NULL,
        D.FECHA_VALIDACION = NULL,

        D.ID_RECHAZO_ULTIMO = NULL,
        D.MOTIVO_RECHAZO = NULL,

        D.FECHA_ACTUALIZACION =
            SYSTIMESTAMP

    WHERE D.ID_SECOMASVIDA = (
        SELECT T.ID_SECOMASVIDA
        FROM TEMP_SECOMASVIDA T
        WHERE T.REGISTRO_INTERNO_PROCESO = ?
    )

    AND D.ID_ESTADO_SUSTENTO = (
        SELECT E.ID_ESTADO_SUSTENTO
        FROM ESTADO_SUSTENTO E
        WHERE E.COD_ESTADO_DOCUMENTO =
            'PUBLICADO'
    )

    AND D.ID_DOCUMENTO_PUBLICADO
        IS NOT NULL

    AND D.HASH_DOCUMENT_FINAL
        IS NOT NULL

    AND D.FECHA_PUBLICACION
        IS NOT NULL
    """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,
                        registroInternoProceso.trim()
                );

        if (filasActualizadas == 0) {
            throw new IllegalStateException(
                    "No existen documentos finales publicados para ejecutar la depuraciÃ³n documental."
            );
        }
    }

    private void validarActualizacionDocumental(
            int filasActualizadas,
            String operacion
    ) {
        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontrÃ³ metadata documental persistida para "
                            + operacion
                            + "."
            );
        }
    }

    private String normalizarTipoDocumento(
            String tipoDocumento
    ) {
        if (
                tipoDocumento == null
                        || tipoDocumento.trim().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "El tipo de documento lÃ³gico es obligatorio."
            );
        }

        return tipoDocumento
                .trim()
                .toUpperCase();
    }

    private String limitarMotivoRechazo(
            String motivoRechazo
    ) {
        if (motivoRechazo == null) {
            return null;
        }

        String motivo =
                motivoRechazo.trim();

        if (motivo.length() <= 2000) {
            return motivo;
        }

        return motivo.substring(
                0,
                2000
        );
    }

    private Timestamp convertirTimestamp(
            java.time.LocalDateTime fecha
    ) {
        return fecha == null
                ? null
                : Timestamp.valueOf(fecha);
    }
}
