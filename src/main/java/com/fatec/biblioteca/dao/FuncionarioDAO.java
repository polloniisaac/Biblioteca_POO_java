package com.fatec.biblioteca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.fatec.biblioteca.Funcionario;

/** Tabela funcionario (pedido do professor de POO). Senha guardada como hash SHA-256. */
public class FuncionarioDAO extends DAO {

    @Override
    public void criarNovaTabela() throws SQLException {
        executar("CREATE TABLE IF NOT EXISTS funcionario ("
                + " codigo INT NOT NULL AUTO_INCREMENT,"
                + " nome VARCHAR(100) NOT NULL,"
                + " cargo TINYINT NOT NULL,"
                + " login VARCHAR(30) NOT NULL,"
                + " senha_hash CHAR(64) NOT NULL,"
                + " CONSTRAINT pk_funcionario PRIMARY KEY (codigo),"
                + " CONSTRAINT uk_funcionario_login UNIQUE (login),"
                + " CONSTRAINT ck_funcionario_nome CHECK (TRIM(nome) <> ''),"
                + " CONSTRAINT ck_funcionario_cargo CHECK (cargo IN (1, 2))"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    /** Primeira execução: cria o usuário admin/admin (Bibliotecário). Devolve true se criou. */
    public boolean garantirAdministrador() throws SQLException {
        if (contar() > 0) {
            return false;
        }
        String sql = "INSERT INTO funcionario(nome, cargo, login, senha_hash) VALUES(?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "Administrador");
            pstmt.setInt(2, Funcionario.BIBLIOTECARIO);
            pstmt.setString(3, "admin");
            pstmt.setString(4, Funcionario.gerarHash("admin"));
            pstmt.executeUpdate();
            return true;
        }
    }

    public int inserir(Funcionario obj) throws SQLException {
        String sql = "INSERT INTO funcionario(nome, cargo, login, senha_hash) VALUES(?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, obj.getNome());
            pstmt.setInt(2, obj.getCargo());
            pstmt.setString(3, obj.getLogin());
            pstmt.setString(4, obj.getSenhaHash());
            int retorno = pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                try {
                    obj.setCodigo(rs.getInt(1));
                } catch (Exception e) {
                    throw new SQLException(e.getMessage());
                }
            }
            return retorno;
        }
    }

    /** Altera nome, cargo e login. A senha só muda se senhaHash não for null. */
    public int alterar(Funcionario obj) throws SQLException {
        boolean trocaSenha = obj.getSenhaHash() != null;
        String sql = "UPDATE funcionario SET nome=?, cargo=?, login=?" + (trocaSenha ? ", senha_hash=?" : "")
                + " WHERE codigo=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int i = 1;
            pstmt.setString(i++, obj.getNome());
            pstmt.setInt(i++, obj.getCargo());
            pstmt.setString(i++, obj.getLogin());
            if (trocaSenha) {
                pstmt.setString(i++, obj.getSenhaHash());
            }
            pstmt.setInt(i, obj.getCodigo());
            return pstmt.executeUpdate();
        }
    }

    public int alterarSenha(int codigo, String senhaHash) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE funcionario SET senha_hash=? WHERE codigo=?")) {
            pstmt.setString(1, senhaHash);
            pstmt.setInt(2, codigo);
            return pstmt.executeUpdate();
        }
    }

    public int remover(int codigo) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM funcionario WHERE codigo=?")) {
            pstmt.setInt(1, codigo);
            return pstmt.executeUpdate();
        }
    }

    public boolean possuiMovimentacao(int codigo) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM emprestimo WHERE funcionario_emprestimo = ?"
                + " OR funcionario_devolucao = ?) + (SELECT COUNT(*) FROM reserva WHERE funcionario_codigo = ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);
            pstmt.setInt(2, codigo);
            pstmt.setInt(3, codigo);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public int contarBibliotecarios() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM funcionario WHERE cargo = "
                    + Funcionario.BIBLIOTECARIO);
            rs.next();
            return rs.getInt(1);
        }
    }

    public Funcionario carregar(int codigo) throws SQLException {
        return carregarPor("codigo", codigo);
    }

    public Funcionario carregarPorLogin(String login) throws SQLException {
        return carregarPor("login", login == null ? "" : login.trim().toLowerCase());
    }

    private Funcionario carregarPor(String coluna, Object valor) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM funcionario WHERE " + coluna + "=?")) {
            pstmt.setObject(1, valor);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? montar(rs) : null;
        }
    }

    public List<Funcionario> listar() throws SQLException {
        List<Funcionario> lista = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM funcionario ORDER BY nome");
            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    public int contar() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM funcionario");
            rs.next();
            return rs.getInt(1);
        }
    }

    private Funcionario montar(ResultSet rs) throws SQLException {
        Funcionario f = montarFuncionario(rs.getInt("codigo"), rs.getString("nome"), rs.getInt("cargo"));
        try {
            f.setLogin(rs.getString("login"));
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
        f.setSenhaHash(rs.getString("senha_hash"));
        return f;
    }
}
