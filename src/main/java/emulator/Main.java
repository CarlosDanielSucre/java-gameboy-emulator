import cpu.CPU;
import memory.Cartridge;
import memory.MMU;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            Cartridge cartridge = new Cartridge(Files.readAllBytes(Path.of("roms/test/01-special.gb")));
            MMU mmu = new MMU(cartridge);
            CPU cpu = new CPU(mmu);
            while(true) {
                cpu.step();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}