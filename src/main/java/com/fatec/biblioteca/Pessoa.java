package com.fatec.biblioteca;

// Superclasse abstrata de Leitor e Funcionario (herança).
public abstract class Pessoa {

    private int codigo;
    private String nome;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) throws Exception {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    // RN02/RN05 e V4.10: nome obrigatório e sem ";".
    public void setNome(String nome) throws Exception {
        this.nome = Validador.textoObrigatorio(nome, "Nome");
    }

    public abstract String getDescricaoTipo();

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}
