package com.fatec.biblioteca;

import java.util.ArrayList;
import java.util.List;

/**
 * Obra (título) do acervo: o "livro" da especificação.
 * Composição: a obra possui suas cópias (List<Copia>).
 * Agregação: a obra guarda a fila de reservas vivas (List<Reserva>).
 * Total e disponíveis do PDF são calculados a partir das cópias.
 */
public class Obra {

    private int codigo;
    private String titulo;
    private String autor;
    private Categoria categoria;
    private int anoPublicacao;
    private final List<Copia> copias = new ArrayList<>();
    private final List<Reserva> filaReservas = new ArrayList<>();

    // ---------- validações nos setters (estilo poo12) ----------

    /** RN01: código de 100 a 9999. */
    public void setCodigo(int codigo) throws Exception {
        if (codigo < 100 || codigo > 9999) {
            throw new Exception("O código do livro deve ser de 100 a 9999.");
        }
        this.codigo = codigo;
    }

    /** RN02. */
    public void setTitulo(String titulo) throws Exception {
        this.titulo = Validador.textoObrigatorio(titulo, "Título");
    }

    /** RN02. */
    public void setAutor(String autor) throws Exception {
        this.autor = Validador.textoObrigatorio(autor, "Autor");
    }

    /** RN03. */
    public void setCategoria(Categoria categoria) throws Exception {
        if (categoria == null) {
            throw new Exception("Escolha a categoria.");
        }
        this.categoria = categoria;
    }

    /** RN03: ano entre 1450 e o ano atual. */
    public void setAnoPublicacao(int ano) throws Exception {
        if (ano < Constantes.ANO_MINIMO || ano > Constantes.anoAtual()) {
            throw new Exception("O ano de publicação deve estar entre " + Constantes.ANO_MINIMO
                    + " e " + Constantes.anoAtual() + ".");
        }
        this.anoPublicacao = ano;
    }

    // ---------- cópias (composição) ----------

    /**
     * RN04 / RF01 / RF02: cria de 1 a 50 cópias novas, numeradas na sequência.
     * Devolve só as cópias novas, para o DAO gravar.
     */
    public List<Copia> adicionarCopias(int quantidade) throws Exception {
        if (quantidade < Constantes.EXEMPLARES_MIN || quantidade > Constantes.EXEMPLARES_MAX) {
            throw new Exception("A quantidade de exemplares deve ser de " + Constantes.EXEMPLARES_MIN
                    + " a " + Constantes.EXEMPLARES_MAX + ".");
        }
        int proximo = 1;
        for (Copia c : copias) {
            proximo = Math.max(proximo, c.getNumero() + 1);
        }
        List<Copia> novas = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Copia c = new Copia(this, proximo + i);
            copias.add(c);
            novas.add(c);
        }
        return novas;
    }

    /** Usado pelo DAO ao carregar as cópias do banco. */
    public void adicionarCopiaCarregada(Copia copia) {
        copia.setObra(this);
        copias.add(copia);
    }

    public int getTotalCopias() {
        return copias.size();
    }

    public int getQtdDisponiveis() {
        return contar(SituacaoCopia.DISPONIVEL);
    }

    public int getQtdEmprestadas() {
        return contar(SituacaoCopia.EMPRESTADA);
    }

    public int getQtdReservadas() {
        return contar(SituacaoCopia.RESERVADA);
    }

    private int contar(SituacaoCopia s) {
        int n = 0;
        for (Copia c : copias) {
            if (c.getSituacao() == s) {
                n++;
            }
        }
        return n;
    }

    public Copia getCopia(int codigoCopia) {
        for (Copia c : copias) {
            if (c.getCodigo() == codigoCopia) {
                return c;
            }
        }
        return null;
    }

    /**
     * RN06 + reserva (R5): se o leitor tem uma cópia separada para ele, é ela;
     * senão, a primeira cópia disponível. Sem nenhuma, lança exceção.
     */
    public Copia getCopiaParaEmprestimo(Leitor leitor) throws Exception {
        Reserva r = getReservaAguardando(leitor);
        if (r != null && r.getCopiaSeparada() != null) {
            return r.getCopiaSeparada();
        }
        for (Copia c : copias) {
            if (c.getSituacao() == SituacaoCopia.DISPONIVEL) {
                return c;
            }
        }
        String extra = getQtdReservadas() > 0 ? " (há cópias separadas para reservas de outros leitores)" : "";
        throw new Exception("Não há exemplares disponíveis deste livro" + extra + ".\n"
                + "Se quiser, faça uma reserva.");
    }

    // ---------- reservas (agregação) ----------

    public List<Reserva> getFilaReservas() {
        return filaReservas;
    }

    public Reserva getReservaViva(Leitor leitor) {
        for (Reserva r : filaReservas) {
            if (r.getLeitor().getCodigo() == leitor.getCodigo() && r.getSituacao().isViva()) {
                return r;
            }
        }
        return null;
    }

    public Reserva getReservaAguardando(Leitor leitor) {
        Reserva r = getReservaViva(leitor);
        return (r != null && r.getSituacao() == SituacaoReserva.AGUARDANDO) ? r : null;
    }

    /** Posição (1, 2, 3...) da reserva na fila, ou 0 se não estiver nela. */
    public int getPosicaoNaFila(Reserva reserva) {
        int pos = 0;
        for (Reserva r : filaReservas) {
            if (r.getSituacao().isViva()) {
                pos++;
                if (r.getCodigo() == reserva.getCodigo()) {
                    return pos;
                }
            }
        }
        return 0;
    }

    // ---------- getters ----------

    public int getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public int getAnoPublicacao() {
        return anoPublicacao;
    }

    public List<Copia> getCopias() {
        return copias;
    }

    @Override
    public String toString() {
        return codigo + " - " + titulo;
    }
}
