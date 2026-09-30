import cpu.CPU;
import memory.Cartridge;
import memory.MMU;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            byte[] romData = Files.readAllBytes(Path.of("roms/test/01-special.gb"));

            System.out.println("=== ANÁLISE DO LOOP ===\n");
            System.out.println("📍 Loop em 0x0206-0x0209:");
            for (int i = 0x206; i <= 0x209; i++) {
                System.out.printf("  0x%04X: 0x%02X%n", i, romData[i] & 0xFF);
            }

            // JR NZ offset
            byte offset = (byte) (romData[0x20A] & 0xFF);
            System.out.printf("\n0x0209: 0x20 (JR NZ) com offset 0x%02X (%d)%n", romData[0x20A] & 0xFF, offset);
            System.out.println("PC após fetch: 0x020B");
            System.out.println("Destino se salta: 0x" + Integer.toHexString((0x020B + offset) & 0xFFFF));
            System.out.println("Destino se NÃO salta: 0x020B");

            // Agora executa começando do entry point correto
            System.out.println("\n\n=== EXECUTANDO EMULADOR ===\n");
            Cartridge cartridge = new Cartridge(romData);
            MMU mmu = new MMU(cartridge);
            CPU cpu = new CPU(mmu);

            // COMEÇA EM 0x0100!
            cpu.getRegisters().setPc(0x0100);

            long steps = 0;
            long maxSteps = 100;
            int lastPC = -1;
            int loopCount = 0;

            while (steps < maxSteps) {
                try {
                    for (int i = 0x20A; i <= 0x210; i++) {
                        System.out.printf("0x%04X: 0x%02X%n", i, romData[i] & 0xFF);
                    }
                    int currentPC = cpu.getRegisters().getPc();
                    int opcode = mmu.readByte(currentPC);

                    // 🔍 DEBUG: mostra antes de INC E
                    if (currentPC == 0x0208 && opcode == 0x1C) {
                        int eBefore = cpu.getRegisters().getE();
                        System.out.printf("[DEBUG INC E] ANTES: E=0x%02X, Z=%d%n",
                                eBefore,
                                cpu.getFlags().isZero() ? 1 : 0);
                    }


                    System.out.printf("0x%04X: 0x%02X DEC C antes: C=0x%02X%n", cpu.getRegisters().getPc(), opcode, cpu.getRegisters().getC());
                    cpu.step();
                    System.out.printf("0x%04X: DEC C depois: C=0x%02X Z=%d%n", cpu.getRegisters().getPc(), cpu.getRegisters().getC(), cpu.getFlags().isZero() ? 1 : 0);

                    // 🔍 DEBUG: mostra depois de INC E
                    if (currentPC == 0x0208 && opcode == 0x1C) {
                        int eAfter = cpu.getRegisters().getE();
                        System.out.printf("[DEBUG INC E] DEPOIS: E=0x%02X, Z=%d%n",
                                eAfter,
                                cpu.getFlags().isZero() ? 1 : 0);
                    }

                    int newPC = cpu.getRegisters().getPc();

                    // Mostra passagens pelo loop
                    if (currentPC == 0x0209) {
                        loopCount++;
                        System.out.printf("[%6d] LOOP #%d | A=0x%02X | Z=%d C=%d | E=0x%02X | PC: 0x%04X -> 0x%04X%n",
                                steps, loopCount,
                                cpu.getRegisters().getA(),
                                cpu.getFlags().isZero() ? 1 : 0,
                                cpu.getFlags().isCarry() ? 1 : 0,
                                cpu.getRegisters().getE(),
                                currentPC, newPC);

                        if (loopCount > 15) {
                            System.out.println("\n🔄 Loop infinito detectado!");
                            break;
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

                if (cpu.isHalted()) {
                    System.out.println("\n✅ CPU HALTED!");
                    break;
                }
            }

            System.out.println("\n--- RESUMO ---");
            System.out.println("Passos: " + steps);
            System.out.println("PC atual: 0x" + Integer.toHexString(cpu.getRegisters().getPc()));
            System.out.println("A: 0x" + Integer.toHexString(cpu.getRegisters().getA()));
            System.out.println("E: 0x" + Integer.toHexString(cpu.getRegisters().getE()));
            System.out.println("HL: 0x" + Integer.toHexString(cpu.getRegisters().getHL()));
            System.out.println("DE: 0x" + Integer.toHexString(cpu.getRegisters().getDE()));
            System.out.println("Flags Z: " + (cpu.getFlags().isZero() ? 1 : 0));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
