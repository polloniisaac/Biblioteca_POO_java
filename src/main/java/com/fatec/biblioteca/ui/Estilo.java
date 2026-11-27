package com.fatec.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

public final class Estilo {

    public static final Color AZUL = new Color(0x2E6FD8);
    public static final Color VERMELHO = new Color(0xD64545);

    private static final Preferences PREFS = Preferences.userNodeForPackage(Estilo.class);
    private static final String CHAVE_TEMA = "temaEscuro";

    private static final Set<String> PRIMARIOS = Set.of("Inserir", "Entrar", "Salvar", "Realizar empréstimo",
            "Registrar devolução", "Reservar", "Gerar", "Adicionar", "Exportar", "Buscar");
    private static final Set<String> PERIGOSOS = Set.of("Excluir", "Cancelar reserva");

    private Estilo() {
    }

    public static void iniciar() {
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#2E6FD8"));
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("TableHeader.height", 32);
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("OptionPane.buttonMinimumWidth", 90);
        aplicarTema(isEscuro());
    }

    public static boolean isEscuro() {
        return PREFS.getBoolean(CHAVE_TEMA, false);
    }

    public static void aplicarTema(boolean escuro) {
        if (escuro) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        Font fonte = UIManager.getFont("defaultFont");
        if (fonte != null) {
            UIManager.put("defaultFont", fonte.deriveFont(14f));
        }
        UIManager.put("Table.alternateRowColor", escuro ? new Color(0x2B2D30) : new Color(0xF4F7FC));
        PREFS.putBoolean(CHAVE_TEMA, escuro);
        FlatLaf.updateUI();
    }

    public static void padronizar(JFrame tela) {
        Container antigo = tela.getContentPane();
        if (antigo instanceof JComponent) {
            ((JComponent) antigo).setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));
        }
        if (antigo.getLayout() instanceof BorderLayout) {
            ((BorderLayout) antigo.getLayout()).setVgap(10);
        }
        JPanel novo = new JPanel(new BorderLayout());
        novo.add(cabecalho(tela.getTitle(), null), BorderLayout.NORTH);
        novo.add(antigo, BorderLayout.CENTER);
        tela.setContentPane(novo);

        ajustarComponentes(novo);

        Dimension minimo = tela.getPreferredSize();
        tela.setSize(Math.max(tela.getWidth(), minimo.width), Math.max(tela.getHeight(), minimo.height));
        tela.setMinimumSize(minimo);
        tela.setLocationRelativeTo(null);
    }

    public static JPanel cabecalho(String titulo, String subtitulo) {
        JPanel p = new PainelFundo(new BorderLayout(), PainelFundo.ESTANTE, new Color(18, 28, 52, 165));
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(Color.WHITE);
        lbl.putClientProperty("FlatLaf.styleClass", "h3");
        p.add(lbl, BorderLayout.CENTER);
        if (subtitulo != null) {
            JLabel sub = new JLabel(subtitulo);
            sub.setForeground(new Color(0xDCE7FA));
            p.add(sub, BorderLayout.SOUTH);
        }
        return p;
    }

    public static void primario(JButton b) {
        b.putClientProperty("FlatLaf.style",
                "background: #2E6FD8; foreground: #FFFFFF; hoverBackground: #2860BD; pressedBackground: #2253A3;"
                        + " borderWidth: 0; focusedBorderColor: #2E6FD8; font: bold;"
                        + " default.background: #2E6FD8; default.foreground: #FFFFFF;"
                        + " default.hoverBackground: #2860BD; default.pressedBackground: #2253A3; default.borderWidth: 0");
    }

    public static void perigo(JButton b) {
        b.putClientProperty("FlatLaf.style", "foreground: #D64545; borderColor: #D64545");
    }

    private static void ajustarComponentes(Container c) {
        for (Component filho : c.getComponents()) {
            if (filho instanceof JTable) {
                ajustarTabela((JTable) filho);
            } else if (filho instanceof JTextArea) {
                JTextArea area = (JTextArea) filho;
                area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
                area.setMargin(new java.awt.Insets(8, 10, 8, 10));
            } else if (filho instanceof JButton) {
                JButton b = (JButton) filho;
                if (PRIMARIOS.contains(b.getText())) {
                    primario(b);
                } else if (PERIGOSOS.contains(b.getText())) {
                    perigo(b);
                }
                b.setMargin(new java.awt.Insets(6, 14, 6, 14));
            }
            if (filho instanceof Container) {
                ajustarComponentes((Container) filho);
            }
        }
    }

    private static void ajustarTabela(JTable t) {
        t.setFillsViewportHeight(true);
        t.getTableHeader().putClientProperty("FlatLaf.style", "font: bold");
        t.getTableHeader().setReorderingAllowed(false);
    }
}
