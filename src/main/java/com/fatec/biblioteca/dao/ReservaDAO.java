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
import com.fatec.biblioteca.Obra;
import com.fatec.biblioteca.Reserva;
import com.fatec.biblioteca.SituacaoCopia;
import com.fatec.biblioteca.SituacaoReserva;

/** Tabela reserva (pedido do professor de POO). Regras R1–R7 no documento de proposta. */
public class ReservaDAO extends DAO {

    private static final String SELECT = "SELECT r.*, l.nome AS leitor_nome, l.tipo AS leitor_tipo,"
            + " f.nome AS f_nome, f.cargo AS f_cargo,"
            + " o.titulo, o.autor, o.categoria, o.ano_publicacao, cp.numero AS copia_numero"
            + " FROM reserva r"
            + " LEFT JOIN copia cp ON cp.codigo = r.copia_codigo"
            + " JOIN leitor l ON l.codigo = r.leitor_codigo"
            + " JOIN funcionario f ON f.codigo = r.funcionario_codigo"
            + " JOIN obra o ON o.codigo = r.obra_codigo";

    @Override
    public void criarNovaTabela() throws SQLException {
        executar("CREATE TABLE IF NOT EXISTS reserva ("
                + " codigo INT NOT NULL AUTO_INCREMENT,"
                + " obra_codigo INT NOT NULL,"
                + " leitor_codigo INT NOT NULL,"
                + " funcionario_codigo INT NOT NULL,"
                + " data_reserva DATE NOT NULL,"
                + " copia_codigo INT NULL,"
                + " data_limite_retirada DATE NULL,"
                + " emprestimo_codigo INT NULL,"
                + " situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVA',"
                // colunas calculadas: só têm valor enquanto a reserva está viva
                + " viva_leitor INT AS (IF(situacao IN ('ATIVA','AGUARDANDO'), leitor_codigo, NULL)) STORED,"
                + " viva_obra INT AS (IF(situacao IN ('ATIVA','AGUARDANDO'), obra_codigo, NULL)) STORED,"
                + " CONSTRAINT pk_reserva PRIMARY KEY (codigo),"
                + " CONSTRAINT fk_res_obra FOREIGN KEY (obra_codigo) REFERENCES obra (codigo),"
                + " CONSTRAINT fk_res_leitor FOREIGN KEY (leitor_codigo) REFERENCES leitor (codigo),"
                + " CONSTRAINT fk_res_func FOREIGN KEY (funcionario_codigo) REFERENCES funcionario (codigo),"
                + " CONSTRAINT fk_res_copia FOREIGN KEY (copia_codigo) REFERENCES copia (codigo),"
                + " CONSTRAINT fk_res_emp FOREIGN KEY (emprestimo_codigo) REFERENCES emprestimo (codigo),"
                + " CONSTRAINT ck_res_situacao CHECK (situacao IN"
                + "   ('ATIVA','AGUARDANDO','ATENDIDA','CANCELADA','EXPIRADA')),"
                // um leitor só tem uma reserva viva por obra
                + " CONSTRAINT uk_res_viva UNIQUE (viva_leitor, viva_obra),"
                + " INDEX idx_res_fila (obra_codigo, situacao, codigo)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    public int inserir(Reserva obj) throws SQLException {
        String sql = "INSERT INTO reserva(obra_codigo, leitor_codigo, funcionario_codigo, data_reserva, situacao)"
                + " VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, obj.getObra().getCodigo());
            pstmt.setInt(2, obj.getLeitor().getCodigo());
            pstmt.setInt(3, obj.getFuncionario().getCodigo());
            pstmt.setDate(4, paraSql(obj.getDataReserva()));
            pstmt.setString(5, obj.getSituacao().name());
            int retorno = pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                obj.setCodigo(rs.getInt(1));
            }
            obj.getObra().getFilaReservas().add(obj);
            return retorno;
        }
    }

    /** Grava situação, cópia separada, prazo e empréstimo da reserva (dentro de transação). */
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

    /** R7: grava o cancelamento (a tela já chamou reserva.cancelar()) e anda a fila. */
    public List<Reserva> salvarCancelamento(Reserva r, LocalDate data) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                atualizar(conn, r);
                if (r.getCopiaSeparada() != null) {
                    new CopiaDAO().atualizarSituacao(conn, r.getCopiaSeparada());
                }
                List<Reserva> separadas = atualizarFila(conn, r.getObra().getCodigo(), data);
                conn.commit();
                return separadas;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Coração das regras R4 e R6, chamado dentro de uma transação:
     * 1) expira as reservas AGUARDANDO com prazo vencido e libera a cópia;
     * 2) enquanto houver cópia DISPONIVEL e reserva ATIVA na fila, separa a
     *    cópia para o próximo da fila (prazo = data + 3 dias).
     * Devolve as reservas que receberam cópia agora.
     */
    public List<Reserva> atualizarFila(Connection conn, int codigoObra, LocalDate data) throws SQLException {
        List<Reserva> separadas = new ArrayList<>();
        Obra obra = new ObraDAO().carregar(conn, codigoObra);
        if (obra == null) {
            return separadas;
        }
        CopiaDAO copiaDAO = new CopiaDAO();
        for (Reserva r : obra.getFilaReservas()) {
            if (r.prazoVencido(data)) {
                r.expirar();
                atualizar(conn, r);
                copiaDAO.atualizarSituacao(conn, r.getCopiaSeparada());
            }
        }
        for (Reserva r : obra.getFilaReservas()) {
            if (r.getSituacao() != SituacaoReserva.ATIVA) {
                continue;
            }
            Copia livre = null;
            for (Copia c : obra.getCopias()) {
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

    /** R6: expira, em todas as obras, as reservas com prazo de retirada vencido na data. */
    public void expirarVencidas(LocalDate data) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Integer> obras = new ArrayList<>();
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT DISTINCT obra_codigo FROM reserva"
                        + " WHERE situacao = 'AGUARDANDO' AND data_limite_retirada < ?")) {
                    pstmt.setDate(1, paraSql(data));
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        obras.add(rs.getInt(1));
                    }
                }
                for (int cod : obras) {
                    atualizarFila(conn, cod, data);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Preenche a fila de reservas vivas da obra (agregação), em ordem de chegada. */
    public void carregarFila(Connection conn, Obra obra) throws SQLException {
        String sql = SELECT + " WHERE r.obra_codigo = ? AND r.situacao IN ('ATIVA','AGUARDANDO') ORDER BY r.codigo";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, obra.getCodigo());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                obra.getFilaReservas().add(montar(rs, obra));
            }
        }
    }

    /** Carrega uma reserva viva (com a obra completa), ou null se não existir ou já tiver encerrado. */
    public Reserva carregarViva(int codigo) throws SQLException {
        int codigoObra;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT obra_codigo FROM reserva WHERE codigo=?")) {
            pstmt.setInt(1, codigo);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            codigoObra = rs.getInt(1);
        }
        Obra obra = new ObraDAO().carregar(codigoObra);
        for (Reserva r : obra.getFilaReservas()) {
            if (r.getCodigo() == codigo) {
                return r;
            }
        }
        return null;
    }

    /** Lista reservas (todas ou só as vivas), por obra e ordem de chegada. */
    public List<Reserva> listar(boolean somenteVivas) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = SELECT + (somenteVivas ? " WHERE r.situacao IN ('ATIVA','AGUARDANDO')" : "")
                + " ORDER BY r.obra_codigo, r.codigo";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Obra obra = montarObra(rs.getInt("obra_codigo"), rs.getString("titulo"), rs.getString("autor"),
                        rs.getInt("categoria"), rs.getInt("ano_publicacao"));
                lista.add(montar(rs, obra));
            }
        }
        return lista;
    }

    private Reserva montar(ResultSet rs, Obra obra) throws SQLException {
        Reserva r = new Reserva();
        r.setCodigo(rs.getInt("codigo"));
        r.setObra(obra);
        r.setLeitor(montarLeitor(rs.getInt("leitor_codigo"), rs.getString("leitor_nome"), rs.getInt("leitor_tipo")));
        r.setFuncionario(montarFuncionario(rs.getInt("funcionario_codigo"), rs.getString("f_nome"),
                rs.getInt("f_cargo")));
        r.setDataReserva(paraLocalDate(rs.getDate("data_reserva")));
        int copia = rs.getInt("copia_codigo");
        if (!rs.wasNull()) {
            Copia c = obra.getCopia(copia);
            if (c == null) { // obra carregada sem as cópias (listagem)
                c = new Copia();
                c.setCodigo(copia);
                c.setNumero(rs.getInt("copia_numero"));
                c.setObra(obra);
            }
            r.setCopiaSeparada(c);
        }
        r.setDataLimiteRetirada(paraLocalDate(rs.getDate("data_limite_retirada")));
        r.setCodigoEmprestimo(rs.getInt("emprestimo_codigo"));
        r.setSituacao(SituacaoReserva.valueOf(rs.getString("situacao")));
        return r;
    }
}
