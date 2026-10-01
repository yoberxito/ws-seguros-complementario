package essalud.gob.pe.seguroshijomenormayor.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NuevoLoteCorreoRequest {
    private String correoDestino;
    private String destinatario;
    private String tipoDocumento;
    private String periodoLote;
    private Integer cantidadDocumentos;
    private String codigoLote;
    private String urlAcceso;
}
