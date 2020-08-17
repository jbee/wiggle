package se.jbee.wiggle.engine;

/**
 * Encodes a {@link Substance}'s behaviour in the {@link World} pixel by pixel.
 *
 * Usually each pixel looks at a few if its direct neighbours to determine if and where it wants to move.
 *
 * Some behaviours also morph or transform themselves or neighbouring pixels.
 */
public interface Effect {

    /**
     * Simulates a single pixel.
     *
     * @param x the x coordinate of the pixel to simulate
     * @param y the y coordinate of the pixel to simulate
     * @param world the game world with the pixel data
     * @param dx the direction in which the simulation processes pixels horizontally
     * @return true if the pixel moved, else false
     */
    boolean applyTo(Substance cell, int x, int y, World world, int dx);

    @Deprecated // => sequence
    default Effect and(Effect effect) {
        // this is important to use | (single) so that the effect is executed second but also executed in any case
        return (cell, x, y, world, dx) -> this.applyTo(cell, x,y,world,dx) | effect.applyTo(cell, x,y,world,dx);
    }

}
