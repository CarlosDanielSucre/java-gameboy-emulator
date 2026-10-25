import cpu.CPU;
import input.Joypad;
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
        try {
            byte[] romData = Files.readAllBytes(Path.of("roms/test/tetris.gb"));

            Cartridge cartridge = new Cartridge(romData);
            Joypad joypad = new Joypad();
            MMU mmu = new MMU(cartridge, joypad);
            CPU cpu = new CPU(mmu);
            Screen screen = new Screen(joypad);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLocationRelativeTo(null);
            window.add(screen);
            window.pack();
            window.setVisible(true);
            screen.requestFocusInWindow();
            PPU ppu = mmu.getPpu();
            int[] framebuffer = ppu.getFramebuffer();

            long steps = 0;
            long alvo = System.nanoTime();
            long secondInitial = System.nanoTime();
            while (true) {
                try {
                    long timeRest;


                    if (cpu.step()) {
                        alvo += 16742706;
                        screen.updateFrame(framebuffer);
                        if ((System.nanoTime() - secondInitial) >= 1_000_000_000) {
                            secondInitial = System.nanoTime();
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
