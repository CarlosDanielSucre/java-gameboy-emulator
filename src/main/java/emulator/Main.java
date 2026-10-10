import cpu.CPU;
import memory.Cartridge;
import memory.MMU;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            byte[] romData = Files.readAllBytes(Path.of("roms/test/individual/01-special.gb"));

            Cartridge cartridge = new Cartridge(romData);
            System.out.println("Tipo de cartucho (0x0147): 0x" + Integer.toHexString(cartridge.readByte(0x0147)));
            System.out.println("Tamanho da ROM (0x0148): 0x" + Integer.toHexString(cartridge.readByte(0x0148)));
            MMU mmu = new MMU(cartridge);
            CPU cpu = new CPU(mmu);

            cpu.getRegisters().setPc(0x0100);

            long steps = 0;

            while (true) {
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


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
