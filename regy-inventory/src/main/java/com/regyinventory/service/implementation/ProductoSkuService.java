package com.regyinventory.service.implementation;

import com.regyinventory.utils.constants.numeros.Numeros;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ProductoSkuService {

    /** Genera el SKU sugerido con número, marca, categoría y los últimos seis caracteres del código de barras. */
    public String generar(String numero, String marca, String categoria, String codigoBarras) {
        String marcaParte = abreviar(marca, Numeros.CINCO);
        String categoriaParte = abreviar(categoria, Numeros.TRES);
        String barras = codigoBarras == null ? "" : codigoBarras.replaceAll("\\s+", "");
        String barrasParte = barras.length() <= Numeros.SEIS
                ? barras
                : barras.substring(barras.length() - Numeros.SEIS);

        return String.join(
                "-",
                normalizarNumero(numero),
                marcaParte,
                categoriaParte,
                barrasParte
        ).replaceAll("-+$", "");
    }

    /** Normaliza un segmento textual antes de incorporarlo al SKU. */
    private String abreviar(String valor, int longitud) {
        String normalizado = valor == null ? "" : Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
        return normalizado.substring(Numeros.CERO, Math.min(longitud, normalizado.length()));
    }

    private String normalizarNumero(String numero) {
        return numero == null ? "" : numero.trim().toUpperCase(Locale.ROOT);
    }
}
