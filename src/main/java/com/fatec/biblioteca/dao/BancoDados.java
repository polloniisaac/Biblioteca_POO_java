package com.fatec.biblioteca.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// RF15: cria as tabelas, o usuário admin e confere as cópias (RN18)
public class BancoDados {

    public List<String> inicializar() throws SQLException {
        List<String> mensagens = new ArrayList<>();
        ObraDAO obraDAO = new ObraDAO();
        CopiaDAO copiaDAO = new CopiaDAO();
        LeitorDAO leitorDAO = new LeitorDAO();
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        EmprestimoDAO emprestimoDAO = new EmprestimoDAO();
        ReservaDAO reservaDAO = new ReservaDAO();

        obraDAO.criarNovaTabela();
        copiaDAO.criarNovaTabela();
        leitorDAO.criarNovaTabela();
        funcionarioDAO.criarNovaTabela();
        emprestimoDAO.criarNovaTabela();
        reservaDAO.criarNovaTabela();

        if (funcionarioDAO.garantirAdministrador()) {
            mensagens.add("Primeira execução: criado o usuário \"admin\" com senha \"admin\" (Bibliotecário)."
                    + " Troque a senha em Sistema > Alterar minha senha.");
        }
        int obras = obraDAO.contar();
        int leitores = leitorDAO.contar();
        int emprestimos = emprestimoDAO.contar();
        if (obras == 0) {
            mensagens.add("Nenhuma obra cadastrada. Iniciando com acervo vazio.");
        }
        if (leitores == 0) {
            mensagens.add("Nenhum leitor cadastrado. Iniciando com cadastro vazio.");
        }
        mensagens.addAll(copiaDAO.verificarConsistencia());
        mensagens.add(String.format("Dados carregados: %d obra(s), %d cópia(s), %d leitor(es), %d empréstimo(s)."
                + " Próximo código de empréstimo: %d.",
                obras, copiaDAO.contar(), leitores, emprestimos, emprestimoDAO.proximoCodigo()));
        return mensagens;
    }
}
