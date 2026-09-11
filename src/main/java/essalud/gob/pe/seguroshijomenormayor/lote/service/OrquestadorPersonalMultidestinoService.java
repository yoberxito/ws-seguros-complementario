package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.entrega.model.CierreLotePreparado;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.CierreLoteDistribucionService;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.GrupoDocumentosPersonalDrive;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoDistribucionPersonalDrive;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoPipelinePersonalDestino;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoReportePersonalDestino;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Pipeline PERSONAL multidestino.
 *
 * Orquesta exclusivamente componentes ya construidos:
 *
 * 3A -> planificacion
 * 3B -> distribucion Drive
 * 3C -> Excel por destino
 * 2C -> lote + entrega + token
 *
 * No envia correo.
 * No implementa SOMOS.
 * No resuelve datos institucionales por cuenta propia.
 */
@Service
public class OrquestadorPersonalMultidestinoService {

    private final PlanificadorDistribucionPersonalDriveService
            planificador;

    private final DistribuidorPersonalDriveService
            distribuidor;

    private final GeneradorReportesPersonalPorDestinoService
            generadorReportes;

    private final CierreLoteDistribucionService
            cierreLoteDistribucionService;

    public OrquestadorPersonalMultidestinoService(
            PlanificadorDistribucionPersonalDriveService planificador,
            DistribuidorPersonalDriveService distribuidor,
            GeneradorReportesPersonalPorDestinoService generadorReportes,
            CierreLoteDistribucionService cierreLoteDistribucionService
    ) {

        this.planificador =
                planificador;

        this.distribuidor =
                distribuidor;

        this.generadorReportes =
                generadorReportes;

        this.cierreLoteDistribucionService =
                cierreLoteDistribucionService;
    }

    public List<ResultadoPipelinePersonalDestino> procesar(
            List<DocumentoDriveLoteVida> documentos,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            ResolvedorDestinoPersonalVida resolvedorDestino,
            ResolvedorCorreoDestinoPersonalVida resolvedorCorreo
    ) throws IOException {

        if (documentos == null) {
            throw new IllegalArgumentException(
                    "Los documentos PERSONAL son obligatorios."
            );
        }

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        if (resolvedorDestino == null) {
            throw new IllegalArgumentException(
                    "El resolvedor de destino PERSONAL es obligatorio."
            );
        }

        if (resolvedorCorreo == null) {
            throw new IllegalArgumentException(
                    "El resolvedor de correo PERSONAL es obligatorio."
            );
        }

        List<GrupoDocumentosPersonalDrive> grupos =
                planificador.planificar(
                        documentos,
                        resolvedorDestino
                );

        /*
         * Ningun trabajador:
         * no creamos carpetas, reportes, lotes ni entregas.
         */
        if (grupos.isEmpty()) {
            return List.of();
        }
        /*
         * Antes de ejecutar cualquier efecto externo
         * (Drive / Excel) o persistencia Oracle,
         * resolvemos y validamos los correos de TODOS
         * los destinos PERSONAL.
         *
         * De esta forma, si un destino carece de correo,
         * el pipeline se detiene antes de procesar
         * parcialmente otros destinos.
         */
        Map<String, String> correosPorDestino =
                resolverCorreosAntesDeEfectos(
                        grupos,
                        resolvedorCorreo
                );

        List<ResultadoDistribucionPersonalDrive> distribuciones =
                distribuidor.distribuir(
                        grupos,
                        fechaInicio,
                        fechaFin
                );

        List<ResultadoReportePersonalDestino> reportes =
                generadorReportes.generar(
                        distribuciones,
                        fechaInicio,
                        fechaFin
                );

        Map<String, ResultadoDistribucionPersonalDrive>
                distribucionPorDestino =
                indexarDistribuciones(
                        distribuciones
                );

        if (
                reportes.size()
                        != distribucionPorDestino.size()
        ) {

            throw new IllegalStateException(
                    "La cantidad de reportes PERSONAL no coincide "
                            + "con la cantidad de destinos distribuidos."
            );
        }

        Set<String> reportesProcesados =
                new HashSet<>();

        List<ResultadoPipelinePersonalDestino> resultados =
                new ArrayList<>();

        for (
                ResultadoReportePersonalDestino reporte
                : reportes
        ) {

            if (reporte == null) {
                throw new IllegalStateException(
                        "Existe un reporte PERSONAL nulo."
                );
            }

            String codigoDestino =
                    reporte
                            .getDestino()
                            .codigoDestino();

            if (!reportesProcesados.add(codigoDestino)) {

                throw new IllegalStateException(
                        "Existe mas de un reporte para el destino PERSONAL "
                                + codigoDestino
                                + "."
                );
            }

            ResultadoDistribucionPersonalDrive distribucion =
                    distribucionPorDestino.get(
                            codigoDestino
                    );

            if (distribucion == null) {

                throw new IllegalStateException(
                        "El reporte PERSONAL del destino "
                                + codigoDestino
                                + " no tiene una distribucion Drive asociada."
                );
            }

            validarCorrespondencia(
                    distribucion,
                    reporte
            );

            String correo =
                    correosPorDestino.get(
                            codigoDestino
                    );

            if (correo == null) {
                throw new IllegalStateException(
                        "No se encontro el correo PERSONAL "
                                + "prevalidado para el destino "
                                + codigoDestino
                                + "."
                );
            }

            CierreLotePreparado cierre =
                    cierreLoteDistribucionService
                            .prepararLotePublicadoPersonal(
                                    codigoDestino,
                                    fechaInicio,
                                    fechaFin,
                                    correo.trim(),
                                    reporte.getCantidadDocumentos(),
                                    reporte.getUrlCarpetaPeriodo()
                            );

            resultados.add(
                    new ResultadoPipelinePersonalDestino(
                            reporte.getDestino(),
                            reporte,
                            cierre
                    )
            );
        }

        return List.copyOf(
                resultados
        );
    }

