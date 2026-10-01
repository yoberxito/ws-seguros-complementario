package essalud.gob.pe.seguroshijomenormayor.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
@Entity
@Table(name = "INTEGRACION_SOMOS_TOKEN")
@Getter
@Setter
public class TockenSomosEntity {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "intg_seq"
    )
    @SequenceGenerator(
            name = "intg_seq",
            sequenceName = "USRCSA.SEQ_INTEG_SOMOS_TOKEN",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "TOKEN_HASH", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "TIPO_DOCUMENTO", nullable = false)
    private String tipoDocumento;

    @Column(name = "NUMERO_DOCUMENTO", nullable = false)
    private String numeroDocumento;

    @Column(name = "CODIGO_PLANILLA", nullable = false)
    private String codigoPlanilla;

    @Column(name = "REGIMEN_LABORAL", nullable = false)
    private String regimenLaboral;

    @Column(name = "CODIGO_RED", nullable = false)
    private String codigoRed;
    @Email(message = "El correo no tiene un formato válido")
    @NotBlank(message = "El correo es obligatorio")
    @Column(name = "CORREO", nullable = false, length = 150)
    private String correo;
    @Column(name = "FECHA_EXPIRACION", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "USADO", nullable = false)
    private boolean usado;
}
