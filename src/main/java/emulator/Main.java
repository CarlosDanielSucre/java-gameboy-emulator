import cpu.CPU;
import memory.Cartridge;
import memory.MMU;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            byte[] romData = Files.readAllBytes(Path.of("roms/test/02-interrupts.gb"));

            Cartridge cartridge = new Cartridge(romData);
            MMU mmu = new MMU(cartridge);
            CPU cpu = new CPU(mmu);

            cpu.getRegisters().setPc(0x0100);

            long steps = 0;
            long maxSteps = 10000000;
            int lastPC = -1;
            int loopCount = 0;

            while (steps < maxSteps) {
                try {
                    int currentPC = cpu.getRegisters().getPc();
                    int opcode = mmu.readByte(currentPC);

                    cpu.step();


                    int newPC = cpu.getRegisters().getPc();

                } catch (Exception e) {
                    System.out.println("\n❌ ERRO!");
                    System.out.println("Passos: " + steps);
                    System.out.println("PC: 0x" + Integer.toHexString(cpu.getRegisters().getPc()));
                    System.out.println("Erro: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }

                steps++;

                if (cpu.isHalted()) {
                    System.out.println("\n✅ CPU HALTED!");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
