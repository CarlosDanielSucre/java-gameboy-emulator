package memory;

public class Cartridge {
    private final byte[] rom;

    public Cartridge(byte[] rom) {
        this.rom = rom;
    }

    public int readByte(int address) {
        // Game Boy tem 32KB de ROM
        if (address < rom.length) {
            return rom[address] & 0xFF;
        }
        // Fora da ROM retorna 0xFF (comportamento real do hardware)
        return 0xFF;
    }
}
