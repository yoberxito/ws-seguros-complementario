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
    private String clientId;
}