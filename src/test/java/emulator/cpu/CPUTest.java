package test.java.emulator.cpu;

import cpu.Registers;
import memory.Cartridge;
import memory.MMU;
import cpu.CPU;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CPUTest {

    private CPU cpu;
    private MMU mmu;
    private Cartridge cartridge;
    private Registers registers;

    @BeforeEach
    void setUp() {
        cartridge = new Cartridge(new byte[0x8000]);;
        mmu = new MMU(cartridge);
        cpu = new CPU(mmu);
        registers = cpu.getRegisters();
    }

    @Test
    void shouldFetchOpcodeAndIncrementPC() {
        // Arrange
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x3E);

        int opcode = cpu.fetch();

        assertEquals(0x3E, opcode);
        assertEquals(0xC001, registers.getPc());
    }

    @Test
    void shouldExecuteNOP() {
        assertDoesNotThrow(() -> {
            registers.setPc(0xC000);
            mmu.writeByte(0xC000, 0x00);

            cpu.step();
        });
    }

    @Test
    void shouldThrowWhenOpcodeNotImplemented() {
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x01);
        assertThrows(RuntimeException.class, () ->
            cpu.step()
        );
    }

    @Test
    void opcode0x3E() {
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x3E);
        mmu.writeByte(0xC001, 0x42);

        cpu.step();
        assertEquals(0x42, registers.getA());
    }
}