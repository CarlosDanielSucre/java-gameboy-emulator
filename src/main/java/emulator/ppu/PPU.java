package ppu;

public class PPU {
    private int cycleCounter = 0;
    private int ly = 0;
    private int lcdc = 0x91; //inicialized screen ON
    private int scy, scx;
    private int lyc;
    private int bgp = 0xFC;
    private final int[] vram;
    private final int[] framebuffer = new int[160 * 144];
    private int mode = 2;

    public PPU(int[] vram, int[] oam) {
        this.vram = vram;
    }

    public int[] getFramebuffer() {
        return framebuffer;
    }

    public void renderScanline() {
        int tilemapBase;
        if (isBitSet(lcdc, 3)) {
            tilemapBase = 0x9C00;
        } else {
            tilemapBase = 0x9800;
        }
        int bgY = (ly + scy) & 0xFF;
        int tileRow = bgY / 8;
        for(int x = 0; x < 160; x ++) {
            int bgX = (x + scx) & 0xFF;
            int tileCol = bgX / 8;
            int tileMapIndex = tilemapBase - 0x8000 + tileRow * 32 + tileCol;
            int tileDataIndex = vram[tileMapIndex];
            if (isBitSet(lcdc,4)) {
                tileDataIndex = tileDataIndex * 16;
            } else {
                tileDataIndex = 0x1000 + ((byte) tileDataIndex) * 16;
            }
        }
    }
    private boolean isBitSet(int value, int bit) {
        int result = value >> bit;
        return (result & 0x1) == 1;
    }

    public boolean step(int cycles) {
        boolean overflowed = false;

        if ((lcdc & 0x80) == 0) return false;
        cycleCounter += cycles;

        if (ly < 144) {
            if (mode == 2 && cycleCounter >= 80) {
                mode = 3;
            } else if (mode == 3 && cycleCounter >= 80 + 172) {
                mode = 0;
            } else if (mode == 0 && cycleCounter >= 456) {
                cycleCounter -= 456;
                ly++;
                if (ly == 144) {
                    overflowed = true;
                }
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
        return overflowed;
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
