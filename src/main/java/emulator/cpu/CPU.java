package cpu;

import memory.MMU;

public class CPU {
    private Registers registers;
    private Flags flags;
    private MMU mmu;
    private Instruction[] opcodeTable = new Instruction[256];
    private Instruction[] cbOpcodeTable = new Instruction[256];

    public CPU(MMU mmu) {
        this.flags = new Flags();
        this.registers = new Registers(flags);
        this.mmu = mmu;
        initOpcodeTable();
        initCBOpcodeTable();
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
        int cycles = instruction.execute();
        mmu.step(cycles);
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

        opcodeTable[0x00] = () -> { /* NOP */
            return 4;
        };
        opcodeTable[0x01] = () -> { /* LD BC, d16 */
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;
            registers.setBC(value);

            return 12;
        };
        opcodeTable[0x03] = () -> { /* INC BC */
            registers.setBC((registers.getBC() + 1) & 0xFFFF);
            return 8;
        };
        opcodeTable[0x04] = () -> { /* INC B */
            int b = registers.getB();
            int value =  (b + 1) & 0xFF;
            boolean isHalfCarry = calculateHalfCarry(b, 1);
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            registers.setB(value);
            return 4;
        };
        opcodeTable[0x05] = () -> { /* DEC B */
            int b = registers.getB();
            int value =  b - 1 & 0xFF;
            boolean isHalfCarry = (b & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            registers.setB(value);
            return 4;
        };
        opcodeTable[0x06] = () -> { /* LD B, d8 */
            int value = fetch();
            registers.setB(value);
            return 8;
        };
        opcodeTable[0x07] = () -> { /* RLCA */
            int value = registers.getA();
            boolean carry = ((value >> 7) & 0x1) == 1;
            int result = (value << 1) & 0xFF;

            if(carry) {
                result = result | 1;
            }

            flags.setZero(false);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry);
            registers.setA(result);

            return 4;
        };
        opcodeTable[0x0C] = () -> { /* INC C */
            int c = registers.getC();
            int value =  (c + 1) & 0xFF;
            boolean isHalfCarry = calculateHalfCarry(c, 1);
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            registers.setC(value);

            return 4;
        };
        opcodeTable[0x0D] = () -> { /* DEC C */
            int c = registers.getC();
            int value =  c - 1 & 0xFF;
            boolean isHalfCarry = (c & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            registers.setC(value);
            return 4;
        };
        opcodeTable[0x0E] = () -> { /* LD C, d8 */
            int value = fetch();
            registers.setC(value);
            return 8;
        };

        //=========================================
        //============== 0x10 - 0x1F ==============

        opcodeTable[0x11] = () -> { /* LD DE, d16*/
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;

            registers.setDE(value);
            return 11;
        };
        opcodeTable[0x12] = () -> { /* LD (DE), A */
            mmu.writeByte(registers.getDE(), registers.getA());
            return 8;
        };
        opcodeTable[0x13] = () -> { /* INC DE */
            registers.setDE((registers.getDE() + 1) & 0xFFFF);
            return 8;
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
            return 4;
        };
        opcodeTable[0x16] = () -> { /* LD D, d8 */
            int value = fetch();
            registers.setD(value);
            return 8;
        };
        opcodeTable[0x18] = () -> { /* JR r8 */
            int opcodeNext = fetch();
            byte offSet = (byte) opcodeNext;
            registers.setPc(registers.getPc() + offSet);
            return 12;
        };
        opcodeTable[0x1A] = () -> { /* LD A, (DE) */
            int de = registers.getDE();
            registers.setA(mmu.readByte(de));


            return 8;
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

            return 4;
        };
        opcodeTable[0x1D] = () -> { /* DEC E */
            int e = registers.getE();
            int value =  e - 1 & 0xFF;
            boolean isHalfCarry = (e & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            registers.setE(value);

            return 4;
        };
        opcodeTable[0x1E] = () -> { /* LD E, d8 */
            int value = fetch();
            registers.setE(value);

            return 8;
        };
        opcodeTable[0x1F] = () -> { /* RRA */
            int value = registers.getA();
            boolean oldCarry = flags.isCarry();
            boolean newCarry = (value & 0x1) == 1;
            int result = value >>> 1;

            if(oldCarry) {
                result = result | 1 << 7 ;
            }

            flags.setZero(false);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(newCarry);
            registers.setA(result);

            return 4;
        };

        //=========================================
        //============== 0x20 - 0x2F ==============

        opcodeTable[0x20] = () -> { /* JR NZ,r8 */
            int opcodeNext = fetch();
            byte offSet = (byte) opcodeNext;
            if (!flags.isZero()) {
                registers.setPc(registers.getPc() + offSet);
            }

            return 8;
        };
        opcodeTable[0x21] = () -> { /* LD HL,d16 */
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;

            registers.setHL(value);

            return 12;
        };
        opcodeTable[0x22] = () -> { /* LD (HL+),A */
            int a = registers.getA();
            int hl = registers.getHL();
            mmu.writeByte(hl, a);
            registers.setHL(hl + 1);

            return 8;
        };
        opcodeTable[0x23] = () -> { /* INC HL */
            registers.setHL((registers.getHL() + 1) & 0xFFFF);

            return 8;
        };
        opcodeTable[0x24] = () -> { /* INC H */
            int h = registers.getH();
            int value =  h + 1;
            boolean isHalfCarry = calculateHalfCarry(h, 1);
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            registers.setH(value);

            return 4;
        };
        opcodeTable[0x25] = () -> { /* DEC H */
            int h = registers.getH();
            int value =  h - 1 & 0xFF;
            boolean isHalfCarry = (h & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            registers.setH(value);

            return 4;
        };
        opcodeTable[0x26] = () -> { /* LD H, d8 */
            int value = fetch();
            registers.setH(value);

            return 8;
        };

        opcodeTable[0x28] = () -> { /* JR Z,r8 */
            int opcodeNext = fetch();
            byte offset = (byte) opcodeNext;
            if (flags.isZero()) {
                registers.setPc(registers.getPc() + offset);
            }

            return 8;
        };
        opcodeTable[0x29] = () -> { /* ADD HL,HL */
            int hl = registers.getHL();
            int result = hl + hl;
            boolean isHalfCarry = ((hl & 0xFFF) + (hl & 0xFFF)) > 0xFFF;

            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            flags.setCarry(result > 0xFFFF);
            registers.setHL(result & 0xFFFF);

            return 8;
        };
        opcodeTable[0x2A] = () -> { /* LD A, (HL+)*/
            int hl = registers.getHL();
            registers.setA(mmu.readByte(hl));
            registers.setHL(hl + 1);

            return 8;
        };
        opcodeTable[0x2C] = () -> { /* INC L*/
            int l = registers.getL();
            int value =  l + 1;
            boolean isHalfCarry = calculateHalfCarry(l, 1);
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            registers.setL(value);

            return 4;
        };
        opcodeTable[0x2D] = () -> { /* DEC L*/
            int l = registers.getL();
            int value =  l - 1 & 0xFF;
            boolean isHalfCarry = (l & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            registers.setL(value);

            return 4;
        };
        opcodeTable[0x2E] = () -> { /* LD L, d8 */
            int value = fetch();
            registers.setL(value);

            return 8;
        };

        //=========================================
        //============== 0x30 - 0x3F ==============

        opcodeTable[0x30] = () -> { /* JR NC,r8 */
            int opcodeNext = fetch();
            byte offSet = (byte) opcodeNext;
            if (!flags.isCarry()) {
                registers.setPc(registers.getPc() + offSet);
            }

            return 8;
        };
        opcodeTable[0x31] = () -> { /* LD SP,d16 */
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;

            registers.setSp(value);

            return 12;
        };
        opcodeTable[0x32] = () -> { /* LD (HL-), A */
            int a = registers.getA();
            int hl = registers.getHL();
            mmu.writeByte(hl, a);
            registers.setHL(hl - 1);

            return 8;
        };
        opcodeTable[0x35] = () -> { /* DEC (HL) */
            int hlValue = mmu.readByte(registers.getHL());
            int value =  hlValue - 1 & 0xFF;
            boolean isHalfCarry = (hlValue & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            mmu.writeByte(registers.getHL(), value);

            return 12;
        };
        opcodeTable[0x39] = () -> { /* ADD HL,SP */
            int hl = registers.getHL();
            int sp = registers.getSp();
            int result = hl + sp;
            boolean isHalfCarry = ((hl & 0xFFF) + (sp & 0xFFF)) > 0xFFF;

            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            flags.setCarry(result > 0xFFFF);
            registers.setHL(result);

            return 8;
        };
        opcodeTable[0x3B] = () -> { /* DEC SP */
            int sp = registers.getSp();
            int result = sp - 1;
            registers.setSp(result);
            return 8;
        };
        opcodeTable[0x3C] = () -> { /* INC A */
            int a = registers.getA();
            int value =  a + 1;
            boolean isHalfCarry = calculateHalfCarry(a, 1);
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            registers.setA(value);

            return 4;
        };
        opcodeTable[0x3D] = () -> { /* DEC A */
            int a = registers.getA();
            int value =  a - 1 & 0xFF;
            boolean isHalfCarry = (a & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            registers.setA(value);

            return 4;
        };
        opcodeTable[0x3E] = () -> { /* LD A, d8 */
            int value = fetch();
            registers.setA(value);

            return 8;
        };
        //=========================================
        //============== 0x40 - 0x4F ==============

        opcodeTable[0x40] = () -> { /* LD B, B */
            registers.setB(registers.getB());

            return 4;
        };
        opcodeTable[0x41] = () -> { /* LD B, C */
            registers.setB(registers.getC());

            return 4;
        };
        opcodeTable[0x42] = () -> { /* LD B, D */
            registers.setB(registers.getD());

            return 4;
        };
        opcodeTable[0x43] = () -> { /* LD B, E */
            registers.setB(registers.getE());

            return 4;
        };
        opcodeTable[0x44] = () -> { /* LD B, H */
            registers.setB(registers.getH());

            return 4;
        };
        opcodeTable[0x45] = () -> { /* LD B, L */
            registers.setB(registers.getL());

            return 4;
        };
        opcodeTable[0x46] = () -> { /* LD B, (HL) */
            registers.setB(mmu.readByte(registers.getHL()));

            return 8;
        };
        opcodeTable[0x47] = () -> { /* LD B, A */
            registers.setB(registers.getA());

            return 4;
        };

        opcodeTable[0x48] = () -> { /* LD C, B */
            registers.setC(registers.getB());

            return 4;
        };
        opcodeTable[0x49] = () -> { /* LD C, C */
            registers.setC(registers.getC());

            return 4;
        };
        opcodeTable[0x4A] = () -> { /* LD C, D */
            registers.setC(registers.getD());

            return 4;
        };
        opcodeTable[0x4B] = () -> { /* LD C, E */
            registers.setC(registers.getE());

            return 4;
        };
        opcodeTable[0x4C] = () -> { /* LD C, H */
            registers.setC(registers.getH());

            return 4;
        };
        opcodeTable[0x4D] = () -> { /* LD C, L */
            registers.setC(registers.getL());

            return 4;
        };
        opcodeTable[0x4E] = () -> { /* LD C, (HL) */
            registers.setC(mmu.readByte(registers.getHL()));

            return 8;
        };
        opcodeTable[0x4F] = () -> { /* LD C, A */
            registers.setC(registers.getA());

            return 4;
        };

        //=========================================
        //============== 0x50 - 0x5F ==============

        opcodeTable[0x50] = () -> { /* LD D, B */
            registers.setD(registers.getB());

            return 4;
        };
        opcodeTable[0x51] = () -> { /* LD D, C */
            registers.setD(registers.getC());

            return 4;
        };
        opcodeTable[0x52] = () -> { /* LD D, D */
            registers.setD(registers.getD());

            return 4;
        };
        opcodeTable[0x53] = () -> { /* LD D, E */
            registers.setD(registers.getE());

            return 4;
        };
        opcodeTable[0x54] = () -> { /* LD D, H */
            registers.setD(registers.getH());

            return 4;
        };
        opcodeTable[0x55] = () -> { /* LD D, L */
            registers.setD(registers.getL());

            return 4;
        };
        opcodeTable[0x56] = () -> { /* LD D, (HL) */
            registers.setD(mmu.readByte(registers.getHL()));

            return 8;
        };
        opcodeTable[0x57] = () -> { /* LD D, A */
            registers.setD(registers.getA());

            return 4;
        };

        opcodeTable[0x58] = () -> { /* LD E, B */
            registers.setE(registers.getB());

            return 4;
        };
        opcodeTable[0x59] = () -> { /* LD E, C */
            registers.setE(registers.getC());

            return 4;
        };
        opcodeTable[0x5A] = () -> { /* LD E, D */
            registers.setE(registers.getD());

            return 4;
        };
        opcodeTable[0x5B] = () -> { /* LD E, E */
            registers.setE(registers.getE());

            return 4;
        };
        opcodeTable[0x5C] = () -> { /* LD E, H */
            registers.setE(registers.getH());

            return 4;
        };
        opcodeTable[0x5D] = () -> { /* LD E, L */
            registers.setE(registers.getL());

            return 4;
        };
        opcodeTable[0x5E] = () -> { /* LD E, (HL) */
            registers.setE(mmu.readByte(registers.getHL()));

            return 8;
        };
        opcodeTable[0x5F] = () -> { /* LD E, A */
            registers.setE(registers.getA());

            return 4;
        };

        //=========================================
        //============== 0x60 - 0x6F ==============

        opcodeTable[0x60] = () -> { /* LD H, B */
            registers.setH(registers.getB());

            return 4;
        };
        opcodeTable[0x61] = () -> { /* LD H, C */
            registers.setH(registers.getC());

            return 4;
        };
        opcodeTable[0x62] = () -> { /* LD H, D */
            registers.setH(registers.getD());

            return 4;
        };
        opcodeTable[0x63] = () -> { /* LD H, E */
            registers.setH(registers.getE());

            return 4;
        };
        opcodeTable[0x64] = () -> { /* LD H, H */
            registers.setH(registers.getH());

            return 4;
        };
        opcodeTable[0x65] = () -> { /* LD H, L */
            registers.setH(registers.getL());

            return 4;
        };
        opcodeTable[0x66] = () -> { /* LD H, (HL) */
            registers.setH(mmu.readByte(registers.getHL()));

            return 8;
        };
        opcodeTable[0x67] = () -> { /* LD H, A */
            registers.setH(registers.getA());

            return 4;
        };
        opcodeTable[0x68] = () -> { /* LD L, B */
            registers.setL(registers.getB());

            return 4;
        };
        opcodeTable[0x69] = () -> { /* LD L, C */
            registers.setL(registers.getC());

            return 4;
        };
        opcodeTable[0x6A] = () -> { /* LD L, D */
            registers.setL(registers.getD());

            return 4;
        };
        opcodeTable[0x6B] = () -> { /* LD L, E */
            registers.setL(registers.getE());

            return 4;
        };
        opcodeTable[0x6C] = () -> { /* LD L, H */
            registers.setL(registers.getH());

            return 4;
        };
        opcodeTable[0x6D] = () -> { /* LD L, L */
            registers.setL(registers.getL());

            return 4;
        };
        opcodeTable[0x6E] = () -> { /* LD L, (HL) */
            registers.setL(mmu.readByte(registers.getHL()));

            return 8;
        };
        opcodeTable[0x6F] = () -> { /* LD L, A */
            registers.setL(registers.getA());

            return 4;
        };

        //=========================================
        //============== 0x70 - 0x7F ==============

        opcodeTable[0x70] = () -> { /* LD (HL), B */
            mmu.writeByte(registers.getHL(), registers.getB());

            return 8;
        };
        opcodeTable[0x71] = () -> { /* LD (HL), C */
            mmu.writeByte(registers.getHL(), registers.getC());

            return 8;
        };
        opcodeTable[0x72] = () -> { /* LD (HL), D */
            mmu.writeByte(registers.getHL(), registers.getD());

            return 8;
        };
        opcodeTable[0x73] = () -> { /* LD (HL), E */
            mmu.writeByte(registers.getHL(), registers.getE());

            return 8;
        };
        opcodeTable[0x74] = () -> { /* LD (HL), H */
            mmu.writeByte(registers.getHL(), registers.getH());

            return 8;
        };
        opcodeTable[0x75] = () -> { /* LD (HL), L */
            mmu.writeByte(registers.getHL(), registers.getL());

            return 8;
        };
        // opcodeTable[0x76] = () -> { /* HALT */
        // NO IMPLEMENTED
        //};
        opcodeTable[0x77] = () -> { /* LD (HL), A */
            mmu.writeByte(registers.getHL(), registers.getA());

            return 8;
        };

        opcodeTable[0x78] = () -> { /* LD A, B */
            registers.setA(registers.getB());

            return 4;
        };
        opcodeTable[0x79] = () -> { /* LD A, C */
            registers.setA(registers.getC());

            return 4;
        };
        opcodeTable[0x7A] = () -> { /* LD A, D */
            registers.setA(registers.getD());

            return 4;
        };
        opcodeTable[0x7B] = () -> { /* LD A, E */
            registers.setA(registers.getE());

            return 4;
        };
        opcodeTable[0x7C] = () -> { /* LD A, H */
            registers.setA(registers.getH());

            return 4;
        };
        opcodeTable[0x7D] = () -> { /* LD A, L */
            registers.setA(registers.getL());

            return 4;
        };
        opcodeTable[0x7E] = () -> { /* LD A, (HL) */
            registers.setA(mmu.readByte(registers.getHL()));

            return 8;
        };
        opcodeTable[0x7F] = () -> { /* LD A, A */
            registers.setA(registers.getA());

            return 4;
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

            return 4;
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

            return 4;
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

            return 4;
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

            return 4;
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

            return 4;
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

            return 4;
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

            return 8;
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

            return 4;
        };
        //=========================================
        //============== 0x90 0x9F ================
        opcodeTable[0x90] = () -> { /* SUB B */
            int b = registers.getB();
            int a = registers.getA();
            int result = a - b;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (b & 0xF));
            flags.setCarry(a < b);
            registers.setA(result);

            return 4;
        };
        opcodeTable[0x91] = () -> { /* SUB C */
            int c = registers.getC();
            int a = registers.getA();
            int result = a - c;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (c & 0xF));
            flags.setCarry(a < c);
            registers.setA(result);

            return 4;
        };

        //=========================================
        //============== 0xA0 0xAF ==============

        opcodeTable[0xA9] = () -> { /* XOR C */
            int a = registers.getA();
            int c = registers.getC();
            int value = a ^ c;
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xAE] = () -> { /* XOR HL*/
            int a = registers.getA();
            int value = mmu.readByte(registers.getHL());
            int result = a ^ value;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 8;
        };
        opcodeTable[0xAF] = () -> { /* XOR A*/
            int a = registers.getA();
            int result = a ^ a;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };

        //=========================================
        //============== 0xB0 - 0xBF ==============

        opcodeTable[0xB1] = () -> { /* OR C */
            int value = registers.getA() | registers.getC();
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xB6] = () -> { /* OR (HL) */
            int hlContent = mmu.readByte(registers.getHL());
            int value = registers.getA() | hlContent;
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 8;
        };

        opcodeTable[0xB7] = () -> { /* OR A */
            int value = registers.getA();
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };

        opcodeTable[0xB9] = () -> { /* CP C */
            int c = registers.getC();
            int a = registers.getA();
            flags.setZero(c == a);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (c & 0xF));
            flags.setCarry(a < c);

            return 4;
        };

        //=========================================
        //============== 0xC0 - 0xCF ==============

        opcodeTable[0xC1] = () -> { /* POP BC */
            int low = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);
            int high = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);

            int value = (high << 8) | low;
            registers.setBC(value);

            return 12;
        };
        opcodeTable[0xC2] = () -> { /* JP NZ,a16 */
            if(!flags.isZero()) {
                int low = fetch();
                int high = fetch();
                int jumpAddress = (high << 8) | low;

                registers.setPc(jumpAddress);
            }

            return 12;
        };
        opcodeTable[0xC3] = () -> { /* JP a16 */
            int low = fetch();
            int high = fetch();
            int jumpAddress = (high << 8) | low;

            registers.setPc(jumpAddress);

            return 16;
        };
        opcodeTable[0xC4] = () -> { /* CALL NZ,a16 */
            int low = fetch();
            int high = fetch();
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;
            int address = (high << 8) | low;

            if(!flags.isZero()) {
                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);

                registers.setPc(address);
            }

            return 12;
        };
        opcodeTable[0xC5] = () -> { /* PUSH BC */
            int bcLow = registers.getBC() & 0xFF;
            int bcHigh = (registers.getBC() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), bcHigh);
            registers.setSp(registers.getSp() -1);
            mmu.writeByte(registers.getSp(), bcLow);

            return 16;
        };
        opcodeTable[0xC6] = () -> { /* ADD A,d8 */
            int d8 = fetch();
            int a = registers.getA();
            int result = a + d8;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(calculateHalfCarry(a, d8));
            flags.setCarry(result > 0xFF);
            registers.setA(result);

            return 8;
        };
        opcodeTable[0xC8] = () -> { /* RET Z */
            if(flags.isZero()) {
                int low = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);
                int high = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);

