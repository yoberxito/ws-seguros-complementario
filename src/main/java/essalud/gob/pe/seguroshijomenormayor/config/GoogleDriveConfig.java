package essalud.gob.pe.seguroshijomenormayor.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
public class GoogleDriveConfig {

    private static final String APPLICATION_NAME =
            "mia-seguros-hijomenormayor";

    private final GoogleDriveProperties properties;

    public GoogleDriveConfig(
            GoogleDriveProperties properties
    ) {
        this.properties = properties;
    }

    @Bean
    public Drive googleDrive()
            throws GeneralSecurityException,
            IOException {

        String privateKey =
                properties
                        .getPrivateKey()
                        .replace(
                                "\\n",
                                "\n"
                        );

        ServiceAccountCredentials credentials =
                ServiceAccountCredentials
                        .fromPkcs8(
                                properties.getClientId(),
                                properties.getClientEmail(),
                                privateKey,
                                null,
                                List.of(
                                        DriveScopes.DRIVE
                                )
                        );

        return new Drive.Builder(
                GoogleNetHttpTransport
                        .newTrustedTransport(),
                GsonFactory
                        .getDefaultInstance(),
                new HttpCredentialsAdapter(
                        credentials
                )
        )
                .setApplicationName(
                        APPLICATION_NAME
                )
                .build();
    }
}