package com.fatec.biblioteca;

/** Voluntário: faz circulação, consultas e relatórios, mas não gerencia funcionários. */
public class Voluntario extends Funcionario {

    @Override
    public int getCargo() {
        return VOLUNTARIO;
    }

    @Override
    public boolean podeGerenciarCadastros() {
        return false;
    }

    @Override
    public String getDescricaoTipo() {
        return "Voluntário";
    }
}
