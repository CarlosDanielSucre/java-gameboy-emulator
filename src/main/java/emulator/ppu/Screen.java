package ppu;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Screen extends JPanel {

    private BufferedImage image = new BufferedImage(160, 144, BufferedImage.TYPE_INT_RGB);
    public Screen() {
        setPreferredSize(new Dimension(640, 576));
    }

    public void updateFrame(int[] pixels) {
        image.setRGB(0, 0, 160, 144, pixels, 0, 160);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }
}
