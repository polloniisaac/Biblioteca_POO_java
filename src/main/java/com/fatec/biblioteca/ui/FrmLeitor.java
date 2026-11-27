package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.fatec.biblioteca.Constantes;
import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Emprestimo;
import com.fatec.biblioteca.Leitor;
import com.fatec.biblioteca.Validador;
import com.fatec.biblioteca.dao.LeitorDAO;

// RF06 (cadastrar leitor), consultar, alterar e excluir.
public class FrmLeitor extends JFrame {

    private JTextField txtCodigo;
    private JTextField txtNome;
    private JComboBox<String> cbTipo;
    private JTextArea txtSituacao;

    private JButton btnConsultar;
    private JButton btnInserir;
    private JButton btnAlterar;
    private JButton btnExcluir;
    private JButton btnLimpar;
    private JButton btnListar;

    private LeitorDAO dao;

    public FrmLeitor() {

        dao = new LeitorDAO();

        setTitle("Cadastro de Leitores");
        setSize(600, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel(new GridLayout(3, 2, 6, 6));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        painel.add(new JLabel("Código (1 a 9999)"));
        txtCodigo = new JTextField();
        painel.add(txtCodigo);

        painel.add(new JLabel("Nome"));
        txtNome = new JTextField();
        painel.add(txtNome);

        painel.add(new JLabel("Tipo"));
        cbTipo = new JComboBox<>(Leitor.nomesTipos());
        painel.add(cbTipo);

        txtSituacao = new JTextArea(6, 40);
        txtSituacao.setEditable(false);
        txtSituacao.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

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

        add(painel, BorderLayout.NORTH);
        add(new JScrollPane(txtSituacao), BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        configurarEventos();

        Estilo.padronizar(this);
    }

    private void configurarEventos() {
        btnInserir.addActionListener(e -> inserir());
        btnConsultar.addActionListener(e -> consultar());
        btnAlterar.addActionListener(e -> alterar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limparCampos());
        btnListar.addActionListener(e -> new FrmConsultaLeitores().setVisible(true));
    }

    private Leitor lerCampos() throws Exception {
        Leitor leitor = Leitor.criar(cbTipo.getSelectedIndex() + 1);
        leitor.setCodigo(Validador.inteiro(txtCodigo.getText(), "Código"));
        leitor.setNome(txtNome.getText());
        return leitor;
    }

    private void inserir() {
        try {
            if (dao.contar() >= Constantes.MAX_LEITORES) {
                throw new Exception("Capacidade máxima de " + Constantes.MAX_LEITORES + " leitores atingida.");
            }
            Leitor leitor = lerCampos();
            if (dao.existe(leitor.getCodigo())) {
                throw new Exception("Já existe um leitor com o código " + leitor.getCodigo() + ".");
            }
            if (dao.inserir(leitor) > 0) {
                Mensagem.info(this, "Leitor cadastrado com sucesso!\nTipo: " + leitor.getDescricaoTipo()
                        + " | Prazo: " + leitor.getPrazoDias() + " dias | Limite: "
                        + leitor.getLimiteLivros() + " livro(s).");
                mostrarSituacao(dao.carregar(leitor.getCodigo()));
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void consultar() {
        try {
            Leitor leitor = dao.carregar(Validador.inteiro(txtCodigo.getText(), "Código"));
            if (leitor == null) {
                txtSituacao.setText("");
                Mensagem.info(this, "Leitor não encontrado.");
                return;
            }
            txtNome.setText(leitor.getNome());
            cbTipo.setSelectedIndex(leitor.getTipo() - 1);
            mostrarSituacao(leitor);
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void mostrarSituacao(Leitor l) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.getNome()).append(" (").append(l.getDescricaoTipo()).append(") - prazo ")
          .append(l.getPrazoDias()).append(" dias - ").append(l.getSituacao()).append(" livros\n\n");
        sb.append("Empréstimos em aberto:\n");
        if (l.getEmprestimosAbertos().isEmpty()) {
            sb.append("  (nenhum)\n");
        }
        for (Emprestimo e : l.getEmprestimosAbertos()) {
            sb.append(String.format("  nº %-4d %-30s previsto %s%n", e.getCodigo(), e.getObra().getTitulo(),
                    Datas.formatar(e.getDataPrevista())));
        }
        txtSituacao.setText(sb.toString());
    }

    private void alterar() {
        try {
            Leitor leitor = lerCampos();
            if (!dao.existe(leitor.getCodigo())) {
                throw new Exception("Leitor não encontrado. Use Inserir para cadastrar.");
            }
            if (dao.alterar(leitor) > 0) {
                Mensagem.info(this, "Leitor alterado com sucesso!");
                mostrarSituacao(dao.carregar(leitor.getCodigo()));
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void excluir() {
        try {
            int codigo = Validador.inteiro(txtCodigo.getText(), "Código");
            if (!dao.existe(codigo)) {
                throw new Exception("Leitor não encontrado.");
            }
            if (dao.possuiMovimentacao(codigo)) {
                throw new Exception("Este leitor já teve empréstimos ou reservas e não pode ser excluído.");
            }
            if (Mensagem.confirmar(this, "Excluir o leitor " + codigo + "?") && dao.remover(codigo) > 0) {
                Mensagem.info(this, "Leitor excluído com sucesso!");
                limparCampos();
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void limparCampos() {
        txtCodigo.setText("");
        txtNome.setText("");
        cbTipo.setSelectedIndex(0);
        txtSituacao.setText("");
        txtCodigo.requestFocus();
    }
}
