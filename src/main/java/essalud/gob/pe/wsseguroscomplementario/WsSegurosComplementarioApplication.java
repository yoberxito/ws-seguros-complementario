package essalud.gob.pe.wsseguroscomplementario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = {
                "essalud.gob.pe.wsseguroscomplementario",
                "essalud.gob.pe.seguroshijomenormayor"
        }
)
@EnableScheduling
@EnableFeignClients(
        basePackages = "essalud.gob.pe.seguroshijomenormayor"
)
public class WsSegurosComplementarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(WsSegurosComplementarioApplication.class, args);
	}
}