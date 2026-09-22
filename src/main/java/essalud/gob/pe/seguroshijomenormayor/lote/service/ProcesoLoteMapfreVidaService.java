package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import essalud.gob.pe.seguroshijomenormayor.entrega.model.CierreLotePreparado;
import essalud.gob.pe.seguroshijomenormayor.entrega.service.CierreLoteDistribucionService;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoReporteLoteVida;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcesoLoteMapfreVidaService {

    private final ReporteQuincenalLoteVidaService reporteMapfreService;
    private final CierreLoteDistribucionService cierreLoteDistribucionService;

    @Value("${integraciones.lotes-vida.mapfre.correo-destinatario:}")
    private String correoDestinatarioMapfre;

    public void procesar(
            Periodo periodo
    ) {

        validarPeriodo(
                periodo
        );

        log.info(
                "Iniciando job MAPFRE +Vida. periodo={} a {}",
                periodo.inicio(),
                periodo.fin()
        );

        try {

            Optional<ResultadoReporteLoteVida> resultadoOpt =
                    reporteMapfreService
                            .generarMapfre(
                                    periodo.inicio(),
                                    periodo.fin()
                            );

            if (resultadoOpt.isEmpty()) {
                return;
            }

            ResultadoReporteLoteVida resultado =
                    resultadoOpt.get();

            CierreLotePreparado cierre =
                    cierreLoteDistribucionService
                            .prepararLotePublicado(
                                    resultado.getDestinatario(),
                                    resultado.getFechaInicio(),
                                    resultado.getFechaFin(),
                                    requerirCorreoMapfre(),
                                    resultado.getCantidadDocumentos(),
                                    resultado.getUrlCarpetaFinal()
                            );

            validarCierre(
                    cierre
            );

            log.info(
                    "Job MAPFRE preparado correctamente. lote={}, entrega={}, documentos={}, tokenGenerado={}",
                    cierre.getLote().getIdLote(),
                    cierre.getPreparacionEntrega().getEntrega().getIdEntrega(),
                    resultado.getCantidadDocumentos(),
                    cierre.isTokenGenerado()
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No fue posible completar el job MAPFRE +Vida para el periodo "
                            + periodo.inicio()
                            + " a "
                            + periodo.fin()
                            + ".",
                    e
            );
        }
    }

    private String requerirCorreoMapfre() {

        if (
                correoDestinatarioMapfre == null
                        || correoDestinatarioMapfre.trim().isEmpty()
        ) {
            throw new IllegalStateException(
                    "El correo MAPFRE no esta configurado. Configure integraciones.lotes-vida.mapfre.correo-destinatario."
            );
        }

        return correoDestinatarioMapfre.trim();
    }

    private void validarCierre(
            CierreLotePreparado cierre
    ) {

        if (
                cierre == null
                        || cierre.getLote() == null
                        || cierre.getPreparacionEntrega() == null
                        || cierre.getPreparacionEntrega().getEntrega() == null
        ) {
            throw new IllegalStateException(
                    "El job MAPFRE no devolvio lote y entrega validos."
            );
        }
    }

    private void validarPeriodo(
            Periodo periodo
    ) {
        if (
                periodo == null
                        || periodo.inicio() == null
                        || periodo.fin() == null
        ) {
            throw new IllegalArgumentException(
                    "El periodo MAPFRE es obligatorio."
            );
        }
    }
}
