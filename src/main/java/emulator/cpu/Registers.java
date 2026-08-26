package cpu;

public class Registers {
    private int a, b, c, d, e, h, l, f;
    private int sp, pc;
    private Flags flags;

    public int getA() {
        return a;
    }
    public void setA(int a) {
        this.a = a & 0xFF;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b & 0xFF;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c & 0xFF;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d & 0xFF;
    }

    public int getE() {
        return e;
    }

    public void setE(int e) {
        this.e = e & 0xFF;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h& 0xFF;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l & 0xFF;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f & 0xFF;
    }

    public int getSp() {
        return sp;
    }

    public void setSp(int sp) {
        this.sp = sp & 0xFFFF;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc & 0xFFFF;
    }

    public int getBC() {
        return (this.b << 8) | this.c;
    }
    public void setBC(int bc) {
        this.b = (bc >> 8) & 0xFF;
        this.c = bc & 0xFF;
    }

    public int getDE() {
        return (this.d << 8) | this.e;
    }
    public void setDE(int de) {
        this.d = (de >> 8) & 0xFF;
        this.e = de & 0xFF;
    }

    public int getHL() {
        return (this.h << 8) | this.l;
    }
    public void setHL(int hl) {
        this.h = (hl >> 8) & 0xFF;
        this.l = hl & 0xFF;
    }
    public int getAF() {
        return (a << 8) | flags.toByte();
    }

    public void setAF(int value) {
        a = (value >> 8) & 0xFF;
        flags.fromByte(value & 0xFF);
    }
}
