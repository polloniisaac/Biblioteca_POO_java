package com.fatec.biblioteca;

import java.time.LocalDate;

// Associação com Livro, Leitor e Funcionario.
public class Reserva {

    private int codigo;
    private Livro livro;
    private Leitor leitor;
    private Funcionario funcionario;
    private LocalDate dataReserva;
    private Copia copiaSeparada;
    private LocalDate dataLimiteRetirada;
    private int codigoEmprestimo;
    private SituacaoReserva situacao = SituacaoReserva.ATIVA;

    public Reserva() {
    }

    public Reserva(Livro livro, Leitor leitor, Funcionario funcionario, LocalDate data) throws Exception {
        if (data == null) {
            throw new Exception("Informe a data da reserva.");
        }
        this.livro = livro;
        this.leitor = leitor;
        this.funcionario = funcionario;
        this.dataReserva = data;
    }

    // R4: separa a cópia para o leitor retirar
    public void separarCopia(Copia copia, LocalDate data) throws Exception {
        if (situacao != SituacaoReserva.ATIVA) {
            throw new Exception("Só é possível separar cópia para reserva ativa.");
        }
        copia.reservar();
        this.copiaSeparada = copia;
        this.dataLimiteRetirada = data.plusDays(Constantes.PRAZO_RETIRADA_RESERVA);
        this.situacao = SituacaoReserva.AGUARDANDO;
    }

    // R6: passou do prazo de retirada?
    public boolean prazoVencido(LocalDate data) {
        return situacao == SituacaoReserva.AGUARDANDO && data.isAfter(dataLimiteRetirada);
    }

    // R6: expira e libera a cópia.
    public void expirar() {
        if (copiaSeparada != null) {
            copiaSeparada.liberar();
        }
        situacao = SituacaoReserva.EXPIRADA;
    }

    // R7.
    public void cancelar() throws Exception {
        if (!situacao.isViva()) {
            throw new Exception("A reserva " + codigo + " não está ativa (situação: " + situacao + ").");
        }
        if (copiaSeparada != null) {
            copiaSeparada.liberar();
        }
        situacao = SituacaoReserva.CANCELADA;
    }

    // R5: o leitor retirou a cópia separada.
    public void atender(Emprestimo emprestimo) {
        situacao = SituacaoReserva.ATENDIDA;
        codigoEmprestimo = emprestimo.getCodigo();
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public void setLeitor(Leitor leitor) {
        this.leitor = leitor;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public LocalDate getDataReserva() {
        return dataReserva;
    }

    public void setDataReserva(LocalDate dataReserva) {
        this.dataReserva = dataReserva;
    }

    public Copia getCopiaSeparada() {
        return copiaSeparada;
    }

    public void setCopiaSeparada(Copia copiaSeparada) {
        this.copiaSeparada = copiaSeparada;
    }

    public LocalDate getDataLimiteRetirada() {
        return dataLimiteRetirada;
    }

    public void setDataLimiteRetirada(LocalDate d) {
        this.dataLimiteRetirada = d;
    }

    public int getCodigoEmprestimo() {
        return codigoEmprestimo;
    }

    public void setCodigoEmprestimo(int codigoEmprestimo) {
        this.codigoEmprestimo = codigoEmprestimo;
    }

    public SituacaoReserva getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoReserva situacao) {
        this.situacao = situacao;
    }
}
