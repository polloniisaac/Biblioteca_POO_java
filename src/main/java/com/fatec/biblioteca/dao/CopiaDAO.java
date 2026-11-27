package com.fatec.biblioteca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fatec.biblioteca.Copia;
import com.fatec.biblioteca.Livro;
import com.fatec.biblioteca.SituacaoCopia;

public class CopiaDAO extends DAO {

    @Override
    public void criarNovaTabela() throws SQLException {
        executar("CREATE TABLE IF NOT EXISTS copia ("
                + " codigo INT NOT NULL AUTO_INCREMENT,"
                + " livro_codigo INT NOT NULL,"
                + " numero INT NOT NULL,"
                + " situacao VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',"
                + " CONSTRAINT pk_copia PRIMARY KEY (codigo),"
                + " CONSTRAINT fk_copia_livro FOREIGN KEY (livro_codigo) REFERENCES livro (codigo),"
                + " CONSTRAINT uk_copia_livro_numero UNIQUE (livro_codigo, numero),"
                + " CONSTRAINT ck_copia_numero CHECK (numero >= 1),"
                + " CONSTRAINT ck_copia_situacao CHECK (situacao IN ('DISPONIVEL','EMPRESTADA','RESERVADA'))"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    public void inserir(Connection conn, Copia c) throws SQLException {
        String sql = "INSERT INTO copia(livro_codigo, numero, situacao) VALUES(?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, c.getLivro().getCodigo());
            pstmt.setInt(2, c.getNumero());
            pstmt.setString(3, c.getSituacao().name());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                c.setCodigo(rs.getInt(1));
            }
        }
    }

    // RF02: grava as cópias e já separa para a fila de reservas
    public int adicionarCopias(Livro livro, List<Copia> novas) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Copia c : novas) {
                    inserir(conn, c);
                }
                int separadas = new ReservaDAO().atualizarFila(conn, livro.getCodigo(), LocalDate.now()).size();
                conn.commit();
                return separadas;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void atualizarSituacao(Connection conn, Copia c) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE copia SET situacao=? WHERE codigo=?")) {
            pstmt.setString(1, c.getSituacao().name());
            pstmt.setInt(2, c.getCodigo());
            pstmt.executeUpdate();
        }
    }

    // Preenche a lista de cópias do livro (composição).
    public void carregarCopias(Connection conn, Livro livro) throws SQLException {
        String sql = "SELECT * FROM copia WHERE livro_codigo=? ORDER BY numero";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, livro.getCodigo());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Copia c = new Copia();
                c.setCodigo(rs.getInt("codigo"));
                c.setNumero(rs.getInt("numero"));
                c.setSituacao(SituacaoCopia.valueOf(rs.getString("situacao")));
                livro.adicionarCopiaCarregada(c);
            }
        }
    }

    // RF15 / RN18: confere a situação de cada cópia
    public List<String> verificarConsistencia() throws SQLException {
        List<String> avisos = new ArrayList<>();
        String sql = "SELECT c.codigo, c.livro_codigo, c.numero, c.situacao,"
                + " CASE WHEN EXISTS (SELECT 1 FROM emprestimo e WHERE e.copia_codigo = c.codigo"
                + "                   AND e.data_devolucao IS NULL) THEN 'EMPRESTADA'"
                + "      WHEN EXISTS (SELECT 1 FROM reserva r WHERE r.copia_codigo = c.codigo"
                + "                   AND r.situacao = 'AGUARDANDO') THEN 'RESERVADA'"
                + "      ELSE 'DISPONIVEL' END AS esperado"
                + " FROM copia c";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<Copia> corrigir = new ArrayList<>();
            while (rs.next()) {
                String gravado = rs.getString("situacao");
                String esperado = rs.getString("esperado");
                if (!gravado.equals(esperado)) {
                    avisos.add("AVISO: livro " + rs.getInt("livro_codigo") + " - cópia " + rs.getInt("livro_codigo")
                            + "-" + rs.getInt("numero") + " estava " + gravado
                            + "; pelos empréstimos/reservas deveria estar " + esperado + ". Situação corrigida.");
                    Copia c = new Copia();
                    c.setCodigo(rs.getInt("codigo"));
                    c.setSituacao(SituacaoCopia.valueOf(esperado));
                    corrigir.add(c);
                }
            }
            for (Copia c : corrigir) {
                atualizarSituacao(conn, c);
            }
        }
        return avisos;
    }

    public int contar() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM copia");
            rs.next();
            return rs.getInt(1);
        }
    }
}
