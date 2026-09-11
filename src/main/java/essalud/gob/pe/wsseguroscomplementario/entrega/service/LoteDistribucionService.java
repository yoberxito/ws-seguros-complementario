package essalud.gob.pe.wsseguroscomplementario.entrega.service;

import essalud.gob.pe.wsseguroscomplementario.entrega.model.LoteDistribucion;
import essalud.gob.pe.wsseguroscomplementario.entrega.repository.LoteDistribucionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LoteDistribucionService {

    private static final String
            ESTADO_PREPARADO =
            "PREPARADO";

    private final LoteDistribucionRepository
            loteDistribucionRepository;

    public LoteDistribucionService(
            LoteDistribucionRepository
                    loteDistribucionRepository
    ) {
        this.loteDistribucionRepository =
                loteDistribucionRepository;
    }

    public LoteDistribucion obtenerOCrear(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        validarDestinatario(destinatario);

        if (
                fechaInicio == null
                        || fechaFin == null
        ) {
            throw new IllegalArgumentException(
                    "El período del lote es obligatorio."
            );
        }

        String codigo =
                generarCodigoLote(
                        destinatario,
                        fechaInicio,
                        fechaFin
                );

        return loteDistribucionRepository
                .buscarPorCodigo(codigo)
                .orElseGet(
                        () -> crearLote(
                                codigo,
                                fechaInicio,
                                fechaFin
                        )
                );
    }

    public boolean marcarPublicado(
            Long idLote
    ) {

        return loteDistribucionRepository
                .marcarPublicado(idLote);
    }

    private LoteDistribucion crearLote(
            String codigo,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        LoteDistribucion lote =
                new LoteDistribucion();

        lote.setCodLote(codigo);
        lote.setFechaInicioPeriodo(fechaInicio);
        lote.setFechaFinPeriodo(fechaFin);
        lote.setEstadoLote(ESTADO_PREPARADO);

        return loteDistribucionRepository
                .crear(lote);
    }

    public String generarCodigoLote(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        validarDestinatario(destinatario);

        if (
                fechaInicio == null
                        || fechaFin == null
        ) {
            throw new IllegalArgumentException(
                    "El período es obligatorio."
            );
        }

        return "VIDA-"
                + destinatario.trim().toUpperCase()
                + "-"
                + fechaInicio
                + "-"
                + fechaFin;
    }

    private void validarDestinatario(
            String destinatario
    ) {

        if (destinatario == null) {
            throw new IllegalArgumentException(
                    "El destinatario es obligatorio."
            );
        }

        String destino =
                destinatario.trim().toUpperCase();

        if (
                !"MAPFRE".equals(destino)
                        && !"PERSONAL".equals(destino)
        ) {
            throw new IllegalArgumentException(
                    "El destinatario debe ser MAPFRE o PERSONAL."
            );
        }
    }
}
