package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DestinoPersonalVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.GrupoDocumentosPersonalDrive;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*
 * Construye el plan logico de distribucion de PDFs 247
 * hacia sus destinos PERSONAL.
 *
 * IMPORTANTE:
 *
 * Este servicio NO ejecuta operaciones sobre Google Drive.
 *
 * Solamente determina:
 *
 * DNI -> destino -> grupo de documentos.
 *
 * El movimiento fisico de los archivos se implementa
 * en una etapa posterior.
 */
@Service
public class PlanificadorDistribucionPersonalDriveService {

    public List<GrupoDocumentosPersonalDrive> planificar(
            List<DocumentoDriveLoteVida> documentos,
            ResolvedorDestinoPersonalVida resolvedor
    ) {

        if (documentos == null) {
            throw new IllegalArgumentException(
                    "La coleccion de documentos PERSONAL es obligatoria."
            );
        }

        if (resolvedor == null) {
            throw new IllegalArgumentException(
                    "El resolvedor de destino PERSONAL es obligatorio."
            );
        }

        if (documentos.isEmpty()) {
            return List.of();
        }

        Map<String, GrupoMutable> grupos =
                new TreeMap<>();

        Set<String> trabajadoresProcesados =
                new HashSet<>();

        for (DocumentoDriveLoteVida documento : documentos) {

            if (documento == null) {
                throw new IllegalStateException(
                        "Existe un documento PERSONAL nulo."
                );
            }

            String dni =
                    requerir(
                            documento.getNumeroDocumentoTitular(),
                            "Existe un documento PERSONAL sin DNI."
                    );

            /*
             * El extractor ya protege esta regla.
             * Se valida nuevamente porque este servicio
             * tambien puede utilizarse aisladamente.
             */
            if (!trabajadoresProcesados.add(dni)) {

                throw new IllegalStateException(
                        "Existe mas de un documento PERSONAL para el DNI "
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

            String codigo =
                    destino.codigoDestino();

            GrupoMutable grupo =
                    grupos.get(
                            codigo
                    );

            if (grupo == null) {

                grupo =
                        new GrupoMutable(
                                destino
                        );

                grupos.put(
                        codigo,
                        grupo
                );

            } else {

                String nombreActual =
                        grupo.destino
                                .nombreDestino();

                String nombreNuevo =
                        destino.nombreDestino();

                if (!nombreActual.equals(nombreNuevo)) {

                    throw new IllegalStateException(
                            "El codigo de destino PERSONAL "
                                    + codigo
                                    + " fue resuelto con nombres distintos."
                    );
                }
            }

            grupo.documentos.add(
                    documento
            );
        }

        List<GrupoDocumentosPersonalDrive> resultado =
                new ArrayList<>();

        for (GrupoMutable grupo : grupos.values()) {

            grupo.documentos.sort(
                    Comparator.comparing(
                            documento ->
                                    requerir(
                                            documento.getNumeroDocumentoTitular(),
                                            "Existe un documento PERSONAL sin DNI."
                                    )
                    )
            );

            resultado.add(
                    new GrupoDocumentosPersonalDrive(
                            grupo.destino,
                            grupo.documentos
                    )
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

        if (
                valor == null
                        || valor.trim().isEmpty()
        ) {

            throw new IllegalStateException(
                    mensaje
            );
        }

        return valor.trim();
    }

    private static final class GrupoMutable {

        private final DestinoPersonalVida destino;

        private final List<DocumentoDriveLoteVida> documentos =
                new ArrayList<>();

        private GrupoMutable(
                DestinoPersonalVida destino
        ) {

            this.destino =
                    destino;
        }
    }
}