package se.jbee.wiggle.engine;

public enum Momentum {

    LEFT,
    RIGHT,
    UP,
    DOWN,
    SPIN;

    public final int asGameCell;

    Momentum() {
        this.asGameCell = 1 << ordinal() + 16;
    }
}
