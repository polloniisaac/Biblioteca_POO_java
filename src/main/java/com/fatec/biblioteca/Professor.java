package com.fatec.biblioteca;

public class Professor extends Leitor {

    @Override
    public int getTipo() {
        return PROFESSOR;
    }

    @Override
    public int getPrazoDias() {
        return 14;
    }

    @Override
    public int getLimiteLivros() {
        return 4;
    }

    @Override
    public String getDescricaoTipo() {
        return "Professor";
    }
}
