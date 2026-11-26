package com.fatec.biblioteca.ui;

import java.awt.Component;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Locale;

import javax.swing.JOptionPane;

/**
 * Tratamento de erros na camada de UI (requisito do professor): toda tela
 * captura as exceções e chama estes métodos para mostrar a mensagem.
 */
public final class Mensagem {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    private Mensagem() {
    }

    public static void info(Component tela, String texto) {
        JOptionPane.showMessageDialog(tela, texto);
    }

    public static void erro(Component tela, Exception e) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            JOptionPane.showMessageDialog(tela, "O banco recusou a operação (registro duplicado ou em uso).\n"
                    + e.getMessage(), "Erro no banco de dados", JOptionPane.ERROR_MESSAGE);
        } else if (e instanceof SQLException) {
            JOptionPane.showMessageDialog(tela, "Erro no banco de dados. Os dados não foram gravados.\n"
                    + e.getMessage(), "Erro no banco de dados", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(tela, e.getMessage(), "Verifique!", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static boolean confirmar(Component tela, String texto) {
        return JOptionPane.showConfirmDialog(tela, texto, "Confirmação",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static String moeda(double valor) {
        return String.format(BR, "R$ %.2f", valor);
    }

    public static String numero(double valor) {
        return String.format(BR, "%.2f", valor);
    }
}
