package essalud.gob.pe.seguroshijomenormayor.entrega.service;

import essalud.gob.pe.seguroshijomenormayor.entrega.model.LoteDistribucion;
import essalud.gob.pe.seguroshijomenormayor.entrega.repository.LoteDistribucionRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;

@Service("masVidaLoteDistribucionService")
public class LoteDistribucionService {

    private static final String
            ESTADO_PREPARADO =
            "PREPARADO";

    private static final String
            DESTINO_MAPFRE =
            "MAPFRE";

    private static final String
            DESTINO_PERSONAL =
            "PERSONAL";

    private final LoteDistribucionRepository
            loteDistribucionRepository;

    public LoteDistribucionService(
            LoteDistribucionRepository
                    loteDistribucionRepository
    ) {

        this.loteDistribucionRepository =
                loteDistribucionRepository;
    }

    /*
     * Metodo existente.
     *
     * Se conserva para no romper MAPFRE ni los consumidores
     * actuales del servicio.
     */
    public LoteDistribucion obtenerOCrear(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        validarDestinatario(
                destinatario
        );

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        String codigo =
                generarCodigoLote(
                        destinatario,
                        fechaInicio,
                        fechaFin
                );

        return obtenerOCrearPorCodigo(
                codigo,
                fechaInicio,
                fechaFin
        );
    }

    /*
     * PERSONAL MULTIDESTINO.
     *
     * Cada destino institucional obtiene una identidad
     * de lote distinta aun cuando comparte el mismo periodo
     * con otras Redes/unidades.
     *
     * Ejemplo:
     *
     * VIDA-PERSONAL-RED-LIMA-2026-09-04-2026-09-18
     */
    public LoteDistribucion obtenerOCrearPersonal(
            String codigoDestino,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        String codigo =
                generarCodigoLotePersonal(
                        codigoDestino,
                        fechaInicio,
                        fechaFin
                );

        return obtenerOCrearPorCodigo(
                codigo,
                fechaInicio,
                fechaFin
        );
    }

    public boolean marcarPublicado(
            Long idLote
    ) {

        return loteDistribucionRepository
                .marcarPublicado(
                        idLote
                );
    }

    /*
     * Codigo original.
     *
     * IMPORTANTE:
     * MAPFRE mantiene exactamente el contrato anterior.
     */
    public String generarCodigoLote(
            String destinatario,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        validarDestinatario(
                destinatario
        );

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        return "VIDA-"
                + destinatario
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        )
                + "-"
                + fechaInicio
                + "-"
                + fechaFin;
    }

    /*
     * Codigo deterministico de lote PERSONAL por destino.
     *
     * No utiliza correo como identidad.
     * No utiliza nombre visible de la Red como identidad.
     *
     * Utiliza solamente codigoDestino, que posteriormente
     * provendra del servicio institucional correspondiente.
     */
    public String generarCodigoLotePersonal(
            String codigoDestino,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        String destinoNormalizado =
                normalizarCodigoDestinoPersonal(
                        codigoDestino
                );

        return "VIDA-"
                + DESTINO_PERSONAL
                + "-"
                + destinoNormalizado
                + "-"
                + fechaInicio
                + "-"
                + fechaFin;
    }

    private LoteDistribucion obtenerOCrearPorCodigo(
            String codigo,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        return loteDistribucionRepository
                .buscarPorCodigo(
                        codigo
                )
                .orElseGet(
                        () ->
                                crearLote(
                                        codigo,
                                        fechaInicio,
                                        fechaFin
                                )
                );
    }

    private LoteDistribucion crearLote(
            String codigo,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        LoteDistribucion lote =
                new LoteDistribucion();

        lote.setCodLote(
                codigo
        );

        lote.setFechaInicioPeriodo(
                fechaInicio
        );

        lote.setFechaFinPeriodo(
                fechaFin
        );

        lote.setEstadoLote(
                ESTADO_PREPARADO
        );

        return loteDistribucionRepository
                .crear(
                        lote
                );
    }

    private String normalizarCodigoDestinoPersonal(
            String codigoDestino
    ) {

        if (
                codigoDestino == null
                        || codigoDestino.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El codigo del destino PERSONAL es obligatorio."
            );
        }

        return codigoDestino
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private void validarPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        if (
                fechaInicio == null
                        || fechaFin == null
        ) {

            throw new IllegalArgumentException(
                    "El periodo del lote es obligatorio."
            );
        }

        if (
                fechaInicio.isAfter(
                        fechaFin
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha inicial del lote no puede ser posterior a la final."
            );
        }
    }

    private void validarDestinatario(
            String destinatario
    ) {

        if (
                destinatario == null
                        || destinatario.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El destinatario es obligatorio."
            );
        }

        String destino =
                destinatario
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        boolean esMapfre =
                DESTINO_MAPFRE.equals(
                        destino
                );

        boolean esPersonal =
                DESTINO_PERSONAL.equals(
                        destino
                );

        if (!esMapfre && !esPersonal) {

            throw new IllegalArgumentException(
                    "El destinatario debe ser MAPFRE o PERSONAL."
            );
        }
    }
}