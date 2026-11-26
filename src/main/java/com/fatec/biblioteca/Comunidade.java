package com.fatec.biblioteca;

/** Leitor do tipo Comunidade: prazo de 7 dias, até 1 livro(s) ao mesmo tempo. */
public class Comunidade extends Leitor {

    @Override
    public int getTipo() {
        return COMUNIDADE;
    }

    @Override
    public int getPrazoDias() {
        return 7;
    }

    @Override
    public int getLimiteLivros() {
        return 1;
    }

    @Override
    public String getDescricaoTipo() {
        return "Comunidade";
    }
}
