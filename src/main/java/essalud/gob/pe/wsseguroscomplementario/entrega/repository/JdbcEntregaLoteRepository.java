package essalud.gob.pe.wsseguroscomplementario.entrega.repository;

import essalud.gob.pe.wsseguroscomplementario.entrega.model.EntregaLote;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class JdbcEntregaLoteRepository
        implements EntregaLoteRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEntregaLoteRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                jdbcTemplate;
    }

    @Override
    public Optional<EntregaLote> buscarPorTokenHash(
            String tokenHash
    ) {

        String sql = """
                SELECT
                    E.TIPO_DESTINATARIO,
                    E.CORREO_DESTINATARIO,
                    E.CANTIDAD_DOCUMENTOS,
                    E.URL_ACCESO,
                    E.FECHA_OTP_VALIDADO,
                    E.FECHA_ACUSE,
                    E.TEXTO_ACUSE,
                    E.VERSION_TEXTO_ACUSE,

                    L.FECHA_INICIO_PERIODO,
                    L.FECHA_FIN_PERIODO,
                    L.FECHA_PUBLICACION

                FROM ENTREGA_LOTE E

                INNER JOIN LOTE_DISTRIBUCION L
                    ON L.ID_LOTE =
                       E.ID_LOTE

                WHERE E.TOKEN_HASH = ?
                """;

        try {

            EntregaLote entrega =
                    jdbcTemplate.queryForObject(
                            sql,
                            this::mapearEntrega,
                            tokenHash
                    );

            return Optional.ofNullable(
                    entrega
            );

        } catch (
                EmptyResultDataAccessException e
        ) {

            return Optional.empty();
        }
    }

    @Override
    public boolean marcarOtpValidadoSiPendiente(
            String tokenHash
    ) {

        /*
         * La fecha solamente se escribe una vez.
         *
         * Un reintento posterior no modifica
         * FECHA_OTP_VALIDADO.
         *
         * Tampoco se permite modificar una entrega
         * cuyo acuse ya haya sido registrado.
         */
        String sql = """
            UPDATE ENTREGA_LOTE
            SET
                FECHA_OTP_VALIDADO =
                    SYSTIMESTAMP,

                FECHA_ACTUALIZACION =
                    SYSTIMESTAMP

            WHERE TOKEN_HASH = ?
              AND FECHA_OTP_VALIDADO IS NULL
              AND FECHA_ACUSE IS NULL
            """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,
                        tokenHash
                );

        return filasActualizadas > 0;
    }

    @Override
    public boolean registrarAcuseSiPendiente(
            String tokenHash,
            String textoAcuse,
            String versionTextoAcuse,
            String ipAcuse,
            String datosSesionDispositivo
    ) {

        String sql = """
            UPDATE ENTREGA_LOTE
            SET
                FECHA_ACUSE =
                    SYSTIMESTAMP,

                TEXTO_ACUSE =
                    ?,

                VERSION_TEXTO_ACUSE =
                    ?,

                IP_ACUSE =
                    ?,

                DATOS_SESION_DISPOSITIVO =
                    ?,

                FECHA_ACTUALIZACION =
                    SYSTIMESTAMP

            WHERE TOKEN_HASH = ?

              AND FECHA_OTP_VALIDADO
                    IS NOT NULL

              AND FECHA_ACUSE
                    IS NULL

              AND EXISTS (
                    SELECT 1
                    FROM LOTE_DISTRIBUCION L
                    WHERE L.ID_LOTE =
                          ENTREGA_LOTE.ID_LOTE
                      AND L.FECHA_PUBLICACION
                          IS NOT NULL
              )
            """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,
                        textoAcuse,
                        versionTextoAcuse,
                        ipAcuse,
                        datosSesionDispositivo,
                        tokenHash
                );

        return filasActualizadas > 0;
    }

    private EntregaLote mapearEntrega(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        EntregaLote entrega =
                new EntregaLote();

        entrega.setTipoDestinatario(
                rs.getString(
                        "TIPO_DESTINATARIO"
                )
        );

        entrega.setCorreoDestinatario(
                rs.getString(
                        "CORREO_DESTINATARIO"
                )
        );

        entrega.setCantidadDocumentos(
                rs.getInt(
                        "CANTIDAD_DOCUMENTOS"
                )
        );

        entrega.setUrlAcceso(
                rs.getString(
                        "URL_ACCESO"
                )
        );

        entrega.setFechaOtpValidado(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_OTP_VALIDADO"
                        )
                )
        );

        entrega.setFechaAcuse(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_ACUSE"
                        )
                )
        );

        entrega.setTextoAcuse(
                rs.getString(
                        "TEXTO_ACUSE"
                )
        );

        entrega.setVersionTextoAcuse(
                rs.getString(
                        "VERSION_TEXTO_ACUSE"
                )
        );

        entrega.setFechaInicioPeriodo(
                convertirDate(
                        rs.getDate(
                                "FECHA_INICIO_PERIODO"
                        )
                )
        );

        entrega.setFechaFinPeriodo(
                convertirDate(
                        rs.getDate(
                                "FECHA_FIN_PERIODO"
                        )
                )
        );

        entrega.setFechaPublicacion(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_PUBLICACION"
                        )
                )
        );

        return entrega;
    }

    private java.time.LocalDate convertirDate(
            Date fecha
    ) {

        return fecha == null
                ? null
                : fecha.toLocalDate();
    }

    private java.time.LocalDateTime convertirTimestamp(
            Timestamp fecha
    ) {

        return fecha == null
                ? null
                : fecha.toLocalDateTime();
    }
}