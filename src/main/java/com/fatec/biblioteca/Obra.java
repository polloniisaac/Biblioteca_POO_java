package com.fatec.biblioteca;

import java.util.ArrayList;
import java.util.List;

public class Obra {

    private int codigo;
    private String titulo;
    private String autor;
    private Categoria categoria;
    private int anoPublicacao;
    private final List<Copia> copias = new ArrayList<>();
    private final List<Reserva> filaReservas = new ArrayList<>();

    public void setCodigo(int codigo) throws Exception {
        if (codigo < 100 || codigo > 9999) {
            throw new Exception("O código do livro deve ser de 100 a 9999.");
        }
        this.codigo = codigo;
    }

    public void setTitulo(String titulo) throws Exception {
        this.titulo = Validador.textoObrigatorio(titulo, "Título");
    }

    public void setAutor(String autor) throws Exception {
        this.autor = Validador.textoObrigatorio(autor, "Autor");
    }

    public void setCategoria(Categoria categoria) throws Exception {
        if (categoria == null) {
            throw new Exception("Escolha a categoria.");
        }
        this.categoria = categoria;
    }

    public void setAnoPublicacao(int ano) throws Exception {
        if (ano < Constantes.ANO_MINIMO || ano > Constantes.anoAtual()) {
            throw new Exception("O ano de publicação deve estar entre " + Constantes.ANO_MINIMO
                    + " e " + Constantes.anoAtual() + ".");
        }
        this.anoPublicacao = ano;
    }

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
