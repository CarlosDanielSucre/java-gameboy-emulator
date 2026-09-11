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
        opcodeTable[0x3E] = () -> { /* LD A, d8 */
            int value = fetch();
            registers.setA(value);
        };
        opcodeTable[0x06] = () -> { /* LD B, d8 */
            int value = fetch();
            registers.setB(value);
        };
        opcodeTable[0x0E] = () -> { /* LD C, d8 */
            int value = fetch();
            registers.setC(value);
        };
        opcodeTable[0x16] = () -> { /* LD D, d8 */
            int value = fetch();
            registers.setD(value);
        };
        opcodeTable[0x1E] = () -> { /* LD E, d8 */
            int value = fetch();
            registers.setE(value);
        };
        opcodeTable[0x26] = () -> { /* LD H, d8 */
            int value = fetch();
            registers.setH(value);
        };
        opcodeTable[0x2E] = () -> { /* LD L, d8 */
            int value = fetch();
            registers.setL(value);
        };
        opcodeTable[0x40] = () -> { /* LD B, B */
            registers.setB(registers.getB());
        };
        opcodeTable[0x41] = () -> { /* LD B, C */
            registers.setB(registers.getC());
        };
        opcodeTable[0x42] = () -> { /* LD B, D */
            registers.setB(registers.getD());
        };
        opcodeTable[0x43] = () -> { /* LD B, E */
            registers.setB(registers.getE());
        };
        opcodeTable[0x44] = () -> { /* LD B, H */
            registers.setB(registers.getH());
        };
        opcodeTable[0x45] = () -> { /* LD B, L */
            registers.setB(registers.getL());
        };
        opcodeTable[0x46] = () -> { /* LD B, (HL) */
            registers.setB(mmu.readByte(registers.getHL()));
        };
        opcodeTable[0x47] = () -> { /* LD B, A */
            registers.setB(registers.getA());
        };

        opcodeTable[0x48] = () -> { /* LD C, B */
            registers.setC(registers.getB());
        };
        opcodeTable[0x49] = () -> { /* LD C, C */
            registers.setC(registers.getC());
        };
        opcodeTable[0x4A] = () -> { /* LD C, D */
            registers.setC(registers.getD());
        };
        opcodeTable[0x4B] = () -> { /* LD C, E */
            registers.setC(registers.getE());
        };
        opcodeTable[0x4C] = () -> { /* LD C, H */
            registers.setC(registers.getH());
        };
        opcodeTable[0x4D] = () -> { /* LD C, L */
            registers.setC(registers.getL());
        };
        opcodeTable[0x4E] = () -> { /* LD C, (HL) */
            registers.setC(mmu.readByte(registers.getHL()));
        };
        opcodeTable[0x4F] = () -> { /* LD C, A */
            registers.setC(registers.getA());
        };

        opcodeTable[0x50] = () -> { /* LD D, B */
            registers.setD(registers.getB());
        };
        opcodeTable[0x51] = () -> { /* LD D, C */
            registers.setD(registers.getC());
        };
        opcodeTable[0x52] = () -> { /* LD D, D */
            registers.setD(registers.getD());
        };
        opcodeTable[0x53] = () -> { /* LD D, E */
            registers.setD(registers.getE());
        };
        opcodeTable[0x54] = () -> { /* LD D, H */
            registers.setD(registers.getH());
        };
        opcodeTable[0x55] = () -> { /* LD D, L */
            registers.setD(registers.getL());
        };
        opcodeTable[0x56] = () -> { /* LD D, (HL) */
            registers.setD(mmu.readByte(registers.getHL()));
        };
        opcodeTable[0x57] = () -> { /* LD D, A */
            registers.setD(registers.getA());
        };

        opcodeTable[0x58] = () -> { /* LD E, B */
            registers.setE(registers.getB());
        };
        opcodeTable[0x59] = () -> { /* LD E, C */
            registers.setE(registers.getC());
        };
        opcodeTable[0x5A] = () -> { /* LD E, D */
            registers.setE(registers.getD());
        };
        opcodeTable[0x5B] = () -> { /* LD E, E */
            registers.setE(registers.getE());
        };
        opcodeTable[0x5C] = () -> { /* LD E, H */
            registers.setE(registers.getH());
        };
        opcodeTable[0x5D] = () -> { /* LD E, L */
            registers.setE(registers.getL());
        };
        opcodeTable[0x5E] = () -> { /* LD E, (HL) */
            registers.setE(mmu.readByte(registers.getHL()));
        };
        opcodeTable[0x5F] = () -> { /* LD E, A */
            registers.setE(registers.getA());
        };

        opcodeTable[0x60] = () -> { /* LD H, B */
            registers.setH(registers.getB());
        };
        opcodeTable[0x61] = () -> { /* LD H, C */
            registers.setH(registers.getC());
        };
        opcodeTable[0x62] = () -> { /* LD H, D */
            registers.setH(registers.getD());
        };
        opcodeTable[0x63] = () -> { /* LD H, E */
            registers.setH(registers.getE());
        };
        opcodeTable[0x64] = () -> { /* LD H, H */
            registers.setH(registers.getH());
        };
        opcodeTable[0x65] = () -> { /* LD H, L */
            registers.setH(registers.getL());
        };
        opcodeTable[0x66] = () -> { /* LD H, (HL) */
            registers.setH(mmu.readByte(registers.getHL()));
        };
        opcodeTable[0x67] = () -> { /* LD H, A */
            registers.setH(registers.getA());
        };

        opcodeTable[0x68] = () -> { /* LD L, B */
            registers.setL(registers.getB());
        };
        opcodeTable[0x69] = () -> { /* LD L, C */
            registers.setL(registers.getC());
        };
        opcodeTable[0x6A] = () -> { /* LD L, D */
            registers.setL(registers.getD());
        };
        opcodeTable[0x6B] = () -> { /* LD L, E */
            registers.setL(registers.getE());
        };
        opcodeTable[0x6C] = () -> { /* LD L, H */
            registers.setL(registers.getH());
        };
        opcodeTable[0x6D] = () -> { /* LD L, L */
            registers.setL(registers.getL());
        };
        opcodeTable[0x6E] = () -> { /* LD L, (HL) */
            registers.setL(mmu.readByte(registers.getHL()));
        };
        opcodeTable[0x6F] = () -> { /* LD L, A */
            registers.setL(registers.getA());
        };

        opcodeTable[0x70] = () -> { /* LD (HL), B */
            mmu.writeByte(registers.getHL(), registers.getB());
        };
        opcodeTable[0x71] = () -> { /* LD (HL), C */
            mmu.writeByte(registers.getHL(), registers.getC());
        };
        opcodeTable[0x72] = () -> { /* LD (HL), D */
            mmu.writeByte(registers.getHL(), registers.getD());
        };
        opcodeTable[0x73] = () -> { /* LD (HL), E */
            mmu.writeByte(registers.getHL(), registers.getE());
        };
        opcodeTable[0x74] = () -> { /* LD (HL), H */
            mmu.writeByte(registers.getHL(), registers.getH());
        };
        opcodeTable[0x75] = () -> { /* LD (HL), L */
            mmu.writeByte(registers.getHL(), registers.getL());
        };
        // opcodeTable[0x76] = () -> { /* HALT */
            // NO IMPLEMENTED
        //};
        opcodeTable[0x77] = () -> { /* LD (HL), A */
            mmu.writeByte(registers.getHL(), registers.getA());
        };

        opcodeTable[0x78] = () -> { /* LD A, B */
            registers.setA(registers.getB());
        };
        opcodeTable[0x79] = () -> { /* LD A, C */
            registers.setA(registers.getC());
        };
        opcodeTable[0x7A] = () -> { /* LD A, D */
            registers.setA(registers.getD());
        };
        opcodeTable[0x7B] = () -> { /* LD A, E */
            registers.setA(registers.getE());
        };
        opcodeTable[0x7C] = () -> { /* LD A, H */
            registers.setA(registers.getH());
        };
        opcodeTable[0x7D] = () -> { /* LD A, L */
            registers.setA(registers.getL());
        };
        opcodeTable[0x7E] = () -> { /* LD A, (HL) */
            registers.setA(mmu.readByte(registers.getHL()));
        };
        opcodeTable[0x7F] = () -> { /* LD A, A */
            registers.setA(registers.getA());
        };
    }
}
