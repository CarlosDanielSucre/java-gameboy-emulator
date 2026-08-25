package cpu;

public class Flags {
    private boolean zero, subtract, halfCarry, carry;

    public boolean isZero() {
        return zero;
    }

    public void setZero(boolean zero) {
        this.zero = zero;
    }

    public boolean isSubtract() {
        return subtract;
    }

    public void setSubtract(boolean subtact) {
        this.subtract = subtact;
    }

    public boolean isHalfCarry() {
        return halfCarry;
    }

    public void setHalfCarry(boolean halfCarry) {
        this.halfCarry = halfCarry;
    }

    public boolean isCarry() {
        return carry;
    }

    public void setCarry(boolean carry) {
        this.carry = carry;
    }

    public int toByte() {
        int result = 0;
        if (zero) result |= (1 << 7);
        if (subtract) result |= (1 << 6);
        if (halfCarry) result |= (1 << 5);
        if (carry) result |= (1 << 4);
        return result;
    }

    public void fromByte(int value) {
        value = value & 0xFF;
        zero = ((value & (1 << 7)) != 0);
        subtract = ((value & (1 << 6)) != 0);
        halfCarry = ((value & (1 << 5)) != 0);
        carry = ((value & (1 << 4)) != 0);
        System.out.println("test");
    }
}
