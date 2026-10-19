import cpu.CPU;
import memory.Cartridge;
import memory.MMU;
import ppu.Screen;

import javax.swing.JFrame;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("GameBoy");
        Screen screen = new Screen();
        window.add(screen);
        window.pack();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        int[] test = new int[160 * 144];
        for (int y = 0; y < 144; y++) {
            for (int x = 0; x < 160; x++) {
                if (((x / 8) + (y / 8)) % 2 == 0) {
                    test[y * 160 + x] = 0xFFFFFF; // branco
                } else {
                    test[y * 160 + x] = 0x000000; // preto
                }
            }
        }
        screen.updateFrame(test);


        try {
            byte[] romData = Files.readAllBytes(Path.of("roms/test/cpu_instrs.gb"));

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
