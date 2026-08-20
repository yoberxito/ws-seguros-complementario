package essalud.gob.pe.wsseguroscomplementario.proceso.repository;

import essalud.gob.pe.wsseguroscomplementario.proceso.model.BeneficiarioVida;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcBeneficiarioVidaRepository
        implements BeneficiarioVidaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcBeneficiarioVidaRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void eliminarPorIdSecomasvida(
            Long idSecomasvida
    ) {
        String sql = """
                DELETE FROM BENEFICIARIO
                WHERE ID_SECOMASVIDA = ?
                """;

        jdbcTemplate.update(
                sql,
                idSecomasvida
        );
    }

    @Override
    public void guardarTodos(
            List<BeneficiarioVida> beneficiarios
    ) {
        if (
                beneficiarios == null
                        || beneficiarios.isEmpty()
        ) {
            return;
        }

        String sql = """
                INSERT INTO BENEFICIARIO (
                    ID_SECOMASVIDA,
                    ORDEN_BENEFICIARIO,

                    COD_EDOCUMENT_BENEFICIARIO,
                    DESC_OTRO_DOCUMENTO_BENEFICIARIO,
                    NUM_DOCUMENT_BENEFICIARIO,

                    APE_PATERNO,
                    APE_MATERNO,
                    PRIMER_NOMBRE,
                    SEGUNDO_NOMBRE,

                    PORCENTAJE_BENEFICIO,

                    FECHA_REGISTRO,
                    FECHA_ACTUALIZACION
                )
                VALUES (
                    ?, ?,
                    ?, ?, ?,
                    ?, ?, ?, ?,
                    ?,
                    SYSTIMESTAMP,
                    SYSTIMESTAMP
                )
                """;

        List<Object[]> parametros =
                new ArrayList<>();

        for (
                BeneficiarioVida beneficiario :
                beneficiarios
        ) {
            parametros.add(
                    new Object[] {
                            beneficiario.getIdSecomasvida(),
                            beneficiario.getOrdenBeneficiario(),

                            beneficiario.getCodigoDocumentoBeneficiario(),
                            beneficiario.getDescripcionOtroDocumentoBeneficiario(),
                            beneficiario.getNumeroDocumentoBeneficiario(),

                            beneficiario.getApellidoPaterno(),
                            beneficiario.getApellidoMaterno(),
                            beneficiario.getPrimerNombre(),
                            beneficiario.getSegundoNombre(),

                            beneficiario.getPorcentajeBeneficio()
                    }
            );
        }

        jdbcTemplate.batchUpdate(
                sql,
                parametros
        );
    }
    @Override
    public List<BeneficiarioVida> listarPorIdSecomasvida(
            Long idSecomasvida
    ) {
        String sql = """
            SELECT
                ID_BENEFICIARIO,
                ID_SECOMASVIDA,
                ORDEN_BENEFICIARIO,

                COD_EDOCUMENT_BENEFICIARIO,
                DESC_OTRO_DOCUMENTO_BENEFICIARIO,
                NUM_DOCUMENT_BENEFICIARIO,

                APE_PATERNO,
                APE_MATERNO,
                PRIMER_NOMBRE,
                SEGUNDO_NOMBRE,

                PORCENTAJE_BENEFICIO

            FROM BENEFICIARIO

            WHERE ID_SECOMASVIDA = ?

            ORDER BY ORDEN_BENEFICIARIO
            """;

        return jdbcTemplate.query(
                sql,
                this::mapearBeneficiarioVida,
                idSecomasvida
        );
    }

    private BeneficiarioVida mapearBeneficiarioVida(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        BeneficiarioVida beneficiario =
                new BeneficiarioVida();

        beneficiario.setIdBeneficiario(
                rs.getLong(
                        "ID_BENEFICIARIO"
                )
        );

        beneficiario.setIdSecomasvida(
                rs.getLong(
                        "ID_SECOMASVIDA"
                )
        );

        beneficiario.setOrdenBeneficiario(
                rs.getInt(
                        "ORDEN_BENEFICIARIO"
                )
        );

        beneficiario.setCodigoDocumentoBeneficiario(
                rs.getString(
                        "COD_EDOCUMENT_BENEFICIARIO"
                )
        );

        beneficiario
                .setDescripcionOtroDocumentoBeneficiario(
                        rs.getString(
                                "DESC_OTRO_DOCUMENTO_BENEFICIARIO"
                        )
                );

        beneficiario.setNumeroDocumentoBeneficiario(
                rs.getString(
                        "NUM_DOCUMENT_BENEFICIARIO"
                )
        );

        beneficiario.setApellidoPaterno(
                rs.getString(
                        "APE_PATERNO"
                )
        );

        beneficiario.setApellidoMaterno(
                rs.getString(
                        "APE_MATERNO"
                )
        );

        beneficiario.setPrimerNombre(
                rs.getString(
                        "PRIMER_NOMBRE"
                )
        );

        beneficiario.setSegundoNombre(
                rs.getString(
                        "SEGUNDO_NOMBRE"
                )
        );

        beneficiario.setPorcentajeBeneficio(
                rs.getBigDecimal(
                        "PORCENTAJE_BENEFICIO"
                )
        );

        return beneficiario;
    }
}