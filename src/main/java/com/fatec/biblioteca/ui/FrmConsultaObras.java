package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.fatec.biblioteca.Categoria;
import com.fatec.biblioteca.Obra;
import com.fatec.biblioteca.dao.ObraDAO;

// RF03 (listar acervo) e RF05 (pesquisar por categoria).
public class FrmConsultaObras extends JFrame {

    private JTable tabela;
    private Tabela modelo;
    private JComboBox<Object> cbCategoria;
    private JLabel lblTotais;
    private JButton btnAtualizar;

    private ObraDAO dao;

    public FrmConsultaObras() {

        dao = new ObraDAO();

        setTitle("Acervo");
        setSize(950, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        criarComponentes();

        carregarDados();

        Estilo.padronizar(this);
    }

    private void criarComponentes() {

        modelo = new Tabela("Código", "Título", "Autor", "Categoria", "Ano", "Total", "Disponíveis",
                "Emprestados", "Reservados");
        tabela = new JTable(modelo);
        tabela.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(tabela);

        cbCategoria = new JComboBox<>();
        cbCategoria.addItem("Todas as categorias");
        for (Categoria c : Categoria.values()) {
            cbCategoria.addItem(c);
        }
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Categoria:"));
        topo.add(cbCategoria);

        lblTotais = new JLabel();
        btnAtualizar = new JButton("Atualizar");
        JPanel painelBotoes = new JPanel(new BorderLayout());
        painelBotoes.add(lblTotais, BorderLayout.WEST);
        painelBotoes.add(btnAtualizar, BorderLayout.EAST);

        add(topo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        btnAtualizar.addActionListener(e -> carregarDados());
        cbCategoria.addActionListener(e -> carregarDados());
    }

    private void carregarDados() {
        try {
            modelo.setRowCount(0);
            Categoria filtro = cbCategoria.getSelectedItem() instanceof Categoria
                    ? (Categoria) cbCategoria.getSelectedItem() : null;
            List<Obra> lista = dao.listar(filtro);
            int exemplares = 0;
            for (Obra o : lista) {
                modelo.addRow(new Object[] {
                    o.getCodigo(),
                    o.getTitulo(),
                    o.getAutor(),
                    o.getCategoria().getNome(),
                    o.getAnoPublicacao(),
                    o.getTotalCopias(),
                    o.getQtdDisponiveis(),
                    o.getQtdEmprestadas(),
                    o.getQtdReservadas()
                });
                exemplares += o.getTotalCopias();
            }
            if (lista.isEmpty() && filtro != null) {
                lblTotais.setText(" Nenhum livro nesta categoria.");
            } else {
                lblTotais.setText(" " + (filtro == null ? "Total de títulos: " : "Títulos encontrados: ")
                        + lista.size() + "   |   Total de exemplares: " + exemplares);
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
