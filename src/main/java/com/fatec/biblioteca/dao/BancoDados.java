package com.fatec.biblioteca.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// RF15: cria as tabelas, o usuário admin e confere as cópias (RN18)
public class BancoDados {

    public List<String> inicializar() throws SQLException {
        List<String> mensagens = new ArrayList<>();
        LivroDAO livroDAO = new LivroDAO();
        CopiaDAO copiaDAO = new CopiaDAO();
        LeitorDAO leitorDAO = new LeitorDAO();
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        EmprestimoDAO emprestimoDAO = new EmprestimoDAO();
        ReservaDAO reservaDAO = new ReservaDAO();

        if (livroDAO.migrarTabelaObra()) {
            mensagens.add("Banco atualizado: tabela \"obra\" renomeada para \"livro\" (dados mantidos).");
        }
        livroDAO.criarNovaTabela();
        copiaDAO.criarNovaTabela();
        leitorDAO.criarNovaTabela();
        funcionarioDAO.criarNovaTabela();
        emprestimoDAO.criarNovaTabela();
        reservaDAO.criarNovaTabela();

        if (funcionarioDAO.garantirAdministrador()) {
            mensagens.add("Primeira execução: criado o usuário \"admin\" com senha \"admin\" (Bibliotecário)."
                    + " Troque a senha em Sistema > Alterar minha senha.");
        }
        int livros = livroDAO.contar();
        int leitores = leitorDAO.contar();
        int emprestimos = emprestimoDAO.contar();
        if (livros == 0) {
            mensagens.add("Nenhum livro cadastrado. Iniciando com acervo vazio.");
        }
        if (leitores == 0) {
            mensagens.add("Nenhum leitor cadastrado. Iniciando com cadastro vazio.");
        }
        mensagens.addAll(copiaDAO.verificarConsistencia());
        mensagens.add(String.format("Dados carregados: %d livro(s), %d cópia(s), %d leitor(es), %d empréstimo(s)."
                + " Próximo código de empréstimo: %d.",
                livros, copiaDAO.contar(), leitores, emprestimos, emprestimoDAO.proximoCodigo()));
        return mensagens;
    }
}
