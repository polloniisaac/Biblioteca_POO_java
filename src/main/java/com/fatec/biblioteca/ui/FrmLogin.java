package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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
        setSize(420, 340);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new PainelFundo(new BorderLayout()));

        JPanel painel = new JPanel(new GridLayout(4, 1, 4, 4));
        painel.setBorder(BorderFactory.createEmptyBorder(18, 28, 8, 28));
        painel.setOpaque(false);
        painel.add(new JLabel("Login"));
        txtLogin = new JTextField();
        txtLogin.putClientProperty("JTextField.placeholderText", "Digite seu login");
        painel.add(txtLogin);
        painel.add(new JLabel("Senha"));
        txtSenha = new JPasswordField();
        txtSenha.putClientProperty("JTextField.placeholderText", "Digite sua senha");
        txtSenha.putClientProperty("FlatLaf.style", "showRevealButton: true");
        painel.add(txtSenha);

        btnEntrar = new JButton("Entrar");
        btnSair = new JButton("Sair");
        Estilo.primario(btnEntrar);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setBorder(BorderFactory.createEmptyBorder(8, 28, 20, 28));
        botoes.setOpaque(false);
        botoes.add(btnSair);
        botoes.add(btnEntrar);

        add(Estilo.cabecalho("Biblioteca Comunitária Vila Leitura", "Acesso dos funcionários"),
                BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnEntrar);
        setLocationRelativeTo(null);

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
