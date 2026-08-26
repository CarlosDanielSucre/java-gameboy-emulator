package test.java.emulator.cpu;

import memory.MMU;
import cpu.CPU;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CPUTest {

    private CPU cpu;
    private MMU mmu;

    @BeforeEach
    void setUp() {
        // Criamos as dependências necessárias
        mmu = new MMU();
        cpu = new CPU(mmu);
    }

    @Test
    void shouldFetchOpcodeAndIncrementPC() {
        // Arrange
        cpu.setPC(0x0100);
        mmu.writeByte(0x0100, 0x3E);

        // Act
        int opcode = cpu.fetch();

        // Assert
        assertEquals(0x3E, opcode);
        assertEquals(0x0101, cpu.getPC());
    }
}