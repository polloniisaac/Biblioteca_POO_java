package com.fatec.biblioteca;

import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;

import com.fatec.biblioteca.dao.BancoDados;
import com.fatec.biblioteca.dao.DAO;
import com.fatec.biblioteca.ui.Estilo;
import com.fatec.biblioteca.ui.FrmLogin;

public class Main {
    public static void main(String[] args) {

        Estilo.iniciar();

        try {
            List<String> mensagens = new BancoDados().inicializar();
            JOptionPane.showMessageDialog(null, String.join("\n", mensagens),
                    "Carregando dados", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível conectar ao banco MySQL.\n" + e.getMessage()
                            + "\n\nConfiguração usada: " + DAO.getOrigemConfiguracao()
                            + "\nVerifique se o MySQL está rodando e confira usuário e senha no db.properties"
                            + "\n(mesma pasta do pom.xml).",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        var tela = new FrmLogin();
        tela.setVisible(true);
    }
}
