package cpu;

public class CPU {
    private Registers registers;
    private Flags flags;

    public CPU() {
        this.registers = new Registers();
        this.flags = new Flags();
    }

    public boolean calculateHalfCarry(int a, int b) {
        return ((a & 0xF) + (b & 0xF) > 0xF);
    }
}
