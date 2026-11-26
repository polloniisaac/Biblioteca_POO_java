package com.fatec.biblioteca;

/** Leitor do tipo Professor: prazo de 14 dias, até 4 livro(s) ao mesmo tempo. */
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
