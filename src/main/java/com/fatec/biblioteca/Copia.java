package com.fatec.biblioteca;

// Parte do Livro (composição); a situação só muda pelos métodos (RN15).
public class Copia {

    private int codigo;
    private Livro livro;
    private int numero;
    private SituacaoCopia situacao = SituacaoCopia.DISPONIVEL;

    public Copia() {
    }

    Copia(Livro livro, int numero) {
        this.livro = livro;
        this.numero = numero;
    }

    public void emprestar() throws Exception {
        if (situacao == SituacaoCopia.EMPRESTADA) {
            throw new Exception("A cópia " + getEtiqueta() + " já está emprestada.");
        }
        situacao = SituacaoCopia.EMPRESTADA;
    }

    public void devolver() throws Exception {
        if (situacao != SituacaoCopia.EMPRESTADA) {
            throw new Exception("A cópia " + getEtiqueta() + " não está emprestada.");
        }
        situacao = SituacaoCopia.DISPONIVEL;
    }

    public void reservar() throws Exception {
        if (situacao != SituacaoCopia.DISPONIVEL) {
            throw new Exception("A cópia " + getEtiqueta() + " não está disponível para reserva.");
        }
        situacao = SituacaoCopia.RESERVADA;
    }

    public void liberar() {
        if (situacao == SituacaoCopia.RESERVADA) {
            situacao = SituacaoCopia.DISPONIVEL;
        }
    }

    public String getEtiqueta() {
        return (livro == null ? "?" : String.valueOf(livro.getCodigo())) + "-" + numero;
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

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public SituacaoCopia getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoCopia situacao) {
        this.situacao = situacao;
    }
}
