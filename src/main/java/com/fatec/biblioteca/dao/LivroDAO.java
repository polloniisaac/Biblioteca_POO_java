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
import com.fatec.biblioteca.Livro;
import com.fatec.biblioteca.SituacaoCopia;

public class LivroDAO extends DAO {

    @Override
    public void criarNovaTabela() throws SQLException {
        executar("CREATE TABLE IF NOT EXISTS livro ("
                + " codigo INT NOT NULL,"
                + " titulo VARCHAR(150) NOT NULL,"
                + " autor VARCHAR(150) NOT NULL,"
                + " categoria TINYINT NOT NULL,"
                + " ano_publicacao SMALLINT NOT NULL,"
                + " CONSTRAINT pk_livro PRIMARY KEY (codigo),"
                + " CONSTRAINT ck_livro_codigo CHECK (codigo BETWEEN 100 AND 9999),"
                + " CONSTRAINT ck_livro_titulo CHECK (TRIM(titulo) <> ''),"
                + " CONSTRAINT ck_livro_autor CHECK (TRIM(autor) <> ''),"
                + " CONSTRAINT ck_livro_categoria CHECK (categoria BETWEEN 1 AND 5),"
                + " CONSTRAINT ck_livro_ano CHECK (ano_publicacao >= 1450)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    // Bancos criados antes da troca "Obra" -> "Livro": renomeia a tabela obra e as colunas obra_codigo,
    // mantendo os dados. Cada passo só roda se ainda for necessário. Retorna true se migrou algo.
    public boolean migrarTabelaObra() throws SQLException {
        boolean migrou = false;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (existe(conn, "obra", null) && !existe(conn, "livro", null)) {
                stmt.execute("RENAME TABLE obra TO livro");
                migrou = true;
            }
            if (existe(conn, "copia", "obra_codigo")) {
                stmt.execute("ALTER TABLE copia RENAME COLUMN obra_codigo TO livro_codigo");
                migrou = true;
            }
            if (existe(conn, "reserva", "viva_obra")) {
                stmt.execute("ALTER TABLE reserva DROP INDEX uk_res_viva, DROP COLUMN viva_obra");
                migrou = true;
            }
            if (existe(conn, "reserva", "obra_codigo")) {
                stmt.execute("ALTER TABLE reserva RENAME COLUMN obra_codigo TO livro_codigo");
                migrou = true;
            }
            if (existe(conn, "reserva", null) && !existe(conn, "reserva", "viva_livro")) {
                stmt.execute("ALTER TABLE reserva ADD COLUMN viva_livro INT AS"
                        + " (IF(situacao IN ('ATIVA','AGUARDANDO'), livro_codigo, NULL)) STORED AFTER viva_leitor,"
                        + " ADD CONSTRAINT uk_res_viva UNIQUE (viva_leitor, viva_livro)");
                migrou = true;
            }
        }
        return migrou;
    }

    // Confere no information_schema se a tabela (ou a coluna, quando informada) existe no banco atual.
    private boolean existe(Connection conn, String tabela, String coluna) throws SQLException {
        String sql = coluna == null
                ? "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?"
                : "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE()"
                        + " AND table_name = ? AND column_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tabela);
            if (coluna != null) {
                pstmt.setString(2, coluna);
            }
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    // RF01: grava o livro e as cópias iniciais na mesma transação.
    public int inserir(Livro obj, List<Copia> copias) throws SQLException {
        String sql = "INSERT INTO livro(codigo, titulo, autor, categoria, ano_publicacao) VALUES(?, ?, ?, ?, ?)";
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

    public int alterar(Livro obj) throws SQLException {
        String sql = "UPDATE livro SET titulo=?, autor=?, categoria=?, ano_publicacao=? WHERE codigo=?";
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

    public int remover(int codigo) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement("DELETE FROM copia WHERE livro_codigo=?");
                 PreparedStatement p2 = conn.prepareStatement("DELETE FROM livro WHERE codigo=?")) {
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

    public boolean possuiMovimentacao(int codigo) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM emprestimo e JOIN copia c ON c.codigo = e.copia_codigo"
                + " WHERE c.livro_codigo = ?) + (SELECT COUNT(*) FROM reserva WHERE livro_codigo = ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);
            pstmt.setInt(2, codigo);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public Livro carregar(int codigo) throws SQLException {
        try (Connection conn = getConnection()) {
            return carregar(conn, codigo);
        }
    }

    public Livro carregar(Connection conn, int codigo) throws SQLException {
        Livro obj = null;
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM livro WHERE codigo=?")) {
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

    // RF03/RF05: categoria null = todas
    public List<Livro> listar(Categoria categoria) throws SQLException {
        List<Livro> lista = new ArrayList<>();
        Map<Integer, Livro> porCodigo = new HashMap<>();
        String sql = "SELECT * FROM livro" + (categoria == null ? "" : " WHERE categoria=" + categoria.getCodigo())
                + " ORDER BY titulo";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Livro o = montar(rs);
                lista.add(o);
                porCodigo.put(o.getCodigo(), o);
            }
            ResultSet rc = stmt.executeQuery("SELECT * FROM copia ORDER BY livro_codigo, numero");
            while (rc.next()) {
                Livro o = porCodigo.get(rc.getInt("livro_codigo"));
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
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM livro");
            rs.next();
            return rs.getInt(1);
        }
    }

    private Livro montar(ResultSet rs) throws SQLException {
        return montarLivro(rs.getInt("codigo"), rs.getString("titulo"), rs.getString("autor"),
                rs.getInt("categoria"), rs.getInt("ano_publicacao"));
    }
}
