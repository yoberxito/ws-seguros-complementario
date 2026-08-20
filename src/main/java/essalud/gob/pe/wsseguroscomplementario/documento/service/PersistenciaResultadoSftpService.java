package essalud.gob.pe.wsseguroscomplementario.documento.service;

import essalud.gob.pe.wsseguroscomplementario.documento.model.DocumentoPublicado;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoPublicadoRepository;
import essalud.gob.pe.wsseguroscomplementario.documento.repository.DocumentoSustentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistenciaResultadoSftpService {

    private final DocumentoPublicadoRepository
            documentoPublicadoRepository;

    private final DocumentoSustentoRepository
            documentoSustentoRepository;

    public PersistenciaResultadoSftpService(
            DocumentoPublicadoRepository documentoPublicadoRepository,
            DocumentoSustentoRepository documentoSustentoRepository
    ) {
        this.documentoPublicadoRepository =
                documentoPublicadoRepository;

        this.documentoSustentoRepository =
                documentoSustentoRepository;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public DocumentoPublicado registrarResultadoSftp(
            DocumentoPublicado documentoPublicado
    ) {

        DocumentoPublicado documentoGuardado =
                documentoPublicadoRepository
                        .guardar(
                                documentoPublicado
                        );

        /*
         * El repositorio documental simulado
         * actualmente no conserva RUTA_ARCHIVO.
         *
         * Se recupera del resultado SFTP original
         * para persistirla en DOCUMENTOS_SUSTENTO.
         */
        documentoGuardado.setRutaArchivo(
                documentoPublicado
                        .getRutaArchivo()
        );

        documentoSustentoRepository
                .registrarResultadoSftp(
                        documentoGuardado
                );

        return documentoGuardado;
    }
}