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
import com.fatec.biblioteca.Obra;
import com.fatec.biblioteca.Validador;
import com.fatec.biblioteca.dao.CopiaDAO;
import com.fatec.biblioteca.dao.ObraDAO;

/** RF02: adicionar exemplares (cópias) a uma obra já cadastrada. */
public class FrmAdicionarCopias extends JFrame {

    private JTextField txtCodigo;
    private JLabel lblObra;
    private JTextField txtQuantidade;
    private JButton btnBuscar;
    private JButton btnAdicionar;

    private ObraDAO obraDAO;
    private CopiaDAO copiaDAO;

    public FrmAdicionarCopias() {

        obraDAO = new ObraDAO();
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
        lblObra = new JLabel("-");
        painel.add(lblObra);
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
    }

    private Obra buscar() {
        try {
            Obra obra = obraDAO.carregar(Validador.inteiro(txtCodigo.getText(), "Código"));
            if (obra == null) {
                lblObra.setText("-");
                throw new Exception("Livro não encontrado.");
            }
            lblObra.setText(descricao(obra));
            return obra;
        } catch (Exception err) {
            Mensagem.erro(this, err);
            return null;
        }
    }

    private void adicionar() {
        try {
            Obra obra = buscar();
            if (obra == null) {
                return;
            }
            List<Copia> novas = obra.adicionarCopias(Validador.inteiro(txtQuantidade.getText(), "Quantidade"));
            int separadas = copiaDAO.adicionarCopias(obra, novas);
            Obra atual = obraDAO.carregar(obra.getCodigo());
            lblObra.setText(descricao(atual));
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

    private String descricao(Obra o) {
        return o.getTitulo() + " - total " + o.getTotalCopias() + ", disponíveis " + o.getQtdDisponiveis();
    }
}
