package se.jbee.wiggle.engine;

/**
 * The {@link Phase} of a {@link Substance} is a general classification that allows to make
 * some assumptions about the displacement behaviour of two {@link Substance}s.
 *
 * Valid Assumptions:
 * - two liquids displace purely based on density
 * - two gasses displace purely based on density
 * - a solid (any type) and a gas or liquid display purely based on density
 * - all solids (any type) are more dense then any gas
 * - flaky solids either dissolve in fluids or float on top (as long as there is nothing on top of them)
 * - any combination of two of the solids (flaky, grainy, stiff) do not displace based on density. They stack on top of each other.
 *
 * Invalid Assumptions:
 * - fluids are generally less dense then solids (think molten metal is still very dense)
 *
 */
public enum Phase {

    /*
     * Ordered by usual density from low to high.
     *
     * That said there are solids (both grainy and stiff) that have lower density then fluids
     * which makes them float.
     */

    /**
     * Are those {@link Substance}s that dissolve in air or are air like.
     * Like liquids gasses either mix or layer based on density.
     * All gasses are lighter than any liquid so they always move upwards based on density of the two substances.
     */
    GASSY,

    /**
     * Are those solid and soluble {@link Substance}s that easily flake or naturally occur in form of flakes, paddles, powders and alike.
     * Flakes usually dissolve or float on fluids. They do not displace sink.
     * Their density of this particles is higher then gasses but lower then liquids.
     * Flaky solids may also swirl in air but they have a natural tendency to fall down in gasses
     * because of their higher density.
     */
    FLAKY,

    /**
     * Are those {@link Substance}s that displace other fluids easily in any direction.
     * Some fluids dissolve in others or transform to another {@link Substance}.
     * Some fluids do not mix and float or sink.
     */
    FLUID,

    /**
     * Are those solid {@link Substance}s that mix somewhat because of their grain size.
     * They float or sink in fluids. They do not dissolve.
     */
    GRAINY,

    /**
     * Are those "stiff" {@link Substance}s that hold firmly together, like metals, plastics, stone or wood.
     * When they fall they fall as one block, which might split but does not follow contours like grainy solids do.
     */
    SOLID,

    /**
     * Substance is undergoing a process which causes a transformation between phases.
     */
    COMBUSTION; // Fire

    public boolean isFluid() {
        return this == FLUID;
    }
}
