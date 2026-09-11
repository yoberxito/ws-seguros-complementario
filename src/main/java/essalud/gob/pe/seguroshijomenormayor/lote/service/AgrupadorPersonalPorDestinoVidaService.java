package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DestinoPersonalVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.GrupoPersonalVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ReporteLoteVidaItem;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*
 * Agrupa trabajadores PERSONAL por destino institucional.
 *
 * Este servicio es deliberadamente independiente de:
 *
 * - SOMOS;
 * - Google Drive;
 * - Oracle;
 * - correo;
 * - nombres concretos de Redes.
 *
 * Su unica dependencia funcional es:
 *
 * DNI -> ResolvedorDestinoPersonalVida -> DestinoPersonalVida
 */
@Service
public class AgrupadorPersonalPorDestinoVidaService {

    public List<GrupoPersonalVida> agrupar(
            List<ReporteLoteVidaItem> items,
            ResolvedorDestinoPersonalVida resolvedor
    ) {

        if (items == null) {
            throw new IllegalArgumentException(
                    "La coleccion de trabajadores PERSONAL es obligatoria."
            );
        }

        if (resolvedor == null) {
            throw new IllegalArgumentException(
                    "El resolvedor de destino PERSONAL es obligatorio."
            );
        }

        if (items.isEmpty()) {
            return List.of();
        }

        /*
         * TreeMap garantiza orden deterministico por
         * codigo de destino.
         */
        Map<String, GrupoMutable> grupos =
                new TreeMap<>();

        Set<String> trabajadoresProcesados =
                new HashSet<>();

        for (ReporteLoteVidaItem item : items) {

            if (item == null) {
                throw new IllegalStateException(
                        "Existe un trabajador PERSONAL nulo."
                );
            }

            String dni =
                    requerir(
                            item.getNumeroDocumentoTitular(),
                            "Existe un trabajador PERSONAL sin DNI."
                    );

            if (!trabajadoresProcesados.add(dni)) {
                throw new IllegalStateException(
                        "El trabajador PERSONAL aparece mas de una vez. DNI: "
                                + dni
                                + "."
                );
            }

            DestinoPersonalVida destino =
                    resolvedor.resolverPorDni(
                            dni
                    );

            if (destino == null) {
                throw new IllegalStateException(
                        "No fue posible resolver el destino PERSONAL del DNI "
                                + dni
                                + "."
                );
            }

            String codigoDestino =
                    destino.codigoDestino();

            GrupoMutable grupo =
                    grupos.get(
                            codigoDestino
                    );

            if (grupo == null) {

                grupo =
                        new GrupoMutable(
                                destino
                        );

                grupos.put(
                        codigoDestino,
                        grupo
                );

            } else {

                String nombreExistente =
                        grupo.destino
                                .nombreDestino();

                String nombreResuelto =
                        destino.nombreDestino();

                if (!nombreExistente.equals(nombreResuelto)) {
                    throw new IllegalStateException(
                            "El codigo de destino PERSONAL "
                                    + codigoDestino
                                    + " fue resuelto con nombres distintos."
                    );
                }
            }

            grupo.items.add(
                    item
            );
        }

        List<GrupoPersonalVida> resultado =
                new ArrayList<>();

        for (GrupoMutable grupo : grupos.values()) {

            grupo.items.sort(
                    Comparator.comparing(
                            item ->
                                    requerir(
                                            item.getNumeroDocumentoTitular(),
                                            "Existe un trabajador PERSONAL sin DNI."
                                    )
                    )
            );

            GrupoPersonalVida grupoFinal =
                    new GrupoPersonalVida(
                            grupo.destino,
                            grupo.items
                    );

            resultado.add(
                    grupoFinal
            );
        }

        return List.copyOf(
                resultado
        );
    }

    private String requerir(
            String valor,
            String mensaje
    ) {

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException(
                    mensaje
            );
        }

        return valor.trim();
    }

    private static final class GrupoMutable {

        private final DestinoPersonalVida destino;

        private final List<ReporteLoteVidaItem> items =
                new ArrayList<>();

        private GrupoMutable(
                DestinoPersonalVida destino
        ) {

            this.destino =
                    destino;
        }
    }
}