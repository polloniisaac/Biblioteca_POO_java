package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Emprestimo;
import com.fatec.biblioteca.dao.EmprestimoDAO;

// RF10: empréstimos em aberto
public class FrmEmprestimosAbertos extends JFrame {

    private JTable tabela;
    private Tabela modelo;
    private JTextField txtData;
    private JLabel lblTotais;
    private JButton btnAtualizar;

    private EmprestimoDAO dao;

    public FrmEmprestimosAbertos() {

        dao = new EmprestimoDAO();

        setTitle("Empréstimos em Aberto");
        setSize(950, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        criarComponentes();

        carregarDados();

        Estilo.padronizar(this);
    }

    private void criarComponentes() {
        modelo = new Tabela("Código", "Título", "Exemplar", "Leitor", "Empréstimo", "Previsto", "Status");
        tabela = new JTable(modelo);
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco,
                    int linha, int coluna) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foco, linha, coluna);
                boolean atrasado = String.valueOf(t.getValueAt(linha, 6)).startsWith("ATRASADO");
                if (!sel) {
                    c.setForeground(atrasado ? Estilo.VERMELHO : t.getForeground());
                }
                return c;
            }
        });

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Data de referência (dd/mm/aaaa):"));
        txtData = new JTextField(Datas.hoje(), 10);
        topo.add(txtData);
        btnAtualizar = new JButton("Atualizar");
        topo.add(btnAtualizar);

        lblTotais = new JLabel();

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(lblTotais, BorderLayout.SOUTH);

        btnAtualizar.addActionListener(e -> carregarDados());
        txtData.addActionListener(e -> carregarDados());
    }

    private void carregarDados() {
        try {
            LocalDate ref = Datas.converter(txtData.getText());
            modelo.setRowCount(0);
            List<Emprestimo> lista = dao.listarAbertos();
            int atrasados = 0;
            for (Emprestimo e : lista) {
                if (e.calcularDiasAtraso(ref) > 0) {
                    atrasados++;
                }
                modelo.addRow(new Object[] {
                    e.getCodigo(),
                    e.getLivro().getTitulo(),
                    e.getCopia().getEtiqueta(),
                    e.getLeitor().getNome(),
                    Datas.formatar(e.getDataEmprestimo()),
                    Datas.formatar(e.getDataPrevista()),
                    e.getStatus(ref)
                });
            }
            lblTotais.setText(" Empréstimos em aberto: " + lista.size() + "   |   Atrasados: " + atrasados);
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
