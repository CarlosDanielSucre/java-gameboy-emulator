package memory;

public class Cartridge {
    private final byte[] rom;

    public Cartridge(byte[] rom) {
        this.rom = rom;
    }

    public int readByte(int address) {
        return rom[address] & 0xFF;
    }
}
