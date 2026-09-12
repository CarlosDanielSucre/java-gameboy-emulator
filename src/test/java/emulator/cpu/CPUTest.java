package test.java.emulator.cpu;

import cpu.Flags;
import cpu.Registers;
import memory.Cartridge;
import memory.MMU;
import cpu.CPU;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CPUTest {

    private CPU cpu;
    private MMU mmu;
    private Cartridge cartridge;
    private Registers registers;
    private Flags flags;

    @BeforeEach
    void setUp() {
        cartridge = new Cartridge(new byte[0x8000]);;
        mmu = new MMU(cartridge);
        cpu = new CPU(mmu);
        registers = cpu.getRegisters();
        flags = cpu.getFlags();
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

    @Test
    void opcode0x80WithoutOverflow() {
        registers.setA(0x01);
        registers.setB(0x02);
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x80);

        cpu.step();
        assertEquals(0x03, registers.getA());
        Assertions.assertFalse(flags.isCarry());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertFalse(flags.isZero());
    }
    @Test
    void opcode0x80ResultZero() {
        registers.setA(0x00);
        registers.setB(0x00);
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x80);

        cpu.step();
        assertEquals(0x00, registers.getA());
        Assertions.assertFalse(flags.isCarry());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertTrue(flags.isZero());
    }
    @Test
    void opcode0x80WithOverflow() {
        registers.setA(0xFF);
        registers.setB(0x01);
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x80);

        cpu.step();
        assertEquals(0x00, registers.getA());
        Assertions.assertTrue(flags.isCarry());
        Assertions.assertTrue(flags.isHalfCarry());
        Assertions.assertTrue(flags.isZero());
    }
}