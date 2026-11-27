package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Emprestimo;
import com.fatec.biblioteca.dao.EmprestimoDAO;

// RF12: (a) multas já cobradas e (b) multas previstas na data de referência.
public class FrmRelAtrasos extends JFrame {

    private Tabela modeloCobradas;
    private Tabela modeloPrevistas;
    private JLabel lblCobradas;
    private JLabel lblPrevistas;
    private JTextField txtData;
    private JButton btnGerar;

    private EmprestimoDAO dao;

    public FrmRelAtrasos() {

        dao = new EmprestimoDAO();

        setTitle("Relatório - Atrasos e Multas");
        setSize(850, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Data de referência (dd/mm/aaaa):"));
        txtData = new JTextField(Datas.hoje(), 10);
        topo.add(txtData);
        btnGerar = new JButton("Gerar");
        topo.add(btnGerar);

        modeloCobradas = new Tabela("Emp.", "Leitor", "Livro", "Devolvido em", "Atraso (dias)", "Multa (R$)");
        modeloPrevistas = new Tabela("Emp.", "Leitor", "Livro", "Previsto", "Atraso (dias)", "Multa prevista (R$)");
        lblCobradas = new JLabel();
        lblPrevistas = new JLabel();

        JPanel centro = new JPanel(new GridLayout(2, 1, 6, 6));
        centro.add(secao("(a) Devoluções realizadas com atraso", modeloCobradas, lblCobradas));
        centro.add(secao("(b) Empréstimos em aberto e atrasados na data de referência", modeloPrevistas,
                lblPrevistas));

        add(topo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        btnGerar.addActionListener(e -> gerar());
        txtData.addActionListener(e -> gerar());

        gerar();

        Estilo.padronizar(this);
    }

    private JPanel secao(String titulo, Tabela modelo, JLabel rodape) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        p.add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        p.add(rodape, BorderLayout.SOUTH);
        return p;
    }

    private void gerar() {
        try {
            LocalDate ref = Datas.converter(txtData.getText());

            modeloCobradas.setRowCount(0);
            double arrecadado = 0;
            for (Emprestimo e : dao.listarDevolvidosComMulta()) {
                modeloCobradas.addRow(new Object[] {
                    e.getCodigo(), e.getLeitor().getNome(), e.getObra().getTitulo(),
                    Datas.formatar(e.getDataDevolucao()), e.calcularDiasAtraso(), Mensagem.numero(e.getMulta())
                });
                arrecadado += e.getMulta();
            }
            lblCobradas.setText(" Total arrecadado em multas: " + Mensagem.moeda(arrecadado));

            modeloPrevistas.setRowCount(0);
            double previsto = 0;
            for (Emprestimo e : dao.listarAtrasados(ref)) {
                double multa = e.calcularMulta(ref);
                modeloPrevistas.addRow(new Object[] {
                    e.getCodigo(), e.getLeitor().getNome(), e.getObra().getTitulo(),
                    Datas.formatar(e.getDataPrevista()), e.calcularDiasAtraso(ref), Mensagem.numero(multa)
                });
                previsto += multa;
            }
            lblPrevistas.setText(" Total previsto em multas: " + Mensagem.moeda(previsto));
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
