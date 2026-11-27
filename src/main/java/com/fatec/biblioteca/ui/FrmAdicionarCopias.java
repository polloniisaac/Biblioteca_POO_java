package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.fatec.biblioteca.Copia;
import com.fatec.biblioteca.Livro;
import com.fatec.biblioteca.Validador;
import com.fatec.biblioteca.dao.CopiaDAO;
import com.fatec.biblioteca.dao.LivroDAO;

// RF02: adicionar exemplares (cópias) a um livro já cadastrado.
public class FrmAdicionarCopias extends JFrame {

    private JTextField txtCodigo;
    private JLabel lblLivro;
    private JTextField txtQuantidade;
    private JButton btnBuscar;
    private JButton btnAdicionar;

    private LivroDAO livroDAO;
    private CopiaDAO copiaDAO;

    public FrmAdicionarCopias() {

        livroDAO = new LivroDAO();
        copiaDAO = new CopiaDAO();

        setTitle("Adicionar Exemplares");
        setSize(560, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel(new GridLayout(3, 2, 6, 6));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        painel.add(new JLabel("Código do livro"));
        txtCodigo = new JTextField();
        painel.add(txtCodigo);
        painel.add(new JLabel("Livro"));
        lblLivro = new JLabel("-");
        painel.add(lblLivro);
        painel.add(new JLabel("Novos exemplares (1 a 50)"));
        txtQuantidade = new JTextField();
        painel.add(txtQuantidade);

        btnBuscar = new JButton("Buscar");
        btnAdicionar = new JButton("Adicionar");
        JPanel botoes = new JPanel();
        botoes.add(btnBuscar);
        botoes.add(btnAdicionar);

        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscar());
        btnAdicionar.addActionListener(e -> adicionar());

        Estilo.padronizar(this);
    }

    private Livro buscar() {
        try {
            Livro livro = livroDAO.carregar(Validador.inteiro(txtCodigo.getText(), "Código"));
            if (livro == null) {
                lblLivro.setText("-");
                throw new Exception("Livro não encontrado.");
            }
            lblLivro.setText(descricao(livro));
            return livro;
        } catch (Exception err) {
            Mensagem.erro(this, err);
            return null;
        }
    }

    private void adicionar() {
        try {
            Livro livro = buscar();
            if (livro == null) {
                return;
            }
            List<Copia> novas = livro.adicionarCopias(Validador.inteiro(txtQuantidade.getText(), "Quantidade"));
            int separadas = copiaDAO.adicionarCopias(livro, novas);
            Livro atual = livroDAO.carregar(livro.getCodigo());
            lblLivro.setText(descricao(atual));
            String msg = novas.size() + " exemplar(es) adicionado(s).\nNovas quantidades: total "
                    + atual.getTotalCopias() + ", disponíveis " + atual.getQtdDisponiveis() + ".";
            if (separadas > 0) {
                msg += "\n" + separadas + " exemplar(es) separado(s) para reservas da fila.";
            }
            Mensagem.info(this, msg);
            txtQuantidade.setText("");
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private String descricao(Livro o) {
        return o.getTitulo() + " - total " + o.getTotalCopias() + ", disponíveis " + o.getQtdDisponiveis();
    }
}
