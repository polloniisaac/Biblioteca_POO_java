package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Reserva;
import com.fatec.biblioteca.dao.ReservaDAO;

public class FrmConsultaReservas extends JFrame {

    private JTable tabela;
    private Tabela modelo;
    private JCheckBox chkVivas;
    private JButton btnAtualizar;

    private ReservaDAO dao;

    public FrmConsultaReservas() {

        dao = new ReservaDAO();

        setTitle("Reservas");
        setSize(1000, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        modelo = new Tabela("Código", "Livro", "Leitor", "Data", "Situação", "Posição", "Exemplar separado",
                "Retirar até", "Registrada por");
        tabela = new JTable(modelo);
        chkVivas = new JCheckBox("Somente ativas (na fila ou aguardando retirada)", true);
        btnAtualizar = new JButton("Atualizar");
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(chkVivas);
        topo.add(btnAtualizar);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        btnAtualizar.addActionListener(e -> carregarDados());
        chkVivas.addActionListener(e -> carregarDados());

        carregarDados();

        Estilo.padronizar(this);
    }

    private void carregarDados() {
        try {
            modelo.setRowCount(0);
            List<Reserva> lista = dao.listar(chkVivas.isSelected());
            int livroAnterior = -1;
            int posicao = 0;
            for (Reserva r : lista) {
                if (r.getLivro().getCodigo() != livroAnterior) {
                    livroAnterior = r.getLivro().getCodigo();
                    posicao = 0;
                }
                String pos = "";
                if (r.getSituacao().isViva()) {
                    pos = (++posicao) + "º";
                }
                modelo.addRow(new Object[] {
                    r.getCodigo(),
                    r.getLivro().getCodigo() + " - " + r.getLivro().getTitulo(),
                    r.getLeitor().getNome(),
                    Datas.formatar(r.getDataReserva()),
                    r.getSituacao(),
                    pos,
                    r.getCopiaSeparada() == null ? "" : r.getCopiaSeparada().getEtiqueta(),
                    Datas.formatar(r.getDataLimiteRetirada()),
                    r.getFuncionario().getNome()
                });
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
