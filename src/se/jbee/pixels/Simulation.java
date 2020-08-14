package se.jbee.pixels;

import static se.jbee.pixels.Momenta.*;

public interface Simulation {

    Rnd LEFT_RIGHT = new Rnd(42L);

    /**
     * Simulates a single pixel.
     *
     * @param x the x coordinate of the pixel to simulate
     * @param y the y coordinate of the pixel to simulate
     * @param matrix the game matrix with the pixel data
     * @param dx the direction in which the simulation processes pixels horizontally
     * @return the moment on x axis of the pixel should y stay the same (most likely obsolete)
     */
    int simulate(int x, int y, GameMatrix matrix, int dx);

    /**
     * Go DOWN, DOWN LEFT or DOWN right
     */
    Simulation SOLID_GRANULAR = (x, y, matrix, dx) -> {
        // need to fall?
        if (y >= matrix.height - 1)
            return 0;
        Material solid = matrix.get(x,y);
        if (solid.displaces(matrix.get(x, y +1))) { // fall
            // some random fast falling
            if (solid.displaces(matrix.get(x, y +2)) && LEFT_RIGHT.nextChance(30))
                return matrix.swapMove(x, y, 0, 2);
            return matrix.swapMove(x, y, 0, 1);
        }
        boolean canGoLeft = solid.displaces(matrix.get(x - 1, y + 1));
        boolean canGoRight = solid.displaces(matrix.get(x+1, y+1));
        if (canGoLeft && canGoRight)
            return matrix.swapMove(x, y, LEFT_RIGHT.nextInt() % 2 == 0 ? -1 : 1, 1);
        if (canGoLeft)
            return matrix.swapMove(x, y, -1, +1);
        if (canGoRight)
            return matrix.swapMove(x, y, 1, 1);
        return 0;
    };

    /**
     * Go DOWN, LEFT or RIGHT
     *
     * The tricky bit with fluids is that they go just left or right on a horizontal plain which is also how the simulation progresses.
     * This can cause two unintended effects.
     *
     * 1. A pixel is moved from one side to the opposite side in a single run as it is moving in direction of the processing.
     * 2. A pixel which moves against the processing direction can cause a pattern of x-x-x-x-x-x-x instead of xxxxxx----
     */
    Simulation FLUID = (x, y, matrix, dx) -> {
        if (y >= matrix.height - 1)
            return 0;

        Material fluid = matrix.get(x,y);

        // check straight down
        if (fluid.displaces(matrix.get(x, y + 1))) {
            int dy = 1;
            if (fluid.displaces(matrix.get(x, y +2)) && LEFT_RIGHT.nextChance(25))
                dy = 2;
            if (false && dy == 1 && LEFT_RIGHT.nextChance(5) && fluid.displaces(matrix.get(x+dx, y+1)))
                return matrix.swapMove(x, y, dx, dy);
            return matrix.swapMove(x, y, 0, dy);
        }

        boolean canGoLeft = x > 0 && fluid.displaces(matrix.get(x - 1, y));
        boolean canGoRight = x < matrix.width - 1 && fluid.displaces(matrix.get(x + 1, y));
        if (canGoLeft && canGoRight) {
            if (fluid.displaces(matrix.get(x-dx,y+1)))
                return matrix.swapMove(x,y,dx,1);
            return 0;
        }

        if (!canGoLeft && !canGoRight) {
            matrix.clearMomentum(x,y);
            return 0;
        }
        Momenta momenta = matrix.getMomenta(x, y);
        if (!canGoLeft && momenta.has(Momentum.LEFT)) {
            if (fluid.displaces(matrix.get(x+1,y+1)))
                return matrix.swapMove(x,y,1,1);
            return 0;
        }
        if (!canGoRight && momenta.has(Momentum.RIGHT)) {
            if (fluid.displaces(matrix.get(x - 1, y + 1)))
                return matrix.swapMove(x, y, -1, 1);
            return 0;
        }
        boolean canGoLeft2 = canGoLeft && x > 1 && fluid.displaces(matrix.get(x-2, y));
        if (momenta.has(Momentum.LEFT) && canGoLeft)
            return matrix.swapMove(x, y, canGoLeft2 ? -2 : -1, 0);
        boolean canGoRight2 = canGoRight && x < matrix.width -2 && fluid.displaces(matrix.get(x+2, y));
        if (momenta.has(Momentum.RIGHT) && canGoRight)
            return matrix.swapMove(x, y, canGoRight2 ? +2 : +1, 0);
        return matrix.swapMove(x, y, canGoLeft ? -1 : 1, 0);
    };

}
