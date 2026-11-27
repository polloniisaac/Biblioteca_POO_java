package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import com.fatec.biblioteca.Obra;
import com.fatec.biblioteca.dao.ObraDAO;

// RF11: relatório de livros disponíveis e emprestados.
public class FrmRelCirculacao extends JFrame {

    private JTable tabela;
    private Tabela modelo;
    private JTextArea txtResumo;
    private JButton btnAtualizar;

    private ObraDAO dao;

    public FrmRelCirculacao() {

        dao = new ObraDAO();

        setTitle("Relatório - Livros Disponíveis e Emprestados");
        setSize(850, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        modelo = new Tabela("Código", "Título", "Total", "Disponíveis", "Emprestados", "Reservados");
        tabela = new JTable(modelo);
        txtResumo = new JTextArea(6, 40);
        txtResumo.setEditable(false);
        btnAtualizar = new JButton("Atualizar");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(btnAtualizar);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(new JScrollPane(txtResumo), BorderLayout.CENTER);
        sul.add(botoes, BorderLayout.SOUTH);

        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);

        btnAtualizar.addActionListener(e -> carregarDados());

        carregarDados();

        Estilo.padronizar(this);
    }

    private void carregarDados() {
        try {
            modelo.setRowCount(0);
            List<Obra> lista = dao.listar(null);
            int total = 0;
            int disponiveis = 0;
            int emprestados = 0;
            int reservados = 0;
            StringBuilder semExemplar = new StringBuilder();
            for (Obra o : lista) {
                modelo.addRow(new Object[] {
                    o.getCodigo(), o.getTitulo(), o.getTotalCopias(), o.getQtdDisponiveis(),
                    o.getQtdEmprestadas(), o.getQtdReservadas()
                });
                total += o.getTotalCopias();
                disponiveis += o.getQtdDisponiveis();
                emprestados += o.getQtdEmprestadas();
                reservados += o.getQtdReservadas();
                if (o.getQtdDisponiveis() == 0) {
                    semExemplar.append("  ").append(o.getCodigo()).append(" - ").append(o.getTitulo()).append("\n");
                }
            }
            double percentual = total == 0 ? 0 : emprestados * 100.0 / total;
            txtResumo.setText("Exemplares: " + total + " | disponíveis: " + disponiveis + " | emprestados: "
                    + emprestados + " | separados para reserva: " + reservados + "\n"
                    + "Percentual do acervo emprestado: " + Mensagem.numero(percentual) + "%\n\n"
                    + "Títulos sem exemplar disponível:\n"
                    + (semExemplar.length() == 0 ? "  (nenhum)\n" : semExemplar.toString()));
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
