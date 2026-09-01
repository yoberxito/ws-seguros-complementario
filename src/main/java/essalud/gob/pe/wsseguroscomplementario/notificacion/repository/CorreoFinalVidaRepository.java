package essalud.gob.pe.wsseguroscomplementario.notificacion.repository;

public interface CorreoFinalVidaRepository {

    boolean reservarEnvio(
            String registroInternoProceso,
            String cicloDocumental,
            String destinatario,
            boolean enviaFormulario,
            int cantidadAdjuntos,
            String usuarioResponsable,
            String ipOrigen,
            String datosSesionDispositivo
    );

    boolean estaEnviado(
            String registroInternoProceso,
            String cicloDocumental
    );

    boolean estaEnError(
            String registroInternoProceso,
            String cicloDocumental
    );

    void marcarEnviado(
            String registroInternoProceso,
            String cicloDocumental,
            String codigoResultado,
            String mensajeResultado
    );

    void marcarError(
            String registroInternoProceso,
            String cicloDocumental,
            String mensajeResultado
    );
}