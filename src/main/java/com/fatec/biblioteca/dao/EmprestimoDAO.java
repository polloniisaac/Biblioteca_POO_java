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
import com.fatec.biblioteca.Emprestimo;
import com.fatec.biblioteca.Leitor;
import com.fatec.biblioteca.Obra;
import com.fatec.biblioteca.PerfilUso;
import com.fatec.biblioteca.Reserva;
import com.fatec.biblioteca.SituacaoCopia;

/** Tabela emprestimo (emprestimos.txt), agora apontando para a cópia e para o funcionário. */
public class EmprestimoDAO extends DAO {

    private static final String SELECT = "SELECT e.*, c.numero, c.situacao AS copia_situacao, c.obra_codigo,"
            + " o.titulo, o.autor, o.categoria, o.ano_publicacao,"
            + " l.nome AS leitor_nome, l.tipo AS leitor_tipo,"
            + " fe.nome AS fe_nome, fe.cargo AS fe_cargo, fd.nome AS fd_nome, fd.cargo AS fd_cargo"
            + " FROM emprestimo e"
            + " JOIN copia c ON c.codigo = e.copia_codigo"
            + " JOIN obra o ON o.codigo = c.obra_codigo"
            + " JOIN leitor l ON l.codigo = e.leitor_codigo"
            + " JOIN funcionario fe ON fe.codigo = e.funcionario_emprestimo"
            + " LEFT JOIN funcionario fd ON fd.codigo = e.funcionario_devolucao";

    @Override
    public void criarNovaTabela() throws SQLException {
        executar("CREATE TABLE IF NOT EXISTS emprestimo ("
                + " codigo INT NOT NULL AUTO_INCREMENT,"
                + " copia_codigo INT NOT NULL,"
                + " leitor_codigo INT NOT NULL,"
                + " funcionario_emprestimo INT NOT NULL,"
                + " funcionario_devolucao INT NULL,"
                + " data_emprestimo DATE NOT NULL,"
                + " data_prevista DATE NOT NULL,"
                + " data_devolucao DATE NULL,"
                + " multa DECIMAL(8,2) NOT NULL DEFAULT 0.00,"
                // coluna calculada: só existe valor enquanto o empréstimo está aberto
                + " copia_aberta INT AS (IF(data_devolucao IS NULL, copia_codigo, NULL)) STORED,"
                + " CONSTRAINT pk_emprestimo PRIMARY KEY (codigo),"
                + " CONSTRAINT fk_emp_copia FOREIGN KEY (copia_codigo) REFERENCES copia (codigo),"
                + " CONSTRAINT fk_emp_leitor FOREIGN KEY (leitor_codigo) REFERENCES leitor (codigo),"
                + " CONSTRAINT fk_emp_func_e FOREIGN KEY (funcionario_emprestimo) REFERENCES funcionario (codigo),"
                + " CONSTRAINT fk_emp_func_d FOREIGN KEY (funcionario_devolucao) REFERENCES funcionario (codigo),"
                + " CONSTRAINT ck_emp_prevista CHECK (data_prevista > data_emprestimo),"
                + " CONSTRAINT ck_emp_devolucao CHECK (data_devolucao IS NULL OR data_devolucao >= data_emprestimo),"
                + " CONSTRAINT ck_emp_multa CHECK (multa >= 0),"
                // uma cópia nunca tem dois empréstimos em aberto
                + " CONSTRAINT uk_emp_copia_aberta UNIQUE (copia_aberta),"
                + " INDEX idx_emp_leitor_aberto (leitor_codigo, data_devolucao)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    /**
     * RF08: grava o empréstimo, a nova situação da cópia e (se houver) a reserva
     * atendida, tudo na mesma transação (RN13/RN16). Preenche o código gerado.
     */
    public int registrarEmprestimo(Emprestimo emp, Reserva reservaAtendida) throws SQLException {
        String sql = "INSERT INTO emprestimo(copia_codigo, leitor_codigo, funcionario_emprestimo,"
                + " data_emprestimo, data_prevista) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, emp.getCopia().getCodigo());
                pstmt.setInt(2, emp.getLeitor().getCodigo());
                pstmt.setInt(3, emp.getFuncionarioEmprestimo().getCodigo());
                pstmt.setDate(4, paraSql(emp.getDataEmprestimo()));
                pstmt.setDate(5, paraSql(emp.getDataPrevista()));
                int retorno = pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    emp.setCodigo(rs.getInt(1));
                }
                new CopiaDAO().atualizarSituacao(conn, emp.getCopia());
                if (reservaAtendida != null) {
                    reservaAtendida.atender(emp);
                    new ReservaDAO().atualizar(conn, reservaAtendida);
                }
                conn.commit();
                return retorno;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * RF09: grava a devolução e a cópia. Se a obra tiver fila de reservas, a
     * cópia já é separada para o primeiro da fila (R4). Devolve as reservas
     * que receberam cópia (lista vazia se nenhuma).
     */
    public List<Reserva> registrarDevolucao(Emprestimo emp) throws SQLException {
        String sql = "UPDATE emprestimo SET data_devolucao=?, multa=?, funcionario_devolucao=?"
                + " WHERE codigo=? AND data_devolucao IS NULL";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, paraSql(emp.getDataDevolucao()));
                pstmt.setDouble(2, emp.getMulta());
                pstmt.setInt(3, emp.getFuncionarioDevolucao().getCodigo());
                pstmt.setInt(4, emp.getCodigo());
                if (pstmt.executeUpdate() != 1) {
                    throw new SQLException("O empréstimo " + emp.getCodigo() + " já foi devolvido.");
                }
                new CopiaDAO().atualizarSituacao(conn, emp.getCopia());
                List<Reserva> separadas = new ReservaDAO().atualizarFila(conn, emp.getObra().getCodigo(),
                        emp.getDataDevolucao());
                conn.commit();
                return separadas;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Carrega um empréstimo pelo código, ou null. */
    public Emprestimo carregar(int codigo) throws SQLException {
        List<Emprestimo> l = consultar(" WHERE e.codigo = " + codigo, null);
        return l.isEmpty() ? null : l.get(0);
    }

    /** Empréstimos em aberto do leitor (para a agregação Leitor -> Emprestimo). */
    public List<Emprestimo> listarAbertosPorLeitor(Leitor leitor) throws SQLException {
        return consultar(" WHERE e.data_devolucao IS NULL AND e.leitor_codigo = " + leitor.getCodigo()
                + " ORDER BY e.data_prevista", leitor);
    }

    /** RF04: empréstimos em aberto de uma obra. */
    public List<Emprestimo> listarAbertosPorObra(int codigoObra) throws SQLException {
        return consultar(" WHERE e.data_devolucao IS NULL AND c.obra_codigo = " + codigoObra
                + " ORDER BY e.codigo", null);
    }

    /** RF10. */
    public List<Emprestimo> listarAbertos() throws SQLException {
        return consultar(" WHERE e.data_devolucao IS NULL ORDER BY e.codigo", null);
    }

    /** RF12 (b) e RF14: em aberto e atrasados na data de referência. */
    public List<Emprestimo> listarAtrasados(LocalDate referencia) throws SQLException {
        return consultar(" WHERE e.data_devolucao IS NULL AND e.data_prevista < '" + referencia + "'"
                + " ORDER BY e.codigo", null);
    }

    /** RF12 (a): devolvidos com multa maior que zero. */
    public List<Emprestimo> listarDevolvidosComMulta() throws SQLException {
        return consultar(" WHERE e.data_devolucao IS NOT NULL AND e.multa > 0 ORDER BY e.codigo", null);
    }

    /** Histórico completo. */
    public List<Emprestimo> listar() throws SQLException {
        return consultar(" ORDER BY e.codigo", null);
    }

    /** RF13: monta a matriz tipo de leitor x categoria com todos os empréstimos. */
    public PerfilUso carregarPerfilUso() throws SQLException {
        PerfilUso perfil = new PerfilUso();
        String sql = "SELECT l.tipo, o.categoria, COUNT(*) AS qtd FROM emprestimo e"
                + " JOIN leitor l ON l.codigo = e.leitor_codigo"
                + " JOIN copia c ON c.codigo = e.copia_codigo"
                + " JOIN obra o ON o.codigo = c.obra_codigo"
                + " GROUP BY l.tipo, o.categoria";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                perfil.incrementar(rs.getInt("tipo"), rs.getInt("categoria"), rs.getInt("qtd"));
            }
        }
        return perfil;
    }

