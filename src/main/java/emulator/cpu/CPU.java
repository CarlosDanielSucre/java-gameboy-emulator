package cpu;

import memory.MMU;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CPU {
    private Registers registers;
    private Flags flags;
    private MMU mmu;
    private Instruction[] opcodeTable = new Instruction[256];
    private Instruction[] cbOpcodeTable = new Instruction[256];
    private boolean interruptsEnabled = false;
    private boolean halted = false;
    private long totalCycles = 0;

    public CPU(MMU mmu) {
        this.flags = new Flags();
        this.registers = new Registers(flags);
        registers.setAF(0x01B0);
        registers.setBC(0x0013);
        registers.setDE(0x00D8);
        registers.setHL(0x014D);
        registers.setSp(0xFFFE);
        registers.setPc(0x0100);
        this.mmu = mmu;
        initOpcodeTable();
        initCBOpcodeTable();
    }

    public boolean isHalted() {
        return halted;
    }

    public boolean isInterruptsEnabled() {
        return interruptsEnabled;
    }

    public boolean calculateHalfCarry(int a, int b) {
        return ((a & 0xF) + (b & 0xF) > 0xF);
    }

    public void step() {
        int ie = mmu.readByte(0xFFFF);
        int iFlag = mmu.readByte(0xFF0F);
        int pending = ie & iFlag;

        if (pending != 0) {
            halted = false;
        }
        if (pending != 0 && interruptsEnabled) {
            if ((pending & 0x1) != 0) { // bit 0 - VBlank
                int newFlag = iFlag & ~0x1;
                mmu.writeByte(0xFF0F, newFlag);
                interruptsEnabled = false;

                int pcLow = registers.getPc() & 0xFF;
                int pcHigh = (registers.getPc() >> 8) & 0xFF;

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);

                registers.setPc(0x0040);

            } else if ((pending & 0x2) != 0) { // bit 1 - LCD STAT
                int newFlag = iFlag & ~0x2;
                mmu.writeByte(0xFF0F, newFlag);
                interruptsEnabled = false;

                int pcLow = registers.getPc() & 0xFF;
                int pcHigh = (registers.getPc() >> 8) & 0xFF;

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);

                registers.setPc(0x0048);

            } else if ((pending & 0x4) != 0) { // bit 2 - Timer
                int newFlag = iFlag & ~0x4;
                mmu.writeByte(0xFF0F, newFlag);
                interruptsEnabled = false;

                int pcLow = registers.getPc() & 0xFF;
                int pcHigh = (registers.getPc() >> 8) & 0xFF;

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);

                registers.setPc(0x0050);

            } else if ((pending & 0x8) != 0) { // bit 3 - Serial
                int newFlag = iFlag & ~0x8;
                mmu.writeByte(0xFF0F, newFlag);
                interruptsEnabled = false;

                int pcLow = registers.getPc() & 0xFF;
                int pcHigh = (registers.getPc() >> 8) & 0xFF;

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);

                registers.setPc(0x0058);

            } else if ((pending & 0x10) != 0) { // bit 4 - Joypad
                int newFlag = iFlag & ~0x10;
                mmu.writeByte(0xFF0F, newFlag);
                interruptsEnabled = false;

                int pcLow = registers.getPc() & 0xFF;
                int pcHigh = (registers.getPc() >> 8) & 0xFF;

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);

                registers.setPc(0x0060);

            }
        }
        if (!halted) {
            int pcBeforeFetch = registers.getPc();
            int opcode = fetch();
            if (opcode == 240 && totalCycles == 250824) {
                int nextByte = mmu.readByte(pcBeforeFetch + 1);
                System.out.println("LDH offset: 0x" + Integer.toHexString(nextByte));
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("arquivo.txt", true))) {
                if(totalCycles >= 199672) {
                    writer.write("PC: " + registers.getPc());
                    writer.newLine();
                    writer.write("A: " + registers.getA());
                    writer.newLine();
                    writer.write("F: " + flags.toByte());
                    writer.newLine();
                    writer.write("B: " + registers.getB());
                    writer.newLine();
                    writer.write("C: " + registers.getC());
                    writer.newLine();
                    writer.write("D: " + registers.getD());
                    writer.newLine();
                    writer.write("E: " + registers.getE());
                    writer.newLine();
                    writer.write("H: " + registers.getH());
                    writer.newLine();
                    writer.write("L: " + registers.getL());
                    writer.newLine();
                    writer.write("SP: " + registers.getSp());
                    writer.newLine();
                    writer.write("Cycles: " + totalCycles);
                    writer.newLine();
                    writer.write("Opcode: " + opcode);
                    writer.newLine();
                }

            } catch (IOException e) {
                System.out.println("Happens an error: " + e.getMessage());
            }
            Instruction instruction = opcodeTable[opcode];
            if(instruction == null) {
                throw new RuntimeException(String.format("Opcode no implemented: 0x%02X", opcode));
            }
            int cycles = instruction.execute();
            totalCycles += cycles;
            mmu.step(cycles);
        } else {
            mmu.step(4);
        }

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
        opcodeTable[0x02] = () -> { /* LD (BC), A */
            int a = registers.getA();
            mmu.writeByte(registers.getBC(), a);

            return 8;
        };
        opcodeTable[0x03] = () -> { /* INC BC */
            registers.setBC((registers.getBC() + 1) & 0xFFFF);
            return 8;
        };
        opcodeTable[0x04] = () -> { /* INC B */
            int b = registers.getB();
            int value = (b + 1) & 0xFF;
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
            int value = (b - 1) & 0xFF;
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
        opcodeTable[0x08] = () -> { /* LD (a16),SP */
            int low = fetch();
            int high = fetch();
            int address = (high << 8) | low;
            mmu.writeByte(address, registers.getSp() & 0xFF);
            mmu.writeByte(address + 1, (registers.getSp() >> 8) & 0xFF);

            return 20;
        };
        opcodeTable[0x09] = () -> { /* ADD HL, BC */
            int bc = registers.getBC();
            int hl = registers.getHL();
            int result = hl + bc;
            boolean isHalfCarry = ((hl & 0xFFF) + (bc & 0xFFF)) > 0xFFF;

            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            flags.setCarry(result > 0xFFFF);
            registers.setHL(result & 0xFFFF);

            return 8;
        };
        opcodeTable[0x0A] = () -> { /* LD A,(BC) */
            int bcValue = mmu.readByte(registers.getBC());
            registers.setA(bcValue);

            return 8;
        };
        opcodeTable[0x0B] = () -> { /* DEC BC */
            int bc = registers.getBC();
            int result = bc - 1;
            registers.setBC(result);

            return 8;
        };
        opcodeTable[0x0C] = () -> { /* INC C */
            int c = registers.getC();
            int value = (c + 1) & 0xFF;
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
            int value = (c - 1) & 0xFF;
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
        opcodeTable[0x0F] = () -> { /* RRCA */
            int value = registers.getA();
            boolean carry = (value & 0x1) == 1;
            int result = value >>> 1;

            if(carry) {
                result = result | 1 << 7 ;
            }

            flags.setZero(false);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry);
            registers.setA(result);
            return 4;
        };

        //=========================================
        //============== 0x10 - 0x1F ==============
        opcodeTable[0x10] = () -> { /* STOP 0 */
            fetch();
            return 4;
        };
        opcodeTable[0x11] = () -> { /* LD DE, d16*/
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;

            registers.setDE(value);
            return 12;
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
            int value = (d + 1) & 0xFF;
            boolean isHalfCarry = calculateHalfCarry(d, 1);
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            registers.setD(value);
            return 4;
        };
        opcodeTable[0x15] = () -> { /* DEC D */
            int d = registers.getD();
            int value = (d - 1) & 0xFF;
            boolean isHalfCarry = (d & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            registers.setD(value);
            return 4;
        };
        opcodeTable[0x16] = () -> { /* LD D, d8 */
            int value = fetch();
            registers.setD(value);
            return 8;
        };
        opcodeTable[0x17] = () -> { /* RLA */
            int value = registers.getA();
            boolean oldCarry = flags.isCarry();
            int bit7 = (value >> 7) & 0x1;
            int result = (value << 1) & 0xFF;

            if(oldCarry) {
                result = result | 0x1;
            }

            flags.setZero(false);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setA(result);

            return 4;
        };
        opcodeTable[0x18] = () -> { /* JR r8 */
            int opcodeNext = fetch();
            byte offSet = (byte) opcodeNext;
            registers.setPc(registers.getPc() + offSet);
            return 12;
        };
        opcodeTable[0x19] = () -> { /* ADD HL,DE */
            int de = registers.getDE();
            int hl = registers.getHL();
            int result = hl + de;
            boolean isHalfCarry = ((hl & 0xFFF) + (de & 0xFFF)) > 0xFFF;

            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            flags.setCarry(result > 0xFFFF);
            registers.setHL(result & 0xFFFF);

            return 8;
        };
        opcodeTable[0x1A] = () -> { /* LD A, (DE) */
            int de = registers.getDE();
            registers.setA(mmu.readByte(de));


            return 8;
        };
        opcodeTable[0x1B] = () -> { /* DEC DE */
            int de = registers.getDE();
            registers.setDE(de -1);
            return 8;
        };
        opcodeTable[0x1C] = () -> { /* INC E */
            int e = registers.getE();
            int value = (e + 1) & 0xFF;
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
            int value = (e - 1) & 0xFF;
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
                return 12;
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
            int value = (h + 1) & 0xFF;
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
            int value = (h - 1) & 0xFF;
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
        opcodeTable[0x27] = () -> { /* DAA */
            int a = registers.getA();
            int correction = 0;
            boolean setCarry = false;

            if (!flags.isSubtract()) {
                if (flags.isHalfCarry() || (a & 0xF) > 9) {
                    correction |= 0x06;
                }
                if (flags.isCarry() || a > 0x99) {
                    correction |= 0x60;
                    setCarry = true;
                }
                a = (a + correction) & 0xFF;
            } else {
                if (flags.isHalfCarry()) {
                    correction |= 0x06;
                }
                if (flags.isCarry()) {
                    correction |= 0x60;
                }
                a = (a - correction) & 0xFF;
                setCarry = flags.isCarry();
            }

            flags.setZero(a == 0);
            flags.setHalfCarry(false);
            flags.setCarry(setCarry);
            registers.setA(a);

            return 4;
        };
        opcodeTable[0x28] = () -> { /* JR Z,r8 */
            int opcodeNext = fetch();
            byte offset = (byte) opcodeNext;
            if (flags.isZero()) {
                registers.setPc(registers.getPc() + offset);
                return 12;
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
        opcodeTable[0x2B] = () -> { /* DEC HL */
            int hl = registers.getHL();
            int result = hl - 1;
            registers.setHL(result);

            return 8;
        };
        opcodeTable[0x2C] = () -> { /* INC L*/
            int l = registers.getL();
            int value = (l + 1) & 0xFF;
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
            int value = (l - 1) & 0xFF;
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
        opcodeTable[0x2F] = () -> { /* CPL */
            int a = registers.getA();
            int result = (~a) & 0xFF;

            flags.setSubtract(true);
            flags.setHalfCarry(true);
            registers.setA(result);

            return 4;
        };

        //=========================================
        //============== 0x30 - 0x3F ==============

        opcodeTable[0x30] = () -> { /* JR NC,r8 */
            int opcodeNext = fetch();
            byte offSet = (byte) opcodeNext;
            if (!flags.isCarry()) {
                registers.setPc(registers.getPc() + offSet);
                return 12;
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
        opcodeTable[0x33] = () -> { /* INC SP */
            int sp = registers.getSp();
            int result = sp + 1;
            registers.setSp(result);

            return 8;
        };
        opcodeTable[0x34] = () -> { /* INC (HL) */
            int hl = registers.getHL();
            int hlValue = mmu.readByte(hl);
            int result = (hlValue + 1) & 0xFF;
            boolean isHalfCarry = calculateHalfCarry(hlValue, 1);
            boolean isZero = result == 0;

            flags.setZero(isZero);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            mmu.writeByte(hl, result);

            return 12;
        };
        opcodeTable[0x35] = () -> { /* DEC (HL) */
            int hlValue = mmu.readByte(registers.getHL());
            int value = ((hlValue - 1) & 0xFF);
            boolean isHalfCarry = (hlValue & 0xF) == 0;
            boolean isZero = (value & 0xFF) == 0;

            flags.setZero(isZero);
            flags.setSubtract(true);
            flags.setHalfCarry(isHalfCarry);
            mmu.writeByte(registers.getHL(), value);

            return 12;
        };
        opcodeTable[0x36] = () -> { /* LD (HL),d8 */
            int d8 = fetch();
            mmu.writeByte(registers.getHL(), d8);

            return 12;
        };
        opcodeTable[0x38] = () -> { /* JR C,r8 */
            int opcodeNext = fetch();
            byte offSet = (byte) opcodeNext;
            if (flags.isCarry()) {
                registers.setPc(registers.getPc() + offSet);
                return 12;
            }

            return 8;
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
        opcodeTable[0x3A] = () -> { /* A, (HL-)*/
            int hl = registers.getHL();
            registers.setA(mmu.readByte(hl));
            registers.setHL(hl - 1);

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
            int value = (a + 1) & 0xFF;
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
            int value = (a - 1) & 0xFF;
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
        opcodeTable[0x76] = () -> { /* HALT */
            halted = true;
            return 4;
        };
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
        opcodeTable[0x89] = () -> { /* ADC A,C */
            int c = registers.getC();
            int a = registers.getA();
            int carryIn = flags.isCarry() ? 1 : 0;
            int result = a + c + carryIn;
            boolean isHalfCarry = ((a & 0xF) + (c & 0xF) + carryIn) > 0xF;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(isHalfCarry);
            flags.setCarry(result > 0xFF);
            registers.setA(result);

            return 4;
        };

        // ... continuation truncated here in tool due huge size; not ideal.

        // This file was partially rewritten; it is not valid Java due truncation.

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
        cbOpcodeTable[0x1A] = () -> { /* RR D */
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
        opcodeTable[0x92] = () -> { /* SUB D */
            int d = registers.getD();
            int a = registers.getA();
            int result = a - d;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (d & 0xF));
            flags.setCarry(a < d);
            registers.setA(result);

            return 4;
        };
        opcodeTable[0x93] = () -> { /* SUB E */
            int e = registers.getE();
            int a = registers.getA();
            int result = a - e;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (e & 0xF));
            flags.setCarry(a < e);
            registers.setA(result);

            return 4;
        };
        opcodeTable[0x94] = () -> { /* SUB H */
            int h = registers.getH();
            int a = registers.getA();
            int result = a - h;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (h & 0xF));
            flags.setCarry(a < h);
            registers.setA(result);

            return 4;
        };

        opcodeTable[0x95] = () -> { /* SUB L */
            int l = registers.getL();
            int a = registers.getA();
            int result = a - l;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (l & 0xF));
            flags.setCarry(a < l);
            registers.setA(result);

            return 4;
        };
        opcodeTable[0x96] = () -> { /* SUB (HL) */
            int hl = registers.getHL();
            int hlValue = mmu.readByte(hl);
            int a = registers.getA();
            int result = a - hlValue;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (hlValue & 0xF));
            flags.setCarry(a < hlValue);
            registers.setA(result);

            return 8;
        };
        opcodeTable[0x97] = () -> { /* SUB A */
            int a = registers.getA();
            int result = a - a;

            flags.setZero((result & 0xFF) == 0);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (a & 0xF));
            flags.setCarry(a < a);
            registers.setA(result);

            return 4;
        };

        //=========================================
        //============== 0xA0 0xAF ==============
        opcodeTable[0xA0] = () -> { /* AND B */
            int b = registers.getB();
            int result = registers.getA() & b;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            flags.setCarry(false);

            return 4;
        };
        opcodeTable[0xA1] = () -> { /* AND C */
            int c = registers.getC();
            int result = registers.getA() & c;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            flags.setCarry(false);

            return 4;
        };
        opcodeTable[0xA2] = () -> { /* AND D */
            int d = registers.getD();
            int result = registers.getA() & d;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            flags.setCarry(false);

            return 4;
        };
        opcodeTable[0xA3] = () -> { /* AND E */
            int e = registers.getE();
            int result = registers.getA() & e;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            flags.setCarry(false);

            return 4;
        };
        opcodeTable[0xA4] = () -> { /* AND H */
            int h = registers.getH();
            int result = registers.getA() & h;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            flags.setCarry(false);

            return 4;
        };
        opcodeTable[0xA5] = () -> { /* AND L */
            int l = registers.getL();
            int result = registers.getA() & l;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            flags.setCarry(false);

            return 4;
        };
        opcodeTable[0xA6] = () -> { /* AND (HL) */
            int hl = registers.getHL();
            int hlValue = mmu.readByte(hl);
            int result = registers.getA() & hlValue;
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            flags.setCarry(false);

            return 8;
        };
        opcodeTable[0xA7] = () -> { /* AND A */
            int result = registers.getA() & registers.getA();
            registers.setA(result);

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            flags.setCarry(false);

            return 4;
        };
        opcodeTable[0xA8] = () -> { /* XOR B */
            int a = registers.getA();
            int b = registers.getB();
            int value = a ^ b;
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
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
        opcodeTable[0xAA] = () -> { /* XOR D */
            int a = registers.getA();
            int d = registers.getD();
            int value = a ^ d;
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xAB] = () -> { /* XOR E */
            int a = registers.getA();
            int e = registers.getE();
            int value = a ^ e;
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xAC] = () -> { /* XOR H */
            int a = registers.getA();
            int h = registers.getH();
            int value = a ^ h;
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xAD] = () -> { /* XOR L */
            int a = registers.getA();
            int l = registers.getL();
            int value = a ^ l;
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

        opcodeTable[0xB0] = () -> { /* OR B */
            int value = registers.getA() | registers.getB();
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xB1] = () -> { /* OR C */
            int value = registers.getA() | registers.getC();
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xB2] = () -> { /* OR D */
            int value = registers.getA() | registers.getD();
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xB3] = () -> { /* OR E */
            int value = registers.getA() | registers.getE();
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xB4] = () -> { /* OR H */
            int value = registers.getA() | registers.getH();
            registers.setA(value);

            flags.setZero(value == 0);
            flags.setCarry(false);
            flags.setHalfCarry(false);
            flags.setSubtract(false);

            return 4;
        };
        opcodeTable[0xB5] = () -> { /* OR L */
            int value = registers.getA() | registers.getL();
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
        opcodeTable[0xB8] = () -> { /* CP B */
            int b = registers.getB();
            int a = registers.getA();
            flags.setZero(b == a);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (b & 0xF));
            flags.setCarry(a < b);

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
        opcodeTable[0xBA] = () -> { /* CP D*/
            int d = registers.getD();
            int a = registers.getA();
            flags.setZero(d == a);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (d & 0xF));
            flags.setCarry(a < d);

            return 4;
        };
        opcodeTable[0xBB] = () -> { /* CP E*/
            int e = registers.getE();
            int a = registers.getA();
            flags.setZero(e == a);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (e & 0xF));
            flags.setCarry(a < e);

            return 4;
        };
        opcodeTable[0xBC] = () -> { /* CP H*/
            int h = registers.getH();
            int a = registers.getA();
            flags.setZero(h == a);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (h & 0xF));
            flags.setCarry(a < h);

            return 4;
        };
        opcodeTable[0xBD] = () -> { /* CP L*/
            int l = registers.getL();
            int a = registers.getA();
            flags.setZero(l == a);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (l & 0xF));
            flags.setCarry(a < l);

            return 4;
        };
        opcodeTable[0xBE] = () -> { /* CP (HL)*/
            int value = mmu.readByte(registers.getHL());
            int a = registers.getA();
            flags.setZero(value == a);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (value & 0xF));
            flags.setCarry(a < value);

            return 8;
        };
        opcodeTable[0xBF] = () -> { /* CP A*/
            int a = registers.getA();
            flags.setZero(a == a);
            flags.setSubtract(true);
            flags.setHalfCarry((a & 0xF) < (a & 0xF));
            flags.setCarry(a < a);

            return 4;
        };

        //=========================================
        //============== 0xC0 - 0xCF ==============

        opcodeTable[0xC0] = () -> { /* RET NZ */
            if (!flags.isZero()) {
                int low = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);
                int high = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);

                int address = (high << 8) | low;
                registers.setPc(address);
                return 20;

            }


            return 8;
        };
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
            int low = fetch();
            int high = fetch();
            int jumpAddress = (high << 8) | low;

            if (!flags.isZero()) {
                registers.setPc(jumpAddress);
                return 16;
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

            if (!flags.isZero()) {
                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);
                registers.setPc(address);
                return 24;
            }
            return 12;
        };
        opcodeTable[0xC5] = () -> { /* PUSH BC */
            int bcLow = registers.getBC() & 0xFF;
            int bcHigh = (registers.getBC() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), bcHigh);
            registers.setSp(registers.getSp() - 1);
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
        opcodeTable[0xC7] = () -> { /* RST 00H */
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcLow);

            registers.setPc(0x0000);
            return 16;
        };
        opcodeTable[0xC8] = () -> { /* RET Z */
            if (flags.isZero()) {
                int low = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);
                int high = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);

                int address = (high << 8) | low;
                registers.setPc(address);
                return 20;

            }


            return 8;
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
        opcodeTable[0xCA] = () -> { /* JP Z,    a16 */
            int low = fetch();
            int high = fetch();
            int jumpAddress = (high << 8) | low;
            if (flags.isZero()) {
                registers.setPc(jumpAddress);

                return 16;
            }
            return 12;
        };
        opcodeTable[0xCB] = () -> { /* PREFIX CB */
            int cbOpcode = fetch();
            Instruction instruction = cbOpcodeTable[cbOpcode];
            if (instruction == null) {
                throw new RuntimeException(String.format("CB Opcode não implementado: 0x%02X", cbOpcode));
            }
            return instruction.execute();
        };
        opcodeTable[0xCC] = () -> { /* CALL Z,a16 */
            int low = fetch();
            int high = fetch();
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;
            int address = (high << 8) | low;

            if (flags.isZero()) {
                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);
                registers.setPc(address);
                return 24;
            }
            return 12;
        };
        opcodeTable[0xCD] = () -> { /* CALL a16 */
            int low = fetch();
            int high = fetch();
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;
            int address = (high << 8) | low;
            int returnAddress = registers.getPc();

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
        opcodeTable[0xCF] = () -> { /* RST 08H */
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcLow);

            registers.setPc(0x0008);
            return 16;
        };
        //=========================================
        //============== 0xD0 - 0xDF ==============
        opcodeTable[0xD0] = () -> { /* RET NC */
            if (!flags.isCarry()) {
                int low = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);
                int high = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);

                int address = (high << 8) | low;
                registers.setPc(address);
                return 20;
            }

            return 8;
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
        opcodeTable[0xD2] = () -> { /* JP NC, a16 */
            int low = fetch();
            int high = fetch();
            int jumpAddress = (high << 8) | low;
            if (!flags.isCarry()) {
                registers.setPc(jumpAddress);

                return 16;
            }
            return 12;
        };
        opcodeTable[0xD4] = () -> { /* CALL NC, a16 */
            int low = fetch();
            int high = fetch();
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;
            int address = (high << 8) | low;

            if (!flags.isCarry()) {
                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);
                registers.setPc(address);

                return 24;
            }
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
        opcodeTable[0xD7] = () -> { /* RST 10H */
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcLow);

            registers.setPc(0x0010);
            return 16;
        };
        opcodeTable[0xD8] = () -> { /* RET C */
            if (flags.isCarry()) {
                int low = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);
                int high = mmu.readByte(registers.getSp());
                registers.setSp(registers.getSp() + 1);

                int address = (high << 8) | low;
                registers.setPc(address);
                return 20;
            }

            return 8;
        };
        opcodeTable[0xD9] = () -> { /* RETI */
            int low = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);
            int high = mmu.readByte(registers.getSp());
            registers.setSp(registers.getSp() + 1);

            int address = (high << 8) | low;
            registers.setPc(address);
            interruptsEnabled = true;

            return 16;
        };
        opcodeTable[0xDA] = () -> { /* JP C, a16 */
            int low = fetch();
            int high = fetch();
            int jumpAddress = (high << 8) | low;
            if (flags.isCarry()) {
                registers.setPc(jumpAddress);

                return 16;
            }
            return 12;
        };
        opcodeTable[0xDC] = () -> { /* CALL C, a16 */
            int low = fetch();
            int high = fetch();
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;
            int address = (high << 8) | low;

            if (flags.isCarry()) {
                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcHigh);

                registers.setSp(registers.getSp() - 1);
                mmu.writeByte(registers.getSp(), pcLow);
                registers.setPc(address);

                return 24;
            }
            return 12;
        };
        opcodeTable[0xDF] = () -> { /* RST 18H */
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcLow);

            registers.setPc(0x0018);
            return 16;
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
        opcodeTable[0xE2] = () -> { /* LD (C), A */
            int address = 0xFF00 + registers.getC();
            mmu.writeByte(address, registers.getA());

            return 8;
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
        opcodeTable[0xE7] = () -> { /* RST 20H */
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcLow);

            registers.setPc(0x0020);
            return 16;
        };
        opcodeTable[0xE8] = () -> { /* ADD SP, r8 */
            int opcodeNext = fetch();
            byte offSetSigned = (byte) opcodeNext;
            int sp = registers.getSp();
            int result = (sp + offSetSigned) & 0xFFFF;

            int spLow = sp & 0xFF;
            int offSetUnsigned = opcodeNext & 0xFF;

            flags.setZero(false);
            flags.setSubtract(false);
            flags.setHalfCarry(((spLow & 0xF) + (offSetUnsigned & 0xF)) > 0xF);
            flags.setCarry((spLow + offSetUnsigned) > 0xFF);

            registers.setSp(result);
            return 16;
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
        opcodeTable[0xEF] = () -> { /* RST 28H */
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcLow);

            registers.setPc(0x0028);
            return 16;
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
        opcodeTable[0xF2] = () -> { /* LD A, (C) */
            int value = mmu.readByte(0xFF00 + registers.getC());
            registers.setA(value);

            return 8;
        };
        opcodeTable[0xF3] = () -> { /* DI */
            interruptsEnabled = false;

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
        opcodeTable[0xF7] = () -> { /* RST 30H */
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcLow);

            registers.setPc(0x0030);
            return 16;
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
        opcodeTable[0xFA] = () -> { /* LD A,(a16) */
            int low = fetch();
            int high = fetch();
            int value = (high << 8) | low;

            registers.setA(mmu.readByte(value));

            return 16;
        };
        opcodeTable[0xFB] = () -> { /* EI */
            interruptsEnabled = true;
            return 4;

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
        opcodeTable[0xFF] = () -> { /* RST 38H */
            int pcLow = registers.getPc() & 0xFF;
            int pcHigh = (registers.getPc() >> 8) & 0xFF;

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcHigh);

            registers.setSp(registers.getSp() - 1);
            mmu.writeByte(registers.getSp(), pcLow);

            registers.setPc(0x0038);
            return 16;
        };
        //=========================================
        //============== 0x00 - 0x0F ==============
        cbOpcodeTable[0x00] = () -> { /* RLC B */
            int value = registers.getB();
            int bit7 = (value >> 7) & 0x1;
            int result = ((value << 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setB(result);

            return 8;
        };
        cbOpcodeTable[0x01] = () -> { /* RLC C */
            int value = registers.getC();
            int bit7 = (value >> 7) & 0x1;
            int result = ((value << 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setC(result);

            return 8;
        };
        cbOpcodeTable[0x02] = () -> { /* RLC D */
            int value = registers.getD();
            int bit7 = (value >> 7) & 0x1;
            int result = ((value << 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setD(result);

            return 8;
        };
        cbOpcodeTable[0x03] = () -> { /* RLC E */
            int value = registers.getE();
            int bit7 = (value >> 7) & 0x1;
            int result = ((value << 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setE(result);

            return 8;
        };
        cbOpcodeTable[0x04] = () -> { /* RLC H */
            int value = registers.getH();
            int bit7 = (value >> 7) & 0x1;
            int result = ((value << 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setH(result);

            return 8;
        };
        cbOpcodeTable[0x05] = () -> { /* RLC L */
            int value = registers.getL();
            int bit7 = (value >> 7) & 0x1;
            int result = ((value << 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setL(result);

            return 8;
        };
        cbOpcodeTable[0x06] = () -> { /* RLC (HL) */
            int value = mmu.readByte(registers.getHL());
            int bit7 = (value >> 7) & 0x1;
            int result = ((value << 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            mmu.writeByte(registers.getHL(), result);

            return 16;
        };
        cbOpcodeTable[0x07] = () -> { /* RLC A */
            int value = registers.getA();
            int bit7 = (value >> 7) & 0x1;
            int result = ((value << 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setA(result);

            return 8;
        };
        cbOpcodeTable[0x08] = () -> { /* RRC B */
            int value = registers.getB();
            int bit1 = value & 0x1;
            int result = ((bit1 << 7) | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit1 == 1);
            registers.setB(result);

            return 8;
        };
        cbOpcodeTable[0x09] = () -> { /* RRC C */
            int value = registers.getC();
            int bit1 = value & 0x1;
            int result = ((bit1 << 7) | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit1 == 1);
            registers.setC(result);

            return 8;
        };
        cbOpcodeTable[0x0A] = () -> { /* RRC D */
            int value = registers.getD();
            int bit1 = value & 0x1;
            int result = ((bit1 << 7) | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit1 == 1);
            registers.setD(result);

            return 8;
        };
        cbOpcodeTable[0x0B] = () -> { /* RRC E */
            int value = registers.getE();
            int bit1 = value & 0x1;
            int result = ((bit1 << 7) | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit1 == 1);
            registers.setE(result);

            return 8;
        };
        cbOpcodeTable[0x0C] = () -> { /* RRC H */
            int value = registers.getH();
            int bit1 = value & 0x1;
            int result = ((bit1 << 7) | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit1 == 1);
            registers.setH(result);

            return 8;
        };
        cbOpcodeTable[0x0D] = () -> { /* RRC L */
            int value = registers.getL();
            int bit1 = value & 0x1;
            int result = ((bit1 << 7) | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit1 == 1);
            registers.setL(result);

            return 8;
        };
        cbOpcodeTable[0x0E] = () -> { /* RRC (HL) */
            int value = mmu.readByte(registers.getHL());
            int bit1 = value & 0x1;
            int result = ((bit1 << 7) | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit1 == 1);
            mmu.writeByte(registers.getHL(), result);

            return 16;
        };
        cbOpcodeTable[0x0F] = () -> { /* RRC A */
            int value = registers.getA();
            int bit1 = value & 0x1;
            int result = ((bit1 << 7) | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit1 == 1);
            registers.setA(result);

            return 8;
        };
        //=========================================
        //============== 0x10 - 0x1F ==============

        cbOpcodeTable[0x10] = () -> { /* RL B */
            int value = registers.getB();
            int bit7 = (value >> 7) & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = ((value << 1) | carry) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setB(result);

            return 8;
        };
        cbOpcodeTable[0x11] = () -> { /* RL C */
            int value = registers.getC();
            int bit7 = (value >> 7) & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = ((value << 1) | carry) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setC(result);

            return 8;
        };
        cbOpcodeTable[0x12] = () -> { /* RL D */
            int value = registers.getD();
            int bit7 = (value >> 7) & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = ((value << 1) | carry) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setD(result);

            return 8;
        };
        cbOpcodeTable[0x13] = () -> { /* RL E */
            int value = registers.getE();
            int bit7 = (value >> 7) & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = ((value << 1) | carry) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setE(result);

            return 8;
        };
        cbOpcodeTable[0x14] = () -> { /* RL H */
            int value = registers.getH();
            int bit7 = (value >> 7) & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = ((value << 1) | carry) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setH(result);

            return 8;
        };
        cbOpcodeTable[0x15] = () -> { /* RL L */
            int value = registers.getL();
            int bit7 = (value >> 7) & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = ((value << 1) | carry) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setL(result);

            return 8;
        };
        cbOpcodeTable[0x16] = () -> { /* RL (HL) */
            int value = mmu.readByte(registers.getHL());
            int bit7 = (value >> 7) & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = ((value << 1) | carry) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            mmu.writeByte(registers.getHL(), result);

            return 16;
        };
        cbOpcodeTable[0x17] = () -> { /* RL A */
            int value = registers.getA();
            int bit7 = (value >> 7) & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = ((value << 1) | carry) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit7 == 1);
            registers.setA(result);

            return 8;
        };
        cbOpcodeTable[0x18] = () -> { /* RR B */
            int value = registers.getB();
            int bit0 = value & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = (carry << 7 | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setB(result);

            return 8;
        };
        cbOpcodeTable[0x19] = () -> { /* RR C */
            int value = registers.getC();
            int bit0 = value & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = (carry << 7 | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setC(result);

            return 8;
        };
        cbOpcodeTable[0x1A] = () -> { /* RR D */
            int value = registers.getD();
            int bit0 = value & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = (carry << 7 | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setD(result);

            return 8;
        };
        cbOpcodeTable[0x1B] = () -> { /* RR E */
            int value = registers.getE();
            int bit0 = value & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = (carry << 7 | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setE(result);

            return 8;
        };
        cbOpcodeTable[0x1C] = () -> { /* RR H */
            int value = registers.getH();
            int bit0 = value & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = (carry << 7 | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setH(result);

            return 8;
        };
        cbOpcodeTable[0x1D] = () -> { /* RR L */
            int value = registers.getL();
            int bit0 = value & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = (carry << 7 | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setL(result);

            return 8;
        };
        cbOpcodeTable[0x1E] = () -> { /* RR (HL) */
            int value = mmu.readByte(registers.getHL());
            int bit0 = value & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = (carry << 7 | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            mmu.writeByte(registers.getHL(), result);

            return 16;
        };
        cbOpcodeTable[0x1F] = () -> { /* RR A */
            int value = registers.getA();
            int bit0 = value & 0x1;
            int carry = flags.isCarry() ? 1 : 0;
            int result = (carry << 7 | (value >> 1)) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setA(result);

            return 8;
        };
        //=========================================
        //============== 0x20 - 0x2F ==============

        cbOpcodeTable[0x20] = () -> { /* SLA B */
            int value = registers.getB();
            int carry = (value >> 7) & 0x1;
            int result = (value << 1) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry == 1);
            registers.setB(result);

            return 8;
        };
        cbOpcodeTable[0x21] = () -> { /* SLA C*/
            int value = registers.getC();
            int carry = (value >> 7) & 0x1;
            int result = (value << 1) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry == 1);
            registers.setC(result);

            return 8;
        };
        cbOpcodeTable[0x22] = () -> { /* SLA D */
            int value = registers.getD();
            int carry = (value >> 7) & 0x1;
            int result = (value << 1) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry == 1);
            registers.setD(result);

            return 8;
        };
        cbOpcodeTable[0x23] = () -> { /* SLA E */
            int value = registers.getE();
            int carry = (value >> 7) & 0x1;
            int result = (value << 1) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry == 1);
            registers.setE(result);

            return 8;
        };
        cbOpcodeTable[0x24] = () -> { /* SLA H */
            int value = registers.getH();
            int carry = (value >> 7) & 0x1;
            int result = (value << 1) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry == 1);
            registers.setH(result);

            return 8;
        };
        cbOpcodeTable[0x25] = () -> { /* SLA L */
            int value = registers.getL();
            int carry = (value >> 7) & 0x1;
            int result = (value << 1) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry == 1);
            registers.setL(result);

            return 8;
        };
        cbOpcodeTable[0x26] = () -> { /* SLA (HL) */
            int value = mmu.readByte(registers.getHL());
            int carry = (value >> 7) & 0x1;
            int result = (value << 1) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry == 1);
            mmu.writeByte(registers.getHL(), result);

            return 16;
        };
        cbOpcodeTable[0x27] = () -> { /* SLA A */
            int value = registers.getA();
            int carry = (value >> 7) & 0x1;
            int result = (value << 1) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(carry == 1);
            registers.setA(result);

            return 8;
        };
        cbOpcodeTable[0x28] = () -> { /* SRA B */
            int value = registers.getB();
            int bit0 = value & 0x1;
            int bit7 = value & 0x80; // Keep the sign bit (bit 7) in its original position
            int result = ((value >> 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setB(result);

            return 8;
        };
        cbOpcodeTable[0x29] = () -> { /* SRA C */
            int value = registers.getC();
            int bit0 = value & 0x1;
            int bit7 = value & 0x80;
            int result = ((value >> 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setC(result);

            return 8;
        };
        cbOpcodeTable[0x2A] = () -> { /* SRA D */
            int value = registers.getD();
            int bit0 = value & 0x1;
            int bit7 = value & 0x80;
            int result = ((value >> 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setD(result);

            return 8;
        };
        cbOpcodeTable[0x2B] = () -> { /* SRA E */
            int value = registers.getE();
            int bit0 = value & 0x1;
            int bit7 = value & 0x80;
            int result = ((value >> 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setE(result);

            return 8;
        };
        cbOpcodeTable[0x2C] = () -> { /* SRA H */
            int value = registers.getH();
            int bit0 = value & 0x1;
            int bit7 = value & 0x80;
            int result = ((value >> 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setH(result);

            return 8;
        };
        cbOpcodeTable[0x2D] = () -> { /* SRA L */
            int value = registers.getL();
            int bit0 = value & 0x1;
            int bit7 = value & 0x80;
            int result = ((value >> 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setL(result);

            return 8;
        };
        cbOpcodeTable[0x2E] = () -> { /* SRA (HL) */
            int value = mmu.readByte(registers.getHL());
            int bit0 = value & 0x1;
            int bit7 = value & 0x80;
            int result = ((value >> 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            mmu.writeByte(registers.getHL(), result);

            return 16;
        };
        cbOpcodeTable[0x2F] = () -> { /* SRA A */
            int value = registers.getA();
            int bit0 = value & 0x1;
            int bit7 = value & 0x80;
            int result = ((value >> 1) | bit7) & 0xFF;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bit0 == 1);
            registers.setA(result);

            return 8;
        };
        //=========================================
        //============== 0x30 - 0x3F ==============
        cbOpcodeTable[0x30] = () -> { /* SWAP B */
            int value = registers.getB();
            int highNibble = (value >> 4) & 0xF;
            int lowNibble = value & 0xF;
            int result = (lowNibble << 4) | highNibble;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(false);
            registers.setB(result);

            return 8;
        };
        cbOpcodeTable[0x31] = () -> { /* SWAP C */
            int value = registers.getC();
            int highNibble = (value >> 4) & 0xF;
            int lowNibble = value & 0xF;
            int result = (lowNibble << 4) | highNibble;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(false);
            registers.setC(result);

            return 8;
        };
        cbOpcodeTable[0x32] = () -> { /* SWAP D */
            int value = registers.getD();
            int highNibble = (value >> 4) & 0xF;
            int lowNibble = value & 0xF;
            int result = (lowNibble << 4) | highNibble;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(false);
            registers.setD(result);

            return 8;
        };
        cbOpcodeTable[0x33] = () -> { /* SWAP E */
            int value = registers.getE();
            int highNibble = (value >> 4) & 0xF;
            int lowNibble = value & 0xF;
            int result = (lowNibble << 4) | highNibble;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(false);
            registers.setE(result);

            return 8;
        };
        cbOpcodeTable[0x34] = () -> { /* SWAP H */
            int value = registers.getH();
            int highNibble = (value >> 4) & 0xF;
            int lowNibble = value & 0xF;
            int result = (lowNibble << 4) | highNibble;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(false);
            registers.setH(result);

            return 8;
        };
        cbOpcodeTable[0x35] = () -> { /* SWAP L */
            int value = registers.getL();
            int highNibble = (value >> 4) & 0xF;
            int lowNibble = value & 0xF;
            int result = (lowNibble << 4) | highNibble;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(false);
            registers.setL(result);

            return 8;
        };
        cbOpcodeTable[0x36] = () -> { /* SWAP (HL) */
            int value = mmu.readByte(registers.getHL());
            int highNibble = (value >> 4) & 0xF;
            int lowNibble = value & 0xF;
            int result = (lowNibble << 4) | highNibble;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(false);
            mmu.writeByte(registers.getHL(), result);

            return 16;
        };
        cbOpcodeTable[0x37] = () -> { /* SWAP A */
            int value = registers.getA();
            int highNibble = (value >> 4) & 0xF;
            int lowNibble = value & 0xF;
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
        cbOpcodeTable[0x39] = () -> { /* SRL C */
            int register = registers.getC();
            int bitCarry = register & 0x1;
            int result = register >>> 1;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bitCarry != 0);
            registers.setC(result);

            return 8;
        };
        cbOpcodeTable[0x3A] = () -> { /* SRL D */
            int register = registers.getD();
            int bitCarry = register & 0x1;
            int result = register >>> 1;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bitCarry != 0);
            registers.setD(result);

            return 8;
        };
        cbOpcodeTable[0x3B] = () -> { /* SRL E */
            int register = registers.getE();
            int bitCarry = register & 0x1;
            int result = register >>> 1;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bitCarry != 0);
            registers.setE(result);

            return 8;
        };
        cbOpcodeTable[0x3C] = () -> { /* SRL H */
            int register = registers.getH();
            int bitCarry = register & 0x1;
            int result = register >>> 1;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bitCarry != 0);
            registers.setH(result);

            return 8;
        };
        cbOpcodeTable[0x3D] = () -> { /* SRL L */
            int register = registers.getL();
            int bitCarry = register & 0x1;
            int result = register >>> 1;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bitCarry != 0);
            registers.setL(result);

            return 8;
        };
        cbOpcodeTable[0x3E] = () -> { /* SRL (HL) */
            int register = mmu.readByte(registers.getHL());
            int bitCarry = register & 0x1;
            int result = register >>> 1;

            flags.setZero(result == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(false);
            flags.setCarry(bitCarry != 0);
            mmu.writeByte(registers.getHL(), result);

            return 16;
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
        //=========================================
        //============== BIT 0x40 - 0x7F ==============

        // Bit 0
        cbOpcodeTable[0x40] = () -> {
            flags.setZero(((registers.getB() >> 0) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x41] = () -> {
            flags.setZero(((registers.getC() >> 0) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x42] = () -> {
            flags.setZero(((registers.getD() >> 0) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x43] = () -> {
            flags.setZero(((registers.getE() >> 0) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x44] = () -> {
            flags.setZero(((registers.getH() >> 0) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x45] = () -> {
            flags.setZero(((registers.getL() >> 0) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x46] = () -> {
            flags.setZero(((mmu.readByte(registers.getHL()) >> 0) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 16;
        };
        cbOpcodeTable[0x47] = () -> {
            flags.setZero(((registers.getA() >> 0) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };

        // Bit 1
        cbOpcodeTable[0x48] = () -> {
            flags.setZero(((registers.getB() >> 1) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x49] = () -> {
            flags.setZero(((registers.getC() >> 1) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x4A] = () -> {
            flags.setZero(((registers.getD() >> 1) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x4B] = () -> {
            flags.setZero(((registers.getE() >> 1) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x4C] = () -> {
            flags.setZero(((registers.getH() >> 1) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x4D] = () -> {
            flags.setZero(((registers.getL() >> 1) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x4E] = () -> {
            flags.setZero(((mmu.readByte(registers.getHL()) >> 1) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 16;
        };
        cbOpcodeTable[0x4F] = () -> {
            flags.setZero(((registers.getA() >> 1) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };

        // Bit 2
        cbOpcodeTable[0x50] = () -> {
            flags.setZero(((registers.getB() >> 2) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x51] = () -> {
            flags.setZero(((registers.getC() >> 2) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x52] = () -> {
            flags.setZero(((registers.getD() >> 2) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x53] = () -> {
            flags.setZero(((registers.getE() >> 2) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x54] = () -> {
            flags.setZero(((registers.getH() >> 2) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x55] = () -> {
            flags.setZero(((registers.getL() >> 2) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x56] = () -> {
            flags.setZero(((mmu.readByte(registers.getHL()) >> 2) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 16;
        };
        cbOpcodeTable[0x57] = () -> {
            flags.setZero(((registers.getA() >> 2) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };

        // Bit 3
        cbOpcodeTable[0x58] = () -> {
            flags.setZero(((registers.getB() >> 3) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x59] = () -> {
            flags.setZero(((registers.getC() >> 3) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x5A] = () -> {
            flags.setZero(((registers.getD() >> 3) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x5B] = () -> {
            flags.setZero(((registers.getE() >> 3) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x5C] = () -> {
            flags.setZero(((registers.getH() >> 3) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x5D] = () -> {
            flags.setZero(((registers.getL() >> 3) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x5E] = () -> {
            flags.setZero(((mmu.readByte(registers.getHL()) >> 3) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 16;
        };
        cbOpcodeTable[0x5F] = () -> {
            flags.setZero(((registers.getA() >> 3) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };

        // Bit 4
        cbOpcodeTable[0x60] = () -> {
            flags.setZero(((registers.getB() >> 4) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x61] = () -> {
            flags.setZero(((registers.getC() >> 4) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x62] = () -> {
            flags.setZero(((registers.getD() >> 4) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x63] = () -> {
            flags.setZero(((registers.getE() >> 4) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x64] = () -> {
            flags.setZero(((registers.getH() >> 4) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x65] = () -> {
            flags.setZero(((registers.getL() >> 4) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x66] = () -> {
            flags.setZero(((mmu.readByte(registers.getHL()) >> 4) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 16;
        };
        cbOpcodeTable[0x67] = () -> {
            flags.setZero(((registers.getA() >> 4) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };

        // Bit 5
        cbOpcodeTable[0x68] = () -> {
            flags.setZero(((registers.getB() >> 5) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x69] = () -> {
            flags.setZero(((registers.getC() >> 5) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x6A] = () -> {
            flags.setZero(((registers.getD() >> 5) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x6B] = () -> {
            flags.setZero(((registers.getE() >> 5) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x6C] = () -> {
            flags.setZero(((registers.getH() >> 5) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x6D] = () -> {
            flags.setZero(((registers.getL() >> 5) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x6E] = () -> {
            flags.setZero(((mmu.readByte(registers.getHL()) >> 5) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 16;
        };
        cbOpcodeTable[0x6F] = () -> {
            flags.setZero(((registers.getA() >> 5) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };

        // Bit 6
        cbOpcodeTable[0x70] = () -> {
            flags.setZero(((registers.getB() >> 6) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x71] = () -> {
            flags.setZero(((registers.getC() >> 6) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x72] = () -> {
            flags.setZero(((registers.getD() >> 6) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x73] = () -> {
            flags.setZero(((registers.getE() >> 6) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x74] = () -> {
            flags.setZero(((registers.getH() >> 6) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x75] = () -> {
            flags.setZero(((registers.getL() >> 6) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x76] = () -> {
            flags.setZero(((mmu.readByte(registers.getHL()) >> 6) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 16;
        };
        cbOpcodeTable[0x77] = () -> {
            flags.setZero(((registers.getA() >> 6) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };

        // Bit 7
        cbOpcodeTable[0x78] = () -> {
            flags.setZero(((registers.getB() >> 7) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x79] = () -> {
            flags.setZero(((registers.getC() >> 7) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x7A] = () -> {
            flags.setZero(((registers.getD() >> 7) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x7B] = () -> {
            flags.setZero(((registers.getE() >> 7) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x7C] = () -> {
            flags.setZero(((registers.getH() >> 7) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x7D] = () -> {
            flags.setZero(((registers.getL() >> 7) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };
        cbOpcodeTable[0x7E] = () -> {
            flags.setZero(((mmu.readByte(registers.getHL()) >> 7) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 16;
        };
        cbOpcodeTable[0x7F] = () -> {
            flags.setZero(((registers.getA() >> 7) & 0x1) == 0);
            flags.setSubtract(false);
            flags.setHalfCarry(true);
            return 8;
        };

        //=========================================
        //============== RES 0x80 - 0xBF ==============

        // Bit 0
        cbOpcodeTable[0x80] = () -> {
            registers.setB(registers.getB() & ~(1 << 0));
            return 8;
        };
        cbOpcodeTable[0x81] = () -> {
            registers.setC(registers.getC() & ~(1 << 0));
            return 8;
        };
        cbOpcodeTable[0x82] = () -> {
            registers.setD(registers.getD() & ~(1 << 0));
            return 8;
        };
        cbOpcodeTable[0x83] = () -> {
            registers.setE(registers.getE() & ~(1 << 0));
            return 8;
        };
        cbOpcodeTable[0x84] = () -> {
            registers.setH(registers.getH() & ~(1 << 0));
            return 8;
        };
        cbOpcodeTable[0x85] = () -> {
            registers.setL(registers.getL() & ~(1 << 0));
            return 8;
        };
        cbOpcodeTable[0x86] = () -> {
            mmu.writeByte(registers.getHL(), mmu.readByte(registers.getHL()) & ~(1 << 0));
            return 16;
        };
        cbOpcodeTable[0x87] = () -> {
            registers.setA(registers.getA() & ~(1 << 0));
            return 8;
        };

        // Bit 1
        cbOpcodeTable[0x88] = () -> {
            registers.setB(registers.getB() & ~(1 << 1));
            return 8;
        };
        cbOpcodeTable[0x89] = () -> {
            registers.setC(registers.getC() & ~(1 << 1));
            return 8;
        };
        cbOpcodeTable[0x8A] = () -> {
            registers.setD(registers.getD() & ~(1 << 1));
            return 8;
        };
        cbOpcodeTable[0x8B] = () -> {
            registers.setE(registers.getE() & ~(1 << 1));
            return 8;
        };
        cbOpcodeTable[0x8C] = () -> {
            registers.setH(registers.getH() & ~(1 << 1));
            return 8;
        };
        cbOpcodeTable[0x8D] = () -> {
            registers.setL(registers.getL() & ~(1 << 1));
            return 8;
        };
        cbOpcodeTable[0x8E] = () -> {
            mmu.writeByte(registers.getHL(), mmu.readByte(registers.getHL()) & ~(1 << 1));
            return 16;
        };
        cbOpcodeTable[0x8F] = () -> {
            registers.setA(registers.getA() & ~(1 << 1));
            return 8;
        };

        // Bit 2
        cbOpcodeTable[0x90] = () -> {
            registers.setB(registers.getB() & ~(1 << 2));
            return 8;
        };
        cbOpcodeTable[0x91] = () -> {
            registers.setC(registers.getC() & ~(1 << 2));
            return 8;
        };
        cbOpcodeTable[0x92] = () -> {
            registers.setD(registers.getD() & ~(1 << 2));
            return 8;
        };
        cbOpcodeTable[0x93] = () -> {
            registers.setE(registers.getE() & ~(1 << 2));
            return 8;
        };
        cbOpcodeTable[0x94] = () -> {
            registers.setH(registers.getH() & ~(1 << 2));
            return 8;
        };
        cbOpcodeTable[0x95] = () -> {
            registers.setL(registers.getL() & ~(1 << 2));
            return 8;
        };
        cbOpcodeTable[0x96] = () -> {
            mmu.writeByte(registers.getHL(), mmu.readByte(registers.getHL()) & ~(1 << 2));
            return 16;
        };
        cbOpcodeTable[0x97] = () -> {
            registers.setA(registers.getA() & ~(1 << 2));
            return 8;
        };

        // Bit 3
        cbOpcodeTable[0x98] = () -> {
            registers.setB(registers.getB() & ~(1 << 3));
            return 8;
        };
        cbOpcodeTable[0x99] = () -> {
            registers.setC(registers.getC() & ~(1 << 3));
            return 8;
        };
        cbOpcodeTable[0x9A] = () -> {
            registers.setD(registers.getD() & ~(1 << 3));
            return 8;
        };
        cbOpcodeTable[0x9B] = () -> {
            registers.setE(registers.getE() & ~(1 << 3));
            return 8;
        };
        cbOpcodeTable[0x9C] = () -> {
            registers.setH(registers.getH() & ~(1 << 3));
            return 8;
        };
        cbOpcodeTable[0x9D] = () -> {
            registers.setL(registers.getL() & ~(1 << 3));
            return 8;
        };
        cbOpcodeTable[0x9E] = () -> {
            mmu.writeByte(registers.getHL(), mmu.readByte(registers.getHL()) & ~(1 << 3));
            return 16;
        };
        cbOpcodeTable[0x9F] = () -> {
            registers.setA(registers.getA() & ~(1 << 3));
            return 8;
        };

        // Bit 4
        cbOpcodeTable[0xA0] = () -> {
            registers.setB(registers.getB() & ~(1 << 4));
            return 8;
        };
        cbOpcodeTable[0xA1] = () -> {
            registers.setC(registers.getC() & ~(1 << 4));
            return 8;
        };
        cbOpcodeTable[0xA2] = () -> {
            registers.setD(registers.getD() & ~(1 << 4));
            return 8;
        };
        cbOpcodeTable[0xA3] = () -> {
            registers.setE(registers.getE() & ~(1 << 4));
            return 8;
        };
        cbOpcodeTable[0xA4] = () -> {
            registers.setH(registers.getH() & ~(1 << 4));
            return 8;
        };
        cbOpcodeTable[0xA5] = () -> {
            registers.setL(registers.getL() & ~(1 << 4));
            return 8;
        };
        cbOpcodeTable[0xA6] = () -> {
            mmu.writeByte(registers.getHL(), mmu.readByte(registers.getHL()) & ~(1 << 4));
            return 16;
        };
        cbOpcodeTable[0xA7] = () -> {
            registers.setA(registers.getA() & ~(1 << 4));
            return 8;
        };

        // Bit 5
        cbOpcodeTable[0xA8] = () -> {
            registers.setB(registers.getB() & ~(1 << 5));
            return 8;
        };
        cbOpcodeTable[0xA9] = () -> {
            registers.setC(registers.getC() & ~(1 << 5));
            return 8;
        };
        cbOpcodeTable[0xAA] = () -> {
            registers.setD(registers.getD() & ~(1 << 5));
            return 8;
        };
        cbOpcodeTable[0xAB] = () -> {
            registers.setE(registers.getE() & ~(1 << 5));
            return 8;
        };
        cbOpcodeTable[0xAC] = () -> {
            registers.setH(registers.getH() & ~(1 << 5));
            return 8;
        };
        cbOpcodeTable[0xAD] = () -> {
            registers.setL(registers.getL() & ~(1 << 5));
            return 8;
        };
        cbOpcodeTable[0xAE] = () -> {
            mmu.writeByte(registers.getHL(), mmu.readByte(registers.getHL()) & ~(1 << 5));
            return 16;
        };
        cbOpcodeTable[0xAF] = () -> {
            registers.setA(registers.getA() & ~(1 << 5));
            return 8;
        };

        // Bit 6
        cbOpcodeTable[0xB0] = () -> {
            registers.setB(registers.getB() & ~(1 << 6));
            return 8;
        };
        cbOpcodeTable[0xB1] = () -> {
            registers.setC(registers.getC() & ~(1 << 6));
            return 8;
        };
        cbOpcodeTable[0xB2] = () -> {
            registers.setD(registers.getD() & ~(1 << 6));
            return 8;
        };
        cbOpcodeTable[0xB3] = () -> {
            registers.setE(registers.getE() & ~(1 << 6));
            return 8;
        };
        cbOpcodeTable[0xB4] = () -> {
            registers.setH(registers.getH() & ~(1 << 6));
            return 8;
        };
        cbOpcodeTable[0xB5] = () -> {
            registers.setL(registers.getL() & ~(1 << 6));
            return 8;
        };
        cbOpcodeTable[0xB6] = () -> {
            mmu.writeByte(registers.getHL(), mmu.readByte(registers.getHL()) & ~(1 << 6));
            return 16;
        };
        cbOpcodeTable[0xB7] = () -> {
            registers.setA(registers.getA() & ~(1 << 6));
            return 8;
        };

        // Bit 7
        cbOpcodeTable[0xB8] = () -> {
            registers.setB(registers.getB() & ~(1 << 7));
            return 8;
        };
        cbOpcodeTable[0xB9] = () -> {
            registers.setC(registers.getC() & ~(1 << 7));
            return 8;
        };
        cbOpcodeTable[0xBA] = () -> {
            registers.setD(registers.getD() & ~(1 << 7));
            return 8;
        };
        cbOpcodeTable[0xBB] = () -> {
            registers.setE(registers.getE() & ~(1 << 7));
            return 8;
        };
        cbOpcodeTable[0xBC] = () -> {
            registers.setH(registers.getH() & ~(1 << 7));
            return 8;
        };
        cbOpcodeTable[0xBD] = () -> {
            registers.setL(registers.getL() & ~(1 << 7));
            return 8;
        };
        cbOpcodeTable[0xBE] = () -> {
            mmu.writeByte(registers.getHL(), mmu.readByte(registers.getHL()) & ~(1 << 7));
            return 16;
        };
        cbOpcodeTable[0xBF] = () -> {
            registers.setA(registers.getA() & ~(1 << 7));
            return 8;
        };

    }
}
