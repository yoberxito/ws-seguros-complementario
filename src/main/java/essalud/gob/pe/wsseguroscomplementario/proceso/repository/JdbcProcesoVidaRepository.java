package essalud.gob.pe.wsseguroscomplementario.proceso.repository;

import essalud.gob.pe.wsseguroscomplementario.proceso.model.ProcesoVida;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class JdbcProcesoVidaRepository implements ProcesoVidaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcProcesoVidaRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ProcesoVida crear(
            ProcesoVida procesoVida
    ) {
        String sql = """
                INSERT INTO TEMP_SECOMASVIDA (
                    REGISTRO_INTERNO_PROCESO,
                    ID_ESTADO,
                    ID_ESTADO_NAVEGACION,
                    ESTADO_OPERATIVO,
                    TIPO_FLUJO,

                    COD_EDOCUMENT_TITULAR,
                    DESC_OTRO_DOCUMENTO_TITULAR,
                    NUM_DOCUMENT_TITULAR,

                    APE_PATERNO_TITULAR,
                    APE_MATERNO_TITULAR,
                    PRIMER_NOMBRE_TITULAR,
                    SEGUNDO_NOMBRE_TITULAR,

                    CORREO,
                    NRO_TELEFONO,
                    NOTIFICACIONES_CORREO,

                    TIPO_ASEGURADO,
                    COD_PLANILLA,
                    DEC_LEGISLATIVO,
                    CONVENIO_CGBVP,

                    RUC_EMPLEADOR,
                    RAZON_SOCIAL_ENTIDAD,

                    COD_EDOCUMENT_CONYUGE,
                    DESC_OTRO_DOCUMENTO_CONYUGE,
                    NUM_DOCUMENT_CONYUGE,

                    APE_PATERNO_CONYUGE,
                    APE_MATERNO_CONYUGE,
                    PRIMER_NOMBRE_CONYUGE,
                    SEGUNDO_NOMBRE_CONYUGE,

                    TIPO_RELACION
                )
                VALUES (
                    ?,
                    (
                        SELECT ID_ESTADO
                        FROM ESTADO_PROC_REGMASVIDA
                        WHERE COD_ESTADO_PROCESO = ?
                    ),
                    (
                        SELECT ID_ESTADO
                        FROM ESTADO_PROC_REGMASVIDA
                        WHERE COD_ESTADO_PROCESO = ?
                    ),
                    ?,
                    ?,

                    ?, ?, ?,
                    ?, ?, ?, ?,

                    ?, ?, ?,

                    ?, ?, ?, ?,

                    ?, ?,

                    ?, ?, ?,
                    ?, ?, ?, ?,

                    ?
                )
                """;

        jdbcTemplate.update(
                sql,

                procesoVida.getRegistroInternoProceso(),
                procesoVida.getCodigoEstadoProceso(),
                procesoVida.getCodigoEstadoProceso(),
                procesoVida.getEstadoOperativo(),
                procesoVida.getTipoFlujo(),

                procesoVida.getCodigoDocumentoTitular(),
                procesoVida.getDescripcionOtroDocumentoTitular(),
                procesoVida.getNumeroDocumentoTitular(),

                procesoVida.getApellidoPaternoTitular(),
                procesoVida.getApellidoMaternoTitular(),
                procesoVida.getPrimerNombreTitular(),
                procesoVida.getSegundoNombreTitular(),

                procesoVida.getCorreo(),
                procesoVida.getNumeroTelefono(),
                procesoVida.getNotificacionesCorreo(),

                procesoVida.getTipoAsegurado(),
                procesoVida.getCodigoPlanilla(),
                procesoVida.getDecretoLegislativo(),
                procesoVida.getConvenioCgbvp(),

                procesoVida.getRucEmpleador(),
                procesoVida.getRazonSocialEntidad(),

                procesoVida.getCodigoDocumentoConyuge(),
                procesoVida.getDescripcionOtroDocumentoConyuge(),
                procesoVida.getNumeroDocumentoConyuge(),

                procesoVida.getApellidoPaternoConyuge(),
                procesoVida.getApellidoMaternoConyuge(),
                procesoVida.getPrimerNombreConyuge(),
                procesoVida.getSegundoNombreConyuge(),

                procesoVida.getTipoRelacion()
        );

        return buscarPorRegistroInternoProceso(
                procesoVida.getRegistroInternoProceso()
        ).orElseThrow(
                () -> new IllegalStateException(
                        "El proceso fue registrado, pero no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public Optional<ProcesoVida> buscarPorRegistroInternoProceso(
            String registroInternoProceso
    ) {
        String sql = """
        SELECT
            T.*,

            E_MAX.COD_ESTADO_PROCESO
                AS COD_ESTADO_PROCESO,

            E_MAX.RUTA_FRONTEND
                AS RUTA_FRONTEND,

            E_NAV.COD_ESTADO_PROCESO
                AS COD_ESTADO_NAVEGACION,

            E_NAV.RUTA_FRONTEND
                AS RUTA_FRONTEND_NAVEGACION

        FROM TEMP_SECOMASVIDA T

        INNER JOIN ESTADO_PROC_REGMASVIDA E_MAX
            ON E_MAX.ID_ESTADO =
               T.ID_ESTADO

        INNER JOIN ESTADO_PROC_REGMASVIDA E_NAV
            ON E_NAV.ID_ESTADO =
               T.ID_ESTADO_NAVEGACION

        WHERE T.REGISTRO_INTERNO_PROCESO = ?
        """;

        try {
            ProcesoVida procesoVida = jdbcTemplate.queryForObject(
                    sql,
                    this::mapearProcesoVida,
                    registroInternoProceso
            );

            return Optional.ofNullable(procesoVida);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ProcesoVida> buscarUltimoPorTrabajador(
            String tipoDocumento,
            String numeroDocumento
    ) {
        String sql = """
        SELECT
            T.*,

            E_MAX.COD_ESTADO_PROCESO
                AS COD_ESTADO_PROCESO,

            E_MAX.RUTA_FRONTEND
                AS RUTA_FRONTEND,

            E_NAV.COD_ESTADO_PROCESO
                AS COD_ESTADO_NAVEGACION,

            E_NAV.RUTA_FRONTEND
                AS RUTA_FRONTEND_NAVEGACION

        FROM TEMP_SECOMASVIDA T

        INNER JOIN ESTADO_PROC_REGMASVIDA E_MAX
            ON E_MAX.ID_ESTADO =
               T.ID_ESTADO

        INNER JOIN ESTADO_PROC_REGMASVIDA E_NAV
            ON E_NAV.ID_ESTADO =
               T.ID_ESTADO_NAVEGACION

        WHERE T.COD_EDOCUMENT_TITULAR = ?
          AND T.NUM_DOCUMENT_TITULAR = ?

        ORDER BY T.FECHA_ACTUALIZACION DESC

        FETCH FIRST 1 ROW ONLY
        """;

        try {
            ProcesoVida procesoVida = jdbcTemplate.queryForObject(
                    sql,
                    this::mapearProcesoVida,
                    tipoDocumento,
                    numeroDocumento
            );

            return Optional.ofNullable(procesoVida);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ProcesoVida actualizarTitularYEstado(
            String registroInternoProceso,
            ProcesoVida procesoVida,
            String codigoSiguienteEstado
    ) {
        String sql = """
        UPDATE TEMP_SECOMASVIDA
        SET
            COD_EDOCUMENT_TITULAR = ?,
            DESC_OTRO_DOCUMENTO_TITULAR = ?,
            NUM_DOCUMENT_TITULAR = ?,
            APE_PATERNO_TITULAR = ?,
            APE_MATERNO_TITULAR = ?,
            PRIMER_NOMBRE_TITULAR = ?,
            SEGUNDO_NOMBRE_TITULAR = ?,
            CORREO = ?,
            NRO_TELEFONO = ?,
            TIPO_ASEGURADO = ?,

            ID_ESTADO = CASE
                WHEN (
                    SELECT E_ACTUAL.ORDEN_PROCESO
                    FROM ESTADO_PROC_REGMASVIDA E_ACTUAL
                    WHERE E_ACTUAL.ID_ESTADO =
                        TEMP_SECOMASVIDA.ID_ESTADO
                ) < (
                    SELECT E_SIGUIENTE.ORDEN_PROCESO
                    FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                    WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                )
                THEN (
                    SELECT E_SIGUIENTE.ID_ESTADO
                    FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                    WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                )
                ELSE ID_ESTADO
            END,
            
            ID_ESTADO_NAVEGACION = (
                SELECT E_SIGUIENTE.ID_ESTADO
                FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
            ),

            FECHA_ACTUALIZACION = SYSTIMESTAMP

        WHERE REGISTRO_INTERNO_PROCESO = ?
        """;
        int filasActualizadas = jdbcTemplate.update(
                sql,
                procesoVida.getCodigoDocumentoTitular(),
                procesoVida.getDescripcionOtroDocumentoTitular(),
                procesoVida.getNumeroDocumentoTitular(),
                procesoVida.getApellidoPaternoTitular(),
                procesoVida.getApellidoMaternoTitular(),
                procesoVida.getPrimerNombreTitular(),
                procesoVida.getSegundoNombreTitular(),
                procesoVida.getCorreo(),
                procesoVida.getNumeroTelefono(),
                procesoVida.getTipoAsegurado(),

                codigoSiguienteEstado,
                codigoSiguienteEstado,
                codigoSiguienteEstado,

                registroInternoProceso
        );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró el proceso +Vida indicado."
            );
        }

        return buscarPorRegistroInternoProceso(
                registroInternoProceso
        ).orElseThrow(
                () -> new IllegalStateException(
                        "El proceso fue actualizado, pero no pudo recuperarse desde Oracle."
                )
        );
    }

    private ProcesoVida mapearProcesoVida(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        ProcesoVida procesoVida = new ProcesoVida();

        procesoVida.setIdSecomasvida(
                rs.getLong("ID_SECOMASVIDA")
        );

        procesoVida.setRegistroInternoProceso(
                rs.getString("REGISTRO_INTERNO_PROCESO")
        );

        procesoVida.setIdEstado(
                rs.getLong("ID_ESTADO")
        );

        procesoVida.setCodigoEstadoProceso(
                rs.getString("COD_ESTADO_PROCESO")
        );

        procesoVida.setRutaFrontend(
                rs.getString("RUTA_FRONTEND")
        );

        procesoVida.setIdEstadoNavegacion(
                rs.getLong(
                        "ID_ESTADO_NAVEGACION"
                )
        );

        procesoVida.setCodigoEstadoNavegacion(
                rs.getString(
                        "COD_ESTADO_NAVEGACION"
                )
        );

        procesoVida.setRutaFrontendNavegacion(
                rs.getString(
                        "RUTA_FRONTEND_NAVEGACION"
                )
        );

        procesoVida.setEstadoOperativo(
                rs.getString("ESTADO_OPERATIVO")
        );

        procesoVida.setTipoFlujo(
                rs.getString("TIPO_FLUJO")
        );

        procesoVida.setCodigoDocumentoTitular(
                rs.getString("COD_EDOCUMENT_TITULAR")
        );

        procesoVida.setDescripcionOtroDocumentoTitular(
                rs.getString("DESC_OTRO_DOCUMENTO_TITULAR")
        );

        procesoVida.setNumeroDocumentoTitular(
                rs.getString("NUM_DOCUMENT_TITULAR")
        );

        procesoVida.setApellidoPaternoTitular(
                rs.getString("APE_PATERNO_TITULAR")
        );

        procesoVida.setApellidoMaternoTitular(
                rs.getString("APE_MATERNO_TITULAR")
        );

        procesoVida.setPrimerNombreTitular(
                rs.getString("PRIMER_NOMBRE_TITULAR")
        );

        procesoVida.setSegundoNombreTitular(
                rs.getString("SEGUNDO_NOMBRE_TITULAR")
        );

        procesoVida.setCorreo(
                rs.getString("CORREO")
        );

        procesoVida.setNumeroTelefono(
                rs.getString("NRO_TELEFONO")
        );

        procesoVida.setNotificacionesCorreo(
                rs.getString("NOTIFICACIONES_CORREO")
        );

        procesoVida.setTipoAsegurado(
                rs.getString("TIPO_ASEGURADO")
        );

        procesoVida.setCodigoPlanilla(
                rs.getString("COD_PLANILLA")
        );

        procesoVida.setDecretoLegislativo(
                rs.getString("DEC_LEGISLATIVO")
        );

        procesoVida.setConvenioCgbvp(
                rs.getString("CONVENIO_CGBVP")
        );

        procesoVida.setRucEmpleador(
                rs.getString("RUC_EMPLEADOR")
        );

        procesoVida.setRazonSocialEntidad(
                rs.getString("RAZON_SOCIAL_ENTIDAD")
        );

        procesoVida.setCodigoDocumentoConyuge(
                rs.getString("COD_EDOCUMENT_CONYUGE")
        );

        procesoVida.setDescripcionOtroDocumentoConyuge(
                rs.getString("DESC_OTRO_DOCUMENTO_CONYUGE")
        );

        procesoVida.setNumeroDocumentoConyuge(
                rs.getString("NUM_DOCUMENT_CONYUGE")
        );

        procesoVida.setApellidoPaternoConyuge(
                rs.getString("APE_PATERNO_CONYUGE")
        );

        procesoVida.setApellidoMaternoConyuge(
                rs.getString("APE_MATERNO_CONYUGE")
        );

        procesoVida.setPrimerNombreConyuge(
                rs.getString("PRIMER_NOMBRE_CONYUGE")
        );

        procesoVida.setSegundoNombreConyuge(
                rs.getString("SEGUNDO_NOMBRE_CONYUGE")
        );

        procesoVida.setTipoRelacion(
                rs.getString("TIPO_RELACION")
        );

        procesoVida.setFechaRegistro(
                convertirTimestamp(
                        rs.getTimestamp("FECHA_REGISTRO")
                )
        );

        procesoVida.setFechaActualizacion(
                convertirTimestamp(
                        rs.getTimestamp("FECHA_ACTUALIZACION")
                )
        );

        procesoVida.setBeneficiarioBorradorAbierto(
                rs.getInt(
                        "BENEFICIARIO_BORRADOR_ABIERTO"
                ) == 1
        );

        return procesoVida;
    }

    private java.time.LocalDateTime convertirTimestamp(
            Timestamp timestamp
    ) {
        return timestamp == null
                ? null
                : timestamp.toLocalDateTime();
    }

    @Override
    public ProcesoVida actualizarDatosComplementariosYEstado(
            String registroInternoProceso,
            ProcesoVida procesoVida,
            String codigoSiguienteEstado
    ) {
        String sql = """
        UPDATE TEMP_SECOMASVIDA
        SET
            COD_PLANILLA = ?,
            DEC_LEGISLATIVO = ?,
            CONVENIO_CGBVP = ?,
            RUC_EMPLEADOR = ?,
            RAZON_SOCIAL_ENTIDAD = ?,

            ID_ESTADO = CASE
                WHEN (
                    SELECT E_ACTUAL.ORDEN_PROCESO
                    FROM ESTADO_PROC_REGMASVIDA E_ACTUAL
                    WHERE E_ACTUAL.ID_ESTADO =
                        TEMP_SECOMASVIDA.ID_ESTADO
                ) < (
                    SELECT E_SIGUIENTE.ORDEN_PROCESO
                    FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                    WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                )
                THEN (
                    SELECT E_SIGUIENTE.ID_ESTADO
                    FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                    WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                )
                ELSE ID_ESTADO
            END,

            ID_ESTADO_NAVEGACION = (
                SELECT E_SIGUIENTE.ID_ESTADO
                FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
            ),

            FECHA_ACTUALIZACION = SYSTIMESTAMP

        WHERE REGISTRO_INTERNO_PROCESO = ?
        """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,

                        procesoVida.getCodigoPlanilla(),
                        procesoVida.getDecretoLegislativo(),
                        procesoVida.getConvenioCgbvp(),
                        procesoVida.getRucEmpleador(),
                        procesoVida.getRazonSocialEntidad(),

                        codigoSiguienteEstado,
                        codigoSiguienteEstado,
                        codigoSiguienteEstado,

                        registroInternoProceso
                );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró el proceso +Vida indicado."
            );
        }

        return buscarPorRegistroInternoProceso(
                registroInternoProceso
        ).orElseThrow(
                () -> new IllegalStateException(
                        "Los datos complementarios fueron actualizados, pero el proceso no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public ProcesoVida actualizarConyugeYEstado(
            String registroInternoProceso,
            ProcesoVida procesoVida,
            String codigoSiguienteEstado
    ) {
        String sql = """
        UPDATE TEMP_SECOMASVIDA
        SET
            COD_EDOCUMENT_CONYUGE = ?,
            DESC_OTRO_DOCUMENTO_CONYUGE = ?,
            NUM_DOCUMENT_CONYUGE = ?,

            APE_PATERNO_CONYUGE = ?,
            APE_MATERNO_CONYUGE = ?,
            PRIMER_NOMBRE_CONYUGE = ?,
            SEGUNDO_NOMBRE_CONYUGE = ?,

            TIPO_RELACION = ?,

            ID_ESTADO = CASE
                WHEN (
                    SELECT E_ACTUAL.ORDEN_PROCESO
                    FROM ESTADO_PROC_REGMASVIDA E_ACTUAL
                    WHERE E_ACTUAL.ID_ESTADO =
                        TEMP_SECOMASVIDA.ID_ESTADO
                ) < (
                    SELECT E_SIGUIENTE.ORDEN_PROCESO
                    FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                    WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                )
                THEN (
                    SELECT E_SIGUIENTE.ID_ESTADO
                    FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                    WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                )
                ELSE ID_ESTADO
            END,

            ID_ESTADO_NAVEGACION = (
                SELECT E_SIGUIENTE.ID_ESTADO
                FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
            ),

            FECHA_ACTUALIZACION =
                SYSTIMESTAMP

        WHERE REGISTRO_INTERNO_PROCESO = ?
        """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,

                        procesoVida.getCodigoDocumentoConyuge(),
                        procesoVida.getDescripcionOtroDocumentoConyuge(),
                        procesoVida.getNumeroDocumentoConyuge(),

                        procesoVida.getApellidoPaternoConyuge(),
                        procesoVida.getApellidoMaternoConyuge(),
                        procesoVida.getPrimerNombreConyuge(),
                        procesoVida.getSegundoNombreConyuge(),

                        procesoVida.getTipoRelacion(),

                        codigoSiguienteEstado,
                        codigoSiguienteEstado,
                        codigoSiguienteEstado,

                        registroInternoProceso
                );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró el proceso +Vida indicado."
            );
        }

        return buscarPorRegistroInternoProceso(
                registroInternoProceso
        ).orElseThrow(
                () -> new IllegalStateException(
                        "Los datos del cónyuge fueron actualizados, pero el proceso no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public ProcesoVida actualizarTipoFlujoInicialYEstado(
            String registroInternoProceso,
            String tipoFlujo,
            String codigoSiguienteEstado
    ) {
        String sql = """
            UPDATE TEMP_SECOMASVIDA
            SET
                TIPO_FLUJO =
                    CASE
                        WHEN TIPO_FLUJO IS NULL
                        THEN ?
                        ELSE TIPO_FLUJO
                    END,

                ID_ESTADO =
                    CASE
                        WHEN (
                            SELECT E_ACTUAL.ORDEN_PROCESO
                            FROM ESTADO_PROC_REGMASVIDA E_ACTUAL
                            WHERE E_ACTUAL.ID_ESTADO =
                                TEMP_SECOMASVIDA.ID_ESTADO
                        ) < (
                            SELECT E_SIGUIENTE.ORDEN_PROCESO
                            FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                            WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                        )
                        THEN (
                            SELECT E_SIGUIENTE.ID_ESTADO
                            FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                            WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                        )
                        ELSE ID_ESTADO
                    END,
            
                ID_ESTADO_NAVEGACION = (
                    SELECT E_SIGUIENTE.ID_ESTADO
                    FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                    WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                ),
                
                
                FECHA_ACTUALIZACION =
                    SYSTIMESTAMP

            WHERE REGISTRO_INTERNO_PROCESO = ?
            """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,
                        tipoFlujo,
                        codigoSiguienteEstado,
                        codigoSiguienteEstado,
                        codigoSiguienteEstado,
                        registroInternoProceso
                );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró el proceso +Vida indicado."
            );
        }

        return buscarPorRegistroInternoProceso(
                registroInternoProceso
        ).orElseThrow(
                () -> new IllegalStateException(
                        "El tipo de flujo y el estado fueron actualizados, pero el proceso no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public ProcesoVida actualizarEstado(
            String registroInternoProceso,
            String codigoSiguienteEstado
    ) {

        String sql = """
        UPDATE TEMP_SECOMASVIDA
        SET
            ID_ESTADO = CASE
                WHEN (
                    SELECT E_ACTUAL.ORDEN_PROCESO
                    FROM ESTADO_PROC_REGMASVIDA E_ACTUAL
                    WHERE E_ACTUAL.ID_ESTADO =
                        TEMP_SECOMASVIDA.ID_ESTADO
                ) < (
                    SELECT E_SIGUIENTE.ORDEN_PROCESO
                    FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                    WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                )
                THEN (
                    SELECT E_SIGUIENTE.ID_ESTADO
                    FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                    WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
                )
                ELSE ID_ESTADO
            END,

            ID_ESTADO_NAVEGACION = (
                SELECT E_SIGUIENTE.ID_ESTADO
                FROM ESTADO_PROC_REGMASVIDA E_SIGUIENTE
                WHERE E_SIGUIENTE.COD_ESTADO_PROCESO = ?
            ),

            ESTADO_OPERATIVO = CASE
                WHEN UPPER(?) = 'FINALIZACION'
                     AND UPPER(
                         NVL(
                             TIPO_FLUJO,
                             ''
                         )
                     ) = 'COMPLETO'
                THEN 'REGISTRO_TOTAL'

                ELSE ESTADO_OPERATIVO
            END,

            FECHA_ACTUALIZACION =
                SYSTIMESTAMP

        WHERE REGISTRO_INTERNO_PROCESO = ?
        """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,

                        codigoSiguienteEstado,
                        codigoSiguienteEstado,
                        codigoSiguienteEstado,

                        codigoSiguienteEstado,

                        registroInternoProceso
                );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró el proceso +Vida indicado."
            );
        }

        return buscarPorRegistroInternoProceso(
                registroInternoProceso
        ).orElseThrow(
                () -> new IllegalStateException(
                        "El estado del proceso fue actualizado, pero no pudo recuperarse desde Oracle."
                )
        );
    }
    @Override
    public ProcesoVida actualizarEstadoNavegacion(
            String registroInternoProceso,
            String codigoEstadoNavegacion
    ) {

        String sql = """
        UPDATE TEMP_SECOMASVIDA T

        SET
            ID_ESTADO_NAVEGACION = (
                SELECT E_NAV.ID_ESTADO
                FROM ESTADO_PROC_REGMASVIDA E_NAV
                WHERE E_NAV.COD_ESTADO_PROCESO = ?
            ),

            FECHA_ACTUALIZACION =
                SYSTIMESTAMP

        WHERE T.REGISTRO_INTERNO_PROCESO = ?

          AND EXISTS (
              SELECT 1

              FROM ESTADO_PROC_REGMASVIDA E_NAV

              INNER JOIN ESTADO_PROC_REGMASVIDA E_MAX
                  ON E_MAX.ID_ESTADO =
                     T.ID_ESTADO

              WHERE E_NAV.COD_ESTADO_PROCESO = ?

                AND E_NAV.ORDEN_PROCESO
                    <= E_MAX.ORDEN_PROCESO
          )
        """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,

                        codigoEstadoNavegacion,
                        registroInternoProceso,
                        codigoEstadoNavegacion
                );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No fue posible actualizar la sección actual del trámite."
            );
        }

        return buscarPorRegistroInternoProceso(
                registroInternoProceso
        ).orElseThrow(
                () -> new IllegalStateException(
                        "La navegación fue actualizada, pero el proceso no pudo recuperarse desde Oracle."
                )
        );
    }
    @Override
    public ProcesoVida actualizarTitularBorrador(
            String registroInternoProceso,
            ProcesoVida procesoVida
    ) {

        String sql = """
        UPDATE TEMP_SECOMASVIDA
        SET
            CORREO = ?,
            NRO_TELEFONO = ?,
            FECHA_ACTUALIZACION = SYSTIMESTAMP

        WHERE REGISTRO_INTERNO_PROCESO = ?
        """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,
                        procesoVida.getCorreo(),
                        procesoVida.getNumeroTelefono(),
                        registroInternoProceso
                );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró el proceso +Vida indicado."
            );
        }

        return buscarPorRegistroInternoProceso(
                registroInternoProceso
        ).orElseThrow(
                () -> new IllegalStateException(
                        "El borrador del titular fue actualizado, pero el proceso no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public ProcesoVida actualizarDatosComplementariosBorrador(
            String registroInternoProceso,
            ProcesoVida procesoVida
    ) {

        String sql = """
        UPDATE TEMP_SECOMASVIDA
        SET
            COD_PLANILLA = ?,
            DEC_LEGISLATIVO = ?,
            CONVENIO_CGBVP = ?,
            RUC_EMPLEADOR = ?,
            RAZON_SOCIAL_ENTIDAD = ?,
            FECHA_ACTUALIZACION = SYSTIMESTAMP

        WHERE REGISTRO_INTERNO_PROCESO = ?
        """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,
                        procesoVida.getCodigoPlanilla(),
                        procesoVida.getDecretoLegislativo(),
                        procesoVida.getConvenioCgbvp(),
                        procesoVida.getRucEmpleador(),
                        procesoVida.getRazonSocialEntidad(),
                        registroInternoProceso
                );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró el proceso +Vida indicado."
            );
        }

        return buscarPorRegistroInternoProceso(
                registroInternoProceso
        ).orElseThrow(
                () -> new IllegalStateException(
                        "El borrador de datos complementarios fue actualizado, pero el proceso no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public ProcesoVida actualizarBorradorBeneficiarios(
            String registroInternoProceso,
            boolean beneficiarioBorradorAbierto
    ) {

        String sql = """
        UPDATE TEMP_SECOMASVIDA
        SET
            BENEFICIARIO_BORRADOR_ABIERTO = ?,
            FECHA_ACTUALIZACION = SYSTIMESTAMP

        WHERE REGISTRO_INTERNO_PROCESO = ?
        """;

        int filasActualizadas =
                jdbcTemplate.update(
                        sql,
                        beneficiarioBorradorAbierto
                                ? 1
                                : 0,
                        registroInternoProceso
                );

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró el proceso +Vida indicado."
            );
        }

        return buscarPorRegistroInternoProceso(
                registroInternoProceso
        ).orElseThrow(
                () -> new IllegalStateException(
                        "El borrador de beneficiarios fue actualizado, pero el proceso no pudo recuperarse desde Oracle."
                )
        );
    }

}