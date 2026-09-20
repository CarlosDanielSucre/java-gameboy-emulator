import cpu.CPU;
import memory.Cartridge;
import memory.MMU;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            Cartridge cartridge = new Cartridge(Files.readAllBytes(Path.of("roms/test/cpu_instrs.gb")));
            MMU mmu = new MMU(cartridge);
            CPU cpu = new CPU(mmu);

            long steps = 0;
            while(true) {
                cpu.step();
                steps++;
                if (steps % 5_000_000 == 0) {
                    System.out.println("Steps: " + steps + " PC: " + Integer.toHexString(cpu.getRegisters().getPc()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}