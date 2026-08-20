package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDocumentoPublicadoRepository
        implements DocumentoPublicadoRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDocumentoPublicadoRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DocumentoPublicado guardar(
            DocumentoPublicado documentoPublicado
    ) {

        int filasActualizadas =
                actualizarDocumentoExistente(
                        documentoPublicado
                );

        if (filasActualizadas == 0) {
            insertarDocumento(
                    documentoPublicado
            );
        }

        return buscarPorId(
                documentoPublicado.getIdDocumentoPublicado()
        ).orElseThrow(
                () -> new IllegalStateException(
                        "El documento se registró en el repositorio simulado, "
                                + "pero no pudo recuperarse posteriormente."
                )
        );
    }

    private int actualizarDocumentoExistente(
            DocumentoPublicado documento
    ) {

        String sql = """
            UPDATE VIDA_REPO_SIM.DOCUMENTO_PUBLICADO_SIM
            SET
                ID_DOCUMENTO_PUBLICADO = ?,
                ID_DOCUMENTO_SELLADO = ?,
                NUMERO_DOCUMENTO_TRABAJADOR = ?,
                NOMBRE_ARCHIVO = ?,
                CONTENT_TYPE = ?,
                CONTENIDO_ARCHIVO = ?,
                HASH_SHA256 = ?,
                FECHA_PUBLICACION = ?,
                CANAL_PUBLICACION = ?,
                PUBLICADO_POR = ?,
                ESTADO_PUBLICACION = ?,
                DISPONIBLE_USUARIO = ?,
                FECHA_ACTUALIZACION = SYSTIMESTAMP
            WHERE REGISTRO_INTERNO_PROCESO = ?
              AND TIPO_DOCUMENTO_LOGICO = ?
            """;

        return jdbcTemplate.update(
                connection -> {

                    PreparedStatement ps =
                            connection.prepareStatement(sql);

                    ps.setString(
                            1,
                            documento.getIdDocumentoPublicado()
                    );

                    ps.setString(
                            2,
                            documento.getIdDocumentoSellado()
                    );

                    ps.setString(
                            3,
                            documento.getNumeroDocumentoTrabajador()
                    );

                    ps.setString(
                            4,
                            documento.getNombreArchivo()
                    );

                    ps.setString(
                            5,
                            documento.getContentType()
                    );

                    ps.setBlob(
                            6,
                            new ByteArrayInputStream(
                                    documento.getContenidoArchivo()
                            )
                    );

                    ps.setString(
                            7,
                            documento.getHashSha256DocumentoPublicado()
                    );

                    ps.setTimestamp(
                            8,
                            convertirTimestamp(
                                    documento.getFechaHoraPublicacion()
                            )
                    );

                    ps.setString(
                            9,
                            documento.getCanalPublicacion()
                    );

                    ps.setString(
                            10,
                            documento.getPublicadoPor()
                    );

                    ps.setString(
                            11,
                            documento.getEstadoPublicacionDocumental()
                    );

                    ps.setString(
                            12,
                            documento.isDisponibleParaUsuario()
                                    ? "S"
                                    : "N"
                    );

                    ps.setString(
                            13,
                            documento.getRegistroInternoProceso()
                    );

                    ps.setString(
                            14,
                            documento.getTipoDocumento()
                    );

                    return ps;
                }
        );
    }

    private void insertarDocumento(
            DocumentoPublicado documento
    ) {

        String sql = """
            INSERT INTO VIDA_REPO_SIM.DOCUMENTO_PUBLICADO_SIM (
                ID_DOCUMENTO_PUBLICADO,
                ID_DOCUMENTO_SELLADO,
                REGISTRO_INTERNO_PROCESO,
                TIPO_DOCUMENTO_LOGICO,
                NUMERO_DOCUMENTO_TRABAJADOR,
                NOMBRE_ARCHIVO,
                CONTENT_TYPE,
                CONTENIDO_ARCHIVO,
                HASH_SHA256,
                FECHA_PUBLICACION,
                CANAL_PUBLICACION,
                PUBLICADO_POR,
                ESTADO_PUBLICACION,
                DISPONIBLE_USUARIO,
                FECHA_REGISTRO,
                FECHA_ACTUALIZACION
            )
            VALUES (
                ?, ?,
                ?, ?,
                ?,
                ?, ?,
                ?,
                ?,
                ?,
                ?, ?,
                ?, ?,
                SYSTIMESTAMP,
                SYSTIMESTAMP
            )
            """;

        jdbcTemplate.update(
                connection -> {

                    PreparedStatement ps =
                            connection.prepareStatement(sql);

                    ps.setString(
                            1,
                            documento.getIdDocumentoPublicado()
                    );

                    ps.setString(
                            2,
                            documento.getIdDocumentoSellado()
                    );

                    ps.setString(
                            3,
                            documento.getRegistroInternoProceso()
                    );

                    ps.setString(
                            4,
                            documento.getTipoDocumento()
                    );

                    ps.setString(
                            5,
                            documento.getNumeroDocumentoTrabajador()
                    );

                    ps.setString(
                            6,
                            documento.getNombreArchivo()
                    );

                    ps.setString(
                            7,
                            documento.getContentType()
                    );

                    ps.setBlob(
                            8,
                            new ByteArrayInputStream(
                                    documento.getContenidoArchivo()
                            )
                    );

                    ps.setString(
                            9,
                            documento.getHashSha256DocumentoPublicado()
                    );

                    ps.setTimestamp(
                            10,
                            convertirTimestamp(
                                    documento.getFechaHoraPublicacion()
                            )
                    );

                    ps.setString(
                            11,
                            documento.getCanalPublicacion()
                    );

                    ps.setString(
                            12,
                            documento.getPublicadoPor()
                    );

                    ps.setString(
                            13,
                            documento.getEstadoPublicacionDocumental()
                    );

                    ps.setString(
                            14,
                            documento.isDisponibleParaUsuario()
                                    ? "S"
                                    : "N"
                    );

                    return ps;
                }
        );
    }

    @Override
    public Optional<DocumentoPublicado> buscarPorId(
            String idDocumentoPublicado
    ) {

        String sql = consultaBase()
                + """
                  WHERE ID_DOCUMENTO_PUBLICADO = ?
                  """;

        try {

            DocumentoPublicado documento =
                    jdbcTemplate.queryForObject(
                            sql,
                            this::mapearDocumentoPublicado,
                            idDocumentoPublicado
                    );

            return Optional.ofNullable(documento);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<DocumentoPublicado> listar() {

        String sql = consultaBase()
                + """
                  ORDER BY FECHA_PUBLICACION DESC
                  """;

        return jdbcTemplate.query(
                sql,
                this::mapearDocumentoPublicado
        );
    }

    @Override
    public List<DocumentoPublicado> buscarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {

        String sql = consultaBase()
                + """
                  WHERE REGISTRO_INTERNO_PROCESO = ?
                    AND NUMERO_DOCUMENTO_TRABAJADOR = ?
                  ORDER BY FECHA_PUBLICACION DESC
                  """;

        return jdbcTemplate.query(
                sql,
                this::mapearDocumentoPublicado,
                registroInternoProceso,
                numeroDocumentoTrabajador
        );
    }

    private String consultaBase() {

        return """
                SELECT
                    ID_DOCUMENTO_PUBLICADO,
                    ID_DOCUMENTO_SELLADO,
                    REGISTRO_INTERNO_PROCESO,
                    TIPO_DOCUMENTO_LOGICO,
                    NUMERO_DOCUMENTO_TRABAJADOR,
                    NOMBRE_ARCHIVO,
                    CONTENT_TYPE,
                    CONTENIDO_ARCHIVO,
                    HASH_SHA256,
                    FECHA_PUBLICACION,
                    CANAL_PUBLICACION,
                    PUBLICADO_POR,
                    ESTADO_PUBLICACION,
                    DISPONIBLE_USUARIO
                FROM VIDA_REPO_SIM.DOCUMENTO_PUBLICADO_SIM
                """;
    }

    private DocumentoPublicado mapearDocumentoPublicado(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        DocumentoPublicado documento =
                new DocumentoPublicado();

        documento.setIdDocumentoPublicado(
                rs.getString("ID_DOCUMENTO_PUBLICADO")
        );

        documento.setIdDocumentoSellado(
                rs.getString("ID_DOCUMENTO_SELLADO")
        );

        documento.setRegistroInternoProceso(
                rs.getString("REGISTRO_INTERNO_PROCESO")
        );

        documento.setTipoDocumento(
                rs.getString("TIPO_DOCUMENTO_LOGICO")
        );

        documento.setNumeroDocumentoTrabajador(
                rs.getString("NUMERO_DOCUMENTO_TRABAJADOR")
        );

        documento.setNombreArchivo(
                rs.getString("NOMBRE_ARCHIVO")
        );

        documento.setContentType(
                rs.getString("CONTENT_TYPE")
        );

        documento.setContenidoArchivo(
                rs.getBytes("CONTENIDO_ARCHIVO")
        );

        documento.setHashSha256DocumentoPublicado(
                rs.getString("HASH_SHA256")
        );

        Timestamp fechaPublicacion =
                rs.getTimestamp("FECHA_PUBLICACION");

        if (fechaPublicacion != null) {
            documento.setFechaHoraPublicacion(
                    fechaPublicacion.toLocalDateTime()
            );
        }

        documento.setCanalPublicacion(
                rs.getString("CANAL_PUBLICACION")
        );

        documento.setPublicadoPor(
                rs.getString("PUBLICADO_POR")
        );

        documento.setEstadoPublicacionDocumental(
                rs.getString("ESTADO_PUBLICACION")
        );

        documento.setDisponibleParaUsuario(
                "S".equalsIgnoreCase(
                        rs.getString("DISPONIBLE_USUARIO")
                )
        );

        return documento;
    }

    private Timestamp convertirTimestamp(
            java.time.LocalDateTime fecha
    ) {
        return fecha == null
                ? null
                : Timestamp.valueOf(fecha);
    }
}