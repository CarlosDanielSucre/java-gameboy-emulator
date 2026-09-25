package timer;

public class Timer {
    private int divCounter = 0;
    private int div = 0;

    private int tima = 0;
    private int tma = 0;
    private int tac = 0;

    private int timaCycleCounter = 0;

    public void step(int cycles) {
        divCounter += cycles;
        while (divCounter >= 256) {
            divCounter -= 256;
            div = (div + 1) & 0xFF;
        }
    }

    public boolean tick(int cycles) {
        step(cycles);

        boolean overflowed = false;
        if ((tac & 0x4) != 0) {
            int threshold = switch (tac & 0x3) {
                case 0 -> 1024;
                case 1 -> 16;
                case 2 -> 64;
                case 3 -> 256;
                default -> 1024;
            };

            timaCycleCounter += cycles;
            while (timaCycleCounter >= threshold) {
                timaCycleCounter -= threshold;
                tima++;
                if (tima > 0xFF) {
                    tima = tma;
                    overflowed = true;
                }
            }
        }
        return overflowed;
    }

    public int getDiv() { return div; }
    public void resetDiv() { div = 0; divCounter = 0; }

    public int getTima() { return tima; }
    public void setTima(int value) { tima = value & 0xFF; }

    public int getTma() { return tma; }
    public void setTma(int value) { tma = value & 0xFF; }

    public int getTac() { return tac; }
    public void setTac(int value) { tac = value & 0xFF; }
}