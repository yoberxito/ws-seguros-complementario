package essalud.gob.pe.seguroshijomenormayor.a4;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MapfreReporteDiferidoA4Test {

    @Test
    void reporteMapfreNoCierraCarpetaYLaDerivacionOcurreSoloPostAcuse()
            throws Exception {

        Path reportePath =
                Path.of(
                        "src",
                        "main",
                        "java",
                        "essalud",
                        "gob",
                        "pe",
                        "seguroshijomenormayor",
                        "lote",
                        "service",
                        "ReporteQuincenalLoteVidaService.java"
                );

        Path entregaPath =
                Path.of(
                        "src",
                        "main",
                        "java",
                        "essalud",
                        "gob",
                        "pe",
                        "seguroshijomenormayor",
                        "entrega",
                        "service",
                        "EntregaLoteService.java"
                );

        assertTrue(
                Files.exists(reportePath),
                "Debe existir ReporteQuincenalLoteVidaService."
        );

        assertTrue(
                Files.exists(entregaPath),
                "Debe existir EntregaLoteService."
        );

        String reporteSource =
                Files.readString(
                        reportePath,
                        StandardCharsets.UTF_8
                );

        String entregaSource =
                Files.readString(
                        entregaPath,
                        StandardCharsets.UTF_8
                );

        /*
         * La generacion del reporte MAPFRE solo prepara
         * el periodo y el Excel.
         *
         * No puede derivar/mover la carpeta a historico.
         */
        assertFalse(
                reporteSource.contains(
                        ".cerrarCarpetaPeriodo("
                ),
                "El reporte MAPFRE no debe cerrar ni mover la carpeta del periodo."
        );

        /*
         * El cierre Drive pertenece al flujo posterior
         * a la descarga y confirmacion del receptor.
         */
        int inicioPostAcuse =
                entregaSource.indexOf(
                        "private void asegurarPublicacionDrivePostAcuse("
                );

        assertTrue(
                inicioPostAcuse >= 0,
                "Debe existir el flujo post-acuse de publicacion Drive."
        );

        int validacionAcuse =
                entregaSource.indexOf(
                        "No se puede publicar Drive antes del acuse.",
                        inicioPostAcuse
                );

        assertTrue(
                validacionAcuse > inicioPostAcuse,
                "El flujo debe impedir publicar/mover Drive antes del acuse."
        );

        int cierreMapfre =
                entregaSource.indexOf(
                        ".cerrarCarpetaPeriodo(",
                        inicioPostAcuse
                );

        assertTrue(
                cierreMapfre > validacionAcuse,
                "MAPFRE debe cerrar la carpeta solamente dentro del flujo post-acuse."
        );

        int publicacionCompletada =
                entregaSource.indexOf(
                        ".registrarPublicacionDriveCompletada(",
                        cierreMapfre
                );

        assertTrue(
                publicacionCompletada > cierreMapfre,
                "La publicacion Drive completada debe registrarse despues del movimiento."
        );

        /*
         * Sin descarga completada tampoco debe poder
         * completarse normalmente el flujo MAPFRE actual.
         */
        assertTrue(
                entregaSource.contains(
                        ".existeDescargaLoteCompletada("
                ),
                "La entrega debe validar la descarga completada."
        );

        /*
         * Regresion: el servicio generico antiguo
         * MAPFRE/PERSONAL ya no debe volver.
         */
        assertFalse(
                reporteSource.contains(
                        "if (DESTINO_MAPFRE.equals(destino))"
                ),
                "ReporteQuincenalLoteVidaService no debe recuperar la bifurcacion legacy por destinatario."
        );

        assertFalse(
                reporteSource.contains(
                        "String idCarpetaEntrega;"
                ),
                "No debe existir la estructura legacy de carpeta de entrega."
        );
    }
}