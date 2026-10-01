package essalud.gob.pe.seguroshijomenormayor.lote.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersonalSomosVidaConfig {

    @Bean
    public ResolvedorCorreoDestinoPersonalVida resolvedorCorreoDestinoPersonalVida() {
        // Temporal: reemplazar por la tabla oficial de redes cuando este disponible.
        return destino -> "gctic.sgsass22@essalud.gob.pe";
    }
}
