package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.fatec.biblioteca.Funcionario;
import com.fatec.biblioteca.Sessao;
import com.fatec.biblioteca.dao.FuncionarioDAO;

public class FrmAlterarSenha extends JFrame {

    private JPasswordField txtAtual;
    private JPasswordField txtNova;
    private JPasswordField txtConfirma;
    private JButton btnSalvar;

    private FuncionarioDAO dao;

    public FrmAlterarSenha() {

        dao = new FuncionarioDAO();

        setTitle("Alterar minha senha");
        setSize(420, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel(new GridLayout(3, 2, 6, 6));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        painel.add(new JLabel("Senha atual"));
        txtAtual = new JPasswordField();
        painel.add(txtAtual);
        painel.add(new JLabel("Nova senha"));
        txtNova = new JPasswordField();
        painel.add(txtNova);
        painel.add(new JLabel("Confirme a nova senha"));
        txtConfirma = new JPasswordField();
        painel.add(txtConfirma);

        btnSalvar = new JButton("Salvar");
        JPanel botoes = new JPanel();
        botoes.add(btnSalvar);

        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> salvar());

        Estilo.padronizar(this);
    }

    private void salvar() {
        try {
            Funcionario f = dao.carregar(Sessao.getFuncionario().getCodigo());
            if (!f.autenticar(new String(txtAtual.getPassword()))) {
                throw new Exception("Senha atual incorreta.");
            }
            String nova = new String(txtNova.getPassword());
            if (!nova.equals(new String(txtConfirma.getPassword()))) {
                throw new Exception("A confirmação não confere com a nova senha.");
            }
            f.setSenha(nova);
            dao.alterarSenha(f.getCodigo(), f.getSenhaHash());
            Mensagem.info(this, "Senha alterada com sucesso!");
            dispose();
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
