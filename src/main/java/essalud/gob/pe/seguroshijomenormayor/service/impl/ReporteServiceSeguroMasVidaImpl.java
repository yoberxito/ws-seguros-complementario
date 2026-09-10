package essalud.gob.pe.seguroshijomenormayor.service.impl;

import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.CierreLotePreparado;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.CierreLoteDistribucionService;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoReporteLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.service.ReporteQuincenalLoteVidaService;
import essalud.gob.pe.seguroshijomenormayor.service.ReporteServiceSeguroMasVida;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReporteServiceSeguroMasVidaImpl
        implements ReporteServiceSeguroMasVida {

    private static final ZoneId ZONA_LIMA =
            ZoneId.of("America/Lima");

    private static final String DESTINO_MAPFRE =
            "MAPFRE";

    private static final String DESTINO_PERSONAL =
            "PERSONAL";

    private final ReporteQuincenalLoteVidaService
            reporteQuincenalLoteVidaService;

    private final CierreLoteDistribucionService
            cierreLoteDistribucionService;

    @Value("${integraciones.lotes-vida.mapfre.correo-destinatario:}")
    private String correoDestinatarioMapfre;


    /*
     * ==========================================================
     * MAPFRE
     * ==========================================================
     */
    @Override
    public void generarReporteMafre() {

        LocalDate fechaActual =
                LocalDate.now(
                        ZONA_LIMA
                );

        Periodo periodo =
                obtenerPeriodoMapfre(
                        fechaActual
                );

        ejecutarReporte(
                DESTINO_MAPFRE,
                periodo
        );
    }


    /*
     * ==========================================================
     * PERSONAL / PLANILLAS ESSALUD
     * ==========================================================
     */
    @Override
    public void generarReportePersonal() {

        LocalDate fechaActual =
                LocalDate.now(
                        ZONA_LIMA
                );

        Periodo periodo =
                obtenerPeriodoPersonal(
                        fechaActual
                );

        ejecutarReporte(
                DESTINO_PERSONAL,
                periodo
        );
    }


    /*
     * ==========================================================
     * EJECUCIÓN DEL PIPELINE
     * ==========================================================
     *
     * Actualmente ejecuta:
     *
     * 1. localizar carpeta Drive del período;
     * 2. listar documentos del lote;
     * 3. identificar titular desde metadata Drive;
     * 4. consultar Oracle +Vida;
     * 5. obtener fecha de afiliación desde la publicación
     *    de AUTORIZACION_DESCUENTO;
     * 6. generar Excel;
     * 7. crear/actualizar reporte en Drive.
     *
     * El cierre/movimiento del lote, persistencia de
     * LOTE_DISTRIBUCION, ENTREGA_LOTE, token y notificación
     * se integran en la siguiente etapa.
     */
    private void ejecutarReporte(
            String destinatario,
            Periodo periodo
    ) {

        String correoMapfre = null;

        if (DESTINO_MAPFRE.equals(destinatario)) {
            correoMapfre =
                    requerirCorreoDestinatarioMapfre();
        }

        log.info(
                "Iniciando reporte quincenal +Vida. "
                        + "destinatario={}, periodo={} a {}",
                destinatario,
                periodo.inicio(),
                periodo.fin()
        );

        try {

            ResultadoReporteLoteVida resultado =
                    reporteQuincenalLoteVidaService
                            .generar(
                                    destinatario,
                                    periodo.inicio(),
                                    periodo.fin()
                            );

            if (DESTINO_MAPFRE.equals(destinatario)) {

                CierreLotePreparado cierreLote =
                        cierreLoteDistribucionService
                                .prepararLotePublicado(
                                        resultado.getDestinatario(),
                                        resultado.getFechaInicio(),
                                        resultado.getFechaFin(),
                                        correoMapfre,
                                        resultado.getCantidadDocumentos(),
                                        resultado.getUrlCarpetaFinal()
                                );

                if (
                        cierreLote == null
                                || cierreLote.getLote() == null
                                || cierreLote.getPreparacionEntrega() == null
                                || cierreLote
                                .getPreparacionEntrega()
                                .getEntrega() == null
                ) {

                    throw new IllegalStateException(
                            "El cierre MAPFRE no devolvio lote y entrega validos."
                    );
                }

                log.info(
                        "Lote MAPFRE preparado. lote={}, entrega={}, tokenGenerado={}",
                        cierreLote.getLote().getIdLote(),
                        cierreLote
                                .getPreparacionEntrega()
                                .getEntrega()
                                .getIdEntrega(),
                        cierreLote.isTokenGenerado()
                );

                /*
                 * El token publico permanece solamente en memoria.
                 * No se registra en logs.
                 * Su consumo por el servicio institucional de correo
                 * se conectara en la etapa correspondiente.
                 */
            }

            log.info(
                    "Reporte quincenal +Vida generado "
                            + "correctamente. "
                            + "destinatario={}, periodo={} a {}, "
                            + "documentos={}, carpetaDrive={}, "
                            + "reporteDrive={}, nombreReporte={}",
                    resultado.getDestinatario(),
                    resultado.getFechaInicio(),
                    resultado.getFechaFin(),
                    resultado.getCantidadDocumentos(),
                    resultado.getCarpetaDriveId(),
                    resultado.getReporteDriveId(),
                    resultado.getNombreReporte()
            );

        } catch (IOException e) {

            /*
             * El contrato público de ReporteServiceSeguroMasVida
             * es void y no declara checked exceptions.
             *
             * Propagamos como error de ejecución del Job para
             * evitar que una falla Drive parezca un cierre exitoso.
             */
            throw new IllegalStateException(
                    "No fue posible completar el reporte "
                            + "quincenal +Vida para "
                            + destinatario
                            + ", período "
                            + periodo.inicio()
                            + " a "
                            + periodo.fin()
                            + ".",
                    e
            );
        }
    }


    private String requerirCorreoDestinatarioMapfre() {

        if (
                correoDestinatarioMapfre == null
                        || correoDestinatarioMapfre.trim().isEmpty()
        ) {

            throw new IllegalStateException(
                    "El correo destinatario de MAPFRE no esta configurado. "
                            + "Configure integraciones.lotes-vida.mapfre.correo-destinatario "
                            + "antes de ejecutar el cierre MAPFRE."
            );
        }

        return correoDestinatarioMapfre.trim();
    }


    /*
     * ==========================================================
     * CALENDARIO MAPFRE
     * ==========================================================
     *
     * 16/09/2026
     * -> 01/09/2026 - 15/09/2026
     *
     * 01/10/2026
     * -> 16/09/2026 - 30/09/2026
     */
    public Periodo obtenerPeriodoMapfre(
            LocalDate fecha
    ) {

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha es obligatoria."
            );
        }

        int dia =
                fecha.getDayOfMonth();

        if (dia == 1) {

            LocalDate fin =
                    fecha.minusDays(1);

            LocalDate inicio =
                    fin.withDayOfMonth(16);

            return new Periodo(
                    inicio,
                    fin
            );
        }

        if (dia == 16) {

            LocalDate inicio =
                    fecha.withDayOfMonth(1);

            LocalDate fin =
                    fecha.withDayOfMonth(15);

            return new Periodo(
                    inicio,
                    fin
            );
        }

        throw new IllegalStateException(
                "El proceso MAPFRE solamente puede "
                        + "ejecutarse los días 1 y 16."
        );
    }


    /*
     * ==========================================================
     * CALENDARIO PERSONAL
     * ==========================================================
     *
     * 19/09/2026
     * -> 04/09/2026 - 18/09/2026
     *
     * 04/10/2026
     * -> 19/09/2026 - 03/10/2026
     */
    public Periodo obtenerPeriodoPersonal(
            LocalDate fecha
    ) {

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha es obligatoria."
            );
        }

        int dia =
                fecha.getDayOfMonth();

        if (dia == 19) {

            LocalDate inicio =
                    fecha.withDayOfMonth(4);

            LocalDate fin =
                    fecha.withDayOfMonth(18);

            return new Periodo(
                    inicio,
                    fin
            );
        }

        if (dia == 4) {

            LocalDate inicio =
                    fecha
                            .minusMonths(1)
                            .withDayOfMonth(19);

            LocalDate fin =
                    fecha.withDayOfMonth(3);

            return new Periodo(
                    inicio,
                    fin
            );
        }

        throw new IllegalStateException(
                "El proceso PERSONAL solamente puede "
                        + "ejecutarse los días 4 y 19."
        );
    }
}