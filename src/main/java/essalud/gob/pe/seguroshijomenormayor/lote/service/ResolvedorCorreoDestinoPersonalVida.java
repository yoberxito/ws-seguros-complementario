package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DestinoPersonalVida;

/*
 * Punto de integracion para recuperar el correo responsable
 * de un destino institucional PERSONAL.
 *
 * IMPORTANTE:
 *
 * - No existe implementacion ficticia en src/main.
 * - El correo NO constituye la identidad del destino.
 * - codigoDestino sigue siendo la identidad funcional.
 * - La fuente institucional real se conectara cuando su
 *   contrato sea confirmado.
 */
@FunctionalInterface
public interface ResolvedorCorreoDestinoPersonalVida {

    String resolverCorreo(
            DestinoPersonalVida destino
    );
}