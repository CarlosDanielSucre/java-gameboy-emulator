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
        System.out.println(opcode);
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

    public Flags getFlags() {
        return this.flags;
    }

    private void initOpcodeTable() {

        //=========================================
        //============== 0x00 - 0x0F ==============

        opcodeTable[0x00] = () -> { /* NOP */ };
        opcodeTable[0x3E] = () -> { /* LD A, d8 */
            int value = fetch();
            registers.setA(value);
        };
        opcodeTable[0x06] = () -> { /* LD B, d8 */
            int value = fetch();
            registers.setB(value);
        };
        opcodeTable[0x0D] = () -> { /* DEC C */
            int c = registers.getC();
            int value =  c - 1;
            boolean isHalfCarry = (c & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            registers.setC(value);
        };
        opcodeTable[0x0E] = () -> { /* LD C, d8 */
            int value = fetch();
            registers.setC(value);
        };

        //=========================================
        //============== 0x10 - 0x1F ==============

        opcodeTable[0x11] = () -> { /* LD DE, d16*/
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;

            registers.setDE(value);
        };
        opcodeTable[0x12] = () -> { /* LD (DE), A */
            mmu.writeByte(registers.getDE(), registers.getA());
        };
        opcodeTable[0x14] = () -> { /* INC D */
            int d = registers.getD();
            int value =  d + 1;
            boolean isHalfCarry = calculateHalfCarry(d, 1);
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            registers.setD(value);
        };
        opcodeTable[0x16] = () -> { /* LD D, d8 */
            int value = fetch();
            registers.setD(value);
        };
        opcodeTable[0x1C] = () -> { /* INC E */
            int e = registers.getE();
            int value =  e + 1;
            boolean isHalfCarry = calculateHalfCarry(e, 1);
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            registers.setE(value);

        };
        opcodeTable[0x1E] = () -> { /* LD E, d8 */
            int value = fetch();
            registers.setE(value);
        };

        //=========================================
        //============== 0x20 - 0x2F ==============

        opcodeTable[0x20] = () -> { /* JR NZ,r8 */
            int opcodeNext = fetch();
            byte offSet = (byte) opcodeNext;
            if (!flags.isZero()) {
                registers.setPc(registers.getPc() + offSet);
            }
        };
        opcodeTable[0x21] = () -> { /* LD HL,d16 */
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;

            registers.setHL(value);
        };
        opcodeTable[0x26] = () -> { /* LD H, d8 */
            int value = fetch();
            registers.setH(value);
        };
        opcodeTable[0x2A] = () -> { /* LD A, (HL+)*/
            int hl = registers.getHL();
            registers.setA(mmu.readByte(hl));
            registers.setHL(hl + 1);
        };
        opcodeTable[0x2E] = () -> { /* LD L, d8 */
            int value = fetch();
            registers.setL(value);
        };

        //=========================================
        //============== 0x30 - 0x3F ==============

        opcodeTable[0x31] = () -> { /* LD SP,d16 */
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;

            registers.setSp(value);
        };

        //=========================================
        //============== 0x40 - 0x4F ==============

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

        //=========================================
        //============== 0x50 - 0x5F ==============

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

        //=========================================
        //============== 0x60 - 0x6F ==============

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

        //=========================================
        //============== 0x70 - 0x7F ==============

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

        //=========================================
        //============== 0x80 - 0x8F ==============

        opcodeTable[0x80] = () -> { /* ADD A, B */
            int a = registers.getA();
            int b = registers.getB();
            boolean halfCarry = calculateHalfCarry(a, b);
            int result = a + b;
            boolean isCarry = result > 0xFF;
            boolean isZero = (result & 0xFF) == 0;

            registers.setA(result);
            flags.setZero(isZero);
            flags.setCarry(isCarry);
            flags.setHalfCarry(halfCarry);
            flags.setSubtract(false);
        };
        opcodeTable[0x81] = () -> { /* ADD A, C */
            int a = registers.getA();
            int c = registers.getC();
            boolean halfCarry = calculateHalfCarry(a, c);
            int result = a + c;
            boolean isCarry = result > 0xFF;
            boolean isZero = (result & 0xFF) == 0;

            registers.setA(result);
            flags.setZero(isZero);
            flags.setCarry(isCarry);
            flags.setHalfCarry(halfCarry);
            flags.setSubtract(false);
        };
        opcodeTable[0x82] = () -> { /* ADD A, D */
            int a = registers.getA();
            int d = registers.getD();
            boolean halfCarry = calculateHalfCarry(a, d);
            int result = a + d;
            boolean isCarry = result > 0xFF;
            boolean isZero = (result & 0xFF) == 0;

            registers.setA(result);
            flags.setZero(isZero);
            flags.setCarry(isCarry);
            flags.setHalfCarry(halfCarry);
            flags.setSubtract(false);
        };
        opcodeTable[0x83] = () -> { /* ADD A, E */
            int a = registers.getA();
            int e = registers.getE();
            boolean halfCarry = calculateHalfCarry(a, e);
            int result = a + e;
            boolean isCarry = result > 0xFF;
            boolean isZero = (result & 0xFF) == 0;

            registers.setA(result);
            flags.setZero(isZero);
            flags.setCarry(isCarry);
            flags.setHalfCarry(halfCarry);
            flags.setSubtract(false);
        };
        opcodeTable[0x84] = () -> { /* ADD A, H */
            int a = registers.getA();
            int h = registers.getH();
            boolean halfCarry = calculateHalfCarry(a, h);
            int result = a + h;
            boolean isCarry = result > 0xFF;
            boolean isZero = (result & 0xFF) == 0;

            registers.setA(result);
            flags.setZero(isZero);
            flags.setCarry(isCarry);
            flags.setHalfCarry(halfCarry);
            flags.setSubtract(false);
        };
        opcodeTable[0x85] = () -> { /* ADD A, L */
            int a = registers.getA();
            int l = registers.getL();
            boolean halfCarry = calculateHalfCarry(a, l);
            int result = a + l;
            boolean isCarry = result > 0xFF;
            boolean isZero = (result & 0xFF) == 0;

            registers.setA(result);
            flags.setZero(isZero);
            flags.setCarry(isCarry);
            flags.setHalfCarry(halfCarry);
            flags.setSubtract(false);
        };
        opcodeTable[0x86] = () -> { /* ADD A, (HL) */
            int a = registers.getA();
            int value = mmu.readByte(registers.getHL());
            boolean halfCarry = calculateHalfCarry(a, value);
            int result = a + value;
            boolean isCarry = result > 0xFF;
            boolean isZero = (result & 0xFF) == 0;

            registers.setA(result);
            flags.setZero(isZero);
            flags.setCarry(isCarry);
            flags.setHalfCarry(halfCarry);
            flags.setSubtract(false);
        };
        opcodeTable[0x87] = () -> { /* ADD A, A */
            int a = registers.getA();
            boolean halfCarry = calculateHalfCarry(a, a);
            int result = a + a;
            boolean isCarry = result > 0xFF;
            boolean isZero = (result & 0xFF) == 0;

            registers.setA(result);
            flags.setZero(isZero);
            flags.setCarry(isCarry);
            flags.setHalfCarry(halfCarry);
            flags.setSubtract(false);
        };

        //=========================================
        //============== 0xC0 - 0xCF ==============

        opcodeTable[0xC3] = () -> { /* JP a16 */
            int low = fetch();
            int high = fetch();
            int jumpAddress = (high << 8) | low;

            registers.setPc(jumpAddress);
        };

        //=========================================
        //============== 0xE0 - 0xEF ==============

        opcodeTable[0xEA] = () -> { /* LD (a16),A */
            int low = fetch();
            int high = fetch();
            int address = (high << 8) | low;
            mmu.writeByte(address, registers.getA());
        };
        //=========================================
        //============== 0xF0 - 0xFF ==============

        opcodeTable[0xF3] = () -> { /* DI */
            //===============
            // NO IMPLEMENTED
            //===============
        };

    }
}
