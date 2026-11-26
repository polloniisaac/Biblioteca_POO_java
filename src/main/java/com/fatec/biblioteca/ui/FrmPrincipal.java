package com.fatec.biblioteca.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

import com.fatec.biblioteca.Funcionario;
import com.fatec.biblioteca.Sessao;

public class FrmPrincipal extends JFrame {

    private JMenuBar menuBar;

    public FrmPrincipal() {
        Funcionario usuario = Sessao.getFuncionario();

        setTitle("Biblioteca Comunitária Vila Leitura");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        toFront();
        requestFocus();

        // Painel principal
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(new Color(53, 152, 235));
        JLabel lblTitulo = new JLabel("Biblioteca Comunitária Vila Leitura", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        JLabel lblUsuario = new JLabel("Funcionário: " + usuario.getNome() + " (" + usuario.getDescricaoTipo() + ")",
                SwingConstants.CENTER);
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 16));
        lblUsuario.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        painel.add(lblTitulo, BorderLayout.CENTER);
        painel.add(lblUsuario, BorderLayout.SOUTH);
        add(painel);

        menuBar = new JMenuBar();

        // Menu Cadastros (RF01, RF02, RF06 + funcionários)
        JMenu menuCadastros = new JMenu("Cadastros");
        menuCadastros.add(item("Obras (livros)", FrmObra::new));
        menuCadastros.add(item("Adicionar exemplares", FrmAdicionarCopias::new));
        menuCadastros.add(item("Leitores", FrmLeitor::new));
        JMenuItem miFuncionarios = item("Funcionários", FrmFuncionario::new);
        // polimorfismo: Bibliotecario devolve true, Voluntario devolve false
        miFuncionarios.setEnabled(usuario.podeGerenciarCadastros());
        menuCadastros.add(miFuncionarios);

        // Menu Circulação (RF08, RF09 + reservas)
        JMenu menuCirculacao = new JMenu("Circulação");
        menuCirculacao.add(item("Realizar empréstimo", FrmEmprestimo::new));
        menuCirculacao.add(item("Registrar devolução", FrmDevolucao::new));
        menuCirculacao.add(item("Reservas", FrmReserva::new));

        // Menu Consultas (RF03, RF05, RF07, RF10)
        JMenu menuConsultas = new JMenu("Consultas");
        menuConsultas.add(item("Acervo", FrmConsultaObras::new));
        menuConsultas.add(item("Leitores", FrmConsultaLeitores::new));
        menuConsultas.add(item("Empréstimos em aberto", FrmEmprestimosAbertos::new));
        menuConsultas.add(item("Reservas", FrmConsultaReservas::new));

        // Menu Relatórios (RF11 a RF14)
        JMenu menuRelatorios = new JMenu("Relatórios");
        menuRelatorios.add(item("Livros disponíveis e emprestados", FrmRelCirculacao::new));
        menuRelatorios.add(item("Atrasos e multas", FrmRelAtrasos::new));
        menuRelatorios.add(item("Perfil de uso (tipo de leitor x categoria)", FrmRelPerfilUso::new));
        menuRelatorios.add(item("Exportar lista de cobrança", FrmExportarCobranca::new));

        // Menu Sistema
        JMenu menuSistema = new JMenu("Sistema");
        menuSistema.add(item("Alterar minha senha", FrmAlterarSenha::new));
        JMenuItem miTrocar = new JMenuItem("Trocar usuário");
        miTrocar.addActionListener(e -> {
            Sessao.setFuncionario(null);
            new FrmLogin().setVisible(true);
            dispose();
        });
        menuSistema.add(miTrocar);
        JMenuItem miSair = new JMenuItem("Sair");
        miSair.addActionListener(e -> {
            int opcao = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente sair?",
                    "Confirmação",
                    JOptionPane.YES_NO_OPTION);

            if (opcao == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        menuSistema.add(miSair);

        menuBar.add(menuCadastros);
        menuBar.add(menuCirculacao);
        menuBar.add(menuConsultas);
        menuBar.add(menuRelatorios);
        menuBar.add(menuSistema);

        setJMenuBar(menuBar);
    }

    /** Cria um item de menu que abre a tela informada. */
    private JMenuItem item(String texto, Supplier<JFrame> tela) {
        JMenuItem mi = new JMenuItem(texto);
        mi.addActionListener(e -> tela.get().setVisible(true));
        return mi;
    }
}
