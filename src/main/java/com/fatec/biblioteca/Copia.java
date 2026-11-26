package com.fatec.biblioteca;

/**
 * Exemplar físico de uma obra. Faz parte da Obra (composição): só é criado
 * por Obra.adicionarCopias(). A situação só muda pelos métodos abaixo
 * (encapsulamento), o que garante a RN15.
 */
public class Copia {

    private int codigo;
    private Obra obra;
    private int numero;
    private SituacaoCopia situacao = SituacaoCopia.DISPONIVEL;

    /** Usado pelo DAO. */
    public Copia() {
    }

    Copia(Obra obra, int numero) {
        this.obra = obra;
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

    /** Separa a cópia para uma reserva. */
    public void reservar() throws Exception {
        if (situacao != SituacaoCopia.DISPONIVEL) {
            throw new Exception("A cópia " + getEtiqueta() + " não está disponível para reserva.");
        }
        situacao = SituacaoCopia.RESERVADA;
    }

    /** Desfaz a separação (reserva cancelada ou expirada). */
    public void liberar() {
        if (situacao == SituacaoCopia.RESERVADA) {
            situacao = SituacaoCopia.DISPONIVEL;
        }
    }

    /** Ex.: "101-2" (código da obra - número da cópia). */
    public String getEtiqueta() {
        return (obra == null ? "?" : String.valueOf(obra.getCodigo())) + "-" + numero;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Obra getObra() {
        return obra;
    }

    public void setObra(Obra obra) {
        this.obra = obra;
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

    /** Usado pelo DAO ao carregar do banco. */
    public void setSituacao(SituacaoCopia situacao) {
        this.situacao = situacao;
    }
}
