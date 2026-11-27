package com.fatec.biblioteca;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class ListaCobranca {

    private final LocalDate dataReferencia;
    private final List<Emprestimo> atrasados;

    public ListaCobranca(LocalDate dataReferencia, List<Emprestimo> atrasados) {
        this.dataReferencia = dataReferencia;
        this.atrasados = atrasados;
    }

    public String getNomeArquivo() {
        return "atrasos_" + dataReferencia + ".txt";
    }

    public int getQuantidade() {
        return atrasados.size();
    }

    public double getTotalMultas() {
        double total = 0;
        for (Emprestimo e : atrasados) {
            total += e.calcularMulta(dataReferencia);
        }
        return total;
    }

    public String gerarTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("BIBLIOTECA COMUNITÁRIA VILA LEITURA - EMPRÉSTIMOS EM ATRASO (")
          .append(Datas.formatar(dataReferencia)).append(")\n");
        sb.append(String.format("%-5s %-28s %-20s %-11s %-10s %-8s %8s%n",
                "Emp.", "Livro", "Leitor", "Tipo", "Previsto", "Atraso", "Multa"));
        for (Emprestimo e : atrasados) {
            sb.append(String.format("%-5d %-28s %-20s %-11s %-10s %-8s %8.2f%n",
                    e.getCodigo(), cortar(e.getObra().getTitulo(), 28), cortar(e.getLeitor().getNome(), 20),
                    e.getLeitor().getDescricaoTipo(), Datas.formatar(e.getDataPrevista()),
                    e.calcularDiasAtraso(dataReferencia) + " dias", e.calcularMulta(dataReferencia)));
        }
        sb.append(String.format("Empréstimos em atraso: %d | Total de multas previstas: R$ %.2f",
                getQuantidade(), getTotalMultas()));
        return sb.toString();
    }

    public String exportar() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getNomeArquivo(), StandardCharsets.UTF_8))) {
            for (String linha : gerarTexto().split("\n")) {
                bw.write(linha.replace("\r", ""));
                bw.newLine();
            }
        }
        return new java.io.File(getNomeArquivo()).getAbsolutePath();
    }

    private static String cortar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }
}
