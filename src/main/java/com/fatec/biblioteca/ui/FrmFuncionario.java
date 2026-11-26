package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.fatec.biblioteca.Funcionario;
import com.fatec.biblioteca.Sessao;
import com.fatec.biblioteca.Validador;
import com.fatec.biblioteca.dao.FuncionarioDAO;

/** Cadastro de funcionários (só para Bibliotecário). */
public class FrmFuncionario extends JFrame {

    private JTextField txtCodigo;
    private JTextField txtNome;
    private JComboBox<String> cbCargo;
    private JTextField txtLogin;
    private JPasswordField txtSenha;

    private JButton btnConsultar;
    private JButton btnInserir;
    private JButton btnAlterar;
    private JButton btnExcluir;
    private JButton btnLimpar;
    private JButton btnListar;

    private FuncionarioDAO dao;

    public FrmFuncionario() {

        dao = new FuncionarioDAO();

        setTitle("Cadastro de Funcionários");
        setSize(560, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel(new GridLayout(5, 2, 6, 6));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        painel.add(new JLabel("Código (automático)"));
        txtCodigo = new JTextField();
        painel.add(txtCodigo);

        painel.add(new JLabel("Nome"));
        txtNome = new JTextField();
        painel.add(txtNome);

        painel.add(new JLabel("Cargo"));
        cbCargo = new JComboBox<>(Funcionario.nomesCargos());
        painel.add(cbCargo);

        painel.add(new JLabel("Login"));
        txtLogin = new JTextField();
        painel.add(txtLogin);

        painel.add(new JLabel("Senha (em Alterar, vazio = manter)"));
        txtSenha = new JPasswordField();
        painel.add(txtSenha);

        btnConsultar = new JButton("Consultar");
        btnInserir = new JButton("Inserir");
        btnAlterar = new JButton("Alterar");
        btnExcluir = new JButton("Excluir");
        btnLimpar = new JButton("Limpar");
        btnListar = new JButton("Listar");

        JPanel botoes = new JPanel();
        botoes.add(btnConsultar);
        botoes.add(btnInserir);
        botoes.add(btnAlterar);
        botoes.add(btnExcluir);
        botoes.add(btnLimpar);
        botoes.add(btnListar);

        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        configurarEventos();
    }

    private void configurarEventos() {
        btnInserir.addActionListener(e -> inserir());
        btnConsultar.addActionListener(e -> consultar());
        btnAlterar.addActionListener(e -> alterar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limparCampos());
        btnListar.addActionListener(e -> new FrmConsultaFuncionarios().setVisible(true));
    }

    private Funcionario lerCampos(boolean senhaObrigatoria) throws Exception {
        Funcionario f = Funcionario.criar(cbCargo.getSelectedIndex() + 1);
        f.setNome(txtNome.getText());
        f.setLogin(txtLogin.getText());
        String senha = new String(txtSenha.getPassword());
        if (senhaObrigatoria || !senha.isEmpty()) {
            f.setSenha(senha);
        }
        return f;
    }

    private void inserir() {
        try {
            Funcionario f = lerCampos(true);
            if (dao.carregarPorLogin(f.getLogin()) != null) {
                throw new Exception("Já existe um funcionário com o login \"" + f.getLogin() + "\".");
            }
            if (dao.inserir(f) > 0) {
                txtCodigo.setText(String.valueOf(f.getCodigo()));
                txtSenha.setText("");
                Mensagem.info(this, "Funcionário cadastrado com sucesso! Código: " + f.getCodigo());
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void consultar() {
        try {
            Funcionario f = dao.carregar(Validador.inteiro(txtCodigo.getText(), "Código"));
            if (f == null) {
                Mensagem.info(this, "Funcionário não encontrado.");
                return;
            }
            txtNome.setText(f.getNome());
            cbCargo.setSelectedIndex(f.getCargo() - 1);
            txtLogin.setText(f.getLogin());
            txtSenha.setText("");
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void alterar() {
        try {
            int codigo = Validador.inteiro(txtCodigo.getText(), "Código");
            Funcionario atual = dao.carregar(codigo);
            if (atual == null) {
                throw new Exception("Funcionário não encontrado.");
            }
            Funcionario f = lerCampos(false);
            f.setCodigo(codigo);
            Funcionario mesmoLogin = dao.carregarPorLogin(f.getLogin());
            if (mesmoLogin != null && mesmoLogin.getCodigo() != codigo) {
                throw new Exception("Já existe outro funcionário com o login \"" + f.getLogin() + "\".");
            }
            if (atual.getCargo() == Funcionario.BIBLIOTECARIO && f.getCargo() != Funcionario.BIBLIOTECARIO
                    && dao.contarBibliotecarios() == 1) {
                throw new Exception("É preciso ter pelo menos um Bibliotecário no sistema.");
            }
            if (dao.alterar(f) > 0) {
                txtSenha.setText("");
                Mensagem.info(this, "Funcionário alterado com sucesso!");
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void excluir() {
        try {
            int codigo = Validador.inteiro(txtCodigo.getText(), "Código");
            Funcionario f = dao.carregar(codigo);
            if (f == null) {
                throw new Exception("Funcionário não encontrado.");
            }
            if (codigo == Sessao.getFuncionario().getCodigo()) {
                throw new Exception("Você não pode excluir o próprio usuário.");
            }
            if (f.getCargo() == Funcionario.BIBLIOTECARIO && dao.contarBibliotecarios() == 1) {
                throw new Exception("É preciso ter pelo menos um Bibliotecário no sistema.");
            }
            if (dao.possuiMovimentacao(codigo)) {
                throw new Exception("Este funcionário já registrou empréstimos ou reservas e não pode ser excluído.");
            }
            if (Mensagem.confirmar(this, "Excluir o funcionário " + f.getNome() + "?") && dao.remover(codigo) > 0) {
                Mensagem.info(this, "Funcionário excluído com sucesso!");
                limparCampos();
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void limparCampos() {
        txtCodigo.setText("");
        txtNome.setText("");
        cbCargo.setSelectedIndex(0);
        txtLogin.setText("");
        txtSenha.setText("");
        txtCodigo.requestFocus();
    }
}
