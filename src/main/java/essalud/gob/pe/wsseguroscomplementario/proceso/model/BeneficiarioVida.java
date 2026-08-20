package essalud.gob.pe.wsseguroscomplementario.proceso.model;

import java.math.BigDecimal;

public class BeneficiarioVida {

    private Long idBeneficiario;
    private Long idSecomasvida;

    private Integer ordenBeneficiario;

    private String codigoDocumentoBeneficiario;
    private String descripcionOtroDocumentoBeneficiario;
    private String numeroDocumentoBeneficiario;

    private String apellidoPaterno;
    private String apellidoMaterno;
    private String primerNombre;
    private String segundoNombre;

    private BigDecimal porcentajeBeneficio;

    public BeneficiarioVida() {
    }

    public Long getIdBeneficiario() {
        return idBeneficiario;
    }

    public void setIdBeneficiario(Long idBeneficiario) {
        this.idBeneficiario = idBeneficiario;
    }

    public Long getIdSecomasvida() {
        return idSecomasvida;
    }

    public void setIdSecomasvida(Long idSecomasvida) {
        this.idSecomasvida = idSecomasvida;
    }

    public Integer getOrdenBeneficiario() {
        return ordenBeneficiario;
    }

    public void setOrdenBeneficiario(Integer ordenBeneficiario) {
        this.ordenBeneficiario = ordenBeneficiario;
    }

    public String getCodigoDocumentoBeneficiario() {
        return codigoDocumentoBeneficiario;
    }

    public void setCodigoDocumentoBeneficiario(
            String codigoDocumentoBeneficiario
    ) {
        this.codigoDocumentoBeneficiario =
                codigoDocumentoBeneficiario;
    }

    public String getDescripcionOtroDocumentoBeneficiario() {
        return descripcionOtroDocumentoBeneficiario;
    }

    public void setDescripcionOtroDocumentoBeneficiario(
            String descripcionOtroDocumentoBeneficiario
    ) {
        this.descripcionOtroDocumentoBeneficiario =
                descripcionOtroDocumentoBeneficiario;
    }

    public String getNumeroDocumentoBeneficiario() {
        return numeroDocumentoBeneficiario;
    }

    public void setNumeroDocumentoBeneficiario(
            String numeroDocumentoBeneficiario
    ) {
        this.numeroDocumentoBeneficiario =
                numeroDocumentoBeneficiario;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public BigDecimal getPorcentajeBeneficio() {
        return porcentajeBeneficio;
    }

    public void setPorcentajeBeneficio(
            BigDecimal porcentajeBeneficio
    ) {
        this.porcentajeBeneficio = porcentajeBeneficio;
    }
}