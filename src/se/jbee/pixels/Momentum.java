package se.jbee.pixels;

public enum Momentum {

    LEFT,
    RIGHT,
    UP,
    DOWN,
    SPIN;

    public final int toGameCell;

    Momentum() {
        this.toGameCell = 1 << ordinal() + 16;
    }
}
