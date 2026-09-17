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

    //=========================================
    //============== 0x00 - 0x0F ==============

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
        mmu.writeByte(0xC000, 0xCB);
        assertThrows(RuntimeException.class, () ->
            cpu.step()
        );
    }
    @Test
    void opcode0x0D() {
        registers.setPc(0xC000);
        registers.setC(0x03);
        flags.setCarry(false);
        mmu.writeByte(0xC000, 0x0D);

        cpu.step();
        assertEquals(0x02, registers.getC());
        Assertions.assertTrue(flags.isSubtract());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertFalse(flags.isZero());
        Assertions.assertFalse(flags.isCarry());
    }

    //=========================================
    //============== 0x10 - 0x1F ==============
    @Test
    void opcode0x14() {
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
    void opcode0x18() {
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x20);
        mmu.writeByte(0xC001, 0x03);

        cpu.step();
        assertEquals(0xC005, registers.getPc());
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
    void opcode0x1F() {
        registers.setPc(0xC000);
        registers.setA(0b10101011);
        flags.setCarry(false);
        mmu.writeByte(0xC000, 0x1F);

        cpu.step();
        assertEquals(0b01010101, registers.getA());
        Assertions.assertFalse(flags.isZero());
        Assertions.assertFalse(flags.isSubtract());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertTrue(flags.isCarry());
    }
    @Test
    void opcode0x1FisCarryTrue() {
        registers.setPc(0xC000);
        registers.setA(0b10101011);
        flags.setCarry(true);
        mmu.writeByte(0xC000, 0x1F);

        cpu.step();
        assertEquals(0b11010101, registers.getA());
        Assertions.assertFalse(flags.isZero());
        Assertions.assertFalse(flags.isSubtract());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertTrue(flags.isCarry());
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

    //=========================================
    //============== 0x20 - 0x2F ==============

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
    @Test
    void opcode0x21() {
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x21);
        mmu.writeByte(0xC001, 0x34);
        mmu.writeByte(0xC002, 0x12);

        cpu.step();
        assertEquals(0x1234, registers.getHL());
    }

    //=========================================
    //============== 0x30 - 0x3F ==============
    @Test
    void opcode0x39() {
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x39);
        registers.setSp(0x1234);
        registers.setHL(0x1111);
        flags.setSubtract(true);

        cpu.step();
        assertEquals(0x2345, registers.getHL());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertFalse(flags.isCarry());
        Assertions.assertFalse(flags.isSubtract());
    }
    @Test
    void opcode0x3E() {
        registers.setPc(0xC000);
        mmu.writeByte(0xC000, 0x3E);
        mmu.writeByte(0xC001, 0x42);

        cpu.step();
        assertEquals(0x42, registers.getA());
    }

    //=========================================
    //============== 0x80 - 0x8F ==============
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

    //=========================================
    //============== 0xB0 - 0xBF ==============
    @Test
    void opcode0xB1() {
        registers.setPc(0xC000);
        registers.setA(0b01000000);
        registers.setC(0b00000001);

        mmu.writeByte(0xC000, 0xB1);
        cpu.step();
        assertEquals(0b01000001, registers.getA());
        Assertions.assertFalse(flags.isZero());
    }

    //=========================================
    //============== 0xC0 - 0xCF ==============
    @Test
    void opcode0xC4() {
        registers.setPc(0xC000);
        registers.setSp(0xFFFE);
        mmu.writeByte(0xC000, 0xC4);
        mmu.writeByte(0xC001, 0x34);
        mmu.writeByte(0xC002, 0x12);
        flags.setZero(false);

        cpu.step();
        assertEquals(0x1234, registers.getPc());
        assertEquals(0xFFFC, registers.getSp());
        assertEquals(0xC0, mmu.readByte(0xFFFD));
        assertEquals(0x03, mmu.readByte(0xFFFC));
    }
    @Test
    void opcode0xC4isZero() {
        registers.setPc(0xC000);
        registers.setSp(0xFFFE);
        mmu.writeByte(0xC000, 0xC4);
        mmu.writeByte(0xC001, 0x00);
        mmu.writeByte(0xC002, 0x00);
        flags.setZero(true);

        cpu.step();
        assertEquals(0xC003, registers.getPc());
        assertEquals(0xFFFE, registers.getSp());
    }

    @Test
    void opcode0xCD() {
        registers.setPc(0xC000);
        registers.setSp(0xFFFE);
        mmu.writeByte(0xC000, 0xCD);
        mmu.writeByte(0xC001, 0x34);
        mmu.writeByte(0xC002, 0x12);

        cpu.step();
        assertEquals(0x1234, registers.getPc());
        assertEquals(0xFFFC, registers.getSp());
        assertEquals(0xC0, mmu.readByte(0xFFFD));
        assertEquals(0x03, mmu.readByte(0xFFFC));
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

    //=========================================
    //============== 0xE0 - 0xEF ==============

    @Test
    void opcode0xE1() {
        registers.setPc(0xC000);
        registers.setSp(0xFFFA);
        mmu.writeByte(0xC000, 0xE1);
        mmu.writeByte(0xFFFA, 0x34);
        mmu.writeByte(0xFFFB, 0x12);

        cpu.step();
        assertEquals(0xFFFC, registers.getSp());
        assertEquals(0x1234, registers.getHL());
    }
    @Test
    void opcode0xE5() {
        registers.setPc(0xC000);
        registers.setSp(0xFFFE);
        registers.setHL(0x1234);
        mmu.writeByte(0xC000, 0xE5);

        cpu.step();
        assertEquals(0xFFFC, registers.getSp());
        assertEquals(0x12, mmu.readByte(0xFFFD));
        assertEquals(0x34, mmu.readByte(0xFFFC));
    }


    //=========================================
    //============== 0xF0 - 0xFF ==============

    @Test
    void opcode0xFEisZero() {
        registers.setPc(0xC000);
        registers.setA(0x12);
        mmu.writeByte(0xC000, 0xFE);
        mmu.writeByte(0xC001, 0x12);

        cpu.step();
        Assertions.assertTrue(flags.isZero());
        Assertions.assertTrue(flags.isSubtract());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertFalse(flags.isCarry());
    }
    @Test
    void opcode0xFEisHalfCarry() {
        registers.setPc(0xC000);
        registers.setA(0x11);
        mmu.writeByte(0xC000, 0xFE);
        mmu.writeByte(0xC001, 0x12);

        cpu.step();
        Assertions.assertFalse(flags.isZero());
        Assertions.assertTrue(flags.isSubtract());
        Assertions.assertTrue(flags.isHalfCarry());
        Assertions.assertTrue(flags.isCarry());
    }
    @Test
    void opcode0xFEisGreater() {
        registers.setPc(0xC000);
        registers.setA(0x15);
        mmu.writeByte(0xC000, 0xFE);
        mmu.writeByte(0xC001, 0x12);

        cpu.step();
        Assertions.assertFalse(flags.isZero());
        Assertions.assertTrue(flags.isSubtract());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertFalse(flags.isCarry());
    }

    //=========================================
    //============== 0x10 - 0x1F ==============
    @Test
    void cbOpcode0x19() {
        registers.setPc(0xC000);
        registers.setC(0b10101011);
        flags.setCarry(false);
        mmu.writeByte(0xC000, 0xCB);
        mmu.writeByte(0xC001, 0x19);

        cpu.step();
        assertEquals(0b01010101, registers.getC());
        Assertions.assertFalse(flags.isZero());
        Assertions.assertFalse(flags.isSubtract());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertTrue(flags.isCarry());
    }

    //=========================================
    //============== 0x30 - 0x3F ==============

    @Test
    void cbOpcode0x38() {
        registers.setPc(0xC000);
        registers.setB(0b10101011);
        mmu.writeByte(0xC000, 0xCB);
        mmu.writeByte(0xC001, 0x38);

        cpu.step();
        assertEquals(0b01010101, registers.getB());
        Assertions.assertFalse(flags.isZero());
        Assertions.assertFalse(flags.isSubtract());
        Assertions.assertFalse(flags.isHalfCarry());
        Assertions.assertTrue(flags.isCarry());
    }
}