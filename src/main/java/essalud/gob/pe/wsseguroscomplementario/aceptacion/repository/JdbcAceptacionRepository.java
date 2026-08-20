package essalud.gob.pe.wsseguroscomplementario.aceptacion.repository;

import essalud.gob.pe.wsseguroscomplementario.aceptacion.model.AceptacionLegal;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAceptacionRepository
        implements AceptacionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAceptacionRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AceptacionLegal guardar(
            AceptacionLegal aceptacionLegal
    ) {

        /*
         * Una sola aceptación legal por trámite.
         *
         * Si el ID_SECOMASVIDA ya tiene una aceptación,
         * el MERGE no inserta una segunda fila.
         *
         * Esto hace que un doble clic o reintento
         * sea idempotente y preserve la fecha
         * original de aceptación.
         */
        String sql = """
                MERGE INTO ACEPTACION_LEGAL A
                USING (
                    SELECT ID_SECOMASVIDA
                    FROM TEMP_SECOMASVIDA
                    WHERE REGISTRO_INTERNO_PROCESO = ?
                ) T
                ON (
                    A.ID_SECOMASVIDA =
                    T.ID_SECOMASVIDA
                )

                WHEN NOT MATCHED THEN
                    INSERT (
                        COD_ACEPTACION,
                        ID_SECOMASVIDA,

                        TIPO_DOCUMENTO_TRABAJADOR,
                        NUM_DOCUMENTO_TRABAJADOR,
                        NOMBRES_APELLIDOS_TRABAJADOR,

                        ACEPTA_DECLARACION_JURADA,
                        FECHA_ACEPT_DECLARACION,
                        VERSION_TEXTO_DECLARACION,

                        ACEPTA_TRATAMIENTO_DATOS,
                        FECHA_ACEPT_TRATAMIENTO,
                        VERSION_TEXTO_TRATAMIENTO,

                        REFERENCIA_POLITICA_PRIVACIDAD,

                        IP_ORIGEN,
                        CANAL_ACCESO,
                        DATOS_SESION_DISPOSITIVO,

                        FECHA_REGISTRO,
                        FECHA_ACTUALIZACION
                    )
                    VALUES (
                        ?,
                        T.ID_SECOMASVIDA,

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
                        ?,
                        ?,

                        SYSTIMESTAMP,
                        SYSTIMESTAMP
                    )
                """;

        jdbcTemplate.update(
                sql,

                aceptacionLegal
                        .getRegistroInternoProceso(),

                aceptacionLegal
                        .getIdAceptacion(),

                aceptacionLegal
                        .getTipoDocumentoTrabajador(),

                aceptacionLegal
                        .getNumeroDocumentoTrabajador(),

                aceptacionLegal
                        .getNombresApellidosTrabajador(),

                convertirBooleano(
                        aceptacionLegal
                                .isAceptaDeclaracionJurada()
                ),

                convertirTimestamp(
                        aceptacionLegal
                                .getFechaHoraAceptacionDeclaracionJurada()
                ),

                aceptacionLegal
                        .getVersionTextoDeclaracionJurada(),

                convertirBooleano(
                        aceptacionLegal
                                .isAceptaTratamientoDatosPersonales()
                ),

                convertirTimestamp(
                        aceptacionLegal
                                .getFechaHoraAceptacionTratamientoDatosPersonales()
                ),

                aceptacionLegal
                        .getVersionTextoTratamientoDatos(),

                aceptacionLegal
                        .getReferenciaPoliticaPrivacidad(),

                aceptacionLegal
                        .getIpOrigen(),

                aceptacionLegal
                        .getCanalAcceso(),

                aceptacionLegal
                        .getDatosSesionDispositivo()
        );

        /*
         * Se recupera la fila real.
         *
         * Si ya existía por un intento anterior,
         * se devuelve esa misma aceptación.
         */
        return buscarPorRegistroInternoProceso(
                aceptacionLegal
                        .getRegistroInternoProceso()
        ).orElseThrow(
                () -> new IllegalStateException(
                        "La aceptación legal no pudo recuperarse desde Oracle."
                )
        );
    }

    @Override
    public Optional<AceptacionLegal> buscarPorId(
            String idAceptacion
    ) {

        String sql = """
                SELECT
                    A.*,
                    T.REGISTRO_INTERNO_PROCESO
                FROM ACEPTACION_LEGAL A
                INNER JOIN TEMP_SECOMASVIDA T
                    ON T.ID_SECOMASVIDA =
                       A.ID_SECOMASVIDA
                WHERE A.COD_ACEPTACION = ?
                """;

        try {
            AceptacionLegal resultado =
                    jdbcTemplate.queryForObject(
                            sql,
                            this::mapearAceptacion,
                            idAceptacion
                    );

            return Optional.ofNullable(
                    resultado
            );

        } catch (
                EmptyResultDataAccessException e
        ) {
            return Optional.empty();
        }
    }

    @Override
    public List<AceptacionLegal> listar() {

        String sql = """
                SELECT
                    A.*,
                    T.REGISTRO_INTERNO_PROCESO
                FROM ACEPTACION_LEGAL A
                INNER JOIN TEMP_SECOMASVIDA T
                    ON T.ID_SECOMASVIDA =
                       A.ID_SECOMASVIDA
                ORDER BY A.FECHA_REGISTRO DESC
                """;

        return jdbcTemplate.query(
                sql,
                this::mapearAceptacion
        );
    }

    @Override
    public Optional<AceptacionLegal>
    buscarPorRegistroInternoProceso(
            String registroInternoProceso
    ) {

        String sql = """
                SELECT
                    A.*,
                    T.REGISTRO_INTERNO_PROCESO
                FROM ACEPTACION_LEGAL A
                INNER JOIN TEMP_SECOMASVIDA T
                    ON T.ID_SECOMASVIDA =
                       A.ID_SECOMASVIDA
                WHERE T.REGISTRO_INTERNO_PROCESO = ?
                """;

        try {
            AceptacionLegal resultado =
                    jdbcTemplate.queryForObject(
                            sql,
                            this::mapearAceptacion,
                            registroInternoProceso
                    );

            return Optional.ofNullable(
                    resultado
            );

        } catch (
                EmptyResultDataAccessException e
        ) {
            return Optional.empty();
        }
    }

    private AceptacionLegal mapearAceptacion(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        AceptacionLegal aceptacion =
                new AceptacionLegal();

        /*
         * COD_ACEPTACION es el identificador
         * funcional expuesto por la API.
         *
         * ID_ACEPTACION queda como PK técnica
         * interna de Oracle.
         */
        aceptacion.setIdAceptacion(
                rs.getString(
                        "COD_ACEPTACION"
                )
        );

        aceptacion.setRegistroInternoProceso(
                rs.getString(
                        "REGISTRO_INTERNO_PROCESO"
                )
        );

        aceptacion.setTipoDocumentoTrabajador(
                rs.getString(
                        "TIPO_DOCUMENTO_TRABAJADOR"
                )
        );

        aceptacion.setNumeroDocumentoTrabajador(
                rs.getString(
                        "NUM_DOCUMENTO_TRABAJADOR"
                )
        );

        aceptacion.setNombresApellidosTrabajador(
                rs.getString(
                        "NOMBRES_APELLIDOS_TRABAJADOR"
                )
        );

        aceptacion.setAceptaDeclaracionJurada(
                convertirBooleano(
                        rs.getString(
                                "ACEPTA_DECLARACION_JURADA"
                        )
                )
        );

        aceptacion
                .setFechaHoraAceptacionDeclaracionJurada(
                        convertirTimestamp(
                                rs.getTimestamp(
                                        "FECHA_ACEPT_DECLARACION"
                                )
                        )
                );

        aceptacion.setVersionTextoDeclaracionJurada(
                rs.getString(
                        "VERSION_TEXTO_DECLARACION"
                )
        );

        aceptacion.setAceptaTratamientoDatosPersonales(
                convertirBooleano(
                        rs.getString(
                                "ACEPTA_TRATAMIENTO_DATOS"
                        )
                )
        );

        aceptacion
                .setFechaHoraAceptacionTratamientoDatosPersonales(
                        convertirTimestamp(
                                rs.getTimestamp(
                                        "FECHA_ACEPT_TRATAMIENTO"
                                )
                        )
                );

        aceptacion.setVersionTextoTratamientoDatos(
                rs.getString(
                        "VERSION_TEXTO_TRATAMIENTO"
                )
        );

        aceptacion.setReferenciaPoliticaPrivacidad(
                rs.getString(
                        "REFERENCIA_POLITICA_PRIVACIDAD"
                )
        );

        aceptacion.setIpOrigen(
                rs.getString(
                        "IP_ORIGEN"
                )
        );

        aceptacion.setCanalAcceso(
                rs.getString(
                        "CANAL_ACCESO"
                )
        );

        aceptacion.setDatosSesionDispositivo(
                rs.getString(
                        "DATOS_SESION_DISPOSITIVO"
                )
        );

        return aceptacion;
    }

    private String convertirBooleano(
            boolean valor
    ) {
        return valor
                ? "S"
                : "N";
    }

    private boolean convertirBooleano(
            String valor
    ) {
        return "S".equalsIgnoreCase(
                valor
        );
    }

    private Timestamp convertirTimestamp(
            java.time.LocalDateTime fecha
    ) {
        return fecha == null
                ? null
                : Timestamp.valueOf(fecha);
    }

    private java.time.LocalDateTime convertirTimestamp(
            Timestamp fecha
    ) {
        return fecha == null
                ? null
                : fecha.toLocalDateTime();
    }
}