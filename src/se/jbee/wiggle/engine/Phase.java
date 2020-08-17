package se.jbee.wiggle.engine;

public enum Phase {

    FLUID,
    SOLID_GRANULAR,
    SOLID_CRYSTALLINE,
    FLAKY,
    GOOEY,
    GASSY,
    COMBUSTION; // Fire

    public boolean isFluid() {
        return this == FLUID;
    }
}
