package essalud.gob.pe.seguroshijomenormayor.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CargaArchivoRes {

    private ArchivoUploadRes archivo;
    private String flagResultado;
    private String mensaje;
}