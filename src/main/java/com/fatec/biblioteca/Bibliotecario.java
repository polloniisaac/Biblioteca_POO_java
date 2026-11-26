package com.fatec.biblioteca;

/** Bibliotecário: pode tudo, inclusive cadastrar funcionários. */
public class Bibliotecario extends Funcionario {

    @Override
    public int getCargo() {
        return BIBLIOTECARIO;
    }

    @Override
    public boolean podeGerenciarCadastros() {
        return true;
    }

    @Override
    public String getDescricaoTipo() {
        return "Bibliotecário";
    }
}
