package cpu;

import memory.MMU;

public class CPU {
    private Registers registers;
    private Flags flags;
    private MMU mmu;
    private int pc;

    public CPU(MMU mmu) {
        this.registers = new Registers();
        this.flags = new Flags();
        this.mmu = mmu;
    }

    public boolean calculateHalfCarry(int a, int b) {
        return ((a & 0xF) + (b & 0xF) > 0xF);
    }

    public void step() {
        int opcode = fetch();

        System.out.printf(
                "PC=%04X OPCODE=%02X%n",
                (pc - 1) & 0xFFFF,
                opcode
        );
    }

    private int fetch() {
        int opcode = mmu.readByte(pc);
        pc = (pc + 1) & 0xFFFF;
        return opcode;
    }
}
