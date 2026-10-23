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
            long alvo = System.nanoTime();
            int countFps = 0;
            long secondInitial = System.nanoTime();
            while (true) {
                try {
                    long timeRest;


                    if (cpu.step()) {
                        alvo += 16742706;
                        screen.updateFrame(framebuffer);
                        countFps++;
                        if ((System.nanoTime() - secondInitial) >= 1_000_000_000) {
                            System.out.println(countFps);
                            secondInitial = System.nanoTime();
                            countFps = 0;
                        }
                        timeRest = alvo - System.nanoTime();
                        if (timeRest < -100_000_000) {
                            alvo = System.nanoTime();
                        }
                        if (timeRest > 0) {
                            Thread.sleep(
                                    timeRest / 1_000_000,
                                    (int) (timeRest % 1_000_000)
                            );
                        }
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
