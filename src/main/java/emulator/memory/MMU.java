package memory;

import ppu.PPU;

public class MMU {
    private final Cartridge cartridge;

    public MMU(Cartridge cartridge) {
        this.cartridge = cartridge;
    }
    private int[] wram = new int[0x2000]; //8KB
    private int[] hram = new int[0x7F]; //127 bytes
    private int[] vram = new int[0x2000];
    private int[] oam = new int[0xA0];
    private PPU ppu = new PPU();
    private int interruptEnable;
    private int interruptFlag;

    public void step(int cycles) {
        ppu.step(cycles);
    }

    public int readByte(int address) {
        address &= 0xFFFF;

        if(address <= 0x7FFF) {
            return cartridge.readByte(address);
        } else if (address >= 0x8000 && address <= 0x9FFF) {
            address -= 0x8000;
            return vram[address];
        }else if(address >= 0xC000 && address <= 0xDFFF) {
            address -= 0xC000;
            return wram[address];
        }else if (address >= 0xFE00 && address <= 0xFE9F) {
            address -= 0xFE00;
            return oam[address];
        } else if (address == 0xFF41) {
            return ppu.getMode();
        } else if (address == 0xFF44) {
            return ppu.getLy();
        } else if(address >= 0xFF80 && address <= 0xFFFE) {
            address -= 0xFF80;
            return hram[address];
        } else if (address == 0xFF0F) {
            return interruptFlag;
        } else if (address == 0xFFFF) {
            return interruptEnable;
        }

        return 0;
    }

    private int serialData = 0;

    public void writeByte(int address, int value) {
        address &= 0xFFFF;
        value &= 0xFF;

        if (address == 0xFF01) {
            serialData = value;
        }

        if (address == 0xFF02 && value == 0x81) {
            System.out.print((char)serialData);
        }

        if (address <= 0x7FFF) {

        } else if (address >= 0x8000 && address <= 0x9FFF) {
            address -= 0x8000;
            vram[address] = value;
        } else if (address >= 0xC000 && address <= 0xDFFF) {
            address -= 0xC000;
            wram[address] = value;
        } else if (address >= 0xFE00 && address <= 0xFE9F) {
            address -= 0xFE00;
            oam[address] = value;
        } else if (address >= 0xFF80 && address <= 0xFFFE) {
            address -= 0xFF80;
            hram[address] = value;
        } else if (address == 0xFF0F) {
            interruptFlag = value;
        } else if (address == 0xFFFF) {
            interruptEnable = value;
        }

    }
}
