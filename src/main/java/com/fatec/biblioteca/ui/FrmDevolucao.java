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

import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Emprestimo;
import com.fatec.biblioteca.Reserva;
import com.fatec.biblioteca.Sessao;
import com.fatec.biblioteca.Validador;
import com.fatec.biblioteca.dao.EmprestimoDAO;

// RF09: registrar devolução (RN11–RN13, RN15, R4)
public class FrmDevolucao extends JFrame {

    private JTextField txtCodigo;
    private JLabel lblInfo;
    private JTextField txtData;
    private JButton btnBuscar;
    private JButton btnRegistrar;

    private EmprestimoDAO dao;

    public FrmDevolucao() {

        dao = new EmprestimoDAO();

        setTitle("Registrar Devolução");
        setSize(680, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel(new GridLayout(3, 2, 6, 6));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        painel.add(new JLabel("Código do empréstimo"));
        txtCodigo = new JTextField();
        painel.add(txtCodigo);
        painel.add(new JLabel("Empréstimo"));
        lblInfo = new JLabel("-");
        painel.add(lblInfo);
        painel.add(new JLabel("Data da devolução (dd/mm/aaaa)"));
        txtData = new JTextField(Datas.hoje());
        painel.add(txtData);

        btnBuscar = new JButton("Buscar");
        btnRegistrar = new JButton("Registrar devolução");
        JPanel botoes = new JPanel();
        botoes.add(btnBuscar);
        botoes.add(btnRegistrar);

        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        txtCodigo.addActionListener(e -> buscar());
        btnBuscar.addActionListener(e -> buscar());
        btnRegistrar.addActionListener(e -> registrar());

        Estilo.padronizar(this);
    }

    private Emprestimo buscar() {
        try {
            Emprestimo emp = dao.carregar(Validador.inteiro(txtCodigo.getText(), "Código do empréstimo"));
            if (emp == null) {
                lblInfo.setText("-");
                throw new Exception("Empréstimo não encontrado.");
            }
            lblInfo.setText("Livro: " + emp.getLivro().getTitulo() + " | Leitor: " + emp.getLeitor().getNome()
                    + " | Previsto: " + Datas.formatar(emp.getDataPrevista()));
            return emp;
        } catch (Exception err) {
            Mensagem.erro(this, err);
            return null;
        }
    }

    private void registrar() {
        try {
            Emprestimo emp = buscar();
            if (emp == null) {
                return;
            }
            LocalDate data = Datas.converter(txtData.getText());
            emp.registrarDevolucao(data, Sessao.getFuncionario());    // RN11, RN12
            List<Reserva> separadas = dao.registrarDevolucao(emp);

            int atraso = emp.calcularDiasAtraso();
            String msg = atraso > 0
                    ? "Devolução registrada com " + atraso + " dia(s) de atraso. Multa: " + Mensagem.moeda(emp.getMulta())
                    : "Devolução no prazo.";
            for (Reserva r : separadas) {
                msg += "\n\nExemplar separado para a reserva nº " + r.getCodigo() + " de " + r.getLeitor().getNome()
                        + ".\nRetirada até " + Datas.formatar(r.getDataLimiteRetirada()) + ".";
            }
            Mensagem.info(this, msg);
            txtCodigo.setText("");
            lblInfo.setText("-");
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
