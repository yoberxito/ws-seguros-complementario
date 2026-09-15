package essalud.gob.pe.seguroshijomenormayor.entrega.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("masVidaJdbcHistorialEntregaLoteRepository")
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

        String sql = """
                INSERT INTO HISTORIAL_ENTREGA_LOTE (
                    ID_HISTORIAL_ENTREGA,
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
                    SEQ_HIST_ENTREGA_LOTE.NEXTVAL,
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

    @Override
    public boolean existeEvento(
            String tokenHash,
            String tipoEvento
    ) {

        String sql = """
                SELECT COUNT(1)

                FROM HISTORIAL_ENTREGA_LOTE H

                INNER JOIN ENTREGA_LOTE E
                    ON E.ID_ENTREGA = H.ID_ENTREGA

                WHERE E.TOKEN_HASH = ?
                  AND H.TIPO_EVENTO = ?
                  AND H.RESULTADO_EVENTO = 'OK'
                """;

        Integer cantidad =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                        tokenHash,
                        tipoEvento
                );

        return cantidad != null
                && cantidad > 0;
    }
}