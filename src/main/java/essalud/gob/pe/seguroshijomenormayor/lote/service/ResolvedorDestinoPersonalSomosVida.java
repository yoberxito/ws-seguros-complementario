package essalud.gob.pe.seguroshijomenormayor.lote.service;

import essalud.gob.pe.seguroshijomenormayor.lote.model.DestinoPersonalVida;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lector temporal exclusivo de PERSONAL.
 * Consulta la informacion SOMOS existente, sin modificar tokens ni consumirlos.
 * Como no existe un vinculo con la afiliacion, exige una unica red validada
 * para el tipo y numero de documento. No elige una red entre varias.
 */
@Service
@Transactional(readOnly = true)
public class ResolvedorDestinoPersonalSomosVida implements ResolvedorDestinoPersonalVida {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DestinoPersonalVida resolverPorDni(String dni) {
        return resolverPorDocumento("01", dni);
    }

    @Override
    public DestinoPersonalVida resolverPorDocumento(String tipoDocumento, String numeroDocumento) {
        String tipo = requerir(tipoDocumento, "El tipo de documento PERSONAL es obligatorio.");
        String numero = requerir(numeroDocumento, "El numero de documento PERSONAL es obligatorio.");

        // USADO=true indica que el front valido el acceso SOMOS.
        // La expiracion del enlace no determina la vigencia de los datos laborales.
        List<String> registros = entityManager.createQuery("""
                select distinct t.codigoRed
                from TockenSomosEntity t
                where t.tipoDocumento = :tipo
                  and t.numeroDocumento = :numero
                  and t.usado = true
                """, String.class)
                .setParameter("tipo", tipo)
                .setParameter("numero", numero)
                .getResultList();

        Set<String> codigos = new LinkedHashSet<>();
        for (String registro : registros) {
            codigos.add(requerir(registro, "Existe un registro SOMOS validado sin codigo de red."));
        }
        if (codigos.isEmpty()) {
            throw new IllegalStateException(
                    "No se encontro una red SOMOS validada para " + tipo + "/" + numero + ".");
        }
        if (codigos.size() != 1) {
            throw new IllegalStateException(
                    "Hay distintas redes SOMOS para " + tipo + "/" + numero
                            + ". Se necesita identificar la red correspondiente a la afiliacion.");
        }

        String codigo = codigos.iterator().next();
        // Hasta contar con la tabla oficial, el codigo tambien sirve como etiqueta.
        return new DestinoPersonalVida(codigo, codigo);
    }

    private static String requerir(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException(mensaje);
        }
        return valor.trim();
    }
}
