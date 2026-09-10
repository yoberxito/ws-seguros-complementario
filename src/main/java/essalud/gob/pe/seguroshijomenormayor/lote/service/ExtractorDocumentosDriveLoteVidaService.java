package essalud.gob.pe.seguroshijomenormayor.lote.service;

import com.google.api.services.drive.model.File;
import essalud.gob.pe.seguroshijomenormayor.lote.model.DocumentoDriveLoteVida;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExtractorDocumentosDriveLoteVidaService {

    private static final String MIME_TYPE_PDF =
            "application/pdf";

    public List<DocumentoDriveLoteVida> extraer(
            List<File> archivos,
            String idTpDocEsperado
    ) {

        validarIdTpDoc(
                idTpDocEsperado
        );

        if (archivos == null) {

            throw new IllegalArgumentException(
                    "La colección de archivos Drive es obligatoria."
            );
        }

        List<DocumentoDriveLoteVida> documentos =
                new ArrayList<>();

        Set<String> trabajadoresEncontrados =
                new HashSet<>();

        for (File archivo : archivos) {

            if (archivo == null) {
                continue;
            }

            /*
             * La carpeta podrá contener el reporte Excel
             * generado por una ejecución anterior.
             *
             * El Job procesa únicamente PDFs.
             */
            if (!esPdf(archivo)) {
                continue;
            }

            DocumentoDriveLoteVida documento =
                    convertir(
                            archivo,
                            idTpDocEsperado
                    );

            String llaveTrabajador =
                    documento
                            .getTipoDocumentoTitular()
                            .trim()
                            .toUpperCase()
                            + "|"
                            + documento
                            .getNumeroDocumentoTitular()
                            .trim();

            /*
             * No elegimos arbitrariamente entre dos PDFs
             * del mismo trabajador.
             *
             * Si ocurre, el lote debe detenerse para evitar
             * enviar información ambigua.
             */
            if (
                    !trabajadoresEncontrados.add(
                            llaveTrabajador
                    )
            ) {

                throw new IllegalStateException(
                        "La carpeta Drive contiene más de un PDF "
                                + "para el mismo trabajador. "
                                + "Tipo/número: "
                                + llaveTrabajador
                                + "."
                );
            }

            documentos.add(
                    documento
            );
        }

        return documentos;
    }


    private DocumentoDriveLoteVida convertir(
            File archivo,
            String idTpDocEsperado
    ) {

        validarTexto(
                archivo.getId(),
                "Existe un PDF Drive sin identificador."
        );

        validarTexto(
                archivo.getName(),
                "Existe un PDF Drive sin nombre."
        );

        validarTexto(
                archivo.getWebViewLink(),
                "El PDF "
                        + archivo.getName()
                        + " no cuenta con webViewLink."
        );

        Map<String, String> metadata =
                archivo.getAppProperties();

        /*
         * No se intenta inferir el DNI desde posiciones
         * del nombre del archivo.
         *
         * Eso sería frágil y podría provocar que un PDF
         * termine asociado a otro trabajador.
         */
        if (
                metadata == null
                        || metadata.isEmpty()
        ) {

            throw new IllegalStateException(
                    "El PDF Drive "
                            + archivo.getName()
                            + " no contiene metadata +Vida "
                            + "(appProperties). "
                            + "No es seguro procesarlo automáticamente."
            );
        }

        String idTpDoc =
                limpiar(
                        metadata.get(
                                "idTpDoc"
                        )
                );

        String tipoDocumento =
                limpiar(
                        metadata.get(
                                "tpDocument"
                        )
                );

        String numeroDocumento =
                limpiar(
                        metadata.get(
                                "numDocument"
                        )
                );

        validarTexto(
                idTpDoc,
                "El PDF "
                        + archivo.getName()
                        + " no contiene idTpDoc."
        );

        validarTexto(
                tipoDocumento,
                "El PDF "
                        + archivo.getName()
                        + " no contiene tpDocument."
        );

        validarTexto(
                numeroDocumento,
                "El PDF "
                        + archivo.getName()
                        + " no contiene numDocument."
        );

        validarIdTpDoc(
                idTpDoc
        );

        if (
                !idTpDocEsperado.equals(
                        idTpDoc
                )
        ) {

            throw new IllegalStateException(
                    "La carpeta del lote "
                            + idTpDocEsperado
                            + " contiene un PDF de tipo "
                            + idTpDoc
                            + ": "
                            + archivo.getName()
                            + "."
            );
        }

        return new DocumentoDriveLoteVida(
                archivo.getId().trim(),
                archivo.getName().trim(),
                archivo.getWebViewLink().trim(),
                idTpDoc,
                tipoDocumento,
                numeroDocumento
        );
    }


    private boolean esPdf(
            File archivo
    ) {

        String mimeType =
                limpiar(
                        archivo.getMimeType()
                );

        return MIME_TYPE_PDF.equalsIgnoreCase(
                mimeType
        );
    }


    private void validarIdTpDoc(
            String idTpDoc
    ) {

        String valor =
                limpiar(
                        idTpDoc
                );

        if (
                !"244".equals(valor)
                        && !"247".equals(valor)
        ) {

            throw new IllegalArgumentException(
                    "El tipo institucional del lote "
                            + "solamente puede ser 244 o 247."
            );
        }
    }


    private void validarTexto(
            String valor,
            String mensaje
    ) {

        if (
                valor == null
                        || valor.trim().isEmpty()
        ) {

            throw new IllegalStateException(
                    mensaje
            );
        }
    }


    private String limpiar(
            String valor
    ) {

        return valor == null
                ? null
                : valor.trim();
    }
}