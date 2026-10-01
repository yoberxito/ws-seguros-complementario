package essalud.gob.pe.seguroshijomenormayor.persistence.repository;

import essalud.gob.pe.seguroshijomenormayor.persistence.entity.TockenSomosEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IntegracionTokenRepository  extends JpaRepository<TockenSomosEntity, Long> {
    Optional<TockenSomosEntity> findByTokenHashAndUsadoFalse(
            String tokenHash
    );

    Optional<TockenSomosEntity>
    findFirstByTipoDocumentoAndNumeroDocumentoAndUsadoFalseAndFechaExpiracionAfter(
            String tipoDocumento,
            String numeroDocumento,
            LocalDateTime ahora
    );
}
