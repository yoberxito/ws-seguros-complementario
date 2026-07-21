package essalud.gob.pe.wsseguroscomplementario.documento.model;

import java.util.Arrays;

public enum TipoDocumentoDigital {

    FORMULARIO_6012,
    AUTORIZACION_DESCUENTO;

    public static boolean esValido(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return false;
        }

        return Arrays.stream(values())
                .anyMatch(tipo -> tipo.name().equalsIgnoreCase(valor.trim()));
    }

    public static String valoresPermitidos() {
        return "FORMULARIO_6012, AUTORIZACION_DESCUENTO";
    }
}