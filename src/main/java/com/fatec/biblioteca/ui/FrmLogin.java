package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.fatec.biblioteca.Funcionario;
import com.fatec.biblioteca.Sessao;
import com.fatec.biblioteca.dao.FuncionarioDAO;

public class FrmLogin extends JFrame {

    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnEntrar;
    private JButton btnSair;

    private FuncionarioDAO dao;

    public FrmLogin() {

        dao = new FuncionarioDAO();

        setTitle("Login - Biblioteca Vila Leitura");
        setSize(380, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel lblTitulo = new JLabel("Biblioteca Comunitária Vila Leitura", SwingConstants.CENTER);

        JPanel painel = new JPanel(new GridLayout(2, 2, 6, 6));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painel.add(new JLabel("Login"));
        txtLogin = new JTextField();
        painel.add(txtLogin);
        painel.add(new JLabel("Senha"));
        txtSenha = new JPasswordField();
        painel.add(txtSenha);

        btnEntrar = new JButton("Entrar");
        btnSair = new JButton("Sair");
        JPanel botoes = new JPanel();
        botoes.add(btnEntrar);
        botoes.add(btnSair);

        add(lblTitulo, BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnEntrar);

        btnEntrar.addActionListener(e -> entrar());
        btnSair.addActionListener(e -> System.exit(0));
    }

    private void entrar() {
        try {
            Funcionario f = dao.carregarPorLogin(txtLogin.getText());
            if (f == null || !f.autenticar(new String(txtSenha.getPassword()))) {
                throw new Exception("Login ou senha inválidos.");
            }
            Sessao.setFuncionario(f);
            var tela = new FrmPrincipal();
            tela.setVisible(true);
            dispose();
        } catch (Exception err) {
            txtSenha.setText("");
            Mensagem.erro(this, err);
        }
    }
}
