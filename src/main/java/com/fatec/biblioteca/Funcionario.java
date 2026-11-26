package com.fatec.biblioteca;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Funcionário da biblioteca (pedido do professor de POO). Classe abstrata:
 * Bibliotecario e Voluntario respondem de forma diferente a
 * podeGerenciarCadastros() (polimorfismo).
 */
public abstract class Funcionario extends Pessoa {

    public static final int BIBLIOTECARIO = 1;
    public static final int VOLUNTARIO = 2;

    private String login;
    private String senhaHash;

    /** 1 = Bibliotecário, 2 = Voluntário. */
    public abstract int getCargo();

    /** Bibliotecário cadastra/exclui funcionários; Voluntário não (regra F1). */
    public abstract boolean podeGerenciarCadastros();

    public static Funcionario criar(int cargo) throws Exception {
        switch (cargo) {
            case BIBLIOTECARIO:
                return new Bibliotecario();
            case VOLUNTARIO:
                return new Voluntario();
            default:
                throw new Exception("Cargo deve ser 1 (Bibliotecário) ou 2 (Voluntário).");
        }
    }

    public static String[] nomesCargos() {
        return new String[] { "1 - Bibliotecário", "2 - Voluntário" };
    }

    public boolean autenticar(String senha) {
        return senha != null && gerarHash(senha).equals(senhaHash);
    }

    /** Recebe a senha digitada e guarda só o hash SHA-256. */
    public void setSenha(String senha) throws Exception {
        if (senha == null || senha.length() < 4) {
            throw new Exception("A senha deve ter pelo menos 4 caracteres.");
        }
        this.senhaHash = gerarHash(senha);
    }

    public static String gerarHash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) throws Exception {
        String l = Validador.textoObrigatorio(login, "Login");
        if (l.contains(" ") || l.length() > 30) {
            throw new Exception("Login sem espaços e com até 30 caracteres.");
        }
        this.login = l.toLowerCase();
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    /** Usado pelo DAO ao carregar do banco. */
    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
}
