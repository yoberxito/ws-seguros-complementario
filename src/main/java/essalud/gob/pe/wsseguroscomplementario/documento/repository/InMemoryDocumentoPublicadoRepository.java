package essalud.gob.pe.wsseguroscomplementario.documento.repository;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryDocumentoPublicadoRepository implements DocumentoPublicadoRepository {

    private final ConcurrentHashMap<String, DocumentoPublicado> documentosPublicados = new ConcurrentHashMap<>();

    @Override
    public DocumentoPublicado guardar(DocumentoPublicado documentoPublicado) {
        documentosPublicados.put(documentoPublicado.getIdDocumentoPublicado(), documentoPublicado);
        return documentoPublicado;
    }

    @Override
    public Optional<DocumentoPublicado> buscarPorId(String idDocumentoPublicado) {
        return Optional.ofNullable(documentosPublicados.get(idDocumentoPublicado));
    }

    @Override
    public List<DocumentoPublicado> listar() {
        return new ArrayList<>(documentosPublicados.values());
    }

    @Override
    public List<DocumentoPublicado> buscarPorProcesoYTrabajador(
            String registroInternoProceso,
            String numeroDocumentoTrabajador
    ) {
        return documentosPublicados.values()
                .stream()
                .filter(documento ->
                        documento.getRegistroInternoProceso().equalsIgnoreCase(registroInternoProceso)
                                && documento.getNumeroDocumentoTrabajador().equalsIgnoreCase(numeroDocumentoTrabajador)
                )
                .collect(Collectors.toList());
    }
}