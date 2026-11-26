package com.fatec.biblioteca;

import java.time.LocalDate;

/**
 * Constantes do sistema (Quadro 7.1 e "Constantes sugeridas" da especificação).
 * A política de prazo/limite por tipo de leitor NÃO fica aqui: ela está nas
 * subclasses de Leitor (polimorfismo).
 */
public final class Constantes {

    private Constantes() {
    }

    public static final int MAX_OBRAS = 200;        // RN14 (MAX_LIVROS)
    public static final int MAX_LEITORES = 100;     // RN14
    public static final int MAX_EMPRESTIMOS = 500;  // RN14

    public static final int EXEMPLARES_MIN = 1;     // RN04
    public static final int EXEMPLARES_MAX = 50;    // RN04

    public static final int ANO_MINIMO = 1450;      // RN03

    /** Multa por dia de atraso (RN12). Para mudar o valor, altere só aqui. */
    public static final double MULTA_POR_DIA = 0.50;

    /** Dias que o leitor tem para retirar uma cópia separada por reserva (regra R4). */
    public static final int PRAZO_RETIRADA_RESERVA = 3;

    /** Ano atual (ANO_ATUAL do PDF), lido do relógio do computador. */
    public static int anoAtual() {
        return LocalDate.now().getYear();
    }
}
