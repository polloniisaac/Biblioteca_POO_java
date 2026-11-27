package com.fatec.biblioteca.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class PainelFundo extends JPanel {

    public static final String FUNDO = "/imagens/vila_leitura.jpg";
    public static final String ESTANTE = "/imagens/estante.png";

    private static final Map<String, Image> CACHE = new HashMap<>();

    private final String recurso;
    private final Color veu;

    public PainelFundo(LayoutManager layout) {
        this(layout, FUNDO, null);
    }

    public PainelFundo(LayoutManager layout, String recurso, Color veu) {
        super(layout);
        this.recurso = recurso;
        this.veu = veu;
    }

    private static Image getImagem(String recurso) {
        if (!CACHE.containsKey(recurso)) {
            Image img = null;
            try {
                URL url = PainelFundo.class.getResource(recurso);
                if (url != null) {
                    img = ImageIO.read(url);
                }
            } catch (Exception e) {
                img = null;
            }
            CACHE.put(recurso, img);
        }
        return CACHE.get(recurso);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image img = getImagem(recurso);
        if (img == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        double escala = Math.max((double) w / img.getWidth(null), (double) h / img.getHeight(null));
        int iw = (int) Math.ceil(img.getWidth(null) * escala);
        int ih = (int) Math.ceil(img.getHeight(null) * escala);
        g2.drawImage(img, (w - iw) / 2, (h - ih) / 2, iw, ih, null);

        Color cor = veu;
        if (cor == null) {
            Color base = UIManager.getColor("Panel.background");
            int alfa = Estilo.isEscuro() ? 200 : 150;
            cor = new Color(base.getRed(), base.getGreen(), base.getBlue(), alfa);
        }
        g2.setColor(cor);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}
