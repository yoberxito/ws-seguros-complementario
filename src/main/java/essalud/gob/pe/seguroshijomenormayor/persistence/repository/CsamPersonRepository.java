package essalud.gob.pe.seguroshijomenormayor.persistence.repository;/*
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


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CsamPersonRepository extends JpaRepository<CsamPerson,Long> {
    Optional<CsamPerson> findFirstByCodEdocumentPersonaAndNumDocumentPersona(String tipoDocumentoTitular, String numeroDocumentoTitular);
    @Query(value = """
    SELECT fn_genera_autogenerado(
        :fecNacimiento,
        :sexo,
        :apePaterno,
        :apeMaterno,
        :nombres
    ) 
    FROM dual
""", nativeQuery = true)
    String generarCodigoAutogenerado(
            @Param("fecNacimiento") String fecNacimiento,
            @Param("sexo") String sexo,
            @Param("apePaterno") String apePaterno,
            @Param("apeMaterno") String apeMaterno,
            @Param("nombres") String nombres
    );

}
