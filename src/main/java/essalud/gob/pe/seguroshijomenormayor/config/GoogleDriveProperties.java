package essalud.gob.pe.seguroshijomenormayor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "google.drive")
public class GoogleDriveProperties {

    private String projectId;
    private String clientEmail;
    private String privateKey;
    private String folderId;

    /*
     * PERSONAL multidestino.
     *
     * Estos IDs deben apuntar a carpetas Drive YA configuradas
     * con los permisos institucionales correspondientes.
     *
     * El backend no administra permisos Drive.
     */
    private String personalPendingFolderId;
    private String personalHistoricalFolderId;

    private String clientId;
}