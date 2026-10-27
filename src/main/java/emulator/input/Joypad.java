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
            left = true;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            right = true;
        } else if (keyCode == KeyEvent.VK_UP) {
            up = true;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            down = true;
        } else if (keyCode == KeyEvent.VK_H) {
            a = true;
        } else if (keyCode == KeyEvent.VK_J) {
            b = true;
        } else if (keyCode == KeyEvent.VK_K) {
            select = true;
        } else if (keyCode == KeyEvent.VK_L) {
            start = true;
        }

    }

    public void releaseButton(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT) {
            left = false;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            right = false;
        } else if (keyCode == KeyEvent.VK_UP) {
            up = false;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            down = false;
        } else if (keyCode == KeyEvent.VK_H) {
            a = false;
        } else if (keyCode == KeyEvent.VK_J) {
            b = false;
        } else if (keyCode == KeyEvent.VK_K) {
            select = false;
        } else if (keyCode == KeyEvent.VK_L) {
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
