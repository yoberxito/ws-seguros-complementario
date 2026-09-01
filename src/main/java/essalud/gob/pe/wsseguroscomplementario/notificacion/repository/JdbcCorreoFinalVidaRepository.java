package essalud.gob.pe.wsseguroscomplementario.notificacion.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class JdbcCorreoFinalVidaRepository
        implements CorreoFinalVidaRepository {

    private static final String
            ESTADO_ENVIANDO =
            "ENVIANDO";

    private static final String
            ESTADO_ENVIADO =
            "ENVIADO";

    private static final String
            ESTADO_ERROR =
            "ERROR_ENVIO";

    private final JdbcTemplate jdbcTemplate;

    public JdbcCorreoFinalVidaRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                jdbcTemplate;
    }

    @Override
    public boolean reservarEnvio(
            String registroInternoProceso,
            String cicloDocumental,
            String destinatario,
            boolean enviaFormulario,
            int cantidadAdjuntos,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        String registro =
                registroInternoProceso
                        .trim();

        String ciclo =
                cicloDocumental
                        .trim()
                        .toUpperCase();

        /*
         * Si existe un intento anterior
         * que terminó en ERROR_ENVIO,
         * se permite adquirir nuevamente
         * el envío para reintentar.
         */
        String sqlReintento = """
                UPDATE NOTIFICACION_TRABAJADOR N
                SET
                    N.DESTINATARIO = ?,
                    N.ENVIA_FORMULARIO = ?,
                    N.CANTIDAD_ADJUNTOS = ?,

                    N.ESTADO_NOTIFICACION = ?,

                    N.CODIGO_RESULTADO = NULL,
                    N.MENSAJE_RESULTADO = NULL,

                    N.NUMERO_INTENTOS =
                        N.NUMERO_INTENTOS + 1,

                    N.FECHA_ULTIMO_INTENTO =
                        SYSTIMESTAMP,

                    N.USUARIO_RESPONSABLE = ?,
                    N.IP_ORIGEN = ?,
                    N.DATOS_SESION_DISPOSITIVO = ?,

                    N.FECHA_ACTUALIZACION =
                        SYSTIMESTAMP

                WHERE N.ID_SECOMASVIDA = (
                    SELECT T.ID_SECOMASVIDA
                    FROM TEMP_SECOMASVIDA T
                    WHERE T.REGISTRO_INTERNO_PROCESO = ?
                )

                AND N.CICLO_DOCUMENTAL = ?

                AND N.ESTADO_NOTIFICACION = ?
                """;

        int filasReintento =
                jdbcTemplate.update(
                        sqlReintento,

                        destinatario.trim(),
                        enviaFormulario ? "S" : "N",
                        cantidadAdjuntos,

                        ESTADO_ENVIANDO,

                        usuarioResponsable,
                        ipOrigen,
                        datosSesionDispositivo,

                        registro,
                        ciclo,

                        ESTADO_ERROR
                );

        if (filasReintento > 0) {
            return true;
        }

        /*
         * Si nunca se intentó este ciclo,
         * se crea la evidencia inicial.
         *
         * UK_NOTIF_TRAMITE_CICLO evita que
         * dos cierres concurrentes reserven
         * el mismo correo.
         */
        String sqlInsert = """
                INSERT INTO NOTIFICACION_TRABAJADOR (
                    COD_NOTIFICACION,
                    ID_SECOMASVIDA,
                    CICLO_DOCUMENTAL,
                    DESTINATARIO,
                    ENVIA_FORMULARIO,
                    CANTIDAD_ADJUNTOS,
                    ESTADO_NOTIFICACION,
                    NUMERO_INTENTOS,
                    FECHA_ULTIMO_INTENTO,
                    USUARIO_RESPONSABLE,
                    IP_ORIGEN,
                    DATOS_SESION_DISPOSITIVO
                )

                SELECT
                    ?,
                    T.ID_SECOMASVIDA,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    1,
                    SYSTIMESTAMP,
                    ?,
                    ?,
                    ?

                FROM TEMP_SECOMASVIDA T

                WHERE
                    T.REGISTRO_INTERNO_PROCESO = ?
                """;

        try {

            int filasInsertadas =
                    jdbcTemplate.update(
                            sqlInsert,

                            generarCodigoNotificacion(),

                            ciclo,

                            destinatario.trim(),

                            enviaFormulario
                                    ? "S"
                                    : "N",

                            cantidadAdjuntos,

                            ESTADO_ENVIANDO,

                            usuarioResponsable,
                            ipOrigen,
                            datosSesionDispositivo,

                            registro
                    );

            if (filasInsertadas == 0) {

                throw new IllegalArgumentException(
                        "No se encontro el proceso +Vida para registrar el envio del correo final."
                );
            }

            return true;

        } catch (
                DuplicateKeyException e
        ) {

            /*
             * Si ya existe la combinación
             *
             * ID_SECOMASVIDA +
             * CICLO_DOCUMENTAL
             *
             * no se ejecuta un segundo envío.
             */
            return false;
        }
    }

    @Override
    public boolean estaEnviado(
            String registroInternoProceso,
            String cicloDocumental
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM NOTIFICACION_TRABAJADOR N

                INNER JOIN TEMP_SECOMASVIDA T
                    ON T.ID_SECOMASVIDA =
                       N.ID_SECOMASVIDA

                WHERE
                    T.REGISTRO_INTERNO_PROCESO = ?

                    AND N.CICLO_DOCUMENTAL = ?

                    AND N.ESTADO_NOTIFICACION = ?
                """;

        Integer cantidad =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,

                        registroInternoProceso
                                .trim(),

                        cicloDocumental
                                .trim()
                                .toUpperCase(),

                        ESTADO_ENVIADO
                );

        return cantidad != null
                && cantidad > 0;
    }

    @Override
    public boolean estaEnError(
            String registroInternoProceso,
            String cicloDocumental
    ) {

        String sql = """
            SELECT COUNT(*)

            FROM NOTIFICACION_TRABAJADOR N

            INNER JOIN TEMP_SECOMASVIDA T
                ON T.ID_SECOMASVIDA =
                   N.ID_SECOMASVIDA

            WHERE
                T.REGISTRO_INTERNO_PROCESO = ?

                AND N.CICLO_DOCUMENTAL = ?

                AND N.ESTADO_NOTIFICACION = ?
            """;

        Integer cantidad =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,

                        registroInternoProceso
                                .trim(),

                        cicloDocumental
                                .trim()
                                .toUpperCase(),

                        ESTADO_ERROR
                );

        return cantidad != null
                && cantidad > 0;
    }

    @Override
    public void marcarEnviado(
            String registroInternoProceso,
            String cicloDocumental,
            String codigoResultado,
            String mensajeResultado
    ) {

        String sql = """
                UPDATE NOTIFICACION_TRABAJADOR N

                SET
                    N.ESTADO_NOTIFICACION = ?,

                    N.CODIGO_RESULTADO = ?,
                    N.MENSAJE_RESULTADO = ?,

                    N.FECHA_ENVIO =
                        SYSTIMESTAMP,

                    N.FECHA_ACTUALIZACION =
                        SYSTIMESTAMP

                WHERE N.ID_SECOMASVIDA = (
                    SELECT T.ID_SECOMASVIDA
                    FROM TEMP_SECOMASVIDA T
                    WHERE T.REGISTRO_INTERNO_PROCESO = ?
                )

                AND N.CICLO_DOCUMENTAL = ?

                AND N.ESTADO_NOTIFICACION = ?
                """;

        int filas =
                jdbcTemplate.update(
                        sql,

                        ESTADO_ENVIADO,

                        limitar(
                                codigoResultado,
                                20
                        ),

                        limitar(
                                mensajeResultado,
                                500
                        ),

                        registroInternoProceso
                                .trim(),

                        cicloDocumental
                                .trim()
                                .toUpperCase(),

                        ESTADO_ENVIANDO
                );

        if (filas == 0) {

            throw new IllegalStateException(
                    "No fue posible confirmar el envio del correo final en Oracle."
            );
        }
    }

    @Override
    public void marcarError(
            String registroInternoProceso,
            String cicloDocumental,
            String mensajeResultado
    ) {

        String sql = """
                UPDATE NOTIFICACION_TRABAJADOR N

                SET
                    N.ESTADO_NOTIFICACION = ?,

                    N.CODIGO_RESULTADO = NULL,

                    N.MENSAJE_RESULTADO = ?,

                    N.FECHA_ACTUALIZACION =
                        SYSTIMESTAMP

                WHERE N.ID_SECOMASVIDA = (
                    SELECT T.ID_SECOMASVIDA
                    FROM TEMP_SECOMASVIDA T
                    WHERE T.REGISTRO_INTERNO_PROCESO = ?
                )

                AND N.CICLO_DOCUMENTAL = ?

                AND N.ESTADO_NOTIFICACION = ?
                """;

        jdbcTemplate.update(
                sql,

                ESTADO_ERROR,

                limitar(
                        mensajeResultado,
                        500
                ),

                registroInternoProceso
                        .trim(),

                cicloDocumental
                        .trim()
                        .toUpperCase(),

                ESTADO_ENVIANDO
        );
    }

    private String generarCodigoNotificacion() {

        return "NOTIF-"
                + UUID.randomUUID();
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
}