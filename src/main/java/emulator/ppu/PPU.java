package ppu;

public class PPU {
    private int cycleCounter = 0;
    private int ly = 0;

    public void step(int cycles) {
        cycleCounter += cycles;

        if (cycleCounter >= 456) {
            cycleCounter -= 456;
            ly = (ly + 1) % 154;
        }
    }

    public int getLY() {
        return ly;
    }
}
