package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.fatec.biblioteca.Constantes;
import com.fatec.biblioteca.Copia;
import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Emprestimo;
import com.fatec.biblioteca.Leitor;
import com.fatec.biblioteca.Obra;
import com.fatec.biblioteca.Reserva;
import com.fatec.biblioteca.Sessao;
import com.fatec.biblioteca.Validador;
import com.fatec.biblioteca.dao.EmprestimoDAO;
import com.fatec.biblioteca.dao.LeitorDAO;
import com.fatec.biblioteca.dao.ObraDAO;
import com.fatec.biblioteca.dao.ReservaDAO;

// RF08: realizar empréstimo (RN06–RN10, RN13–RN16).
public class FrmEmprestimo extends JFrame {

    private JTextField txtLeitor;
    private JLabel lblLeitor;
    private JTextField txtObra;
    private JLabel lblObra;
    private JTextField txtData;

    private JButton btnRealizar;
    private JButton btnLimpar;

    private LeitorDAO leitorDAO;
    private ObraDAO obraDAO;
    private EmprestimoDAO emprestimoDAO;
    private ReservaDAO reservaDAO;

    public FrmEmprestimo() {

        leitorDAO = new LeitorDAO();
        obraDAO = new ObraDAO();
        emprestimoDAO = new EmprestimoDAO();
        reservaDAO = new ReservaDAO();

        setTitle("Realizar Empréstimo");
        setSize(640, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel(new GridLayout(5, 2, 6, 6));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        painel.add(new JLabel("Código do leitor"));
        txtLeitor = new JTextField();
        painel.add(txtLeitor);
        painel.add(new JLabel("Leitor"));
        lblLeitor = new JLabel("-");
        painel.add(lblLeitor);

        painel.add(new JLabel("Código do livro"));
        txtObra = new JTextField();
        painel.add(txtObra);
        painel.add(new JLabel("Livro"));
        lblObra = new JLabel("-");
        painel.add(lblObra);

        painel.add(new JLabel("Data do empréstimo (dd/mm/aaaa)"));
        txtData = new JTextField(Datas.hoje());
        painel.add(txtData);

        btnRealizar = new JButton("Realizar empréstimo");
        btnLimpar = new JButton("Limpar");
        JPanel botoes = new JPanel();
        botoes.add(btnRealizar);
        botoes.add(btnLimpar);

        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        txtLeitor.addActionListener(e -> mostrarLeitor());
        txtLeitor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                mostrarLeitor();
            }
        });
        txtObra.addActionListener(e -> mostrarObra());
        txtObra.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                mostrarObra();
            }
        });
        btnRealizar.addActionListener(e -> realizar());
        btnLimpar.addActionListener(e -> limparCampos());

        Estilo.padronizar(this);
    }

    private void mostrarLeitor() {
        try {
            if (txtLeitor.getText().isBlank()) {
                return;
            }
            Leitor l = leitorDAO.carregar(Validador.inteiro(txtLeitor.getText(), "Código do leitor"));
            lblLeitor.setText(l == null ? "Leitor não encontrado"
                    : l.getNome() + " (" + l.getDescricaoTipo() + ") - " + l.getSituacao() + " livro(s)");
        } catch (Exception err) {
            lblLeitor.setText(err.getMessage());
        }
    }

    private void mostrarObra() {
        try {
            if (txtObra.getText().isBlank()) {
                return;
            }
            Obra o = obraDAO.carregar(Validador.inteiro(txtObra.getText(), "Código do livro"));
            lblObra.setText(o == null ? "Livro não encontrado"
                    : o.getTitulo() + " - " + o.getQtdDisponiveis() + " disponível(is)"
                            + (o.getQtdReservadas() > 0 ? ", " + o.getQtdReservadas() + " reservado(s)" : ""));
        } catch (Exception err) {
            lblObra.setText(err.getMessage());
        }
    }

    private void realizar() {
        try {
            if (emprestimoDAO.contar() >= Constantes.MAX_EMPRESTIMOS) {               // RN14
                throw new Exception("Capacidade máxima de " + Constantes.MAX_EMPRESTIMOS + " empréstimos atingida.");
            }
            LocalDate data = Datas.converter(txtData.getText());
            reservaDAO.expirarVencidas(data);                                          // R6

            Leitor leitor = leitorDAO.carregar(Validador.inteiro(txtLeitor.getText(), "Código do leitor"));
            if (leitor == null) {
                throw new Exception("Leitor não encontrado.");                          // RN07
            }
            Obra obra = obraDAO.carregar(Validador.inteiro(txtObra.getText(), "Código do livro"));
            if (obra == null) {
                throw new Exception("Livro não encontrado.");                           // RN06
            }

            leitor.validarNovoEmprestimo(obra, data);                                  // RN07, RN08, RN09
            Reserva reserva = obra.getReservaAguardando(leitor);                       // R5
            Copia copia = obra.getCopiaParaEmprestimo(leitor);                         // RN06
            Emprestimo emp = new Emprestimo(copia, leitor, Sessao.getFuncionario(), data); // RN10 (polimorfismo)

            emprestimoDAO.registrarEmprestimo(emp, reserva);                           // RN13 + RN16 (transação)

            Mensagem.info(this, "Empréstimo nº " + emp.getCodigo() + " registrado (exemplar "
                    + copia.getEtiqueta() + ").\nDevolução prevista para " + Datas.formatar(emp.getDataPrevista())
                    + "." + (reserva != null ? "\nReserva nº " + reserva.getCodigo() + " atendida." : ""));
            limparCampos();
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void limparCampos() {
        txtLeitor.setText("");
        txtObra.setText("");
        lblLeitor.setText("-");
        lblObra.setText("-");
        txtData.setText(Datas.hoje());
        txtLeitor.requestFocus();
    }
}
