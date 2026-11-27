package com.fatec.biblioteca.ui;

import javax.swing.table.DefaultTableModel;

public class Tabela extends DefaultTableModel {

    public Tabela(String... colunas) {
        for (String c : colunas) {
            addColumn(c);
        }
    }

    @Override
    public boolean isCellEditable(int linha, int coluna) {
        return false;
    }
}
