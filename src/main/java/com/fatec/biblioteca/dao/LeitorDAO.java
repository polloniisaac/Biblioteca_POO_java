package com.fatec.biblioteca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.fatec.biblioteca.Leitor;

/** Tabela leitor (leitores.txt). Empréstimos em aberto são contados, não gravados. */
public class LeitorDAO extends DAO {

    @Override
    public void criarNovaTabela() throws SQLException {
        executar("CREATE TABLE IF NOT EXISTS leitor ("
                + " codigo INT NOT NULL,"
                + " nome VARCHAR(100) NOT NULL,"
                + " tipo TINYINT NOT NULL,"
                + " CONSTRAINT pk_leitor PRIMARY KEY (codigo),"
                + " CONSTRAINT ck_leitor_codigo CHECK (codigo BETWEEN 1 AND 9999),"
                + " CONSTRAINT ck_leitor_nome CHECK (TRIM(nome) <> ''),"
                + " CONSTRAINT ck_leitor_tipo CHECK (tipo BETWEEN 1 AND 3)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    public int inserir(Leitor obj) throws SQLException {
        String sql = "INSERT INTO leitor(codigo, nome, tipo) VALUES(?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, obj.getCodigo());
            pstmt.setString(2, obj.getNome());
            pstmt.setInt(3, obj.getTipo());
            return pstmt.executeUpdate();
        }
    }

    public int alterar(Leitor obj) throws SQLException {
        String sql = "UPDATE leitor SET nome=?, tipo=? WHERE codigo=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, obj.getNome());
            pstmt.setInt(2, obj.getTipo());
            pstmt.setInt(3, obj.getCodigo());
            return pstmt.executeUpdate();
        }
    }

    public int remover(int codigo) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM leitor WHERE codigo=?")) {
            pstmt.setInt(1, codigo);
            return pstmt.executeUpdate();
        }
    }

    public boolean existe(int codigo) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM leitor WHERE codigo=?")) {
            pstmt.setInt(1, codigo);
            return pstmt.executeQuery().next();
        }
    }

    public boolean possuiMovimentacao(int codigo) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM emprestimo WHERE leitor_codigo = ?)"
                + " + (SELECT COUNT(*) FROM reserva WHERE leitor_codigo = ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);
            pstmt.setInt(2, codigo);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    /** Carrega o leitor com a lista dos empréstimos em aberto (agregação), ou null. */
    public Leitor carregar(int codigo) throws SQLException {
        Leitor obj = null;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM leitor WHERE codigo=?")) {
            pstmt.setInt(1, codigo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                obj = montarLeitor(rs.getInt("codigo"), rs.getString("nome"), rs.getInt("tipo"));
            }
        }
        if (obj != null) {
            obj.setEmprestimosAbertos(new EmprestimoDAO().listarAbertosPorLeitor(obj));
        }
        return obj;
    }

    /** RF07: leitores com a quantidade de empréstimos em aberto. */
    public List<Leitor> listar() throws SQLException {
        List<Leitor> lista = new ArrayList<>();
        String sql = "SELECT l.*, (SELECT COUNT(*) FROM emprestimo e WHERE e.leitor_codigo = l.codigo"
                + " AND e.data_devolucao IS NULL) AS abertos FROM leitor l ORDER BY nome";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Leitor l = montarLeitor(rs.getInt("codigo"), rs.getString("nome"), rs.getInt("tipo"));
                l.setQtdEmprestimosAbertos(rs.getInt("abertos"));
                lista.add(l);
            }
        }
        return lista;
    }

    public int contar() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM leitor");
            rs.next();
            return rs.getInt(1);
        }
    }
}
