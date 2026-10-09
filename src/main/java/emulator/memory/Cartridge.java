package memory;

public class Cartridge {
    private final byte[] rom;
    private int romBank = 1;

    public Cartridge(byte[] rom) {
        this.rom = rom;
    }

    public int readByte(int address) {
        // Game Boy tem 32KB de ROM
        if (address < 0x4000) {
            return rom[address] & 0xFF;
        }
        if (address <= 0x7FFF) {
            return rom[romBank * 0x4000 + (address - 0x4000)] & 0xFF;
        }
        // Fora da ROM retorna 0xFF (comportamento real do hardware)
        return 0xFF;
    }
    public void writeByte(int address, int value) {
        if (address >= 0x2000 && address <= 0x3FFF) {
            int lowValue = value & 0x1F;
            if (lowValue == 0) {
                lowValue = 1;
            }

            setRomBank(lowValue);
        }
    }

    public void setRomBank(int value) {
        this.romBank = value;
    }
}
