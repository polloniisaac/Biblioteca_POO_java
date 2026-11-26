package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.fatec.biblioteca.Constantes;
import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Leitor;
import com.fatec.biblioteca.Obra;
import com.fatec.biblioteca.Reserva;
import com.fatec.biblioteca.Sessao;
import com.fatec.biblioteca.Validador;
import com.fatec.biblioteca.dao.LeitorDAO;
import com.fatec.biblioteca.dao.ObraDAO;
import com.fatec.biblioteca.dao.ReservaDAO;

/** Reservar obra sem exemplar disponível e cancelar reserva (regras R1–R7). */
public class FrmReserva extends JFrame {

    private JTextField txtLeitor;
    private JTextField txtObra;
    private JTextField txtData;
    private JTextField txtCodigoReserva;

    private JButton btnReservar;
    private JButton btnCancelar;
    private JButton btnListar;

    private LeitorDAO leitorDAO;
    private ObraDAO obraDAO;
    private ReservaDAO dao;

    public FrmReserva() {

        leitorDAO = new LeitorDAO();
        obraDAO = new ObraDAO();
        dao = new ReservaDAO();

        setTitle("Reservas");
        setSize(560, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel(new GridLayout(4, 2, 6, 6));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        painel.add(new JLabel("Código do leitor"));
        txtLeitor = new JTextField();
        painel.add(txtLeitor);
        painel.add(new JLabel("Código do livro"));
        txtObra = new JTextField();
        painel.add(txtObra);
        painel.add(new JLabel("Data (dd/mm/aaaa)"));
        txtData = new JTextField(Datas.hoje());
        painel.add(txtData);
        painel.add(new JLabel("Código da reserva (para cancelar)"));
        txtCodigoReserva = new JTextField();
        painel.add(txtCodigoReserva);

        btnReservar = new JButton("Reservar");
        btnCancelar = new JButton("Cancelar reserva");
        btnListar = new JButton("Listar");
        JPanel botoes = new JPanel();
        botoes.add(btnReservar);
        botoes.add(btnCancelar);
        botoes.add(btnListar);

        add(new JLabel("  O leitor tem " + Constantes.PRAZO_RETIRADA_RESERVA
                + " dias para retirar o exemplar quando ele for separado."), BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        btnReservar.addActionListener(e -> reservar());
        btnCancelar.addActionListener(e -> cancelar());
        btnListar.addActionListener(e -> new FrmConsultaReservas().setVisible(true));
    }

    private void reservar() {
        try {
            LocalDate data = Datas.converter(txtData.getText());
            dao.expirarVencidas(data);
            Leitor leitor = leitorDAO.carregar(Validador.inteiro(txtLeitor.getText(), "Código do leitor"));
            if (leitor == null) {
                throw new Exception("Leitor não encontrado.");
            }
            Obra obra = obraDAO.carregar(Validador.inteiro(txtObra.getText(), "Código do livro"));
            if (obra == null) {
                throw new Exception("Livro não encontrado.");
            }
            leitor.validarNovaReserva(obra, data);   // R1, R2, R3
            Reserva reserva = new Reserva(obra, leitor, Sessao.getFuncionario(), data);
            dao.inserir(reserva);
            Mensagem.info(this, "Reserva nº " + reserva.getCodigo() + " registrada para " + leitor.getNome()
                    + ".\nPosição na fila: " + obra.getPosicaoNaFila(reserva) + "º.");
            txtLeitor.setText("");
            txtObra.setText("");
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void cancelar() {
        try {
            LocalDate data = Datas.converter(txtData.getText());
            int codigo = Validador.inteiro(txtCodigoReserva.getText(), "Código da reserva");
            Reserva r = dao.carregarViva(codigo);
            if (r == null) {
                throw new Exception("Reserva não encontrada ou já encerrada.");
            }
            if (!Mensagem.confirmar(this, "Cancelar a reserva nº " + codigo + " de " + r.getLeitor().getNome()
                    + " (" + r.getObra().getTitulo() + ")?")) {
                return;
            }
            r.cancelar();                                            // R7
            List<Reserva> separadas = dao.salvarCancelamento(r, data);
            String msg = "Reserva cancelada.";
            for (Reserva s : separadas) {
                msg += "\nExemplar separado para a reserva nº " + s.getCodigo() + " de " + s.getLeitor().getNome()
                        + " (retirada até " + Datas.formatar(s.getDataLimiteRetirada()) + ").";
            }
            Mensagem.info(this, msg);
            txtCodigoReserva.setText("");
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
