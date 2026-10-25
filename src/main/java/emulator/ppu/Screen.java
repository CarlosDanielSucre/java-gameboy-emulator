package ppu;

import input.Joypad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class Screen extends JPanel implements KeyListener {
    private Joypad joypad;

    private BufferedImage image = new BufferedImage(160, 144, BufferedImage.TYPE_INT_RGB);
    public Screen(Joypad joypad) {
        this.joypad = joypad;
        setPreferredSize(new Dimension(640, 576));
        addKeyListener(this);
        setFocusable(true);
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

    @Override
    public void keyPressed(KeyEvent e) {
        joypad.pressButton(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        joypad.releaseButton(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not Necessary
    }
}
