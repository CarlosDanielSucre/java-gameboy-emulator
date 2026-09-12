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
        registers.setC(0x02);
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x81);

        cpu.step();
        assertEquals(0x03, registers.getA());
        Assertions.assertFalse(flags.isCarry());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertFalse(flags.isZero());
    }
    @Test
    void opcode0x80ResultZero() {
        registers.setA(0x00);
        registers.setC(0x00);
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x81);

        cpu.step();
        assertEquals(0x00, registers.getA());
        Assertions.assertFalse(flags.isCarry());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertTrue(flags.isZero());
    }
    @Test
    void opcode0x80WithOverflow() {
        registers.setA(0xFF);
        registers.setC(0x01);
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x81);

        cpu.step();
        assertEquals(0x00, registers.getA());
        Assertions.assertTrue(flags.isCarry());
        Assertions.assertTrue(flags.isHalfCarry());
        Assertions.assertTrue(flags.isZero());
    }

    @Test
    void opcode0xC3JP() {
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0xC3);
        mmu.writeByte(0xC001, 0x34);
        mmu.writeByte(0xC002, 0x12);

        cpu.step();
        assertEquals(0x1234, registers.getPc());
    }
    @Test
    void opcode0x21() {
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x21);
        mmu.writeByte(0xC001, 0x34);
        mmu.writeByte(0xC002, 0x12);

        cpu.step();
        assertEquals(0x1234, registers.getHL());
    }

    @Test
    void opcode0x1C() {
        registers.setPc(0xC000);
        registers.setE(0x00);
        flags.setCarry(false);
        mmu.writeByte(0xC000, 0x1C);

        cpu.step();
        assertEquals(0x01, registers.getE());
        Assertions.assertFalse(flags.isSubtract());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertFalse(flags.isZero());
        Assertions.assertFalse(flags.isCarry());
    }
    @Test
    void opcode0x1CHalfCarryTrue() {
        registers.setPc(0xC000);
        registers.setE(0xFF);
        flags.setCarry(true);
        mmu.writeByte(0xC000, 0x1C);

        cpu.step();
        assertEquals(0x00, registers.getE());
        Assertions.assertFalse(flags.isSubtract());
        Assertions.assertTrue(flags.isHalfCarry());
        Assertions.assertTrue(flags.isZero());
        Assertions.assertTrue(flags.isCarry());
    }
    @Test
    void opcode0x20() {
        registers.setPc(0xC000);
        flags.setZero(false);
        mmu.writeByte(0xC000, 0x20);
        mmu.writeByte(0xC001, 0x03);

        cpu.step();
        assertEquals(0xC005, registers.getPc());
    }
    @Test
    void opcode0x20Negative() {
        registers.setPc(0xC013);
        flags.setZero(false);
        mmu.writeByte(0xC013, 0x20);
        mmu.writeByte(0xC014, 0xFD);

        cpu.step();
        assertEquals(0xC012, registers.getPc());
    }
}