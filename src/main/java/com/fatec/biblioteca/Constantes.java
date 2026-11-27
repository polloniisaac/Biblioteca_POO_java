package com.fatec.biblioteca;

import java.time.LocalDate;

// Prazo e limite por tipo de leitor ficam nas subclasses de Leitor (polimorfismo).
public final class Constantes {

    private Constantes() {
    }

    public static final int MAX_OBRAS = 200;        // RN14 (MAX_LIVROS)
    public static final int MAX_LEITORES = 100;     // RN14
    public static final int MAX_EMPRESTIMOS = 500;  // RN14

    public static final int EXEMPLARES_MIN = 1;     // RN04
    public static final int EXEMPLARES_MAX = 50;    // RN04

    public static final int ANO_MINIMO = 1450;      // RN03

    // RN12: multa por dia de atraso
    public static final double MULTA_POR_DIA = 0.50;

    // R4: dias para retirar a cópia reservada
    public static final int PRAZO_RETIRADA_RESERVA = 3;

    public static int anoAtual() {
        return LocalDate.now().getYear();
    }
}
