package memory;

public class MMU {
    private final Cartridge cartridge;

    public MMU(Cartridge cartridge) {
        this.cartridge = cartridge;
    }
    private int[] wram = new int[0x2000]; //8KB
    private int[] hram = new int[0x7F]; //127 bytes

    public int readByte(int address) {
        address &= 0xFFFF;

        if (address == 0xFF44) {
            return 0x90; // LY sempre em VBlank (144)
        }

        if (address == 0xFF41) {
            return 0x01; // STAT indicando modo VBlank
        }
        if(address <= 0x7FFF) {
            return cartridge.readByte(address);
        }else if(address >= 0xC000 && address <= 0xDFFF) {
            address -= 0xC000;
            return wram[address];
        }else if(address >= 0xFF80 && address <= 0xFFFE) {
            address -= 0xFF80;
            return hram[address];
        }
        return 0;
    }

    private int serialData = 0; // representa 0xFF01

    public void writeByte(int address, int value) {
        address &= 0xFFFF;
        value &= 0xFF;
        int contador;

        if (address == 0xFF01) {
            serialData = value;
        }

        if (address == 0xFF02 && value == 0x81) {
            System.out.print((char)serialData);
        }

        if (address <= 0x7FFF) {

        } else if (address >= 0xC000 && address <= 0xDFFF) {
            address -= 0xC000;
            wram[address] = value;
        } else if (address >= 0xFF80 && address <= 0xFFFE) {
            address -= 0xFF80;
            hram[address] = value;
        }
    }
}
