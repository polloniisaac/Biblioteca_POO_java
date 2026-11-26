package com.fatec.biblioteca;

/** Categorias do acervo (domínio fixo do Quadro 7.1). */
public enum Categoria {
    LITERATURA(1, "Literatura"),
    DIDATICO(2, "Didático"),
    TECNICO(3, "Técnico"),
    INFANTOJUVENIL(4, "Infantojuvenil"),
    OUTROS(5, "Outros");

    private final int codigo;
    private final String nome;

    Categoria(int codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    /** Índice da coluna na matriz de perfil de uso (categoria - 1). */
    public int getIndice() {
        return codigo - 1;
    }

    public static Categoria fromCodigo(int codigo) throws Exception {
        for (Categoria c : values()) {
            if (c.codigo == codigo) {
                return c;
            }
        }
        throw new Exception("Categoria deve ser de 1 a 5.");
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}