    private Map<String, String>
    resolverCorreosAntesDeEfectos(
            List<GrupoDocumentosPersonalDrive> grupos,
            ResolvedorCorreoDestinoPersonalVida resolvedorCorreo
    ) {

        Map<String, String> correos =
                new HashMap<>();

        for (
                GrupoDocumentosPersonalDrive grupo
                : grupos
        ) {

            if (
                    grupo == null
                            || grupo.getDestino() == null
            ) {

                throw new IllegalStateException(
                        "Existe un grupo PERSONAL sin destino."
                );
            }

            String codigoDestino =
                    grupo
                            .getDestino()
                            .codigoDestino();

            if (
                    codigoDestino == null
                            || codigoDestino.trim().isEmpty()
            ) {

                throw new IllegalStateException(
                        "Existe un destino PERSONAL sin codigo."
                );
            }

            String correo =
                    resolvedorCorreo.resolverCorreo(
                            grupo.getDestino()
                    );

            if (
                    correo == null
                            || correo.trim().isEmpty()
            ) {

                throw new IllegalStateException(
                        "No fue posible resolver el correo responsable "
                                + "del destino PERSONAL "
                                + codigoDestino
                                + "."
                );
            }

            String anterior =
                    correos.put(
                            codigoDestino,
                            correo.trim()
                    );

            if (anterior != null) {

                throw new IllegalStateException(
                        "Existe mas de un grupo para el destino PERSONAL "
                                + codigoDestino
                                + "."
                );
            }
        }

        return Map.copyOf(correos);
    }

    private Map<String, ResultadoDistribucionPersonalDrive>
    indexarDistribuciones(
            List<ResultadoDistribucionPersonalDrive> distribuciones
    ) {

        if (distribuciones == null) {
            throw new IllegalStateException(
                    "La etapa Drive PERSONAL no devolvio distribuciones."
            );
        }

        Map<String, ResultadoDistribucionPersonalDrive> indice =
                new HashMap<>();

        for (
                ResultadoDistribucionPersonalDrive distribucion
                : distribuciones
        ) {

            if (distribucion == null) {
                throw new IllegalStateException(
                        "Existe una distribucion Drive PERSONAL nula."
                );
            }

            String codigo =
                    distribucion
                            .getDestino()
                            .codigoDestino();

            ResultadoDistribucionPersonalDrive anterior =
                    indice.put(
                            codigo,
                            distribucion
                    );

            if (anterior != null) {

                throw new IllegalStateException(
                        "Existe mas de una distribucion Drive "
                                + "para el destino PERSONAL "
                                + codigo
                                + "."
                );
            }
        }

        return indice;
    }

    private void validarCorrespondencia(
            ResultadoDistribucionPersonalDrive distribucion,
            ResultadoReportePersonalDestino reporte
    ) {

        if (
                distribucion.getCantidadDocumentos()
                        != reporte.getCantidadDocumentos()
        ) {

            throw new IllegalStateException(
                    "La cantidad de documentos no coincide entre "
                            + "Drive y Excel para el destino "
                            + reporte
                                    .getDestino()
                                    .codigoDestino()
                            + "."
            );
        }

        if (
                !distribucion
                        .getCarpetaPeriodoId()
                        .equals(
                                reporte.getCarpetaPeriodoId()
                        )
        ) {

            throw new IllegalStateException(
                    "El Excel PERSONAL no corresponde a la misma "
                            + "carpeta Drive del destino "
                            + reporte
                                    .getDestino()
                                    .codigoDestino()
                            + "."
            );
        }

        if (
                !distribucion
                        .getUrlCarpetaPeriodo()
                        .equals(
                                reporte.getUrlCarpetaPeriodo()
                        )
        ) {

            throw new IllegalStateException(
                    "La URL de acceso del reporte PERSONAL "
                            + "no coincide con su distribucion Drive."
            );
        }
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
                    "El periodo PERSONAL es obligatorio."
            );
        }

        if (fechaInicio.isAfter(fechaFin)) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la final."
            );
        }
    }
}