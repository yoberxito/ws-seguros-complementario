package essalud.gob.pe.seguroshijomenormayor.a4;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class MapfreReporteDiferidoA4Test {

    @Test
    void reporteMapfreNoCierraCarpetaAntesDelAcuse()
            throws Exception {

        Path sourcePath =
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


        assertTrue(
                Files.exists(
                        sourcePath
                )
        );


        String source =
                Files.readString(
                        sourcePath,
                        StandardCharsets.UTF_8
                );


        int inicioBloque =
                source.indexOf(
                        "String idCarpetaEntrega;"
                );


        assertTrue(
                inicioBloque >= 0
        );


        int ramaMapfre =
                source.indexOf(
                        "if (DESTINO_MAPFRE.equals(destino))",
                        inicioBloque
                );


        assertTrue(
                ramaMapfre > inicioBloque
        );


        int ramaElse =
                source.indexOf(
                        "} else {",
                        ramaMapfre
                );


        assertTrue(
                ramaElse > ramaMapfre
        );


        String bloqueMapfre =
                source.substring(
                        ramaMapfre,
                        ramaElse
                );


        assertFalse(
                bloqueMapfre.contains(
                        "cerrarCarpetaPeriodo"
                )
        );


        assertTrue(
                bloqueMapfre.contains(
                        "obtenerInformacionArchivo"
                )
        );


        int cierre =
                source.indexOf(
                        ".cerrarCarpetaPeriodo(",
                        ramaElse
                );


        assertTrue(
                cierre > ramaElse
        );


        assertTrue(
                source.contains(
                        "La derivacion final de MAPFRE ocurre despues del acuse."
                )
        );
    }
}