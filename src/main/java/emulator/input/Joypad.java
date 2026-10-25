package input;

import java.awt.event.KeyEvent;

public class Joypad {
    private boolean right;
    private boolean left;
    private boolean up;
    private boolean down;
    private boolean a;
    private boolean b;
    private boolean select;
    private boolean start;
    private int selectBits = 0x30;

    public Joypad (){}

    public void pressButton(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT) {
            System.out.println("Pressed: " + keyCode);
            left = true;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            System.out.println("Pressed: " + keyCode);
            right = true;
        } else if (keyCode == KeyEvent.VK_UP) {
            System.out.println("Pressed: " + keyCode);
            up = true;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            System.out.println("Pressed: " + keyCode);
            down = true;
        } else if (keyCode == KeyEvent.VK_H) {
            System.out.println("Pressed: " + keyCode);
            a = true;
        } else if (keyCode == KeyEvent.VK_J) {
            System.out.println("Pressed: " + keyCode);
            b = true;
        } else if (keyCode == KeyEvent.VK_K) {
            System.out.println("Pressed: " + keyCode);
            select = true;
        } else if (keyCode == KeyEvent.VK_L) {
            System.out.println("Pressed: " + keyCode);
            start = true;
        }

    }

    public void releaseButton(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT) {
            System.out.println("Released: " + keyCode);
            left = false;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            System.out.println("Released: " + keyCode);
            right = false;
        } else if (keyCode == KeyEvent.VK_UP) {
            System.out.println("Released: " + keyCode);
            up = false;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            System.out.println("Released: " + keyCode);
            down = false;
        } else if (keyCode == KeyEvent.VK_H) {
            System.out.println("Released: " + keyCode);
            a = false;
        } else if (keyCode == KeyEvent.VK_J) {
            System.out.println("Released: " + keyCode);
            b = false;
        } else if (keyCode == KeyEvent.VK_K) {
            System.out.println("Released: " + keyCode);
            select = false;
        } else if (keyCode == KeyEvent.VK_L) {
            System.out.println("Released: " + keyCode);
            start = false;
        }

    }
    public void write(int value) {
        selectBits = value & 0x30;
    }

    public int read() {
        int result = 0xCF | selectBits;

        int bit4 = (selectBits >> 4) & 1;
        int bit5 = (selectBits >> 5) & 1;
        if (bit4 == 0) {
            if (right) {
                result &= ~(1 << 0);
            }
            if (left) {
                result &= ~(2 << 0);
            }
            if (up) {
                result &= ~(4 << 0);
            }
            if (down) {
                result &= ~(8 << 0);
            }
        }
        if(bit5 == 0) {
            if (a) {
                result &= ~(1 << 0);
            }
            if (b) {
                result &= ~(2 << 0);
            }
            if (select) {
                result &= ~(4 << 0);
            }
            if (start) {
                result &= ~(8 << 0);
            }
        }
        return result;
    }
}
