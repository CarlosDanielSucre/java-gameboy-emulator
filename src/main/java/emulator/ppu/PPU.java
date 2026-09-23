package ppu;

public class PPU {
    private int cycleCounter = 0;
    private int ly = 0;

    private int mode = 2;

    public void step(int cycles) {
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
}
