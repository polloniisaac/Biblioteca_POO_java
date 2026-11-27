package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import com.fatec.biblioteca.Categoria;
import com.fatec.biblioteca.PerfilUso;
import com.fatec.biblioteca.dao.EmprestimoDAO;

// RF13: matriz tipo de leitor x categoria
public class FrmRelPerfilUso extends JFrame {

    private Tabela modelo;
    private JTextArea txtResumo;
    private JButton btnAtualizar;

    private EmprestimoDAO dao;

    public FrmRelPerfilUso() {

        dao = new EmprestimoDAO();

        setTitle("Relatório - Perfil de Uso (tipo de leitor x categoria)");
        setSize(900, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String[] colunas = new String[7];
        colunas[0] = "Tipo";
        for (Categoria c : Categoria.values()) {
            colunas[c.getCodigo()] = c.getNome();
        }
        colunas[6] = "Total";
        modelo = new Tabela(colunas);

        txtResumo = new JTextArea(5, 40);
        txtResumo.setEditable(false);
        btnAtualizar = new JButton("Atualizar");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(btnAtualizar);
        JPanel sul = new JPanel(new BorderLayout());
        sul.add(new JScrollPane(txtResumo), BorderLayout.CENTER);
        sul.add(botoes, BorderLayout.SOUTH);

        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);

        btnAtualizar.addActionListener(e -> carregarDados());

        carregarDados();

        Estilo.padronizar(this);
    }

    private void carregarDados() {
        try {
            PerfilUso perfil = dao.carregarPerfilUso();
            modelo.setRowCount(0);
            for (int t = 0; t < 3; t++) {
                Object[] linha = new Object[7];
                linha[0] = PerfilUso.NOMES_TIPOS[t];
                for (int c = 0; c < 5; c++) {
                    linha[c + 1] = perfil.getValor(t, c);
                }
                linha[6] = perfil.totalLinha(t);
                modelo.addRow(linha);
            }
            Object[] totais = new Object[7];
            totais[0] = "Total";
            for (int c = 0; c < 5; c++) {
                totais[c + 1] = perfil.totalColuna(c);
            }
            totais[6] = perfil.totalGeral();
            modelo.addRow(totais);

            StringBuilder sb = new StringBuilder("Mais emprestada - ");
            for (int t = 0; t < 3; t++) {
                Categoria c = perfil.categoriaPreferida(t);
                sb.append(PerfilUso.NOMES_TIPOS[t]).append(": ").append(c == null ? "-" : c.getNome())
                  .append(t < 2 ? " | " : "\n");
            }
            Categoria geral = perfil.categoriaMaisProcurada();
            sb.append("Categoria mais procurada no geral: ")
              .append(geral == null ? "-" : geral.getNome() + " (" + perfil.totalColuna(geral.getIndice())
                      + " empréstimos)")
              .append("\n(Em caso de empate, vale a primeira categoria na ordem da tabela.)");
            txtResumo.setText(sb.toString());
        } catch (Exception err) {
            Mensagem.erro(this, err);
        }
    }
}
