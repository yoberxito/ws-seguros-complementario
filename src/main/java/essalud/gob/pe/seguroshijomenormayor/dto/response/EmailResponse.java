package essalud.gob.pe.seguroshijomenormayor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class EmailResponse<D> {
    private String codigoResultado;
    private String mensaje;
    private D body;

    public ResponseEntity<EmailResponse> createResponse(){
        return new ResponseEntity<>(this, HttpStatus.OK);
    }

    public ResponseEntity<EmailResponse> createResponse(HttpStatus httpStatus){
        return new ResponseEntity<>(this, httpStatus);
    }

    public static EmailResponse getRespuesta(Integer respuesta){
        if(Objects.equals("1", respuesta)) {
            return new EmailResponse(respuesta.toString(), "1",null);
        }
        return new EmailResponse(respuesta.toString(),"0",null);
    }

}
