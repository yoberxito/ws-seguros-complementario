package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoGenerado;
import essalud.gob.pe.wsseguroscomplementario.documento.model.PaginaDocumentoGenerado;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDocumentoGeneradoRepository
        implements DocumentoGeneradoRepository {

    private static final String
            ESTADO_PENDIENTE_CARGA =
            "PENDIENTE_CARGA";

    /*
     * Se conserva el estado utilizado por el
     * modelo/API actual de metadata generada.
     *
     * El estado funcional persistido del
     * DOCUMENTOS_SUSTENTO es PENDIENTE_CARGA.
     */
    private static final String
            ESTADO_GENERADO_PENDIENTE_FIRMA =
            "GENERADO_PENDIENTE_FIRMA";

    private final JdbcTemplate jdbcTemplate;

    public JdbcDocumentoGeneradoRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                jdbcTemplate;
    }

    @Override
    public DocumentoGenerado guardar(
            DocumentoGenerado documentoGenerado
    ) {

        String sql = """
                MERGE INTO DOCUMENTOS_SUSTENTO D
                USING (
                    SELECT
                        T.ID_SECOMASVIDA
                    FROM TEMP_SECOMASVIDA T
                    WHERE
                        T.REGISTRO_INTERNO_PROCESO = ?
                ) P
                ON (
                    D.ID_SECOMASVIDA =
                        P.ID_SECOMASVIDA
                    AND
                    D.TIPO_DOCUMENTO_LOGICO = ?
                )

                WHEN NOT MATCHED THEN
                    INSERT (
                        ID_SECOMASVIDA,
                        ID_ESTADO_SUSTENTO,
                        TIPO_DOCUMENTO_LOGICO,

                        ID_DOCUMENTO_GENERADO,
                        VERSION_FORMATO,

                        NOMBRE_ARCHIVO_GENERADO,
                        TAMANIO_BYTES_GENERADO,
                        NUMERO_PAGINAS_GENERADAS,
                        HASH_DOCUMENT_ORIGINAL,

                        CANT_BENEFICIARIOS_GENERACION,
                        FECHA_GENERACION,

                        GENERADO_POR,
                        CANAL_GENERACION,

                        FECHA_REGISTRO,
                        FECHA_ACTUALIZACION
                    )
                    VALUES (
                        P.ID_SECOMASVIDA,

                        (
                            SELECT
                                E.ID_ESTADO_SUSTENTO
                            FROM ESTADO_SUSTENTO E
                            WHERE
                                E.COD_ESTADO_DOCUMENTO = ?
                        ),

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
                        ?,

                        SYSTIMESTAMP,
                        SYSTIMESTAMP
                    )
                """;

        jdbcTemplate.update(
                sql,

                documentoGenerado
                        .getRegistroInternoProceso(),

                documentoGenerado
                        .getTipoDocumento(),

                ESTADO_PENDIENTE_CARGA,

                documentoGenerado
                        .getTipoDocumento(),

                documentoGenerado
                        .getIdDocumentoGenerado(),

                documentoGenerado
                        .getVersionFormato(),

                documentoGenerado
                        .getNombreArchivoOriginal(),

                documentoGenerado
                        .getTamanioBytes(),

                documentoGenerado
                        .getNumeroPaginasGeneradas(),

                documentoGenerado
                        .getHashSha256DocumentoOriginal(),

                documentoGenerado
                        .getCantidadBeneficiariosRegistrados(),

                convertirTimestamp(
                        documentoGenerado
                                .getFechaHoraGeneracion()
                ),

                documentoGenerado
                        .getGeneradoPor(),

                documentoGenerado
                        .getCanalGeneracion()
        );

        return buscarPorRegistroInternoProcesoYTipoDocumento(
                documentoGenerado
                        .getRegistroInternoProceso(),

                documentoGenerado
                        .getTipoDocumento()
        ).orElseThrow(
                () -> new IllegalStateException(
                        "La metadata documental fue procesada, pero no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public Optional<DocumentoGenerado>
    buscarPorId(
            String idDocumentoGenerado
    ) {

        String sql =
                consultaBase()
                        + """
                  WHERE
                      D.ID_DOCUMENTO_GENERADO = ?
                  """;

        try {
            DocumentoGenerado documento =
                    jdbcTemplate.queryForObject(
                            sql,
                            this::mapearDocumentoGenerado,
                            idDocumentoGenerado
                    );

            return Optional.ofNullable(
                    documento
            );

        } catch (
                EmptyResultDataAccessException e
        ) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<DocumentoGenerado>
    buscarPorRegistroInternoProcesoYTipoDocumento(
            String registroInternoProceso,
            String tipoDocumento
    ) {

        String sql =
                consultaBase()
                        + """
                  WHERE
                      T.REGISTRO_INTERNO_PROCESO = ?
                      AND
                      D.TIPO_DOCUMENTO_LOGICO = ?
                  """;

        try {
            DocumentoGenerado documento =
                    jdbcTemplate.queryForObject(
                            sql,
                            this::mapearDocumentoGenerado,
                            registroInternoProceso,
                            tipoDocumento
                    );

            return Optional.ofNullable(
                    documento
            );

        } catch (
                EmptyResultDataAccessException e
        ) {
            return Optional.empty();
        }
    }

    @Override
    public List<DocumentoGenerado>
    buscarPorRegistroInternoProceso(
            String registroInternoProceso
    ) {

        String sql =
                consultaBase()
                        + """
                  WHERE
                      T.REGISTRO_INTERNO_PROCESO = ?
                  ORDER BY
                      D.ID_DOC_SUSTENTO
                  """;

        return jdbcTemplate.query(
                sql,
                this::mapearDocumentoGenerado,
                registroInternoProceso
        );
    }

    @Override
    public List<DocumentoGenerado> listar() {

        String sql =
                consultaBase()
                        + """
                  ORDER BY
                      D.ID_DOC_SUSTENTO
                  """;

        return jdbcTemplate.query(
                sql,
                this::mapearDocumentoGenerado
        );
    }

    private String consultaBase() {
        return """
                SELECT
                    D.ID_DOCUMENTO_GENERADO,
                    D.TIPO_DOCUMENTO_LOGICO,
                    D.VERSION_FORMATO,

                    D.NOMBRE_ARCHIVO_GENERADO,
                    D.TAMANIO_BYTES_GENERADO,
                    D.NUMERO_PAGINAS_GENERADAS,
                    D.HASH_DOCUMENT_ORIGINAL,

                    D.CANT_BENEFICIARIOS_GENERACION,
                    D.FECHA_GENERACION,
                    D.GENERADO_POR,
                    D.CANAL_GENERACION,

                    T.REGISTRO_INTERNO_PROCESO,

                    T.COD_EDOCUMENT_TITULAR,
                    T.NUM_DOCUMENT_TITULAR,

                    T.APE_PATERNO_TITULAR,
                    T.APE_MATERNO_TITULAR,
                    T.PRIMER_NOMBRE_TITULAR,
                    T.SEGUNDO_NOMBRE_TITULAR,

                    E.COD_ESTADO_DOCUMENTO

                FROM DOCUMENTOS_SUSTENTO D

                INNER JOIN TEMP_SECOMASVIDA T
                    ON T.ID_SECOMASVIDA =
                        D.ID_SECOMASVIDA

                INNER JOIN ESTADO_SUSTENTO E
                    ON E.ID_ESTADO_SUSTENTO =
                        D.ID_ESTADO_SUSTENTO
                """;
    }

    private DocumentoGenerado
    mapearDocumentoGenerado(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        DocumentoGenerado documento =
                new DocumentoGenerado();

        documento.setIdDocumentoGenerado(
                rs.getString(
                        "ID_DOCUMENTO_GENERADO"
                )
        );

        documento.setRegistroInternoProceso(
                rs.getString(
                        "REGISTRO_INTERNO_PROCESO"
                )
        );

        documento.setTipoDocumento(
                rs.getString(
                        "TIPO_DOCUMENTO_LOGICO"
                )
        );

        documento.setVersionFormato(
                rs.getString(
                        "VERSION_FORMATO"
                )
        );

        documento.setTipoDocumentoTrabajador(
                rs.getString(
                        "COD_EDOCUMENT_TITULAR"
                )
        );

        documento.setNumeroDocumentoTrabajador(
                rs.getString(
                        "NUM_DOCUMENT_TITULAR"
                )
        );

        documento.setNombresApellidosTrabajador(
                construirNombreCompleto(
                        rs
                )
        );

        documento.setNombreArchivoOriginal(
                rs.getString(
                        "NOMBRE_ARCHIVO_GENERADO"
                )
        );

        documento.setTamanioBytes(
                rs.getLong(
                        "TAMANIO_BYTES_GENERADO"
                )
        );

        documento.setNumeroPaginasGeneradas(
                rs.getInt(
                        "NUMERO_PAGINAS_GENERADAS"
                )
        );

        documento.setHashSha256DocumentoOriginal(
                rs.getString(
                        "HASH_DOCUMENT_ORIGINAL"
                )
        );

        documento
                .setCantidadBeneficiariosRegistrados(
                        rs.getInt(
                                "CANT_BENEFICIARIOS_GENERACION"
                        )
                );

        Timestamp fechaGeneracion =
                rs.getTimestamp(
                        "FECHA_GENERACION"
                );

        documento.setFechaHoraGeneracion(
                fechaGeneracion == null
                        ? null
                        : fechaGeneracion
                        .toLocalDateTime()
        );

        documento.setGeneradoPor(
                rs.getString(
                        "GENERADO_POR"
                )
        );

        documento.setCanalGeneracion(
                rs.getString(
                        "CANAL_GENERACION"
                )
        );

        /*
         * El modelo DocumentoGenerado conserva
         * su semántica histórica.
         *
         * El estado funcional completo vive en
         * DOCUMENTOS_SUSTENTO.
         */
        documento.setEstadoDocumentoGenerado(
                ESTADO_GENERADO_PENDIENTE_FIRMA
        );

        documento.setPaginasEsperadas(
                construirPaginasEsperadas(
                        documento
                                .getIdDocumentoGenerado(),

                        documento
                                .getTipoDocumento(),

                        documento
                                .getNumeroPaginasGeneradas()
                )
        );

        return documento;
    }

    private List<PaginaDocumentoGenerado>
    construirPaginasEsperadas(
            String idDocumentoGenerado,
            String tipoDocumento,
            int totalPaginas
    ) {

        List<PaginaDocumentoGenerado> paginas =
                new ArrayList<>();

        for (
                int numeroPagina = 1;
                numeroPagina <= totalPaginas;
                numeroPagina++
        ) {

            String identificadorPagina =
                    idDocumentoGenerado
                            + "-PAG-"
                            + numeroPagina
                            + "-DE-"
                            + totalPaginas;

            String contenidoQrEsperado =
                    "ID_DOCUMENTO="
                            + idDocumentoGenerado
                            + "|TIPO_DOCUMENTO="
                            + tipoDocumento
                            + "|PAGINA="
                            + numeroPagina
                            + "|TOTAL_PAGINAS="
                            + totalPaginas;

            paginas.add(
                    new PaginaDocumentoGenerado(
                            numeroPagina,
                            totalPaginas,
                            identificadorPagina,
                            contenidoQrEsperado
                    )
            );
        }

        return paginas;
    }

    private String construirNombreCompleto(
            ResultSet rs
    ) throws SQLException {

        List<String> partes =
                new ArrayList<>();

        agregarParte(
                partes,
                rs.getString(
                        "APE_PATERNO_TITULAR"
                )
        );

        agregarParte(
                partes,
                rs.getString(
                        "APE_MATERNO_TITULAR"
                )
        );

        agregarParte(
                partes,
                rs.getString(
                        "PRIMER_NOMBRE_TITULAR"
                )
        );

        agregarParte(
                partes,
                rs.getString(
                        "SEGUNDO_NOMBRE_TITULAR"
                )
        );

        return String.join(
                " ",
                partes
        );
    }

    private void agregarParte(
            List<String> partes,
            String valor
    ) {
        if (
                valor != null
                        && !valor.trim().isEmpty()
        ) {
            partes.add(
                    valor.trim()
            );
        }
    }

    private Timestamp convertirTimestamp(
            java.time.LocalDateTime fecha
    ) {
        return fecha == null
                ? null
                : Timestamp.valueOf(fecha);
    }
}