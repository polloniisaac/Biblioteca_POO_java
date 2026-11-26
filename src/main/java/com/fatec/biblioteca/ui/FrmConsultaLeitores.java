package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.fatec.biblioteca.Leitor;
import com.fatec.biblioteca.dao.LeitorDAO;

/** RF07: listar leitores com "empréstimos em aberto de limite". */
public class FrmConsultaLeitores extends JFrame {

    private JTable tabela;
    private Tabela modelo;
    private JLabel lblTotal;
    private JButton btnAtualizar;

    private LeitorDAO dao;

    public FrmConsultaLeitores() {

        dao = new LeitorDAO();

        setTitle("Lista de Leitores");
        setSize(750, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        criarComponentes();

        carregarDados();
    }

    private void criarComponentes() {
        modelo = new Tabela("Código", "Nome", "Tipo", "Prazo (dias)", "Empréstimos em aberto");
        tabela = new JTable(modelo);
        tabela.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(tabela);

        lblTotal = new JLabel();
        btnAtualizar = new JButton("Atualizar");
        JPanel painelBotoes = new JPanel(new BorderLayout());
        painelBotoes.add(lblTotal, BorderLayout.WEST);
        painelBotoes.add(btnAtualizar, BorderLayout.EAST);

        add(scroll, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        btnAtualizar.addActionListener(e -> carregarDados());
    }

    private void carregarDados() {
        try {
            modelo.setRowCount(0);
            List<Leitor> lista = dao.listar();
            for (Leitor l : lista) {
                modelo.addRow(new Object[] {
                    l.getCodigo(),
                    l.getNome(),
                    l.getDescricaoTipo(),
                    l.getPrazoDias(),
                    l.getSituacao()
                });
            }
            lblTotal.setText(" Total de leitores: " + lista.size());
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
