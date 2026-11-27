package com.fatec.biblioteca;

public final class Sessao {

    private static Funcionario funcionario;

    private Sessao() {
    }

    public static Funcionario getFuncionario() {
        return funcionario;
    }

    public static void setFuncionario(Funcionario f) {
        funcionario = f;
    }
}
