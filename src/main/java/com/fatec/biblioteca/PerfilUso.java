package com.fatec.biblioteca;

/**
 * Matriz de perfil de uso (Seção 7.3): linha = tipo de leitor, coluna = categoria.
 * Conta todos os empréstimos realizados (abertos e devolvidos).
 * Empate: vale a primeira categoria encontrada na varredura da linha/colunas.
 */
public class PerfilUso {

    public static final String[] NOMES_TIPOS = { "Estudante", "Professor", "Comunidade" };

    private final int[][] matriz = new int[3][5];

    public void incrementar(int tipoLeitor, int categoria, int quantidade) {
        matriz[tipoLeitor - 1][categoria - 1] += quantidade;
    }

    public int getValor(int linha, int coluna) {
        return matriz[linha][coluna];
    }

    public int totalLinha(int linha) {
        int soma = 0;
        for (int c = 0; c < 5; c++) {
            soma += matriz[linha][c];
        }
        return soma;
    }

    public int totalColuna(int coluna) {
        int soma = 0;
        for (int t = 0; t < 3; t++) {
            soma += matriz[t][coluna];
        }
        return soma;
    }

    public int totalGeral() {
        int soma = 0;
        for (int t = 0; t < 3; t++) {
            soma += totalLinha(t);
        }
        return soma;
    }

    /** Categoria mais emprestada por um tipo de leitor (índice da linha), ou null se não houver. */
    public Categoria categoriaPreferida(int linha) {
        int maior = 0;
        int indice = -1;
        for (int c = 0; c < 5; c++) {
            if (matriz[linha][c] > maior) {
                maior = matriz[linha][c];
                indice = c;
            }
        }
        return indice < 0 ? null : Categoria.values()[indice];
    }

    /** Categoria com maior soma de coluna, ou null se não houver empréstimos. */
    public Categoria categoriaMaisProcurada() {
        int maior = 0;
        int indice = -1;
        for (int c = 0; c < 5; c++) {
            if (totalColuna(c) > maior) {
                maior = totalColuna(c);
                indice = c;
            }
        }
        return indice < 0 ? null : Categoria.values()[indice];
    }
}
