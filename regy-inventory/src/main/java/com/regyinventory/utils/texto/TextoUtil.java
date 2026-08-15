package com.regyinventory.utils.texto;

import java.text.Normalizer;
import java.util.Locale;

/** Utilidades de normalización para búsquedas tolerantes a tildes y mayúsculas. */
public final class TextoUtil {

    private TextoUtil() {
    }

    /** Normaliza un texto para comparaciones de búsqueda sin tildes y sin distinción de mayúsculas. */
    public static String normalizarBusqueda(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String sinTildes = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }
}
