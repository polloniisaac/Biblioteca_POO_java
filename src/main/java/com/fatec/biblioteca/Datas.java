package com.fatec.biblioteca;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class Datas {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Datas() {
    }

    public static LocalDate converter(String texto) throws Exception {
        if (texto == null || texto.isBlank()) {
            throw new Exception("Informe a data (dd/mm/aaaa).");
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO);
        } catch (DateTimeParseException e) {
            throw new Exception("Data inválida: " + texto + ". Use o formato dd/mm/aaaa.");
        }
    }

    public static String formatar(LocalDate data) {
        return data == null ? "" : data.format(FORMATO);
    }

    public static String hoje() {
        return formatar(LocalDate.now());
    }
}
