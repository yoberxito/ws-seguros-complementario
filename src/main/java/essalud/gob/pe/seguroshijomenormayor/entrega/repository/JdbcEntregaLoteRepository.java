package essalud.gob.pe.seguroshijomenormayor.entrega.repository;

import essalud.gob.pe.seguroshijomenormayor.entrega.model.EntregaLote;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository("masVidaJdbcEntregaLoteRepository")
public class JdbcEntregaLoteRepository
        implements EntregaLoteRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEntregaLoteRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public EntregaLote crear(
            EntregaLote entrega
    ) {

        validarEntregaNueva(entrega);

        Long idEntrega =
                jdbcTemplate.queryForObject(
                        "SELECT SEQ_ENTREGA_LOTE.NEXTVAL FROM DUAL",
                        Long.class
                );

        String sql = """
                INSERT INTO ENTREGA_LOTE (
                    ID_ENTREGA,
                    COD_ENTREGA,
                    ID_LOTE,
                    TIPO_DESTINATARIO,
                    CORREO_DESTINATARIO,
                    TOKEN_HASH,
                    CANTIDAD_DOCUMENTOS,
                    ESTADO_NOTIFICACION,
                    FECHA_NOTIFICACION,
                    URL_ACCESO,
                    FECHA_ACUSE,
                    TEXTO_ACUSE,
                    VERSION_TEXTO_ACUSE,
                    IP_ACUSE,
                    DATOS_SESION_DISPOSITIVO,
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
                    ?,
                    ?,
                    NULL,
                    ?,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    SYSTIMESTAMP,
                    SYSTIMESTAMP
                )
                """;

        jdbcTemplate.update(
                sql,
                idEntrega,
                entrega.getCodEntrega().trim(),
                entrega.getIdLote(),
                entrega.getTipoDestinatario().trim(),
                entrega.getCorreoDestinatario().trim(),
                entrega.getTokenHash().trim(),
                entrega.getCantidadDocumentos(),
                entrega.getEstadoNotificacion().trim(),
                normalizarNullable(
                        entrega.getUrlAcceso()
                )
        );

        return buscarPorLoteYDestinatario(
                entrega.getIdLote(),
                entrega.getTipoDestinatario()
        ).orElseThrow(
                () -> new IllegalStateException(
                        "La entrega fue insertada, pero no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public Optional<EntregaLote>
    buscarPorLoteYDestinatario(
            Long idLote,
            String tipoDestinatario
    ) {

        if (
                idLote == null
                        || campoVacio(tipoDestinatario)
        ) {
            return Optional.empty();
        }

        String sql = selectBase() + """
                WHERE E.ID_LOTE = ?
                  AND E.TIPO_DESTINATARIO = ?
                """;

        List<EntregaLote> resultados =
                jdbcTemplate.query(
                        sql,
                        this::mapearEntrega,
                        idLote,
                        tipoDestinatario.trim()
                );

        return resultados.isEmpty()
                ? Optional.empty()
                : Optional.of(resultados.get(0));
    }

    @Override
    public Optional<EntregaLote> buscarPorTokenHash(
            String tokenHash
    ) {

        if (campoVacio(tokenHash)) {
            return Optional.empty();
        }

        String sql = selectBase() + """
                WHERE E.TOKEN_HASH = ?
                """;

        List<EntregaLote> resultados =
                jdbcTemplate.query(
                        sql,
                        this::mapearEntrega,
                        tokenHash.trim()
                );

        return resultados.isEmpty()
                ? Optional.empty()
                : Optional.of(resultados.get(0));
    }

    @Override
    public boolean actualizarPreparacionPendiente(
            Long idEntrega,
            String correoDestinatario,
            String tokenHash,
            int cantidadDocumentos,
            String urlAcceso
    ) {

        String sql = """
                UPDATE ENTREGA_LOTE
                SET
                    CORREO_DESTINATARIO = ?,
                    TOKEN_HASH = ?,
                    CANTIDAD_DOCUMENTOS = ?,
                    URL_ACCESO = ?,
                    ESTADO_NOTIFICACION = 'PENDIENTE',
                    FECHA_NOTIFICACION = NULL,
                    FECHA_ACTUALIZACION = SYSTIMESTAMP
                WHERE ID_ENTREGA = ?
                  AND ESTADO_NOTIFICACION IN (
                      'PENDIENTE',
                      'ERROR_ENVIO'
                  )
                  AND FECHA_ACUSE IS NULL
                """;

        int filas = jdbcTemplate.update(
                sql,
                correoDestinatario.trim(),
                tokenHash.trim(),
                cantidadDocumentos,
                normalizarNullable(urlAcceso),
                idEntrega
        );

        return filas > 0;
    }

    @Override
    public boolean marcarEnviando(
            Long idEntrega
    ) {

        String sql = """
                UPDATE ENTREGA_LOTE
                SET
                    ESTADO_NOTIFICACION = 'ENVIANDO',
                    FECHA_ACTUALIZACION = SYSTIMESTAMP
                WHERE ID_ENTREGA = ?
                  AND ESTADO_NOTIFICACION IN (
                      'PENDIENTE',
                      'ERROR_ENVIO'
                  )
                """;

        return jdbcTemplate.update(
                sql,
                idEntrega
        ) > 0;
    }

    @Override
    public boolean marcarEnviado(
            Long idEntrega
    ) {

        String sql = """
                UPDATE ENTREGA_LOTE
                SET
                    ESTADO_NOTIFICACION = 'ENVIADO',
                    FECHA_NOTIFICACION = SYSTIMESTAMP,
                    FECHA_ACTUALIZACION = SYSTIMESTAMP
                WHERE ID_ENTREGA = ?
                  AND ESTADO_NOTIFICACION = 'ENVIANDO'
                """;

        return jdbcTemplate.update(
                sql,
                idEntrega
        ) > 0;
    }

    @Override
    public boolean marcarErrorEnvio(
            Long idEntrega
    ) {

        String sql = """
                UPDATE ENTREGA_LOTE
                SET
                    ESTADO_NOTIFICACION = 'ERROR_ENVIO',
                    FECHA_ACTUALIZACION = SYSTIMESTAMP
                WHERE ID_ENTREGA = ?
                  AND ESTADO_NOTIFICACION = 'ENVIANDO'
                """;

        return jdbcTemplate.update(
                sql,
                idEntrega
        ) > 0;
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
                    FECHA_ACUSE = SYSTIMESTAMP,
                    TEXTO_ACUSE = ?,
                    VERSION_TEXTO_ACUSE = ?,
                    IP_ACUSE = ?,
                    DATOS_SESION_DISPOSITIVO = ?,
                    FECHA_ACTUALIZACION = SYSTIMESTAMP
                WHERE TOKEN_HASH = ?
                  AND FECHA_ACUSE IS NULL
                  AND EXISTS (
                      SELECT 1
                      FROM LOTE_DISTRIBUCION L
                      WHERE L.ID_LOTE =
                            ENTREGA_LOTE.ID_LOTE
                        AND L.FECHA_PUBLICACION
                            IS NOT NULL
                  )
                """;

        int filas =
                jdbcTemplate.update(
                        sql,
                        textoAcuse,
                        versionTextoAcuse,
                        ipAcuse,
                        datosSesionDispositivo,
                        tokenHash
                );

        return filas > 0;
    }

    private String selectBase() {

        return """
                SELECT
                    E.ID_ENTREGA,
                    E.COD_ENTREGA,
                    E.ID_LOTE,
                    E.TIPO_DESTINATARIO,
                    E.CORREO_DESTINATARIO,
                    E.TOKEN_HASH,
                    E.CANTIDAD_DOCUMENTOS,
                    E.ESTADO_NOTIFICACION,
                    E.FECHA_NOTIFICACION,
                    E.URL_ACCESO,
                    E.FECHA_ACUSE,
                    E.TEXTO_ACUSE,
                    E.VERSION_TEXTO_ACUSE,
                    E.IP_ACUSE,
                    E.DATOS_SESION_DISPOSITIVO,
                    E.FECHA_REGISTRO,
                    E.FECHA_ACTUALIZACION,
                    L.FECHA_INICIO_PERIODO,
                    L.FECHA_FIN_PERIODO,
                    L.FECHA_PUBLICACION
                FROM ENTREGA_LOTE E
                INNER JOIN LOTE_DISTRIBUCION L
                    ON L.ID_LOTE = E.ID_LOTE
                """;
    }

    private EntregaLote mapearEntrega(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        EntregaLote entrega =
                new EntregaLote();

        entrega.setIdEntrega(
                rs.getLong("ID_ENTREGA")
        );

        entrega.setCodEntrega(
                rs.getString("COD_ENTREGA")
        );

        entrega.setIdLote(
                rs.getLong("ID_LOTE")
        );

        entrega.setTipoDestinatario(
                rs.getString("TIPO_DESTINATARIO")
        );

        entrega.setCorreoDestinatario(
                rs.getString("CORREO_DESTINATARIO")
        );

        entrega.setTokenHash(
                rs.getString("TOKEN_HASH")
        );

        entrega.setCantidadDocumentos(
                rs.getInt("CANTIDAD_DOCUMENTOS")
        );

        entrega.setEstadoNotificacion(
                rs.getString("ESTADO_NOTIFICACION")
        );

        entrega.setFechaNotificacion(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_NOTIFICACION"
                        )
                )
        );

        entrega.setUrlAcceso(
                rs.getString("URL_ACCESO")
        );

        entrega.setFechaAcuse(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_ACUSE"
                        )
                )
        );

        entrega.setTextoAcuse(
                rs.getString("TEXTO_ACUSE")
        );

        entrega.setVersionTextoAcuse(
                rs.getString(
                        "VERSION_TEXTO_ACUSE"
                )
        );

        entrega.setIpAcuse(
                rs.getString("IP_ACUSE")
        );

        entrega.setDatosSesionDispositivo(
                rs.getString(
                        "DATOS_SESION_DISPOSITIVO"
                )
        );

        entrega.setFechaRegistro(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_REGISTRO"
                        )
                )
        );

        entrega.setFechaActualizacion(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_ACTUALIZACION"
                        )
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

    private void validarEntregaNueva(
            EntregaLote entrega
    ) {

        if (entrega == null) {
            throw new IllegalArgumentException(
                    "La entrega es obligatoria."
            );
        }

        if (
                entrega.getIdLote() == null
                        || campoVacio(
                                entrega.getCodEntrega()
                        )
                        || campoVacio(
                                entrega.getTipoDestinatario()
                        )
                        || campoVacio(
                                entrega.getCorreoDestinatario()
                        )
                        || campoVacio(
                                entrega.getTokenHash()
                        )
                        || campoVacio(
                                entrega.getEstadoNotificacion()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Los datos obligatorios de la entrega están incompletos."
            );
        }

        if (
                entrega.getTokenHash()
                        .trim()
                        .length() != 64
        ) {
            throw new IllegalArgumentException(
                    "TOKEN_HASH debe contener 64 caracteres."
            );
        }

        if (entrega.getCantidadDocumentos() < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de documentos no puede ser negativa."
            );
        }
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

    private String normalizarNullable(
            String valor
    ) {

        return campoVacio(valor)
                ? null
                : valor.trim();
    }

    private boolean campoVacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }
}