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
    private static final int[] SHADES = {0xFFFFFF, 0xAAAAAA, 0x555555, 0x000000};
    private int mode = 2;

    public PPU(int[] vram, int[] oam) {
        this.vram = vram;
    }

    public int[] getFramebuffer() {
        return framebuffer;
    }

    public void renderScanline() {

        if (!isBitSet(lcdc, 0)) {
            for (int x = 0; x < 160; x++) {
                framebuffer[ly * 160 + x] = SHADES[0];
            }
            return;
        }

        int tilemapBase = isBitSet(lcdc, 3) ? 0x9C00 : 0x9800;
        boolean unsignedTiles = isBitSet(lcdc,4);
        int bgY = (ly + scy) & 0xFF;
        int tileRow = bgY / 8;
        int lineInTile = bgY % 8;

        for(int x = 0; x < 160; x ++) {
            int bgX = (x + scx) & 0xFF;
            int tileCol = bgX / 8;

            int tileMapIndex = tilemapBase - 0x8000 + tileRow * 32 + tileCol;
            int tileNumber = vram[tileMapIndex];

            int tileDataIndex;

            if (unsignedTiles) {
                tileDataIndex = tileNumber * 16;
            } else {
                tileDataIndex = 0x1000 + ((byte) tileNumber) * 16;
            }

            int low = vram[tileDataIndex + lineInTile * 2];
            int high = vram[tileDataIndex + lineInTile * 2 + 1];

            int bit = 7 - (bgX % 8);
            int bitLow = (low >> bit) & 1;
            int bitHigh = (high >> bit) & 1;
            int color = (bitHigh << 1) | bitLow;
            int shade = (bgp >> (color * 2)) & 0x3;
            framebuffer[ly * 160 + x] = SHADES[shade];
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
                renderScanline();
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
