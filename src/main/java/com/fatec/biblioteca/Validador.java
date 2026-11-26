package com.fatec.biblioteca;

/** Validações de texto reaproveitadas pelas entidades. */
public final class Validador {

    private Validador() {
    }

    /** Não vazio, não só espaços (RN02) e sem ";" (V4.10). Devolve o texto sem espaços nas pontas. */
    public static String textoObrigatorio(String texto, String campo) throws Exception {
        if (texto == null || texto.isBlank()) {
            throw new Exception(campo + " é obrigatório.");
        }
        if (texto.contains(";")) {
            throw new Exception(campo + " não pode conter o caractere \";\".");
        }
        return texto.trim();
    }

    public static int inteiro(String texto, String campo) throws Exception {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new Exception(campo + " deve ser um número inteiro.");
        }
    }
}
