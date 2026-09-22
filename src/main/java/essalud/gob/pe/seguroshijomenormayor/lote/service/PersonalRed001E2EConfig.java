package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DestinoPersonalVida;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * TEMP E2E QA 22/09/2026.
 *
 * ELIMINAR DESPUES DE VALIDAR EL JOB EN QA.
 *
 * Sustituye temporalmente la resolucion SOMOS:
 * todos los trabajadores se enrutan a Red 001.
 */
@Configuration
public class PersonalRed001E2EConfig {

    @Bean
    public ResolvedorDestinoPersonalVida resolvedorDestinoPersonalVidaE2E() {
        return dni -> new DestinoPersonalVida("001", "RED 001");
    }

    @Bean
    public ResolvedorCorreoDestinoPersonalVida resolvedorCorreoDestinoPersonalVidaE2E() {
        return destino -> "personal-e2e@example.test";
    }
}
