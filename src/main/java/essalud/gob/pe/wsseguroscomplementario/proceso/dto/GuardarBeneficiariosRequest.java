package essalud.gob.pe.wsseguroscomplementario.proceso.dto;

import java.util.List;

public class GuardarBeneficiariosRequest {

    private List<BeneficiarioProgresoRequest> beneficiarios;
    private Boolean beneficiarioBorradorAbierto;
    public GuardarBeneficiariosRequest() {
    }

    public List<BeneficiarioProgresoRequest> getBeneficiarios() {
        return beneficiarios;
    }

    public void setBeneficiarios(
            List<BeneficiarioProgresoRequest> beneficiarios
    ) {
        this.beneficiarios = beneficiarios;
    }

    public Boolean getBeneficiarioBorradorAbierto() {
        return beneficiarioBorradorAbierto;
    }

    public void setBeneficiarioBorradorAbierto(
            Boolean beneficiarioBorradorAbierto
    ) {
        this.beneficiarioBorradorAbierto =
                beneficiarioBorradorAbierto;
    }
}