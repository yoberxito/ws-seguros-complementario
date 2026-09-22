package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;

import essalud.gob.pe.seguroshijomenormayor.config.GoogleDriveProperties;
import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import essalud.gob.pe.seguroshijomenormayor.lote.model.GrupoDocumentosPersonalDrive;
import essalud.gob.pe.seguroshijomenormayor.lote.model.ResultadoDistribucionPersonalDrive;
import essalud.gob.pe.seguroshijomenormayor.service.GoogleDriveService;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Ejecuta fisicamente el plan PERSONAL multidestino.
 *
 * Estructura:
 *
 * root configurado
 *   -> PERSONAL_PENDING / Red / periodo
 *      -> codigoDestino - nombreDestino
 *         -> fechaInicio_fechaFin
 *            -> copias de PDFs 247
 *
 * Los originales no se mueven.
 */
@Service
public class DistribuidorPersonalDriveService {


    private final GoogleDriveService
            googleDriveService;

    private final GoogleDriveProperties
            googleDriveProperties;

    public DistribuidorPersonalDriveService(
            GoogleDriveService googleDriveService,
            GoogleDriveProperties googleDriveProperties
    ) {

        this.googleDriveService =
                googleDriveService;

        this.googleDriveProperties =
                googleDriveProperties;
    }

    public List<ResultadoDistribucionPersonalDrive> distribuir(
            List<GrupoDocumentosPersonalDrive> grupos,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        if (grupos == null) {
            throw new IllegalArgumentException(
                    "Los grupos PERSONAL son obligatorios."
            );
        }

        validarPeriodo(
                fechaInicio,
                fechaFin
        );

        if (grupos.isEmpty()) {
            return List.of();
        }

        /*
         * A3:
         *
         * La primera entrega PERSONAL se construye siempre bajo
         * una raiz Pending privada previamente configurada.
         *
         * No existe fallback al folderId historico/general:
         * si Pending no esta configurado, el proceso debe fallar.
         */
        String rootId =
                requerir(
                        googleDriveProperties
                                .getPersonalPendingFolderId(),
                        "google.drive.personal-pending-folder-id es obligatorio."
                );

        String nombrePeriodo =
                fechaInicio
                        + "_"
                        + fechaFin;

        List<ResultadoDistribucionPersonalDrive> resultados =
                new ArrayList<>();

        for (
                GrupoDocumentosPersonalDrive grupo
                : grupos
        ) {

            if (grupo == null) {
                throw new IllegalStateException(
                        "Existe un grupo PERSONAL nulo."
                );
            }

            String codigoDestino =
                    requerir(
                            grupo
                                    .getDestino()
                                    .codigoDestino(),
                            "El codigo del destino PERSONAL es obligatorio."
                    );

            File carpetaDestino =
                    googleDriveService
                            .obtenerOCrearCarpeta(
                                    construirNombreCarpetaDestino(
                                            grupo.getDestino()
                                    ),
                                    rootId
                            );

            validarCarpeta(
                    carpetaDestino,
                    "No se pudo resolver la carpeta del destino PERSONAL."
            );

            File carpetaPeriodo =
                    googleDriveService
                            .obtenerOCrearCarpeta(
                                    nombrePeriodo,
                                    carpetaDestino.getId()
                            );

            validarCarpeta(
                    carpetaPeriodo,
                    "No se pudo resolver la carpeta del periodo PERSONAL."
            );

            List<DocumentoDriveLoteVida> documentosFinales =
                    new ArrayList<>();

            for (
                    DocumentoDriveLoteVida documento
                    : grupo.getDocumentos()
            ) {

                if (documento == null) {
                    throw new IllegalStateException(
                            "Existe un documento PERSONAL nulo."
                    );
                }

                String fileId =
                        requerir(
                                documento.getFileId(),
                                "Existe un documento PERSONAL sin ID Drive."
                        );

                File copia =
                        googleDriveService
                                .copiarArchivoEnCarpeta(
                                        fileId,
                                        carpetaPeriodo.getId()
                                );

                validarArchivoCopia(
                        copia
                );

                DocumentoDriveLoteVida documentoFinal =
                        new DocumentoDriveLoteVida(
                                copia.getId().trim(),
                                copia.getName().trim(),
                                copia.getWebViewLink().trim(),
                                documento.getIdTpDoc(),
                                documento.getTipoDocumentoTitular(),
                                documento.getNumeroDocumentoTitular()
                        );

                documentosFinales.add(
                        documentoFinal
                );
            }

            resultados.add(
                    new ResultadoDistribucionPersonalDrive(
                            grupo.getDestino(),
                            carpetaDestino.getId(),
                            carpetaPeriodo.getId(),
                            carpetaPeriodo.getWebViewLink(),
                            documentosFinales
                    )
            );
        }

        return List.copyOf(
                resultados
        );
    }


    static String construirNombreCarpetaDestino(
            essalud.gob.pe.seguroshijomenormayor.lote.model.DestinoPersonalVida destino
    ) {

        if (destino == null) {

            throw new IllegalArgumentException(
                    "El destino PERSONAL es obligatorio para construir la carpeta."
            );
        }

        return destino.codigoDestino()
                + " - "
                + destino.nombreDestino();
    }
    private void validarArchivoCopia(
            File archivo
    ) {

        if (
                archivo == null
                        || campoVacio(
                                archivo.getId()
                        )
                        || campoVacio(
                                archivo.getName()
                        )
                        || campoVacio(
                                archivo.getWebViewLink()
                        )
        ) {

            throw new IllegalStateException(
                    "Google Drive no confirmo correctamente la copia del documento PERSONAL."
            );
        }
    }

    private void validarCarpeta(
            File carpeta,
            String mensaje
    ) {

        if (
                carpeta == null
                        || campoVacio(
                                carpeta.getId()
                        )
                        || campoVacio(
                                carpeta.getWebViewLink()
                        )
        ) {

            throw new IllegalStateException(
                    mensaje
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

        if (
                fechaInicio.isAfter(
                        fechaFin
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la final."
            );
        }
    }

    private String requerir(
            String valor,
            String mensaje
    ) {

        if (campoVacio(valor)) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }

        return valor.trim();
    }

    private boolean campoVacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }
}