package com.fatec.biblioteca;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Empréstimo de uma cópia. Associação com Copia, Leitor e Funcionario.
 * Data de devolução null = empréstimo em aberto (era 0 no PDF).
 */
public class Emprestimo {

    private int codigo;
    private Copia copia;
    private Leitor leitor;
    private Funcionario funcionarioEmprestimo;
    private Funcionario funcionarioDevolucao;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevista;
    private LocalDate dataDevolucao;
    private double multa;

    /** Usado pelo DAO. */
    public Emprestimo() {
    }

    /** Novo empréstimo: a data prevista vem do tipo do leitor (polimorfismo, RN10). */
    public Emprestimo(Copia copia, Leitor leitor, Funcionario funcionario, LocalDate dataEmprestimo)
            throws Exception {
        if (dataEmprestimo == null) {
            throw new Exception("Informe a data do empréstimo.");
        }
        copia.emprestar();
        this.copia = copia;
        this.leitor = leitor;
        this.funcionarioEmprestimo = funcionario;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevista = leitor.calcularDataPrevista(dataEmprestimo);
    }

    public boolean estaAberto() {
        return dataDevolucao == null;
    }

    /** RN12 usando a data da devolução (empréstimo já devolvido). */
    public int calcularDiasAtraso() {
        return dataDevolucao == null ? 0 : calcularDiasAtraso(dataDevolucao);
    }

    /** RN12 usando uma data de referência (sobrecarga). */
    public int calcularDiasAtraso(LocalDate dataReferencia) {
        long dias = ChronoUnit.DAYS.between(dataPrevista, dataReferencia);
        return dias > 0 ? (int) dias : 0;
    }

    /**
     * RN12: multa = dias de atraso x R$ 0,50.
     * Único lugar do cálculo: para mudar a regra da multa, altere aqui.
     */
    public double calcularMulta(LocalDate dataReferencia) {
        return calcularDiasAtraso(dataReferencia) * Constantes.MULTA_POR_DIA;
    }

    /** RN11/RN12: registra a devolução e calcula a multa. */
    public void registrarDevolucao(LocalDate data, Funcionario funcionario) throws Exception {
        if (!estaAberto()) {
            throw new Exception("O empréstimo " + codigo + " já foi devolvido em "
                    + Datas.formatar(dataDevolucao) + ".");
        }
        if (data == null) {
            throw new Exception("Informe a data da devolução.");
        }
        if (data.isBefore(dataEmprestimo)) {
            throw new Exception("A data da devolução não pode ser anterior à do empréstimo ("
                    + Datas.formatar(dataEmprestimo) + ").");
        }
        copia.devolver();
        this.dataDevolucao = data;
        this.funcionarioDevolucao = funcionario;
        this.multa = calcularMulta(data);
    }

    /** RF10: "NO PRAZO" ou "ATRASADO há N dias". */
    public String getStatus(LocalDate dataReferencia) {
        int atraso = calcularDiasAtraso(dataReferencia);
        return atraso == 0 ? "NO PRAZO" : "ATRASADO há " + atraso + " dia(s)";
    }

    public Obra getObra() {
        return copia.getObra();
    }

    // ---------- getters / setters (usados pelo DAO) ----------

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Copia getCopia() {
        return copia;
    }

    public void setCopia(Copia copia) {
        this.copia = copia;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public void setLeitor(Leitor leitor) {
        this.leitor = leitor;
    }

    public Funcionario getFuncionarioEmprestimo() {
        return funcionarioEmprestimo;
    }

    public void setFuncionarioEmprestimo(Funcionario f) {
        this.funcionarioEmprestimo = f;
    }

    public Funcionario getFuncionarioDevolucao() {
        return funcionarioDevolucao;
    }

    public void setFuncionarioDevolucao(Funcionario f) {
        this.funcionarioDevolucao = f;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDate d) {
        this.dataEmprestimo = d;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDate d) {
        this.dataPrevista = d;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDate d) {
        this.dataDevolucao = d;
    }

    public double getMulta() {
        return multa;
    }

    public void setMulta(double multa) {
        this.multa = multa;
    }
}
