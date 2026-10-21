import cpu.CPU;
import memory.Cartridge;
import memory.MMU;
import ppu.PPU;
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


        try {
            byte[] romData = Files.readAllBytes(Path.of("roms/test/cpu_instrs.gb"));

            Cartridge cartridge = new Cartridge(romData);
            System.out.println("Tipo de cartucho (0x0147): 0x" + Integer.toHexString(cartridge.readByte(0x0147)));
            System.out.println("Tamanho da ROM (0x0148): 0x" + Integer.toHexString(cartridge.readByte(0x0148)));
            MMU mmu = new MMU(cartridge);
            CPU cpu = new CPU(mmu);
            PPU ppu = mmu.getPpu();
            int[] framebuffer = ppu.getFramebuffer();

            long steps = 0;

            while (true) {
                try {
                    if (cpu.step()) {
                        screen.updateFrame(framebuffer);
                    }

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
