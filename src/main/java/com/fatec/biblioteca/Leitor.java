package com.fatec.biblioteca;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Leitor extends Pessoa {

    public static final int ESTUDANTE = 1;
    public static final int PROFESSOR = 2;
    public static final int COMUNIDADE = 3;

    private List<Emprestimo> emprestimosAbertos = new ArrayList<>();

    private int qtdEmprestimosAbertos;

    public abstract int getTipo();

    public abstract int getPrazoDias();

    public abstract int getLimiteLivros();

    public static Leitor criar(int tipo) throws Exception {
        switch (tipo) {
            case ESTUDANTE:
                return new Estudante();
            case PROFESSOR:
                return new Professor();
            case COMUNIDADE:
                return new Comunidade();
            default:
                throw new Exception("Tipo de leitor deve ser de 1 a 3.");
        }
    }

    public static String[] nomesTipos() {
        return new String[] { "1 - Estudante", "2 - Professor", "3 - Comunidade" };
    }

    @Override
    public void setCodigo(int codigo) throws Exception {
        if (codigo < 1 || codigo > 9999) {
            throw new Exception("O código do leitor deve ser de 1 a 9999.");
        }
        super.setCodigo(codigo);
    }

    public LocalDate calcularDataPrevista(LocalDate dataEmprestimo) {
        return dataEmprestimo.plusDays(getPrazoDias());
    }

    public boolean atingiuLimite() {
        return getQtdEmprestimosAbertos() >= getLimiteLivros();
    }

    public Emprestimo getEmprestimoEmAtraso(LocalDate data) {
        for (Emprestimo e : emprestimosAbertos) {
            if (e.getDataPrevista().isBefore(data)) {
                return e;
            }
        }
        return null;
    }

    public boolean possuiLivroEmprestado(int codigoLivro) {
        for (Emprestimo e : emprestimosAbertos) {
            if (e.getCopia().getLivro().getCodigo() == codigoLivro) {
                return true;
            }
        }
        return false;
    }

    public void validarNovoEmprestimo(Livro livro, LocalDate data) throws Exception {
        if (atingiuLimite()) {
            throw new Exception("Limite de empréstimos simultâneos atingido para o tipo "
                    + getDescricaoTipo() + " (" + getSituacao() + " livros).");
        }
        Emprestimo atrasado = getEmprestimoEmAtraso(data);
        if (atrasado != null) {
            throw new Exception("O leitor possui empréstimo em atraso (nº " + atrasado.getCodigo()
                    + ", previsto para " + Datas.formatar(atrasado.getDataPrevista()) + ").\n"
                    + "Regularize a devolução antes de realizar novo empréstimo.");
        }
        if (possuiLivroEmprestado(livro.getCodigo())) {
            throw new Exception("O leitor já possui um empréstimo em aberto deste livro.");
        }
    }

    public void validarNovaReserva(Livro livro, LocalDate data) throws Exception {
        Emprestimo atrasado = getEmprestimoEmAtraso(data);
        if (atrasado != null) {
            throw new Exception("O leitor possui empréstimo em atraso (nº " + atrasado.getCodigo()
                    + ") e não pode fazer reservas.");
        }
        if (possuiLivroEmprestado(livro.getCodigo())) {
            throw new Exception("O leitor já está com este livro emprestado.");
        }
        if (livro.getReservaViva(this) != null) {
            throw new Exception("O leitor já possui uma reserva ativa deste livro.");
        }
        if (livro.getQtdDisponiveis() > 0) {
            throw new Exception("Há cópia disponível deste livro. Faça o empréstimo em vez da reserva.");
        }
    }

    public String getSituacao() {
        return getQtdEmprestimosAbertos() + " de " + getLimiteLivros();
    }

    public List<Emprestimo> getEmprestimosAbertos() {
        return emprestimosAbertos;
    }

    public void setEmprestimosAbertos(List<Emprestimo> emprestimosAbertos) {
        this.emprestimosAbertos = emprestimosAbertos;
        this.qtdEmprestimosAbertos = emprestimosAbertos.size();
    }

    public int getQtdEmprestimosAbertos() {
        return Math.max(qtdEmprestimosAbertos, emprestimosAbertos.size());
    }

    public void setQtdEmprestimosAbertos(int qtd) {
        this.qtdEmprestimosAbertos = qtd;
    }
}
