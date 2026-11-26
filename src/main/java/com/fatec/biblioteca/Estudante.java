package com.fatec.biblioteca;

/** Leitor do tipo Estudante: prazo de 7 dias, até 2 livro(s) ao mesmo tempo. */
public class Estudante extends Leitor {

    @Override
    public int getTipo() {
        return ESTUDANTE;
    }

    @Override
    public int getPrazoDias() {
        return 7;
    }

    @Override
    public int getLimiteLivros() {
        return 2;
    }

    @Override
    public String getDescricaoTipo() {
        return "Estudante";
    }
}
