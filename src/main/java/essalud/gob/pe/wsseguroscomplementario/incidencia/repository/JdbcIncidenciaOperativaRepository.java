package essalud.gob.pe.wsseguroscomplementario.incidencia.repository;

import essalud.gob.pe.wsseguroscomplementario.common.constants.EstadoProcesoConstants;
import essalud.gob.pe.wsseguroscomplementario.incidencia.model.IncidenciaOperativa;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import essalud.gob.pe.wsseguroscomplementario.incidencia.model.ReintentoIncidenciaOperativa;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcIncidenciaOperativaRepository
        implements IncidenciaOperativaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcIncidenciaOperativaRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public IncidenciaOperativa guardar(
            IncidenciaOperativa incidenciaOperativa
    ) {

        validarIncidencia(
                incidenciaOperativa
        );

        int filasActualizadas =
                actualizarIncidenciaExistente(
                        incidenciaOperativa
                );

        if (filasActualizadas == 0) {
            insertarNuevaIncidencia(
                    incidenciaOperativa
            );
        }

        return buscarPorId(
                incidenciaOperativa
                        .getIdIncidenciaOperativa()
        ).orElseThrow(
                () -> new IllegalStateException(
                        "La incidencia operativa fue persistida, "
                                + "pero no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public Optional<IncidenciaOperativa> buscarPorId(
            String idIncidenciaOperativa
    ) {

        if (campoVacio(idIncidenciaOperativa)) {
            return Optional.empty();
        }

        String sql = """
            SELECT
                I.COD_INCIDENCIA_OPERATIVA,

                T.REGISTRO_INTERNO_PROCESO,
                T.COD_EDOCUMENT_TITULAR
                    AS TIPO_DOCUMENTO_TRABAJADOR,
                T.NUM_DOCUMENT_TITULAR
                    AS NUMERO_DOCUMENTO_TRABAJADOR,

                D.TIPO_DOCUMENTO_LOGICO
                    AS TIPO_DOCUMENTO_PROCESO,
                D.ID_DOCUMENTO_GENERADO,
                D.ID_DOCUMENTO_CARGADO_ULTIMO,
                D.ID_DOCUMENTO_PUBLICADO,

                I.SISTEMA_INVOLUCRADO,
                I.ETAPA_PROCESO,
                I.TIPO_INCIDENCIA,
                I.MOTIVO_INCIDENCIA,
                I.DETALLE_INCIDENCIA,
                I.ESTADO_INCIDENCIA,
                I.NUMERO_REINTENTOS,
                I.FECHA_ULTIMO_REINTENTO,
                I.USUARIO_RESPONSABLE,
                I.FECHA_REGISTRO,
                I.FECHA_CIERRE,
                I.RESULTADO_CIERRE

            FROM INCIDENCIA_OPERATIVA I

            INNER JOIN TEMP_SECOMASVIDA T
                ON T.ID_SECOMASVIDA =
                   I.ID_SECOMASVIDA

            LEFT JOIN DOCUMENTOS_SUSTENTO D
                ON D.ID_DOC_SUSTENTO =
                   I.ID_DOC_SUSTENTO

            WHERE
                I.COD_INCIDENCIA_OPERATIVA = ?
            """;

        List<IncidenciaOperativa> resultados =
                jdbcTemplate.query(
                        sql,
                        this::mapearIncidencia,
                        idIncidenciaOperativa.trim()
                );

        cargarReintentos(
                resultados
        );

        return resultados
                .stream()
                .findFirst();
    }

    @Override
    public List<IncidenciaOperativa>
    buscarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {

        return consultarPorProcesoYTrabajador(
                registroInternoProceso,
                numeroDocumentoTrabajador
        );
    }

    @Override
    public List<IncidenciaOperativa>
    listarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {

        return consultarPorProcesoYTrabajador(
                registroInternoProceso,
                numeroDocumentoTrabajador
        );
    }

    @Override
    public List<IncidenciaOperativa> listar() {

        String sql = """
            SELECT
                I.COD_INCIDENCIA_OPERATIVA,

                T.REGISTRO_INTERNO_PROCESO,
                T.COD_EDOCUMENT_TITULAR
                    AS TIPO_DOCUMENTO_TRABAJADOR,
                T.NUM_DOCUMENT_TITULAR
                    AS NUMERO_DOCUMENTO_TRABAJADOR,

                D.TIPO_DOCUMENTO_LOGICO
                    AS TIPO_DOCUMENTO_PROCESO,
                D.ID_DOCUMENTO_GENERADO,
                D.ID_DOCUMENTO_CARGADO_ULTIMO,
                D.ID_DOCUMENTO_PUBLICADO,

                I.SISTEMA_INVOLUCRADO,
                I.ETAPA_PROCESO,
                I.TIPO_INCIDENCIA,
                I.MOTIVO_INCIDENCIA,
                I.DETALLE_INCIDENCIA,
                I.ESTADO_INCIDENCIA,
                I.NUMERO_REINTENTOS,
                I.FECHA_ULTIMO_REINTENTO,
                I.USUARIO_RESPONSABLE,
                I.FECHA_REGISTRO,
                I.FECHA_CIERRE,
                I.RESULTADO_CIERRE

            FROM INCIDENCIA_OPERATIVA I

            INNER JOIN TEMP_SECOMASVIDA T
                ON T.ID_SECOMASVIDA =
                   I.ID_SECOMASVIDA

            LEFT JOIN DOCUMENTOS_SUSTENTO D
                ON D.ID_DOC_SUSTENTO =
                   I.ID_DOC_SUSTENTO

            ORDER BY
                I.FECHA_REGISTRO DESC
            """;

        List<IncidenciaOperativa> resultados =
                jdbcTemplate.query(
                        sql,
                        this::mapearIncidencia
                );

        cargarReintentos(
                resultados
        );

        return resultados;
    }

    private int actualizarIncidenciaExistente(
            IncidenciaOperativa incidencia
    ) {

        String sql = """
            UPDATE INCIDENCIA_OPERATIVA I

            SET
                I.ID_DOC_SUSTENTO =
                    COALESCE(
                        (
                            SELECT D.ID_DOC_SUSTENTO
                            FROM DOCUMENTOS_SUSTENTO D

                            INNER JOIN TEMP_SECOMASVIDA T
                                ON T.ID_SECOMASVIDA =
                                   D.ID_SECOMASVIDA

                            WHERE
                                T.REGISTRO_INTERNO_PROCESO = ?

                                AND D.TIPO_DOCUMENTO_LOGICO = ?

                            FETCH FIRST 1 ROW ONLY
                        ),
                        I.ID_DOC_SUSTENTO
                    ),

                I.SISTEMA_INVOLUCRADO = ?,
                I.ETAPA_PROCESO = ?,
                I.TIPO_INCIDENCIA = ?,
                I.MOTIVO_INCIDENCIA = ?,
                I.DETALLE_INCIDENCIA = ?,
                I.ESTADO_INCIDENCIA = ?,
                I.NUMERO_REINTENTOS = ?,
                I.FECHA_ULTIMO_REINTENTO = ?,
                I.USUARIO_RESPONSABLE = ?,
                I.FECHA_CIERRE = ?,
                I.RESULTADO_CIERRE = ?,

                I.FECHA_ACTUALIZACION =
                    SYSTIMESTAMP

            WHERE
                I.COD_INCIDENCIA_OPERATIVA = ?
            """;

        return jdbcTemplate.update(
                sql,

                incidencia.getRegistroInternoProceso(),
                normalizarTipoDocumentoNullable(
                        incidencia
                                .getTipoDocumentoProceso()
                ),

                incidencia.getSistemaInvolucrado(),
                incidencia.getEtapaProceso(),
                incidencia.getTipoIncidenciaOperativa(),

                limitar(
                        incidencia.getMotivoObservado(),
                        500
                ),

                limitar(
                        incidencia.getDetalleIncidencia(),
                        2000
                ),

                incidencia.getEstadoIncidencia(),
                incidencia.getNumeroReintentosInternos(),

                convertirTimestamp(
                        incidencia
                                .getFechaHoraUltimoReintento()
                ),

                limitar(
                        incidencia.getUsuarioResponsable(),
                        100
                ),

                convertirTimestamp(
                        incidencia
                                .getFechaHoraCierre()
                ),

                limitar(
                        incidencia.getResultadoCierre(),
                        1000
                ),

                incidencia
                        .getIdIncidenciaOperativa()
        );
    }

    private void insertarNuevaIncidencia(
            IncidenciaOperativa incidencia
    ) {

        String sql = """
            INSERT INTO INCIDENCIA_OPERATIVA (
                COD_INCIDENCIA_OPERATIVA,
                ID_SECOMASVIDA,
                ID_DOC_SUSTENTO,

                SISTEMA_INVOLUCRADO,
                ETAPA_PROCESO,
                TIPO_INCIDENCIA,

                MOTIVO_INCIDENCIA,
                DETALLE_INCIDENCIA,

                ESTADO_INCIDENCIA,
                NUMERO_REINTENTOS,
                FECHA_ULTIMO_REINTENTO,

                USUARIO_RESPONSABLE,

                FECHA_REGISTRO,
                FECHA_ACTUALIZACION,

                FECHA_CIERRE,
                RESULTADO_CIERRE
            )

            SELECT
                ?,
                T.ID_SECOMASVIDA,

                (
                    SELECT D.ID_DOC_SUSTENTO
                    FROM DOCUMENTOS_SUSTENTO D

                    WHERE
                        D.ID_SECOMASVIDA =
                            T.ID_SECOMASVIDA

                        AND D.TIPO_DOCUMENTO_LOGICO = ?

                    FETCH FIRST 1 ROW ONLY
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
                SYSTIMESTAMP,

                ?,
                ?

            FROM TEMP_SECOMASVIDA T

            WHERE
                T.REGISTRO_INTERNO_PROCESO = ?
            """;

        int filasInsertadas =
                jdbcTemplate.update(
                        sql,

                        incidencia
                                .getIdIncidenciaOperativa(),

                        normalizarTipoDocumentoNullable(
                                incidencia
                                        .getTipoDocumentoProceso()
                        ),

                        incidencia
                                .getSistemaInvolucrado(),

                        incidencia
                                .getEtapaProceso(),

                        incidencia
                                .getTipoIncidenciaOperativa(),

                        limitar(
                                incidencia
                                        .getMotivoObservado(),
                                500
                        ),

                        limitar(
                                incidencia
                                        .getDetalleIncidencia(),
                                2000
                        ),

                        incidencia
                                .getEstadoIncidencia(),

                        incidencia
                                .getNumeroReintentosInternos(),

                        convertirTimestamp(
                                incidencia
                                        .getFechaHoraUltimoReintento()
                        ),

                        limitar(
                                incidencia
                                        .getUsuarioResponsable(),
                                100
                        ),

                        convertirTimestamp(
                                incidencia
                                        .getFechaHoraRegistro()
                        ),

                        convertirTimestamp(
                                incidencia
                                        .getFechaHoraCierre()
                        ),

                        limitar(
                                incidencia
                                        .getResultadoCierre(),
                                1000
                        ),

                        incidencia
                                .getRegistroInternoProceso()
                );

        if (filasInsertadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró el proceso +Vida "
                            + "asociado a la incidencia operativa."
            );
        }
    }

    private List<IncidenciaOperativa>
    consultarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {

        String sql = """
            SELECT
                I.COD_INCIDENCIA_OPERATIVA,

                T.REGISTRO_INTERNO_PROCESO,
                T.COD_EDOCUMENT_TITULAR
                    AS TIPO_DOCUMENTO_TRABAJADOR,
                T.NUM_DOCUMENT_TITULAR
                    AS NUMERO_DOCUMENTO_TRABAJADOR,

                D.TIPO_DOCUMENTO_LOGICO
                    AS TIPO_DOCUMENTO_PROCESO,
                D.ID_DOCUMENTO_GENERADO,
                D.ID_DOCUMENTO_CARGADO_ULTIMO,
                D.ID_DOCUMENTO_PUBLICADO,

                I.SISTEMA_INVOLUCRADO,
                I.ETAPA_PROCESO,
                I.TIPO_INCIDENCIA,
                I.MOTIVO_INCIDENCIA,
                I.DETALLE_INCIDENCIA,
                I.ESTADO_INCIDENCIA,
                I.NUMERO_REINTENTOS,
                I.FECHA_ULTIMO_REINTENTO,
                I.USUARIO_RESPONSABLE,
                I.FECHA_REGISTRO,
                I.FECHA_CIERRE,
                I.RESULTADO_CIERRE

            FROM INCIDENCIA_OPERATIVA I

            INNER JOIN TEMP_SECOMASVIDA T
                ON T.ID_SECOMASVIDA =
                   I.ID_SECOMASVIDA

            LEFT JOIN DOCUMENTOS_SUSTENTO D
                ON D.ID_DOC_SUSTENTO =
                   I.ID_DOC_SUSTENTO

            WHERE
                T.REGISTRO_INTERNO_PROCESO = ?

                AND T.NUM_DOCUMENT_TITULAR = ?

            ORDER BY
                I.FECHA_REGISTRO DESC
            """;

        List<IncidenciaOperativa> resultados =
                jdbcTemplate.query(
                        sql,
                        this::mapearIncidencia,
                        registroInternoProceso.trim(),
                        numeroDocumentoTrabajador.trim()
                );

        cargarReintentos(
                resultados
        );

        return resultados;
    }

    private IncidenciaOperativa mapearIncidencia(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        IncidenciaOperativa incidencia =
                new IncidenciaOperativa();

        /*
         * El contrato Java/API conserva el nombre
         * idIncidenciaOperativa, pero su valor público
         * corresponde a COD_INCIDENCIA_OPERATIVA:
         *
         * INC-OP-xxxxxxxx
         */
        incidencia.setIdIncidenciaOperativa(
                rs.getString(
                        "COD_INCIDENCIA_OPERATIVA"
                )
        );

        incidencia.setRegistroInternoProceso(
                rs.getString(
                        "REGISTRO_INTERNO_PROCESO"
                )
        );

        incidencia.setTipoDocumentoTrabajador(
                rs.getString(
                        "TIPO_DOCUMENTO_TRABAJADOR"
                )
        );

        incidencia.setNumeroDocumentoTrabajador(
                rs.getString(
                        "NUMERO_DOCUMENTO_TRABAJADOR"
                )
        );

        incidencia.setTipoDocumentoProceso(
                rs.getString(
                        "TIPO_DOCUMENTO_PROCESO"
                )
        );

        incidencia.setIdDocumentoGenerado(
                rs.getString(
                        "ID_DOCUMENTO_GENERADO"
                )
        );

        incidencia.setIdDocumentoCargado(
                rs.getString(
                        "ID_DOCUMENTO_CARGADO_ULTIMO"
                )
        );

        incidencia.setIdDocumentoPublicado(
                rs.getString(
                        "ID_DOCUMENTO_PUBLICADO"
                )
        );

        /*
         * INCIDENCIA_OPERATIVA no posee una columna
         * propia para ID_DOCUMENTO_SELLADO.
         *
         * Ese dato se persistirá como evento en
         * HISTORIAL_PROC_REGMASVIDA en el siguiente
         * paso del refactor.
         */
        incidencia.setIdDocumentoSellado(
                null
        );

        incidencia.setSistemaInvolucrado(
                rs.getString(
                        "SISTEMA_INVOLUCRADO"
                )
        );

        incidencia.setEtapaProceso(
                rs.getString(
                        "ETAPA_PROCESO"
                )
        );

        incidencia.setTipoIncidenciaOperativa(
                rs.getString(
                        "TIPO_INCIDENCIA"
                )
        );

        incidencia.setMotivoObservado(
                rs.getString(
                        "MOTIVO_INCIDENCIA"
                )
        );

        incidencia.setDetalleIncidencia(
                rs.getString(
                        "DETALLE_INCIDENCIA"
                )
        );

        String estadoIncidencia =
                rs.getString(
                        "ESTADO_INCIDENCIA"
                );

        incidencia.setEstadoIncidencia(
                estadoIncidencia
        );

        boolean incidenciaCerrada =
                EstadoProcesoConstants
                        .SUBSANADO_OPERATIVAMENTE
                        .equalsIgnoreCase(
                                estadoIncidencia
                        );

        incidencia.setObservadoOperativo(
                !incidenciaCerrada
        );

        incidencia.setPermiteNuevaCargaTrabajador(
                false
        );

        incidencia.setRequiereIntervencionInterna(
                !incidenciaCerrada
        );

        incidencia.setNumeroReintentosInternos(
                rs.getInt(
                        "NUMERO_REINTENTOS"
                )
        );

        incidencia.setFechaHoraRegistro(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_REGISTRO"
                        )
                )
        );

        incidencia.setFechaHoraUltimoReintento(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_ULTIMO_REINTENTO"
                        )
                )
        );

        incidencia.setFechaHoraCierre(
                convertirTimestamp(
                        rs.getTimestamp(
                                "FECHA_CIERRE"
                        )
                )
        );

        incidencia.setUsuarioResponsable(
                rs.getString(
                        "USUARIO_RESPONSABLE"
                )
        );

        incidencia.setResultadoCierre(
                rs.getString(
                        "RESULTADO_CIERRE"
                )
        );

        /*
         * IP, dispositivo y el detalle histórico de
         * cada reintento vivirán en
         * HISTORIAL_PROC_REGMASVIDA.
         *
         * Por ahora no se inventan columnas nuevas
         * en INCIDENCIA_OPERATIVA.
         */
        incidencia.setIpOrigen(
                null
        );

        incidencia.setDatosSesionDispositivo(
                null
        );

        return incidencia;
    }

    @Override
    public void registrarEventoIncidencia(
            String idIncidenciaOperativa,
            String codigoEventoHistorial,
            String tipoEvento,
            String estadoAnterior,
            String estadoNuevo,
            String resultadoEvento,
            String descripcionEvento,
            String usuarioAutenticado,
            String ipOrigen,
            String datosSesionDispositivo,
            String idDocumentoSellado,
            String idDocumentoPublicado,
            java.time.LocalDateTime fechaEvento
    ) {

        String sql = """
        INSERT INTO HISTORIAL_PROC_REGMASVIDA (
            COD_EVENTO_HISTORIAL,
            ID_SECOMASVIDA,
            ID_DOC_SUSTENTO,
            ID_INCIDENCIA_OPERATIVA,

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
            ID_DOCUMENTO_SELLADO,
            ID_DOCUMENTO_PUBLICADO,

            FECHA_EVENTO
        )

        SELECT
            ?,
            I.ID_SECOMASVIDA,
            I.ID_DOC_SUSTENTO,
            I.ID_INCIDENCIA_OPERATIVA,

            ?,
            ?,
            ?,
            ?,
            ?,

            ?,
            ?,
            ?,
            ?,

            D.TIPO_DOCUMENTO_LOGICO,
            ?,
            ?,

            ?

        FROM INCIDENCIA_OPERATIVA I

        LEFT JOIN DOCUMENTOS_SUSTENTO D
            ON D.ID_DOC_SUSTENTO =
               I.ID_DOC_SUSTENTO

        WHERE
            I.COD_INCIDENCIA_OPERATIVA = ?
        """;

        int filasInsertadas =
                jdbcTemplate.update(
                        sql,

                        limitar(
                                codigoEventoHistorial,
                                80
                        ),

                        limitar(
                                tipoEvento,
                                80
                        ),

                        limitar(
                                estadoAnterior,
                                80
                        ),

                        limitar(
                                estadoNuevo,
                                80
                        ),

                        limitar(
                                resultadoEvento,
                                80
                        ),

                        limitar(
                                descripcionEvento,
                                2000
                        ),

                        limitar(
                                campoVacio(usuarioAutenticado)
                                        ? EstadoProcesoConstants.USUARIO_SISTEMA
                                        : usuarioAutenticado,
                                100
                        ),

                        limitar(
                                ipOrigen,
                                64
                        ),

                        limitar(
                                datosSesionDispositivo,
                                1000
                        ),

                        EstadoProcesoConstants
                                .CANAL_SOMOS_ESSALUD,

                        limitar(
                                idDocumentoSellado,
                                80
                        ),

                        limitar(
                                idDocumentoPublicado,
                                80
                        ),

                        convertirTimestamp(
                                fechaEvento
                        ),

                        idIncidenciaOperativa
                );

        if (filasInsertadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró la incidencia operativa "
                            + "para registrar su evento histórico."
            );
        }
    }

    @Override
    public List<ReintentoIncidenciaOperativa>
    listarReintentosPorIncidencia(
            String idIncidenciaOperativa
    ) {

        String sql = """
        SELECT
            H.COD_EVENTO_HISTORIAL,
            H.FECHA_EVENTO,
            H.DESCRIPCION_EVENTO,
            H.RESULTADO_EVENTO,

            H.USUARIO_AUTENTICADO,
            H.IP_ORIGEN,
            H.DATOS_SESION_DISPOSITIVO,

            H.ID_DOCUMENTO_SELLADO,
            H.ID_DOCUMENTO_PUBLICADO

        FROM HISTORIAL_PROC_REGMASVIDA H

        INNER JOIN INCIDENCIA_OPERATIVA I
            ON I.ID_INCIDENCIA_OPERATIVA =
               H.ID_INCIDENCIA_OPERATIVA

        WHERE
            I.COD_INCIDENCIA_OPERATIVA = ?

            AND H.TIPO_EVENTO =
                'REINTENTO_INCIDENCIA_OPERATIVA'

        ORDER BY
            H.FECHA_EVENTO ASC
        """;

        return jdbcTemplate.query(
                sql,

                (rs, rowNum) -> {

                    ReintentoIncidenciaOperativa reintento =
                            new ReintentoIncidenciaOperativa();

                    String codigoEvento =
                            rs.getString(
                                    "COD_EVENTO_HISTORIAL"
                            );

                    if (
                            codigoEvento != null
                                    && codigoEvento.startsWith(
                                    "HIST-"
                            )
                    ) {
                        reintento.setIdReintento(
                                codigoEvento.substring(5)
                        );
                    } else {
                        reintento.setIdReintento(
                                codigoEvento
                        );
                    }

                    Timestamp fecha =
                            rs.getTimestamp(
                                    "FECHA_EVENTO"
                            );

                    if (fecha != null) {
                        reintento.setFechaHoraReintento(
                                fecha.toLocalDateTime()
                        );
                    }

                    reintento.setDescripcionReintento(
                            rs.getString(
                                    "DESCRIPCION_EVENTO"
                            )
                    );

                    reintento.setResultadoReintento(
                            rs.getString(
                                    "RESULTADO_EVENTO"
                            )
                    );

                    reintento.setUsuarioResponsable(
                            rs.getString(
                                    "USUARIO_AUTENTICADO"
                            )
                    );

                    reintento.setIpOrigen(
                            rs.getString(
                                    "IP_ORIGEN"
                            )
                    );

                    reintento.setDatosSesionDispositivo(
                            rs.getString(
                                    "DATOS_SESION_DISPOSITIVO"
                            )
                    );

                    reintento.setIdDocumentoSellado(
                            rs.getString(
                                    "ID_DOCUMENTO_SELLADO"
                            )
                    );

                    reintento.setIdDocumentoPublicado(
                            rs.getString(
                                    "ID_DOCUMENTO_PUBLICADO"
                            )
                    );

                    return reintento;
                },

                idIncidenciaOperativa
        );
    }

    private void cargarReintentos(
            List<IncidenciaOperativa> incidencias
    ) {

        for (
                IncidenciaOperativa incidencia
                : incidencias
        ) {
            incidencia.setReintentos(
                    listarReintentosPorIncidencia(
                            incidencia
                                    .getIdIncidenciaOperativa()
                    )
            );
        }
    }

    @Override
    public boolean existenIncidenciasAbiertas(
            String registroInternoProceso
    ) {

        String sql = """
    SELECT COUNT(*)

    FROM INCIDENCIA_OPERATIVA I

    INNER JOIN TEMP_SECOMASVIDA T
        ON T.ID_SECOMASVIDA =
           I.ID_SECOMASVIDA

    WHERE
        T.REGISTRO_INTERNO_PROCESO = ?

        AND UPPER(
            I.ESTADO_INCIDENCIA
        ) <> UPPER(?)
    """;

        Integer cantidad =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                        registroInternoProceso.trim(),
                        EstadoProcesoConstants
                                .SUBSANADO_OPERATIVAMENTE
                );

        return cantidad != null
                && cantidad > 0;
    }

    @Override
    public void depurarTrazabilidadCerrada(
            String registroInternoProceso
    ) {

        if (
                existenIncidenciasAbiertas(
                        registroInternoProceso
                )
        ) {
            throw new IllegalStateException(
                    "El trámite todavía posee incidencias operativas abiertas y no puede depurarse."
            );
        }

        /*
         * HISTORIAL depende tanto del trámite
         * como de documentos e incidencias.
         * Debe eliminarse primero.
         */
        String sqlHistorial = """
    DELETE FROM HISTORIAL_PROC_REGMASVIDA H

    WHERE H.ID_SECOMASVIDA = (
        SELECT T.ID_SECOMASVIDA
        FROM TEMP_SECOMASVIDA T
        WHERE T.REGISTRO_INTERNO_PROCESO = ?
    )
    """;

        jdbcTemplate.update(
                sqlHistorial,
                registroInternoProceso.trim()
        );

        /*
         * Una vez eliminado el historial,
         * las incidencias ya cerradas pueden
         * descartarse completamente.
         */
        String sqlIncidencias = """
    DELETE FROM INCIDENCIA_OPERATIVA I

    WHERE I.ID_SECOMASVIDA = (
        SELECT T.ID_SECOMASVIDA
        FROM TEMP_SECOMASVIDA T
        WHERE T.REGISTRO_INTERNO_PROCESO = ?
    )
    """;

        jdbcTemplate.update(
                sqlIncidencias,
                registroInternoProceso.trim()
        );
    }

    private void validarIncidencia(
            IncidenciaOperativa incidencia
    ) {

        if (incidencia == null) {
            throw new IllegalArgumentException(
                    "La incidencia operativa es obligatoria."
            );
        }

        if (
                campoVacio(
                        incidencia
                                .getIdIncidenciaOperativa()
                )
        ) {
            throw new IllegalArgumentException(
                    "El código de incidencia operativa es obligatorio."
            );
        }

        if (
                campoVacio(
                        incidencia
                                .getRegistroInternoProceso()
                )
        ) {
            throw new IllegalArgumentException(
                    "El registro interno del proceso es obligatorio."
            );
        }
    }

    private String normalizarTipoDocumentoNullable(
            String tipoDocumento
    ) {

        if (campoVacio(tipoDocumento)) {
            return null;
        }

        return tipoDocumento
                .trim()
                .toUpperCase();
    }

    private String limitar(
            String valor,
            int maximo
    ) {

        if (valor == null) {
            return null;
        }

        String texto =
                valor.trim();

        if (texto.length() <= maximo) {
            return texto;
        }

        return texto.substring(
                0,
                maximo
        );
    }

    private Timestamp convertirTimestamp(
            java.time.LocalDateTime fecha
    ) {

        return fecha == null
                ? null
                : Timestamp.valueOf(
                fecha
        );
    }

    private java.time.LocalDateTime convertirTimestamp(
            Timestamp timestamp
    ) {

        return timestamp == null
                ? null
                : timestamp.toLocalDateTime();
    }

    private boolean campoVacio(
            String valor
    ) {
        return valor == null
                || valor.trim().isEmpty();
    }
}