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
import com.fatec.biblioteca.Reserva;
import com.fatec.biblioteca.SituacaoCopia;
import com.fatec.biblioteca.SituacaoReserva;

public class ReservaDAO extends DAO {

    private static final String SELECT = "SELECT r.*, l.nome AS leitor_nome, l.tipo AS leitor_tipo,"
            + " f.nome AS f_nome, f.cargo AS f_cargo,"
            + " o.titulo, o.autor, o.categoria, o.ano_publicacao, cp.numero AS copia_numero"
            + " FROM reserva r"
            + " LEFT JOIN copia cp ON cp.codigo = r.copia_codigo"
            + " JOIN leitor l ON l.codigo = r.leitor_codigo"
            + " JOIN funcionario f ON f.codigo = r.funcionario_codigo"
            + " JOIN livro o ON o.codigo = r.livro_codigo";

    @Override
    public void criarNovaTabela() throws SQLException {
        executar("CREATE TABLE IF NOT EXISTS reserva ("
                + " codigo INT NOT NULL AUTO_INCREMENT,"
                + " livro_codigo INT NOT NULL,"
                + " leitor_codigo INT NOT NULL,"
                + " funcionario_codigo INT NOT NULL,"
                + " data_reserva DATE NOT NULL,"
                + " copia_codigo INT NULL,"
                + " data_limite_retirada DATE NULL,"
                + " emprestimo_codigo INT NULL,"
                + " situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVA',"
                + " viva_leitor INT AS (IF(situacao IN ('ATIVA','AGUARDANDO'), leitor_codigo, NULL)) STORED,"
                + " viva_livro INT AS (IF(situacao IN ('ATIVA','AGUARDANDO'), livro_codigo, NULL)) STORED,"
                + " CONSTRAINT pk_reserva PRIMARY KEY (codigo),"
                + " CONSTRAINT fk_res_livro FOREIGN KEY (livro_codigo) REFERENCES livro (codigo),"
                + " CONSTRAINT fk_res_leitor FOREIGN KEY (leitor_codigo) REFERENCES leitor (codigo),"
                + " CONSTRAINT fk_res_func FOREIGN KEY (funcionario_codigo) REFERENCES funcionario (codigo),"
                + " CONSTRAINT fk_res_copia FOREIGN KEY (copia_codigo) REFERENCES copia (codigo),"
                + " CONSTRAINT fk_res_emp FOREIGN KEY (emprestimo_codigo) REFERENCES emprestimo (codigo),"
                + " CONSTRAINT ck_res_situacao CHECK (situacao IN"
                + "   ('ATIVA','AGUARDANDO','ATENDIDA','CANCELADA','EXPIRADA')),"
                + " CONSTRAINT uk_res_viva UNIQUE (viva_leitor, viva_livro),"
                + " INDEX idx_res_fila (livro_codigo, situacao, codigo)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    public int inserir(Reserva obj) throws SQLException {
        String sql = "INSERT INTO reserva(livro_codigo, leitor_codigo, funcionario_codigo, data_reserva, situacao)"
                + " VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, obj.getLivro().getCodigo());
            pstmt.setInt(2, obj.getLeitor().getCodigo());
            pstmt.setInt(3, obj.getFuncionario().getCodigo());
            pstmt.setDate(4, paraSql(obj.getDataReserva()));
            pstmt.setString(5, obj.getSituacao().name());
            int retorno = pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                obj.setCodigo(rs.getInt(1));
            }
            obj.getLivro().getFilaReservas().add(obj);
            return retorno;
        }
    }

    public void atualizar(Connection conn, Reserva r) throws SQLException {
        String sql = "UPDATE reserva SET situacao=?, copia_codigo=?, data_limite_retirada=?, emprestimo_codigo=?"
                + " WHERE codigo=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, r.getSituacao().name());
            pstmt.setObject(2, r.getCopiaSeparada() == null ? null : r.getCopiaSeparada().getCodigo());
            pstmt.setDate(3, paraSql(r.getDataLimiteRetirada()));
            pstmt.setObject(4, r.getCodigoEmprestimo() == 0 ? null : r.getCodigoEmprestimo());
            pstmt.setInt(5, r.getCodigo());
            pstmt.executeUpdate();
        }
    }

    // R7: grava o cancelamento e anda a fila
    public List<Reserva> salvarCancelamento(Reserva r, LocalDate data) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                atualizar(conn, r);
                if (r.getCopiaSeparada() != null) {
                    new CopiaDAO().atualizarSituacao(conn, r.getCopiaSeparada());
                }
                List<Reserva> separadas = atualizarFila(conn, r.getLivro().getCodigo(), data);
                conn.commit();
                return separadas;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // R4 e R6: expira reservas vencidas e separa cópias para a fila
    public List<Reserva> atualizarFila(Connection conn, int codigoLivro, LocalDate data) throws SQLException {
        List<Reserva> separadas = new ArrayList<>();
        Livro livro = new LivroDAO().carregar(conn, codigoLivro);
        if (livro == null) {
            return separadas;
        }
        CopiaDAO copiaDAO = new CopiaDAO();
        for (Reserva r : livro.getFilaReservas()) {
            if (r.prazoVencido(data)) {
                r.expirar();
                atualizar(conn, r);
                copiaDAO.atualizarSituacao(conn, r.getCopiaSeparada());
            }
        }
        for (Reserva r : livro.getFilaReservas()) {
            if (r.getSituacao() != SituacaoReserva.ATIVA) {
                continue;
            }
            Copia livre = null;
            for (Copia c : livro.getCopias()) {
                if (c.getSituacao() == SituacaoCopia.DISPONIVEL) {
                    livre = c;
                    break;
                }
            }
            if (livre == null) {
                break;
            }
            try {
                r.separarCopia(livre, data);
            } catch (Exception e) {
                throw new SQLException(e.getMessage());
            }
            atualizar(conn, r);
            copiaDAO.atualizarSituacao(conn, livre);
            separadas.add(r);
        }
        return separadas;
    }

    // R6: expira as reservas com prazo de retirada vencido
    public void expirarVencidas(LocalDate data) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Integer> livros = new ArrayList<>();
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT DISTINCT livro_codigo FROM reserva"
                        + " WHERE situacao = 'AGUARDANDO' AND data_limite_retirada < ?")) {
                    pstmt.setDate(1, paraSql(data));
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        livros.add(rs.getInt(1));
                    }
                }
                for (int cod : livros) {
                    atualizarFila(conn, cod, data);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Preenche a fila de reservas vivas do livro (agregação), em ordem de chegada.
    public void carregarFila(Connection conn, Livro livro) throws SQLException {
        String sql = SELECT + " WHERE r.livro_codigo = ? AND r.situacao IN ('ATIVA','AGUARDANDO') ORDER BY r.codigo";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, livro.getCodigo());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                livro.getFilaReservas().add(montar(rs, livro));
            }
        }
    }

    public Reserva carregarViva(int codigo) throws SQLException {
        int codigoLivro;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT livro_codigo FROM reserva WHERE codigo=?")) {
            pstmt.setInt(1, codigo);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            codigoLivro = rs.getInt(1);
        }
        Livro livro = new LivroDAO().carregar(codigoLivro);
        for (Reserva r : livro.getFilaReservas()) {
            if (r.getCodigo() == codigo) {
                return r;
            }
        }
        return null;
    }

    public List<Reserva> listar(boolean somenteVivas) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = SELECT + (somenteVivas ? " WHERE r.situacao IN ('ATIVA','AGUARDANDO')" : "")
                + " ORDER BY r.livro_codigo, r.codigo";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Livro livro = montarLivro(rs.getInt("livro_codigo"), rs.getString("titulo"), rs.getString("autor"),
                        rs.getInt("categoria"), rs.getInt("ano_publicacao"));
                lista.add(montar(rs, livro));
            }
        }
        return lista;
    }

    private Reserva montar(ResultSet rs, Livro livro) throws SQLException {
        Reserva r = new Reserva();
        r.setCodigo(rs.getInt("codigo"));
        r.setLivro(livro);
        r.setLeitor(montarLeitor(rs.getInt("leitor_codigo"), rs.getString("leitor_nome"), rs.getInt("leitor_tipo")));
        r.setFuncionario(montarFuncionario(rs.getInt("funcionario_codigo"), rs.getString("f_nome"),
                rs.getInt("f_cargo")));
        r.setDataReserva(paraLocalDate(rs.getDate("data_reserva")));
        int copia = rs.getInt("copia_codigo");
        if (!rs.wasNull()) {
            Copia c = livro.getCopia(copia);
            if (c == null) {
                c = new Copia();
                c.setCodigo(copia);
                c.setNumero(rs.getInt("copia_numero"));
                c.setLivro(livro);
            }
            r.setCopiaSeparada(c);
        }
        r.setDataLimiteRetirada(paraLocalDate(rs.getDate("data_limite_retirada")));
        r.setCodigoEmprestimo(rs.getInt("emprestimo_codigo"));
        r.setSituacao(SituacaoReserva.valueOf(rs.getString("situacao")));
        return r;
    }
}
