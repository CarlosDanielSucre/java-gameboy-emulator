package ppu;

public class PPU {
    private int cycleCounter = 0;
    private int ly = 0;
    private int lcdc = 0x91; //inicialized screen on
    private int scy, scx;
    private int lyc;
    private int bgp = 0xFC;

    private int mode = 2;

    public void step(int cycles) {
        if ((lcdc & 0x80) == 0) return;
        cycleCounter += cycles;

        if (ly < 144) {
            if (mode == 2 && cycleCounter >= 80) {
                mode = 3;
            } else if (mode == 3 && cycleCounter >= 80 + 172) {
                mode = 0;
            } else if (mode == 0 && cycleCounter >= 456) {
                cycleCounter -= 456;
                ly++;
                mode = (ly < 144) ? 2 : 1;
            }
        } else {
            if (cycleCounter >= 456) {
                cycleCounter -= 456;
                ly++;
                if (ly > 153) {
                    ly = 0;
                    mode = 2;
                }
            }
        }
    }
    public int getLy() {
        return ly;
    }

    public int getMode() {
        return mode;
    }
    public void setLcdc(int value) {
        boolean wasOn = (lcdc & 0x80) != 0;
        lcdc = value & 0xFF;
        boolean isOn = (lcdc & 0x80) != 0;
        if (wasOn && !isOn) {
            ly = 0;
            mode = 0;
            cycleCounter = 0;
        }
    }

    public int getLcdc() {
        return lcdc;
    }

    public int getScy() {
        return scy;
    }

    public void setScy(int value) {
        scy = value & 0xFF;
    }

    public int getScx() {
        return scx;
    }

    public void setScx(int value) {
        scx = value & 0xFF;
    }

    public int getLyc() {
        return lyc;
    }

    public void setLyc(int value) {
        lyc = value & 0xFF;
    }

    public int getBgp() {
        return bgp;
    }

    public void setBgp(int value) {
        bgp = value & 0xFF;
    }
}
