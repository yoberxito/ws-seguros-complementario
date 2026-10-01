package essalud.gob.pe.seguroshijomenormayor.client;


import essalud.gob.pe.seguroshijomenormayor.dto.request.NuevoLoteCorreoRequest;
import essalud.gob.pe.seguroshijomenormayor.dto.response.EmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "notificacionSegurosComplementarios",
        url = "${feign-clients.servicios-notificacion.url}"
      //  configuration = NotificacionFeignConfig.class

)
public interface NotificacionSegurosComplementarios {
    @PostMapping("/seguros-complementarios/send-otp-email")
    EmailResponse enviarNotificacionGenerarOtp(
            @RequestParam String codigo,
            @RequestParam String correoDestino
    );
    @PostMapping("/seguros-complementarios/send-lotes-job")
    EmailResponse enviaLotesJob(
            @RequestBody NuevoLoteCorreoRequest loteCorreoRequest
    );
}
