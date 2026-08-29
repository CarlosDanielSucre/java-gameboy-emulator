package memory;

public class MMU {
    private final Cartridge cartridge;

    public MMU(Cartridge cartridge) {
        this.cartridge = cartridge;
    }
    private int[] wram = new int[0x2000]; //8KB

    public int readByte(int address) {
        address &= 0xFFFF;

        if(address <= 0x7FFF) {
            return cartridge.readByte(address);
        }else if(address >= 0xC000 && address <= 0xDFFF) {
            address -= 0xC000;
            return wram[address];
        }

        return 0;
    }

    public void writeByte(int address, int value) {
        address &= 0xFFFF;
        value &= 0xFF;
        if (address <= 0x7FFF) {

        }else if(address >= 0xC000 && address <= 0xDFFF) {
            address -= 0xC000;
            wram[address] = value;
        }
    }
}
