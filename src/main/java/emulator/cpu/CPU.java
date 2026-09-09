package cpu;

import memory.MMU;

public class CPU {
    private Registers registers;
    private Flags flags;
    private MMU mmu;
    private Instruction[] opcodeTable = new Instruction[256];

    public CPU(MMU mmu) {
        this.registers = new Registers();
        this.flags = new Flags();
        this.mmu = mmu;
        initOpcodeTable();
    }

    public boolean calculateHalfCarry(int a, int b) {
        return ((a & 0xF) + (b & 0xF) > 0xF);
    }

    public void step() {
        int opcode = fetch();
        Instruction instruction = opcodeTable[opcode];
        if(instruction == null) {
            throw new RuntimeException(String.format("Opcode no implemented: 0x%02X", opcode));
        }
        instruction.execute();
    }

    public int fetch() {
        int opcode = mmu.readByte(registers.getPc());
        registers.setPc((registers.getPc() + 1) & 0xFFFF);
        return opcode;
    }

    public Registers getRegisters() {
        return this.registers;
    }

    private void initOpcodeTable() {
        opcodeTable[0x00] = () -> { /* NOP */ };
    }
}
