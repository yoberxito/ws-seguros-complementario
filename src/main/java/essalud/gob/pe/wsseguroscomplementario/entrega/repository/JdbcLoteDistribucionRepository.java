package essalud.gob.pe.wsseguroscomplementario.entrega.repository;

import essalud.gob.pe.wsseguroscomplementario.entrega.model.LoteDistribucion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcLoteDistribucionRepository
        implements LoteDistribucionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcLoteDistribucionRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LoteDistribucion crear(
            LoteDistribucion lote
    ) {

        validarLote(lote);

        Long idLote =
                jdbcTemplate.queryForObject(
                        "SELECT SEQ_LOTE_DISTRIBUCION.NEXTVAL FROM DUAL",
                        Long.class
                );

        String sql = """
                INSERT INTO LOTE_DISTRIBUCION (
                    ID_LOTE,
                    COD_LOTE,
                    FECHA_INICIO_PERIODO,
                    FECHA_FIN_PERIODO,
                    ESTADO_LOTE,
                    FECHA_PUBLICACION,
                    FECHA_REGISTRO,
                    FECHA_ACTUALIZACION
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    SYSTIMESTAMP,
                    SYSTIMESTAMP
                )
                """;

        jdbcTemplate.update(
                sql,
                idLote,
                lote.getCodLote().trim(),
                Date.valueOf(lote.getFechaInicioPeriodo()),
                Date.valueOf(lote.getFechaFinPeriodo()),
                lote.getEstadoLote().trim(),
                lote.getFechaPublicacion() == null
                        ? null
                        : Timestamp.valueOf(
                                lote.getFechaPublicacion()
                        )
        );

        return buscarPorCodigo(
                lote.getCodLote()
        ).orElseThrow(
                () -> new IllegalStateException(
                        "El lote fue insertado, pero no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public Optional<LoteDistribucion> buscarPorCodigo(
            String codLote
    ) {

        if (campoVacio(codLote)) {
            return Optional.empty();
        }

        String sql = """
                SELECT
                    ID_LOTE,
                    COD_LOTE,
                    FECHA_INICIO_PERIODO,
                    FECHA_FIN_PERIODO,
                    ESTADO_LOTE,
                    FECHA_PUBLICACION,
                    FECHA_REGISTRO,
                    FECHA_ACTUALIZACION
                FROM LOTE_DISTRIBUCION
                WHERE COD_LOTE = ?
                """;

        List<LoteDistribucion> resultados =
                jdbcTemplate.query(
                        sql,
                        mapper(),
                        codLote.trim()
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                resultados.get(0)
        );
    }

    @Override
    public Optional<LoteDistribucion> buscarPorPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        if (
                fechaInicio == null
                        || fechaFin == null
        ) {
            return Optional.empty();
        }

        String sql = """
                SELECT
                    ID_LOTE,
                    COD_LOTE,
                    FECHA_INICIO_PERIODO,
                    FECHA_FIN_PERIODO,
                    ESTADO_LOTE,
                    FECHA_PUBLICACION,
                    FECHA_REGISTRO,
                    FECHA_ACTUALIZACION
                FROM LOTE_DISTRIBUCION
                WHERE FECHA_INICIO_PERIODO = ?
                  AND FECHA_FIN_PERIODO = ?
                ORDER BY ID_LOTE
                """;

        List<LoteDistribucion> resultados =
                jdbcTemplate.query(
                        sql,
                        mapper(),
                        Date.valueOf(fechaInicio),
                        Date.valueOf(fechaFin)
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                resultados.get(0)
        );
    }

    @Override
    public boolean marcarPublicado(
            Long idLote
    ) {

        if (idLote == null) {
            throw new IllegalArgumentException(
                    "El ID del lote es obligatorio."
            );
        }

        String sql = """
                UPDATE LOTE_DISTRIBUCION
                SET
                    ESTADO_LOTE = 'PUBLICADO',
                    FECHA_PUBLICACION = SYSTIMESTAMP,
                    FECHA_ACTUALIZACION = SYSTIMESTAMP
                WHERE ID_LOTE = ?
                  AND FECHA_PUBLICACION IS NULL
                """;

        int filas =
                jdbcTemplate.update(
                        sql,
                        idLote
                );

        return filas > 0;
    }

    private RowMapper<LoteDistribucion> mapper() {

        return (
                ResultSet rs,
                int rowNum
        ) -> {

            LoteDistribucion lote =
                    new LoteDistribucion();

            lote.setIdLote(
                    rs.getLong("ID_LOTE")
            );

            lote.setCodLote(
                    rs.getString("COD_LOTE")
            );

            Date fechaInicio =
                    rs.getDate(
                            "FECHA_INICIO_PERIODO"
                    );

            if (fechaInicio != null) {
                lote.setFechaInicioPeriodo(
                        fechaInicio.toLocalDate()
                );
            }

            Date fechaFin =
                    rs.getDate(
                            "FECHA_FIN_PERIODO"
                    );

            if (fechaFin != null) {
                lote.setFechaFinPeriodo(
                        fechaFin.toLocalDate()
                );
            }

            lote.setEstadoLote(
                    rs.getString("ESTADO_LOTE")
            );

            Timestamp fechaPublicacion =
                    rs.getTimestamp(
                            "FECHA_PUBLICACION"
                    );

            if (fechaPublicacion != null) {
                lote.setFechaPublicacion(
                        fechaPublicacion
                                .toLocalDateTime()
                );
            }

            Timestamp fechaRegistro =
                    rs.getTimestamp(
                            "FECHA_REGISTRO"
                    );

            if (fechaRegistro != null) {
                lote.setFechaRegistro(
                        fechaRegistro
                                .toLocalDateTime()
                );
            }

            Timestamp fechaActualizacion =
                    rs.getTimestamp(
                            "FECHA_ACTUALIZACION"
                    );

            if (fechaActualizacion != null) {
                lote.setFechaActualizacion(
                        fechaActualizacion
                                .toLocalDateTime()
                );
            }

            return lote;
        };
    }

    private void validarLote(
            LoteDistribucion lote
    ) {

        if (lote == null) {
            throw new IllegalArgumentException(
                    "El lote es obligatorio."
            );
        }

        if (campoVacio(lote.getCodLote())) {
            throw new IllegalArgumentException(
                    "El código del lote es obligatorio."
            );
        }

        if (
                lote.getFechaInicioPeriodo() == null
                        || lote.getFechaFinPeriodo() == null
        ) {
            throw new IllegalArgumentException(
                    "El período del lote es obligatorio."
            );
        }

        if (
                lote.getFechaInicioPeriodo()
                        .isAfter(
                                lote.getFechaFinPeriodo()
                        )
        ) {
            throw new IllegalArgumentException(
                    "La fecha inicial del lote no puede ser posterior a la final."
            );
        }

        if (campoVacio(lote.getEstadoLote())) {
            throw new IllegalArgumentException(
                    "El estado del lote es obligatorio."
            );
        }
    }

    private boolean campoVacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }
}
