package memory;

public class MMU {
    private final Cartridge cartridge;

    public MMU(Cartridge cartridge) {
        this.cartridge = cartridge;
    }

    public int readByte(int address) {
        address &= 0xFFFF;

        if(address <= 0x7FFF) {
            return cartridge.readByte(address);
        }

        return 0;
    }
}