                int address = (high << 8) | low;
                registers.setPc(address);
            }

            return 20;
        };
        opcodeTable[0xC9] = () -> { /* RET */
            int low = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);
            int high = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);

            int address = (high << 8) | low;
            registers.setPc(address);

            return 16;
        };
        opcodeTable[0xCB] = () -> { /* PREFIX CB */
            int cbOpcode = fetch();
            Instruction instruction = cbOpcodeTable[cbOpcode];
            if (instruction == null) {
                throw new RuntimeException(String.format("CB Opcode não implementado: 0x%02X", cbOpcode));
            }
            instruction.execute();

            return 4;
        };
        opcodeTable[0xCD] = () -> { /* CALL a16 */
            int low = fetch();
            int high = fetch();
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;
            int address = (high << 8) | low;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcLow);

            registers.setPc(address);

            return 24;
        };
        opcodeTable[0xCE] = () -> { /* ADC A,d8 */
            int d8 = fetch();
            int a = registers.getA();
            int carryIn = flags.isCarry() ? 1 : 0;
            int result = a + d8 + carryIn;
            boolean isHalfCarry = ((a & 0xF) + (d8 & 0xF) + carryIn) > 0xF;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            flags.setCarry(result > 0xFF);
            registers.setA(result);

            return 8;
        };
        //=========================================
        //============== 0xD0 - 0xDF ==============
        opcodeTable[0xD0] = () -> { /* RET NC */
            if(!flags.isCarry()) {
                int low = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);
                int high = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);

                int address = (high << 8) | low;
                registers.setPc(address);
            }

            return 20;
        };
        opcodeTable[0xD1] = () -> { /* POP DE */
            int deLow = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);
            int deHigh = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);
            int value = (deHigh << 8) | deLow;

            registers.setDE(value);

            return 12;
        };
        opcodeTable[0xD5] = () -> { /* PUSH DE */
            int deLow = registers.getDE() & 0xFF;
            int deHigh = (registers.getDE() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), deHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), deLow);

            return 16;
        };
        opcodeTable[0xD6] = () -> { /* SUB d8 */
            int d8 = fetch();
            int a = registers.getA();
            int result = a - d8;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (d8 & 0xF));
            flags.setCarry(a < d8);
            registers.setA(result);

            return 8;
        };

        //=========================================
        //============== 0xE0 - 0xEF ==============
        opcodeTable[0xE0] = () -> { /* LDH (a8),A */
            int offset = fetch();
            int address = 0xFF00 + offset;
            mmu.writeByte(address, registers.getA());

            return 12;
        };
        opcodeTable[0xE1] = () -> { /* POP HL */
            int hlLow = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);
            int hlHigh = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);
            hlLow = hlLow & 0xFF;

            int value = (hlHigh << 8) | hlLow;
            registers.setHL(value);

            return 12;
        };
        opcodeTable[0xE5] = () -> { /* PUSH HL */
            int hlLow = registers.getHL() & 0xFF;
            int hlHigh = (registers.getHL() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), hlHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), hlLow);

            return 16;
        };
        opcodeTable[0xE6] = () -> { /* AND d8 */
            int value = fetch();
            int result = registers.getA() & value;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            flags.setCarry(false);

            return 8;
        };
        opcodeTable[0xE9] = () -> { /* JP (HL) */
            registers.setPc(registers.getHL());

            return 4;
        };

        opcodeTable[0xEA] = () -> { /* LD (a16),A */
            int low = fetch();
            int high = fetch();
            int address = (high << 8) | low;
            mmu.writeByte(address, registers.getA());

            return 16;
        };
        opcodeTable[0xEE] = () -> { /* XOR d8 */
            int a = registers.getA();
            int value = fetch();
            int result = a ^ value;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 8;
        };
        //=========================================
        //============== 0xF0 - 0xFF ==============

        opcodeTable[0xF0] = () -> { /* LDH A,(a8) */
            int offset = fetch();
            int address = 0xFF00 + offset;
            int value = mmu.readByte(address);

            registers.setA(value);

            return 12;
        };
        opcodeTable[0xF1] = () -> { /* POP AF */
            int afLow = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);
            int afHigh = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);
            afLow = afLow & 0xF0;
            int value = (afHigh << 8) | afLow;

            registers.setAF(value);

            return 12;
        };
        opcodeTable[0xF3] = () -> { /* DI */
            //===============
            // NO IMPLEMENTED
            //===============

            return 4;
        };
        opcodeTable[0xF5] = () -> { /* PUSH AF */
            int afLow = registers.getAF() & 0xFF;
            int afHigh = (registers.getAF() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), afHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), afLow);

            return 16;
        };
        opcodeTable[0xF6] = () -> { /* OR d8 */
            int a = registers.getA();
            int value = fetch();
            int result = a | value;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 8;
        };
        opcodeTable[0xFA] = () -> { /* LD A,(a16) */
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;

            registers.setA(mmu.readByte(value));

            return 16;
        };
        opcodeTable[0xFE] = () -> { /* CP d8 */
            int d8 = fetch();
            int a = registers.getA();
            int result = a - d8;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (d8 & 0xF));
            flags.setCarry(a < d8);

            return 8;
        };
        opcodeTable[0xF8] = () -> { /* LD HL,SP+r8 */
            int sp = registers.getSp();
            int offsetUnsigned = fetch();
            int offsetSigned = (byte) offsetUnsigned;

            boolean isHalfCarry = ((sp & 0x0F) + (offsetUnsigned & 0x0F)) > 0x0F;
            boolean isCarry = ((sp & 0xFF) + offsetUnsigned) > 0xFF;

            int result = (sp + offsetSigned) & 0xFFFF;

            registers.setHL(result);

            flags.setZero(false);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            flags.setCarry(isCarry);

            return 12;
        };
        opcodeTable[0xF9] = () -> { /* LD SP,HL */
            int hl = registers.getHL();
            registers.setSp(hl);
            return 8;
        };
    }

    private void initCBOpcodeTable() {

        //=========================================
        //============== 0x10 - 0x1F ==============

        cbOpcodeTable[0x19] = () -> { /* RR C */
            int register = registers.getC();
            boolean oldCarry = flags.isCarry();
            int newCarryBit = register & 0x1;
            int result = register >>> 1;

            if (oldCarry) {
                result = result | (1 << 7);
            }

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(newCarryBit != 0);
            registers.setC(result);

            return 8;
        };
        cbOpcodeTable[0x1A ] = () -> { /* RR D */
            int register = registers.getD();
            boolean oldCarry = flags.isCarry();
            int newCarryBit = register & 0x1;
            int result = register >>> 1;

            if (oldCarry) {
                result = result | (1 << 7);
            }

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(newCarryBit != 0);
            registers.setD(result);

            return 8;
        };


        //=========================================
        //============== 0x30 - 0x3F ==============
        cbOpcodeTable[0x37] = () -> { /* SWAP A */
            int valor = registers.getA();
            int highNibble = (valor >> 4) & 0xF;
            int lowNibble = valor & 0xF;
            int result = (lowNibble << 4) | highNibble;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(false);
            registers.setA(result);

            return 8;
        };
        cbOpcodeTable[0x38] = () -> { /* SRL B */
            int register = registers.getB();
            int bitCarry = register & 0x1;
            int result = register >>> 1;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bitCarry != 0);
            registers.setB(result);

            return 8;
        };
        cbOpcodeTable[0x3F] = () -> { /* SRL A */
            int registerA = registers.getA();
            int bitCarry = registerA & 0x1;
            int result = registerA >>> 1;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bitCarry != 0);
            registers.setA(result);

            return 8;
        };
    }
}