    public int contar() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM emprestimo");
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Próximo código que o banco vai gerar (resumo do RF15). */
    public int proximoCodigo() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(codigo), 0) + 1 FROM emprestimo");
            rs.next();
            return rs.getInt(1);
        }
    }

    private List<Emprestimo> consultar(String filtro, Leitor leitorConhecido) throws SQLException {
        List<Emprestimo> lista = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(SELECT + filtro);
            while (rs.next()) {
                lista.add(montar(rs, leitorConhecido));
            }
        }
        return lista;
    }

    private Emprestimo montar(ResultSet rs, Leitor leitorConhecido) throws SQLException {
        Obra obra = montarObra(rs.getInt("obra_codigo"), rs.getString("titulo"), rs.getString("autor"),
                rs.getInt("categoria"), rs.getInt("ano_publicacao"));
        Copia copia = new Copia();
        copia.setCodigo(rs.getInt("copia_codigo"));
        copia.setNumero(rs.getInt("numero"));
        copia.setSituacao(SituacaoCopia.valueOf(rs.getString("copia_situacao")));
        obra.adicionarCopiaCarregada(copia);

        Emprestimo e = new Emprestimo();
        e.setCodigo(rs.getInt("codigo"));
        e.setCopia(copia);
        e.setLeitor(leitorConhecido != null ? leitorConhecido
                : montarLeitor(rs.getInt("leitor_codigo"), rs.getString("leitor_nome"), rs.getInt("leitor_tipo")));
        e.setFuncionarioEmprestimo(montarFuncionario(rs.getInt("funcionario_emprestimo"),
                rs.getString("fe_nome"), rs.getInt("fe_cargo")));
        int fd = rs.getInt("funcionario_devolucao");
        if (!rs.wasNull()) {
            e.setFuncionarioDevolucao(montarFuncionario(fd, rs.getString("fd_nome"), rs.getInt("fd_cargo")));
        }
        e.setDataEmprestimo(paraLocalDate(rs.getDate("data_emprestimo")));
        e.setDataPrevista(paraLocalDate(rs.getDate("data_prevista")));
        e.setDataDevolucao(paraLocalDate(rs.getDate("data_devolucao")));
        e.setMulta(rs.getDouble("multa"));
        return e;
    }
}
