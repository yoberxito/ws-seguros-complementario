package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.documento.model.RechazoDocumento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcRechazoDocumentoRepository
        implements RechazoDocumentoRepository {

    private static final String
            TIPO_EVENTO_RECHAZO =
            "RECHAZO_DOCUMENTAL";

    private static final String
            ESTADO_RECHAZADO =
            "RECHAZADO_VALIDACION";

    private static final String
            RESULTADO_RECHAZADO =
            "RECHAZADO";

    private final JdbcTemplate jdbcTemplate;

    public JdbcRechazoDocumentoRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RechazoDocumento guardar(
            RechazoDocumento rechazoDocumento
    ) {

        String sql = """
        INSERT INTO HISTORIAL_PROC_REGMASVIDA (
            COD_EVENTO_HISTORIAL,
            ID_SECOMASVIDA,
            ID_DOC_SUSTENTO,
            TIPO_EVENTO,
            ESTADO_ANTERIOR,
            ESTADO_NUEVO,
            RESULTADO_EVENTO,
            DESCRIPCION_EVENTO,
            USUARIO_AUTENTICADO,
            IP_ORIGEN,
            DATOS_SESION_DISPOSITIVO,
            CANAL_EVENTO,
            TIPO_DOCUMENTO_PROCESO,
            ID_DOCUMENTO_CARGADO,
            ID_RECHAZO_DOCUMENTAL,
            NOMBRE_ARCHIVO,
            HASH_ARCHIVO,
            TAMANIO_BYTES,
            NUMERO_PAGINAS,
            FECHA_EVENTO
        )

        SELECT
            ?,
            T.ID_SECOMASVIDA,
            D.ID_DOC_SUSTENTO,
            ?,
            E.COD_ESTADO_DOCUMENTO,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,

            CASE
                WHEN ? IS NOT NULL
                    THEN D.NOMBRE_ARCHIVO_CARGADO
                ELSE ?
            END,

            CASE
                WHEN ? IS NOT NULL
                    THEN D.HASH_DOCUMENT_CARGADO
                ELSE NULL
            END,

            CASE
                WHEN ? IS NOT NULL
                    THEN D.TAMANIO_BYTES_CARGADO
                ELSE ?
            END,

            CASE
                WHEN ? IS NOT NULL
                    THEN D.NUMERO_PAGINAS_CARGADO
                ELSE NULLIF(?, 0)
            END,

            ?

        FROM TEMP_SECOMASVIDA T

        INNER JOIN DOCUMENTOS_SUSTENTO D
            ON D.ID_SECOMASVIDA =
               T.ID_SECOMASVIDA

        INNER JOIN ESTADO_SUSTENTO E
            ON E.ID_ESTADO_SUSTENTO =
               D.ID_ESTADO_SUSTENTO

        WHERE
            T.REGISTRO_INTERNO_PROCESO = ?

            AND D.TIPO_DOCUMENTO_LOGICO = ?

            AND (
                ? IS NULL
                OR D.ID_DOCUMENTO_CARGADO_ULTIMO = ?
            )
        """;

        String idDocumentoCargado =
                rechazoDocumento
                        .getIdDocumentoCargado();

        int filasInsertadas =
                jdbcTemplate.update(
                        sql,

                        construirCodigoEvento(
                                rechazoDocumento
                                        .getIdRechazoDocumental()
                        ),

                        TIPO_EVENTO_RECHAZO,

                        ESTADO_RECHAZADO,

                        RESULTADO_RECHAZADO,

                        construirDescripcion(
                                rechazoDocumento
                                        .getMotivosRechazo()
                        ),

                        EstadoProcesoConstants
                                .USUARIO_SISTEMA,

                        rechazoDocumento
                                .getIpOrigen(),

                        rechazoDocumento
                                .getDatosSesionDispositivo(),

                        EstadoProcesoConstants
                                .CANAL_SOMOS_ESSALUD,

                        normalizarTipoDocumento(
                                rechazoDocumento
                                        .getTipoDocumento()
                        ),

                        idDocumentoCargado,

                        rechazoDocumento
                                .getIdRechazoDocumental(),

                        idDocumentoCargado,

                        rechazoDocumento
                                .getNombreArchivoOriginal(),

                        idDocumentoCargado,

                        idDocumentoCargado,

                        rechazoDocumento
                                .getTamanioBytes(),

                        idDocumentoCargado,

                        rechazoDocumento
                                .getNumeroPaginas(),

                        convertirTimestamp(
                                rechazoDocumento
                                        .getFechaHoraRechazo()
                        ),

                        rechazoDocumento
                                .getRegistroInternoProceso()
                                .trim(),

                        normalizarTipoDocumento(
                                rechazoDocumento
                                        .getTipoDocumento()
                        ),

                        idDocumentoCargado,

                        idDocumentoCargado
                );

        if (filasInsertadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró metadata documental persistida para registrar el rechazo."
            );
        }

        return rechazoDocumento;
    }

    @Override
    public Optional<RechazoDocumento> buscarPorId(
            String idRechazoDocumental
    ) {

        String sql = """
            SELECT
                H.ID_RECHAZO_DOCUMENTAL,
                H.ID_DOCUMENTO_CARGADO,
                T.REGISTRO_INTERNO_PROCESO,
                H.TIPO_DOCUMENTO_PROCESO,
                H.NOMBRE_ARCHIVO,
                H.TAMANIO_BYTES,
                H.NUMERO_PAGINAS,
                H.ESTADO_NUEVO,
                H.DESCRIPCION_EVENTO,
                H.FECHA_EVENTO,
                H.IP_ORIGEN,
                H.DATOS_SESION_DISPOSITIVO

            FROM HISTORIAL_PROC_REGMASVIDA H

            INNER JOIN TEMP_SECOMASVIDA T
                ON T.ID_SECOMASVIDA =
                   H.ID_SECOMASVIDA

            WHERE
                H.ID_RECHAZO_DOCUMENTAL = ?
            """;

        List<RechazoDocumento> resultados =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                mapearRechazo(rs),
                        idRechazoDocumental
                );

        return resultados
                .stream()
                .findFirst();
    }

    @Override
    public List<RechazoDocumento> listar() {

        String sql = """
            SELECT
                H.ID_RECHAZO_DOCUMENTAL,
                H.ID_DOCUMENTO_CARGADO,
                T.REGISTRO_INTERNO_PROCESO,
                H.TIPO_DOCUMENTO_PROCESO,
                H.NOMBRE_ARCHIVO,
                H.TAMANIO_BYTES,
                H.NUMERO_PAGINAS,
                H.ESTADO_NUEVO,
                H.DESCRIPCION_EVENTO,
                H.FECHA_EVENTO,
                H.IP_ORIGEN,
                H.DATOS_SESION_DISPOSITIVO

            FROM HISTORIAL_PROC_REGMASVIDA H

            INNER JOIN TEMP_SECOMASVIDA T
                ON T.ID_SECOMASVIDA =
                   H.ID_SECOMASVIDA

            WHERE
                H.ID_RECHAZO_DOCUMENTAL
                    IS NOT NULL

            ORDER BY
                H.FECHA_EVENTO DESC
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        mapearRechazo(rs)
        );
    }

    private RechazoDocumento mapearRechazo(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        RechazoDocumento rechazo =
                new RechazoDocumento();

        rechazo.setIdRechazoDocumental(
                rs.getString(
                        "ID_RECHAZO_DOCUMENTAL"
                )
        );

        rechazo.setIdDocumentoCargado(
                rs.getString(
                        "ID_DOCUMENTO_CARGADO"
                )
        );

        rechazo.setRegistroInternoProceso(
                rs.getString(
                        "REGISTRO_INTERNO_PROCESO"
                )
        );

        rechazo.setTipoDocumento(
                rs.getString(
                        "TIPO_DOCUMENTO_PROCESO"
                )
        );

        rechazo.setNombreArchivoOriginal(
                rs.getString(
                        "NOMBRE_ARCHIVO"
                )
        );

        rechazo.setTamanioBytes(
                rs.getLong(
                        "TAMANIO_BYTES"
                )
        );

        rechazo.setNumeroPaginas(
                rs.getInt(
                        "NUMERO_PAGINAS"
                )
        );

        rechazo.setEstadoValidacionDocumental(
                rs.getString(
                        "ESTADO_NUEVO"
                )
        );

        rechazo.setPermiteNuevaCarga(
                true
        );

        String descripcion =
                rs.getString(
                        "DESCRIPCION_EVENTO"
                );

        List<String> motivos =
                new ArrayList<>();

        if (
                descripcion != null
                        && !descripcion.trim().isEmpty()
        ) {
            motivos.add(
                    descripcion.trim()
            );
        }

        rechazo.setMotivosRechazo(
                motivos
        );

        Timestamp fechaEvento =
                rs.getTimestamp(
                        "FECHA_EVENTO"
                );

        if (fechaEvento != null) {
            rechazo.setFechaHoraRechazo(
                    fechaEvento.toLocalDateTime()
            );
        }

        rechazo.setIpOrigen(
                rs.getString(
                        "IP_ORIGEN"
                )
        );

        rechazo.setDatosSesionDispositivo(
                rs.getString(
                        "DATOS_SESION_DISPOSITIVO"
                )
        );

        return rechazo;
    }

    private String construirCodigoEvento(
            String idRechazoDocumental
    ) {
        return "HIST-"
                + idRechazoDocumental;
    }

    private String construirDescripcion(
            List<String> motivos
    ) {

        if (
                motivos == null
                        || motivos.isEmpty()
        ) {
            return "Documento rechazado durante la validación.";
        }

        String descripcion =
                String.join(
                        " ",
                        motivos
                ).trim();

        if (descripcion.length() <= 2000) {
            return descripcion;
        }

        return descripcion.substring(
                0,
                2000
        );
    }

    private String normalizarTipoDocumento(
            String tipoDocumento
    ) {

        if (
                tipoDocumento == null
                        || tipoDocumento.trim().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "El tipo de documento lógico es obligatorio."
            );
        }

        return tipoDocumento
                .trim()
                .toUpperCase();
    }

    private Timestamp convertirTimestamp(
            java.time.LocalDateTime fecha
    ) {
        return fecha == null
                ? null
                : Timestamp.valueOf(fecha);
    }
}