package com.fatec.biblioteca;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Leitor da biblioteca. Classe abstrata: cada tipo (Estudante, Professor,
 * Comunidade) define o próprio prazo e limite (polimorfismo), substituindo os
 * arrays PRAZO_POR_TIPO e LIMITE_POR_TIPO da especificação.
 *
 * Agregação: o leitor guarda a lista dos seus empréstimos em aberto. Os
 * empréstimos existem por conta própria (continuam no banco depois de devolvidos).
 */
public abstract class Leitor extends Pessoa {

    public static final int ESTUDANTE = 1;
    public static final int PROFESSOR = 2;
    public static final int COMUNIDADE = 3;

    private List<Emprestimo> emprestimosAbertos = new ArrayList<>();
    /** Usado nas listagens, quando a lista completa não foi carregada. */
    private int qtdEmprestimosAbertos;

    // ---------- métodos abstratos (polimorfismo) ----------

    /** 1 = Estudante, 2 = Professor, 3 = Comunidade. */
    public abstract int getTipo();

    /** Prazo de devolução em dias. */
    public abstract int getPrazoDias();

    /** Quantidade máxima de empréstimos simultâneos. */
    public abstract int getLimiteLivros();

    // ---------- fábrica ----------

    /** Cria a subclasse certa a partir do tipo gravado no banco ou escolhido na tela (RN05). */
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

    // ---------- regras ----------

    /** RN05: código de 1 a 9999. */
    @Override
    public void setCodigo(int codigo) throws Exception {
        if (codigo < 1 || codigo > 9999) {
            throw new Exception("O código do leitor deve ser de 1 a 9999.");
        }
        super.setCodigo(codigo);
    }

    /** RN10: data prevista = data do empréstimo + prazo do tipo. */
    public LocalDate calcularDataPrevista(LocalDate dataEmprestimo) {
        return dataEmprestimo.plusDays(getPrazoDias());
    }

    public boolean atingiuLimite() {
        return getQtdEmprestimosAbertos() >= getLimiteLivros();
    }

    /** RN08: primeiro empréstimo em aberto com data prevista anterior à data informada, ou null. */
    public Emprestimo getEmprestimoEmAtraso(LocalDate data) {
        for (Emprestimo e : emprestimosAbertos) {
            if (e.getDataPrevista().isBefore(data)) {
                return e;
            }
        }
        return null;
    }

    /** RN09: o leitor já tem empréstimo em aberto desta obra? */
    public boolean possuiObraEmprestada(int codigoObra) {
        for (Emprestimo e : emprestimosAbertos) {
            if (e.getCopia().getObra().getCodigo() == codigoObra) {
                return true;
            }
        }
        return false;
    }

    /**
     * Regras do leitor para um novo empréstimo (RN07, RN08, RN09).
     * A disponibilidade da obra (RN06) é verificada pela própria Obra.
     */
    public void validarNovoEmprestimo(Obra obra, LocalDate data) throws Exception {
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
        if (possuiObraEmprestada(obra.getCodigo())) {
            throw new Exception("O leitor já possui um empréstimo em aberto desta obra.");
        }
    }

    /** Regras R1–R3 da reserva, do lado do leitor e da obra. */
    public void validarNovaReserva(Obra obra, LocalDate data) throws Exception {
        Emprestimo atrasado = getEmprestimoEmAtraso(data);
        if (atrasado != null) {
            throw new Exception("O leitor possui empréstimo em atraso (nº " + atrasado.getCodigo()
                    + ") e não pode fazer reservas.");
        }
        if (possuiObraEmprestada(obra.getCodigo())) {
            throw new Exception("O leitor já está com esta obra emprestada.");
        }
        if (obra.getReservaViva(this) != null) {
            throw new Exception("O leitor já possui uma reserva ativa desta obra.");
        }
        if (obra.getQtdDisponiveis() > 0) {
            throw new Exception("Há cópia disponível desta obra. Faça o empréstimo em vez da reserva.");
        }
    }

    /** RF07: "1 de 2". */
    public String getSituacao() {
        return getQtdEmprestimosAbertos() + " de " + getLimiteLivros();
    }

    // ---------- getters / setters ----------

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
