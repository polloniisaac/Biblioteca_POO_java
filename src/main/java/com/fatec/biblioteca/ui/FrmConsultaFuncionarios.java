package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.fatec.biblioteca.Funcionario;
import com.fatec.biblioteca.dao.FuncionarioDAO;

public class FrmConsultaFuncionarios extends JFrame {

    private JTable tabela;
    private Tabela modelo;
    private JButton btnAtualizar;

    private FuncionarioDAO dao;

    public FrmConsultaFuncionarios() {

        dao = new FuncionarioDAO();

        setTitle("Lista de Funcionários");
        setSize(600, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        modelo = new Tabela("Código", "Nome", "Cargo", "Login");
        tabela = new JTable(modelo);
        btnAtualizar = new JButton("Atualizar");
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.add(btnAtualizar);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
        btnAtualizar.addActionListener(e -> carregarDados());

        carregarDados();
    }

    private void carregarDados() {
        try {
            modelo.setRowCount(0);
            for (Funcionario f : dao.listar()) {
                modelo.addRow(new Object[] { f.getCodigo(), f.getNome(), f.getDescricaoTipo(), f.getLogin() });
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
