package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.dto.Periodo;
import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoPipelinePersonalDestino;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcesoLotePersonalVidaService {

    private static final String ID_TP_DOC_PERSONAL =
            "247";

    private final GoogleDriveService googleDriveService;
    private final ExtractorDocumentosDriveLoteVidaService extractorDrive;
    private final OrquestadorPersonalMultidestinoService orquestadorPersonal;
    private final ObjectProvider<ResolvedorDestinoPersonalVida> resolvedorDestinoProvider;
    private final ObjectProvider<ResolvedorCorreoDestinoPersonalVida> resolvedorCorreoProvider;

    public void procesar(
            Periodo periodo
    ) {

        validarPeriodo(
                periodo
        );

        log.info(
                "Iniciando job PERSONAL +Vida. periodo={} a {}",
                periodo.inicio(),
                periodo.fin()
        );

        try {

            Optional<File> carpetaOpt =
                    googleDriveService
                            .buscarCarpetaPeriodo(
                                    ID_TP_DOC_PERSONAL,
                                    periodo.inicio(),
                                    periodo.fin()
                            );

            if (carpetaOpt.isEmpty()) {

                log.info(
                        "PERSONAL sin documentos para el periodo {} a {}. No se generan carpetas, reportes, lotes ni entregas.",
                        periodo.inicio(),
                        periodo.fin()
                );

                return;
            }

            File carpetaPeriodo =
                    carpetaOpt.get();

            List<File> archivos =
                    googleDriveService
                            .listarArchivosCarpeta(
                                    carpetaPeriodo.getId()
                            );

            List<DocumentoDriveLoteVida> documentos =
                    extractorDrive.extraer(
                            archivos,
                            ID_TP_DOC_PERSONAL
                    );

            if (documentos.isEmpty()) {
                throw new IllegalStateException(
                        "La carpeta PERSONAL del periodo existe, pero no contiene PDFs 247 validos."
                );
            }

            /*
             * Fail closed antes de cualquier efecto sobre Pending.
             *
             * El adaptador DNI -> Red se conectara a la informacion
             * laboral persistida proveniente de SOMOS.
             */
            ResolvedorDestinoPersonalVida resolvedorDestino =
                    resolvedorDestinoProvider
                            .getIfAvailable();

            if (resolvedorDestino == null) {
                throw new IllegalStateException(
                        "PERSONAL no puede distribuir el lote porque ResolvedorDestinoPersonalVida aun no esta conectado a SOMOS/datos laborales persistidos."
                );
            }

            ResolvedorCorreoDestinoPersonalVida resolvedorCorreo =
                    resolvedorCorreoProvider
                            .getIfAvailable();

            if (resolvedorCorreo == null) {
                throw new IllegalStateException(
                        "PERSONAL no puede distribuir el lote porque ResolvedorCorreoDestinoPersonalVida no esta configurado."
                );
            }

            List<ResultadoPipelinePersonalDestino> resultados =
                    orquestadorPersonal
                            .procesar(
                                    documentos,
                                    periodo.inicio(),
                                    periodo.fin(),
                                    resolvedorDestino,
                                    resolvedorCorreo
                            );

            log.info(
                    "Job PERSONAL completado. periodo={} a {}, documentosFuente={}, destinos={}",
                    periodo.inicio(),
                    periodo.fin(),
                    documentos.size(),
                    resultados.size()
            );

            for (ResultadoPipelinePersonalDestino resultado : resultados) {
                log.info(
                        "PERSONAL destino preparado. codigoDestino={}, documentos={}, lote={}, entrega={}, tokenGenerado={}",
                        resultado.getDestino().codigoDestino(),
                        resultado.getReporte().getCantidadDocumentos(),
                        resultado.getCierre().getLote().getIdLote(),
                        resultado.getCierre().getPreparacionEntrega().getEntrega().getIdEntrega(),
                        resultado.getCierre().isTokenGenerado()
                );
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No fue posible completar el job PERSONAL +Vida para el periodo "
                            + periodo.inicio()
                            + " a "
                            + periodo.fin()
                            + ".",
                    e
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
                    "El periodo PERSONAL es obligatorio."
            );
        }
    }
}
