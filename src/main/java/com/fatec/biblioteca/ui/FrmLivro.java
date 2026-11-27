package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.fatec.biblioteca.Categoria;
import com.fatec.biblioteca.Constantes;
import com.fatec.biblioteca.Copia;
import com.fatec.biblioteca.Datas;
import com.fatec.biblioteca.Emprestimo;
import com.fatec.biblioteca.Livro;
import com.fatec.biblioteca.Reserva;
import com.fatec.biblioteca.Validador;
import com.fatec.biblioteca.dao.EmprestimoDAO;
import com.fatec.biblioteca.dao.LivroDAO;

// RF01 (cadastrar), RF04 (consultar ficha), alterar e excluir livros.
public class FrmLivro extends JFrame {

    private JTextField txtCodigo;
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JComboBox<Categoria> cbCategoria;
    private JTextField txtAno;
    private JTextField txtExemplares;
    private JTextArea txtFicha;

    private JButton btnConsultar;
    private JButton btnInserir;
    private JButton btnAlterar;
    private JButton btnExcluir;
    private JButton btnLimpar;
    private JButton btnListar;

    private LivroDAO dao;
    private EmprestimoDAO emprestimoDAO;

    public FrmLivro() {

        dao = new LivroDAO();
        emprestimoDAO = new EmprestimoDAO();

        setTitle("Cadastro de Livros");
        setSize(640, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel();
        painel.setLayout(new GridLayout(6, 2, 6, 6));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        painel.add(new JLabel("Código (100 a 9999)"));
        txtCodigo = new JTextField();
        painel.add(txtCodigo);

        painel.add(new JLabel("Título"));
        txtTitulo = new JTextField();
        painel.add(txtTitulo);

        painel.add(new JLabel("Autor"));
        txtAutor = new JTextField();
        painel.add(txtAutor);

        painel.add(new JLabel("Categoria"));
        cbCategoria = new JComboBox<>(Categoria.values());
        painel.add(cbCategoria);

        painel.add(new JLabel("Ano de publicação"));
        txtAno = new JTextField();
        painel.add(txtAno);

        painel.add(new JLabel("Exemplares (só no cadastro, 1 a 50)"));
        txtExemplares = new JTextField("1");
        painel.add(txtExemplares);

        txtFicha = new JTextArea(10, 40);
        txtFicha.setEditable(false);
        txtFicha.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        btnConsultar = new JButton("Consultar");
        btnInserir = new JButton("Inserir");
        btnAlterar = new JButton("Alterar");
        btnExcluir = new JButton("Excluir");
        btnLimpar = new JButton("Limpar");
        btnListar = new JButton("Listar");

        JPanel botoes = new JPanel();
        botoes.add(btnConsultar);
        botoes.add(btnInserir);
        botoes.add(btnAlterar);
        botoes.add(btnExcluir);
        botoes.add(btnLimpar);
        botoes.add(btnListar);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(new JScrollPane(txtFicha), BorderLayout.CENTER);
        sul.add(botoes, BorderLayout.SOUTH);

        add(painel, BorderLayout.NORTH);
        add(sul, BorderLayout.CENTER);

        configurarEventos();

        Estilo.padronizar(this);
    }

    private void configurarEventos() {
        btnInserir.addActionListener(e -> inserir());
        btnConsultar.addActionListener(e -> consultar());
        btnAlterar.addActionListener(e -> alterar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limparCampos());
        btnListar.addActionListener(e -> new FrmConsultaLivros().setVisible(true));
    }

    // Preenche o livro com o que foi digitado; os setters validam RN01–RN03.
    private Livro lerCampos() throws Exception {
        Livro livro = new Livro();
        livro.setCodigo(Validador.inteiro(txtCodigo.getText(), "Código"));
        livro.setTitulo(txtTitulo.getText());
        livro.setAutor(txtAutor.getText());
        livro.setCategoria((Categoria) cbCategoria.getSelectedItem());
        livro.setAnoPublicacao(Validador.inteiro(txtAno.getText(), "Ano de publicação"));
        return livro;
    }

    private void inserir() {
        try {
            if (dao.contar() >= Constantes.MAX_LIVROS) {
                throw new Exception("Capacidade máxima de " + Constantes.MAX_LIVROS + " títulos atingida.");
            }
            Livro livro = lerCampos();
            if (dao.carregar(livro.getCodigo()) != null) {
                throw new Exception("Já existe um livro com o código " + livro.getCodigo() + ".");
            }
            int qtd = Validador.inteiro(txtExemplares.getText(), "Exemplares");
            List<Copia> copias = livro.adicionarCopias(qtd); // RN04
            int retorno = dao.inserir(livro, copias);
            if (retorno > 0) {
                Mensagem.info(this, "Livro cadastrado com sucesso! " + qtd + " exemplar(es) disponível(is).");
                mostrarFicha(dao.carregar(livro.getCodigo()));
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void consultar() {
        try {
            int codigo = Validador.inteiro(txtCodigo.getText(), "Código");
            Livro livro = dao.carregar(codigo);
            if (livro == null) {
                txtFicha.setText("");
                Mensagem.info(this, "Livro não encontrado.");
                return;
            }
            txtTitulo.setText(livro.getTitulo());
            txtAutor.setText(livro.getAutor());
            cbCategoria.setSelectedItem(livro.getCategoria());
            txtAno.setText(String.valueOf(livro.getAnoPublicacao()));
            mostrarFicha(livro);
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    // RF04: ficha com exemplares, empréstimos em aberto e fila de reservas.
    private void mostrarFicha(Livro livro) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d - %s (%s, %s, %d)%n", livro.getCodigo(), livro.getTitulo(), livro.getAutor(),
                livro.getCategoria().getNome(), livro.getAnoPublicacao()));
        sb.append(String.format("Exemplares: total %d | disponíveis %d | emprestados %d | reservados %d%n",
                livro.getTotalCopias(), livro.getQtdDisponiveis(), livro.getQtdEmprestadas(), livro.getQtdReservadas()));
        sb.append("Cópias: ");
        for (Copia c : livro.getCopias()) {
            sb.append(c.getEtiqueta()).append(" [").append(c.getSituacao()).append("]  ");
        }
        sb.append("\n\nEmpréstimos em aberto:\n");
        List<Emprestimo> abertos = emprestimoDAO.listarAbertosPorLivro(livro.getCodigo());
        if (abertos.isEmpty()) {
            sb.append("  (nenhum)\n");
        }
        for (Emprestimo e : abertos) {
            sb.append(String.format("  nº %-4d cópia %-7s %-25s previsto %s%n", e.getCodigo(),
                    e.getCopia().getEtiqueta(), e.getLeitor().getNome(), Datas.formatar(e.getDataPrevista())));
        }
        sb.append("\nFila de reservas:\n");
        if (livro.getFilaReservas().isEmpty()) {
            sb.append("  (vazia)\n");
        }
        for (Reserva r : livro.getFilaReservas()) {
            sb.append(String.format("  %dº reserva nº %-4d %-25s %s%n", livro.getPosicaoNaFila(r), r.getCodigo(),
                    r.getLeitor().getNome(), r.getSituacao()));
        }
        txtFicha.setText(sb.toString());
        txtFicha.setCaretPosition(0);
    }

    private void alterar() {
        try {
            Livro livro = lerCampos();
            if (dao.carregar(livro.getCodigo()) == null) {
                throw new Exception("Livro não encontrado. Use Inserir para cadastrar.");
            }
            int retorno = dao.alterar(livro);
            if (retorno > 0) {
                Mensagem.info(this, "Livro alterado com sucesso!");
                mostrarFicha(dao.carregar(livro.getCodigo()));
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void excluir() {
        try {
            int codigo = Validador.inteiro(txtCodigo.getText(), "Código");
            if (dao.carregar(codigo) == null) {
                throw new Exception("Livro não encontrado.");
            }
            if (dao.possuiMovimentacao(codigo)) {
                throw new Exception("Este livro já teve empréstimos ou reservas e não pode ser excluído.");
            }
            if (!Mensagem.confirmar(this, "Excluir o livro " + codigo + " e todos os seus exemplares?")) {
                return;
            }
            if (dao.remover(codigo) > 0) {
                Mensagem.info(this, "Livro excluído com sucesso!");
                limparCampos();
            }
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }

    private void limparCampos() {
        txtCodigo.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        cbCategoria.setSelectedIndex(0);
        txtAno.setText("");
        txtExemplares.setText("1");
        txtFicha.setText("");
        txtCodigo.requestFocus();
    }
}
