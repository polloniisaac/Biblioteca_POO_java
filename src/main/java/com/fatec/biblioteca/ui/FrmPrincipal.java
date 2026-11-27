package com.fatec.biblioteca.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Funcionario;
import com.fatec.biblioteca.Sessao;

public class FrmPrincipal extends JFrame {

    private JMenuBar menuBar;
    private JPanel painelAtalhos;

    public FrmPrincipal() {
        Funcionario usuario = Sessao.getFuncionario();

        setTitle("Biblioteca Comunitária Vila Leitura");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        toFront();
        requestFocus();

        JPanel cabecalho = Estilo.cabecalho("Biblioteca Comunitária Vila Leitura",
                "Funcionário: " + usuario.getNome() + " (" + usuario.getDescricaoTipo() + ")   •   Hoje: "
                        + Datas.hoje());
        ((JLabel) cabecalho.getComponent(0)).putClientProperty("FlatLaf.styleClass", "h1");
        cabecalho.setBorder(BorderFactory.createEmptyBorder(22, 32, 22, 32));

        painelAtalhos = new JPanel();
        painelAtalhos.setOpaque(false);
        painelAtalhos.setLayout(new BoxLayout(painelAtalhos, BoxLayout.Y_AXIS));
        painelAtalhos.setBorder(BorderFactory.createEmptyBorder(16, 32, 24, 32));

        secao("Circulação",
                atalho("Realizar empréstimo", "Emprestar um livro a um leitor", FrmEmprestimo::new),
                atalho("Registrar devolução", "Receber livro e calcular multa", FrmDevolucao::new),
                atalho("Reservas", "Reservar ou cancelar reserva", FrmReserva::new),
                atalho("Empréstimos em aberto", "Quem está com cada livro", FrmEmprestimosAbertos::new));

        JButton btnFuncionarios = atalho("Funcionários", "Bibliotecários e voluntários", FrmFuncionario::new);
        // polimorfismo: Bibliotecario devolve true, Voluntario devolve false
        btnFuncionarios.setEnabled(usuario.podeGerenciarCadastros());
        secao("Cadastros",
                atalho("Obras (livros)", "Cadastrar, alterar e excluir livros", FrmObra::new),
                atalho("Adicionar exemplares", "Novas cópias de um livro", FrmAdicionarCopias::new),
                atalho("Leitores", "Estudantes, professores e comunidade", FrmLeitor::new),
                btnFuncionarios);

        secao("Consultas e relatórios",
                atalho("Acervo", "Livros por categoria", FrmConsultaObras::new),
                atalho("Disponíveis e emprestados", "Situação da circulação", FrmRelCirculacao::new),
                atalho("Atrasos e multas", "Multas cobradas e previstas", FrmRelAtrasos::new),
                atalho("Perfil de uso", "Tipo de leitor x categoria", FrmRelPerfilUso::new));

        JPanel centro = new PainelFundo(new BorderLayout());
        centro.add(painelAtalhos, BorderLayout.NORTH);

        add(cabecalho, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        menuBar = new JMenuBar();

        // Menu Cadastros (RF01, RF02, RF06 + funcionários)
        JMenu menuCadastros = new JMenu("Cadastros");
        menuCadastros.add(item("Obras (livros)", FrmObra::new));
        menuCadastros.add(item("Adicionar exemplares", FrmAdicionarCopias::new));
        menuCadastros.add(item("Leitores", FrmLeitor::new));
        JMenuItem miFuncionarios = item("Funcionários", FrmFuncionario::new);
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

        JMenu menuSistema = new JMenu("Sistema");
        menuSistema.add(item("Alterar minha senha", FrmAlterarSenha::new));
        JCheckBoxMenuItem miEscuro = new JCheckBoxMenuItem("Tema escuro", Estilo.isEscuro());
        miEscuro.addActionListener(e -> Estilo.aplicarTema(miEscuro.isSelected()));
        menuSistema.add(miEscuro);
        menuSistema.addSeparator();
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

    private JMenuItem item(String texto, Supplier<JFrame> tela) {
        JMenuItem mi = new JMenuItem(texto);
        mi.addActionListener(e -> tela.get().setVisible(true));
        return mi;
    }

    private JButton atalho(String titulo, String descricao, Supplier<JFrame> tela) {
        JButton b = new JButton("<html><div style='padding:2px'><b style='font-size:13px'>" + titulo
                + "</b><br><span style='color:#7A869A'>" + descricao + "</span></div></html>");
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("FlatLaf.style", "arc: 14; margin: 14,16,14,16");
        b.addActionListener(e -> tela.get().setVisible(true));
        return b;
    }

    private void secao(String titulo, JButton... atalhos) {
        JLabel lbl = new JLabel(titulo);
        lbl.putClientProperty("FlatLaf.styleClass", "h3");
        lbl.setBorder(BorderFactory.createEmptyBorder(16, 2, 8, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelAtalhos.add(lbl);

        JPanel linha = new JPanel(new GridLayout(1, atalhos.length, 14, 14));
        for (JButton b : atalhos) {
            linha.add(b);
        }
        linha.setOpaque(false);
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, linha.getPreferredSize().height));
        painelAtalhos.add(linha);
    }
}
