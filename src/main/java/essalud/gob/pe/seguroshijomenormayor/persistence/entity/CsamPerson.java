package essalud.gob.pe.seguroshijomenormayor.persistence.entity;/*
 * Copyright (c) 2026 yober cieza coronel. Todos los derechos reservados.
 *
 * Este archivo es parte de seguros-hijomenormayor.
 *
 * seguros-hijomenormayor es software propietario: no puedes redistribuirlo y/o modificarlo sin el
 * permiso expreso del propietario. Está sujeto a los términos y condiciones
 * que acompañan el uso del software.
 *
 * Cualquier uso no autorizado puede ser sancionado según la ley vigente.
 */


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


@Entity
@Table(name = "CSAMPERSON")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CsamPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "csamperson_seq")
    @SequenceGenerator(
            name = "csamperson_seq",
            sequenceName = "USRCSA.SQ_CSAMPERSON",
            allocationSize = 1
    )
    @Column(name = "IDE_NUMERICO_PERSONA")
    private Long idNumericoPersona;

    @Column(name = "COD_TDOCUMENT_PERSONA")
    private Long codTdocumentPersona;

    @Column(name = "COD_EDOCUMENT_PERSONA", length = 2)
    private String codEdocumentPersona;

    @Column(name = "COD_TPAISEMI_DOCUMENT")
    private Long codTpaisEmiDocument;

    @Column(name = "COD_EPAISEMI_DOCUMENT", length = 3)
    private String codEpaisEmiDocument;

    @Column(name = "NUM_DOCUMENT_PERSONA", length = 15)
    private String numDocumentPersona;

    @Column(name = "COD_AUTOGENE_PERSONA", length = 15)
    private String codAutogenePersona;

    @Column(name = "TXT_APEPATER_PERSONA", length = 40)
    private String apePaterno;

    @Column(name = "TXT_APEMATER_PERSONA", length = 40)
    private String apeMaterno;

    @Column(name = "TXT_APECASAD_PERSONA", length = 40)
    private String apeCasada;

    @Column(name = "TXT_PRINOMBR_PERSONA", length = 40)
    private String primerNombre;

    @Column(name = "TXT_SEGNOMBR_PERSONA", length = 40)
    private String segundoNombre;

    @Column(name = "TXT_NOMBRES_PERSONA", length = 40)
    private String nombres;

    @Column(name = "COD_TESTCIVI_PERSONA")
    private Long codTestciviPersona;

    @Column(name = "COD_EESTCIVI_PERSONA", length = 2)
    private String codEestciviPersona;

    @Column(name = "COD_TSEXO_PERSONA")
    private Long codTsexoPersona;

    @Column(name = "COD_ESEXO_PERSONA", length = 1)
    private String codEsexoPersona;

    @Column(name = "FLG_ESTADO_INHABILI", length = 1)
    private String flagEstadoInhabilitado;

    @Column(name = "NUM_CIE_RUC", length = 11)
    private String numRucEmpleador;

    @Column(name = "FEC_NACIMI_PERSONA", length = 8)
    private String fechaNacimiento;

    @Column(name = "FEC_FALLEC_PERSONA", length = 8)
    private String fechaFallecimiento;

    @Column(name = "FEC_REGISTRO_SISTEMA", length = 8)
    private String fechaRegistroSistema;

    @Column(name = "HOR_REGISTRO_SISTEMA", length = 8)
    private String horaRegistroSistema;

    @Column(name = "COD_USUARIO_SISTEMA", length = 20)
    private String usuarioSistema;

    @Column(name = "COD_TERMINAL_SISTEMA", length = 80)
    private String terminalSistema;

    @Column(name = "TXT_NOMBCOMP_PERSONA", length = 120)
    private String nombreCompleto;



}
