package se.jbee.pixels;

public enum Momentum {

    LEFT,
    RIGHT,
    UP,
    DOWN,
    SPIN;

    public final int mask;

    Momentum() {
        this.mask = 1 << ordinal() + 16;
    }
}
