package com.fatec.biblioteca;

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
