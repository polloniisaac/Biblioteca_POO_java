package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Emprestimo;
import com.fatec.biblioteca.ListaCobranca;
import com.fatec.biblioteca.dao.EmprestimoDAO;

/** RF14: exportar a lista de cobrança para atrasos_AAAA-MM-DD.txt. */
public class FrmExportarCobranca extends JFrame {

    private JTextField txtData;
    private JTextArea txtPrevia;
    private JButton btnExportar;

    private EmprestimoDAO dao;

    public FrmExportarCobranca() {

        dao = new EmprestimoDAO();

        setTitle("Exportar Lista de Cobrança");
        setSize(950, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Data de referência (dd/mm/aaaa):"));
        txtData = new JTextField(Datas.hoje(), 10);
        topo.add(txtData);
        btnExportar = new JButton("Exportar");
        topo.add(btnExportar);

        txtPrevia = new JTextArea();
        txtPrevia.setEditable(false);
        txtPrevia.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(txtPrevia), BorderLayout.CENTER);

        btnExportar.addActionListener(e -> exportar());
    }

    private void exportar() {
        try {
            LocalDate ref = Datas.converter(txtData.getText());
            List<Emprestimo> atrasados = dao.listarAtrasados(ref);
            if (atrasados.isEmpty()) {
                txtPrevia.setText("");
                Mensagem.info(this, "Não há empréstimos em atraso em " + Datas.formatar(ref)
                        + ". O arquivo não foi gerado.");
                return;
            }
            ListaCobranca lista = new ListaCobranca(ref, atrasados);
            txtPrevia.setText(lista.gerarTexto());
            try {
                String caminho = lista.exportar();
                Mensagem.info(this, "Lista de cobrança exportada para " + caminho + " ("
                        + lista.getQuantidade() + " empréstimo(s)).");
            } catch (IOException io) {
                Mensagem.info(this, "Não foi possível gravar o arquivo " + lista.getNomeArquivo() + ".\n"
                        + io.getMessage());
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
