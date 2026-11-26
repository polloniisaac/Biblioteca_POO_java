package com.fatec.biblioteca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fatec.biblioteca.Categoria;
import com.fatec.biblioteca.Copia;
import com.fatec.biblioteca.Obra;
import com.fatec.biblioteca.SituacaoCopia;

/** Tabela obra (parte "título" do livros.txt). As cópias ficam em CopiaDAO. */
public class ObraDAO extends DAO {

    @Override
    public void criarNovaTabela() throws SQLException {
        executar("CREATE TABLE IF NOT EXISTS obra ("
                + " codigo INT NOT NULL,"
                + " titulo VARCHAR(150) NOT NULL,"
                + " autor VARCHAR(150) NOT NULL,"
                + " categoria TINYINT NOT NULL,"
                + " ano_publicacao SMALLINT NOT NULL,"
                + " CONSTRAINT pk_obra PRIMARY KEY (codigo),"
                + " CONSTRAINT ck_obra_codigo CHECK (codigo BETWEEN 100 AND 9999),"
                + " CONSTRAINT ck_obra_titulo CHECK (TRIM(titulo) <> ''),"
                + " CONSTRAINT ck_obra_autor CHECK (TRIM(autor) <> ''),"
                + " CONSTRAINT ck_obra_categoria CHECK (categoria BETWEEN 1 AND 5),"
                + " CONSTRAINT ck_obra_ano CHECK (ano_publicacao >= 1450)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    /** RF01: grava a obra e as cópias iniciais na mesma transação. */
    public int inserir(Obra obj, List<Copia> copias) throws SQLException {
        String sql = "INSERT INTO obra(codigo, titulo, autor, categoria, ano_publicacao) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, obj.getCodigo());
                pstmt.setString(2, obj.getTitulo());
                pstmt.setString(3, obj.getAutor());
                pstmt.setInt(4, obj.getCategoria().getCodigo());
                pstmt.setInt(5, obj.getAnoPublicacao());
                int retorno = pstmt.executeUpdate();
                CopiaDAO copiaDAO = new CopiaDAO();
                for (Copia c : copias) {
                    copiaDAO.inserir(conn, c);
                }
                conn.commit();
                return retorno;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int alterar(Obra obj) throws SQLException {
        String sql = "UPDATE obra SET titulo=?, autor=?, categoria=?, ano_publicacao=? WHERE codigo=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, obj.getTitulo());
            pstmt.setString(2, obj.getAutor());
            pstmt.setInt(3, obj.getCategoria().getCodigo());
            pstmt.setInt(4, obj.getAnoPublicacao());
            pstmt.setInt(5, obj.getCodigo());
            return pstmt.executeUpdate();
        }
    }

    /** Remove a obra e suas cópias. A tela verifica antes possuiMovimentacao(). */
    public int remover(int codigo) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement("DELETE FROM copia WHERE obra_codigo=?");
                 PreparedStatement p2 = conn.prepareStatement("DELETE FROM obra WHERE codigo=?")) {
                p1.setInt(1, codigo);
                p1.executeUpdate();
                p2.setInt(1, codigo);
                int retorno = p2.executeUpdate();
                conn.commit();
                return retorno;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** A obra já teve empréstimo ou reserva? (então não pode ser excluída) */
    public boolean possuiMovimentacao(int codigo) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM emprestimo e JOIN copia c ON c.codigo = e.copia_codigo"
                + " WHERE c.obra_codigo = ?) + (SELECT COUNT(*) FROM reserva WHERE obra_codigo = ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);
            pstmt.setInt(2, codigo);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    /** Carrega a obra com cópias e fila de reservas, ou null se não existir. */
    public Obra carregar(int codigo) throws SQLException {
        try (Connection conn = getConnection()) {
            return carregar(conn, codigo);
        }
    }

    /** Versão que usa uma conexão já aberta (para transações). */
    public Obra carregar(Connection conn, int codigo) throws SQLException {
        Obra obj = null;
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM obra WHERE codigo=?")) {
            pstmt.setInt(1, codigo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                obj = montar(rs);
            }
        }
        if (obj != null) {
            new CopiaDAO().carregarCopias(conn, obj);
            new ReservaDAO().carregarFila(conn, obj);
        }
        return obj;
    }

    /** RF03/RF05: lista as obras (com cópias) ordenadas por título. categoria null = todas. */
    public List<Obra> listar(Categoria categoria) throws SQLException {
        List<Obra> lista = new ArrayList<>();
        Map<Integer, Obra> porCodigo = new HashMap<>();
        String sql = "SELECT * FROM obra" + (categoria == null ? "" : " WHERE categoria=" + categoria.getCodigo())
                + " ORDER BY titulo";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Obra o = montar(rs);
                lista.add(o);
                porCodigo.put(o.getCodigo(), o);
            }
            ResultSet rc = stmt.executeQuery("SELECT * FROM copia ORDER BY obra_codigo, numero");
            while (rc.next()) {
                Obra o = porCodigo.get(rc.getInt("obra_codigo"));
                if (o != null) {
                    Copia c = new Copia();
                    c.setCodigo(rc.getInt("codigo"));
                    c.setNumero(rc.getInt("numero"));
                    c.setSituacao(SituacaoCopia.valueOf(rc.getString("situacao")));
                    o.adicionarCopiaCarregada(c);
                }
            }
        }
        return lista;
    }

    public int contar() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM obra");
            rs.next();
            return rs.getInt(1);
        }
    }

    private Obra montar(ResultSet rs) throws SQLException {
        return montarObra(rs.getInt("codigo"), rs.getString("titulo"), rs.getString("autor"),
                rs.getInt("categoria"), rs.getInt("ano_publicacao"));
    }
}
