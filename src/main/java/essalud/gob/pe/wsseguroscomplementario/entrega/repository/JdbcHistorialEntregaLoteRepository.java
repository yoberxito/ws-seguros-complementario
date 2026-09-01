package essalud.gob.pe.wsseguroscomplementario.entrega.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcHistorialEntregaLoteRepository
        implements HistorialEntregaLoteRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcHistorialEntregaLoteRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                jdbcTemplate;
    }

    @Override
    public void registrarEvento(
            String tokenHash,
            String codigoEvento,
            String tipoEvento,
            String resultadoEvento,
            String descripcionEvento,
            String ipOrigen,
            String datosSesionDispositivo
    ) {

        /*
         * Resolvemos ID_ENTREGA internamente
         * usando TOKEN_HASH.
         *
         * El identificador interno no necesita
         * salir hacia controller ni frontend.
         */
        String sql = """
                INSERT INTO HISTORIAL_ENTREGA_LOTE (
                    COD_EVENTO_HISTORIAL,
                    ID_ENTREGA,
                    TIPO_EVENTO,
                    RESULTADO_EVENTO,
                    DESCRIPCION_EVENTO,
                    IP_ORIGEN,
                    DATOS_SESION_DISPOSITIVO,
                    FECHA_EVENTO
                )

                SELECT
                    ?,
                    E.ID_ENTREGA,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    SYSTIMESTAMP

                FROM ENTREGA_LOTE E

                WHERE E.TOKEN_HASH = ?
                """;

        int filasInsertadas =
                jdbcTemplate.update(
                        sql,
                        codigoEvento,
                        tipoEvento,
                        resultadoEvento,
                        descripcionEvento,
                        ipOrigen,
                        datosSesionDispositivo,
                        tokenHash
                );

        if (filasInsertadas != 1) {

            throw new IllegalStateException(
                    "No fue posible registrar el historial de la entrega."
            );
        }
    }
}