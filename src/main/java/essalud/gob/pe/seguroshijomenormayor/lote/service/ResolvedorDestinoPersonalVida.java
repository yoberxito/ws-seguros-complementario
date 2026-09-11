package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DestinoPersonalVida;

/*
 * Puerto de integracion para resolver el destino institucional
 * de un trabajador PERSONAL a partir de su DNI.
 *
 * En esta etapa NO existe una implementacion productiva.
 *
 * Cuando se disponga del contrato institucional real
 * (por ejemplo SOMOS), su adaptador implementara esta interfaz.
 */
@FunctionalInterface
public interface ResolvedorDestinoPersonalVida {

    DestinoPersonalVida resolverPorDni(
            String dni
    );
}