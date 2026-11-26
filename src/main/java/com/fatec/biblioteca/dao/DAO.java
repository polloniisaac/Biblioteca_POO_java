package com.fatec.biblioteca.dao;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.fatec.biblioteca.Categoria;
import com.fatec.biblioteca.Funcionario;
import com.fatec.biblioteca.Leitor;
import com.fatec.biblioteca.Obra;

/**
 * Classe-mãe de todos os DAOs (herança). Guarda a URL do banco e abre as
 * conexões, como o ClienteDAO do poo12. Diferença proposital: os métodos
 * NÃO capturam exceções; elas sobem até a tela (Frm), que mostra a mensagem.
 *
 * A conexão é lida do arquivo db.properties. Ele é procurado, nesta ordem:
 * 1) na pasta onde o programa roda; 2) em Biblioteca_System/ abaixo dela;
 * 3) subindo a partir da pasta das classes compiladas (acha a pasta do pom.xml);
 * 4) no classpath (src/main/resources). Se não achar, usa root sem senha.
 */
public abstract class DAO {

    protected static final String URL;
    private static final String USUARIO;
    private static final String SENHA;
    private static String origemConfiguracao = "db.properties não encontrado (usando root sem senha)";

    static {
        Properties p = new Properties();
        boolean carregou = false;
        for (Path arquivo : candidatos()) {
            if (Files.isRegularFile(arquivo)) {
                try (InputStreamReader in = new InputStreamReader(new FileInputStream(arquivo.toFile()),
                        StandardCharsets.UTF_8)) {
                    p.load(in);
                    origemConfiguracao = arquivo.toAbsolutePath().normalize().toString();
                    carregou = true;
                    break;
                } catch (Exception e) {
                    System.err.println("Não foi possível ler " + arquivo + ": " + e.getMessage());
                }
            }
        }
        if (!carregou) {
            try (InputStream in = DAO.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                    origemConfiguracao = "classpath:db.properties";
                }
            } catch (Exception e) {
                System.err.println("Não foi possível ler db.properties do classpath: " + e.getMessage());
            }
        }
        System.out.println("Configuração do banco: " + origemConfiguracao);
        URL = p.getProperty("url", "jdbc:mysql://localhost:3306/biblioteca_vila_leitura"
                + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
                + "&serverTimezone=America/Sao_Paulo").trim();
        USUARIO = p.getProperty("usuario", "root").trim();
        SENHA = p.getProperty("senha", "").trim();
    }

    /** Lugares onde o db.properties pode estar, do mais provável ao menos. */
    private static List<Path> candidatos() {
        List<Path> lista = new ArrayList<>();
        lista.add(Path.of("db.properties"));
        lista.add(Path.of("Biblioteca_System", "db.properties"));
        try {
            Path pasta = Path.of(DAO.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // ex.: .../Biblioteca_System/target/classes -> sobe até achar o db.properties
            for (int i = 0; i < 5 && pasta != null; i++) {
                lista.add(pasta.resolve("db.properties"));
                pasta = pasta.getParent();
            }
        } catch (Exception e) {
            // sem localização das classes: fica só com as outras opções
        }
        return lista;
    }

    /** De onde veio a configuração (para mostrar na mensagem de erro). */
    public static String getOrigemConfiguracao() {
        return origemConfiguracao;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    /** Cada DAO cria a sua tabela (CREATE TABLE IF NOT EXISTS), como no poo12. */
    public abstract void criarNovaTabela() throws SQLException;

    protected void executar(String sql) throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // ---------- conversões usadas pelos DAOs ----------

    protected static Date paraSql(LocalDate data) {
        return data == null ? null : Date.valueOf(data);
    }

    protected static LocalDate paraLocalDate(Date data) {
        return data == null ? null : data.toLocalDate();
    }

    /** Monta a subclasse certa de Leitor a partir de uma linha do banco. */
    protected static Leitor montarLeitor(int codigo, String nome, int tipo) throws SQLException {
        try {
            Leitor l = Leitor.criar(tipo);
            l.setCodigo(codigo);
            l.setNome(nome);
            return l;
        } catch (Exception e) {
            throw new SQLException("Leitor " + codigo + " inválido no banco: " + e.getMessage());
        }
    }

    protected static Funcionario montarFuncionario(int codigo, String nome, int cargo) throws SQLException {
        try {
            Funcionario f = Funcionario.criar(cargo);
            f.setCodigo(codigo);
            f.setNome(nome);
            return f;
        } catch (Exception e) {
            throw new SQLException("Funcionário " + codigo + " inválido no banco: " + e.getMessage());
        }
    }

    protected static Obra montarObra(int codigo, String titulo, String autor, int categoria, int ano)
            throws SQLException {
        try {
            Obra o = new Obra();
            o.setCodigo(codigo);
            o.setTitulo(titulo);
            o.setAutor(autor);
            o.setCategoria(Categoria.fromCodigo(categoria));
            o.setAnoPublicacao(ano);
            return o;
        } catch (Exception e) {
            throw new SQLException("Obra " + codigo + " inválida no banco: " + e.getMessage());
        }
    }
}
