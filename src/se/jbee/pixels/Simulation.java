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
    Simulation SOLID_GRANULAR = (x, y, matrix, frame) -> {
        // need to fall?
        if (y >= matrix.height - 1)
            return 0;
        Material solid = matrix.get(x,y);
        if (solid.displaces(matrix.get(x, y +1))) { // fall
            // dome random fast falling
            if (solid.displaces(matrix.get(x, y +2)) && LEFT_RIGHT.nextChance(30))
                return matrix.swapMove(x, y, 0, 2);
            return matrix.swapMove(x, y, 0, 1);
        }
        boolean canGoLeft = solid.displaces(matrix.get(x - 1, y + 1));
        boolean canGoRight = solid.displaces(matrix.get(x+1, y+1));
        if (canGoLeft && !canGoRight)
            return matrix.swapMove(x, y, -1, +1);
        if (canGoRight && !canGoLeft)
            return matrix.swapMove(x, y, 1, 1);
        if (canGoLeft && canGoRight)
            return matrix.swapMove(x, y, LEFT_RIGHT.nextInt() % 2 == 0 ? -1 : 1, 1);
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
    Simulation FLUID2 = (x, y, matrix, dx) -> {
        if (y >= matrix.height - 1)
            return 0;

        Material fluid = matrix.get(x,y);

        // check straight down
        if (fluid.displaces(matrix.get(x, y + 1))) {
            return matrix.swapMove(x,y, 0, 1);
        }

        Momenta momenta = matrix.getMomenta(x, y);
        boolean canGoLeft = x > 0 && fluid.displaces(matrix.get(x - 1, y));
        boolean canGoRight = x < matrix.width - 1 && fluid.displaces(matrix.get(x + 1, y));
        boolean leftToRightProcessing = dx > 0;
        boolean shouldGoLeft = momenta.has(Momentum.LEFT) || !momenta.hasMomentum();
        boolean shouldGoRight = momenta.has(Momentum.RIGHT) || !momenta.hasMomentum();

        // only move to left now
        if (leftToRightProcessing && canGoLeft && shouldGoLeft)
            return matrix.swapMove(x,y, -1, 0);
        if (!leftToRightProcessing && canGoRight && shouldGoRight)
            return matrix.swapMove(x,y, +1, 0);
        return 0;
    };

    Simulation FLUID = (x, y, matrix, dx) -> {
        if (y >= matrix.height - 1)
            return 0;

        Material fluid = matrix.get(x,y);

        // check straight down
        if (fluid.displaces(matrix.get(x, y + 1))) {
            int dy = 1;
            if (fluid.displaces(matrix.get(x, y +2)) && LEFT_RIGHT.nextChance(25))
                dy = 2;
            if (dy == 1 && LEFT_RIGHT.nextChance(5) && fluid.displaces(matrix.get(x+dx, y+1)))
                return matrix.swapMove(x, y, dx, dy);
            return matrix.swapMove(x, y, 0, dy);
        }

        Momenta momenta = matrix.getMomenta(x, y);
        boolean canGoLeft = x > 0 && fluid.displaces(matrix.get(x - 1, y));
        boolean canGoRight = x < matrix.width - 1 && fluid.displaces(matrix.get(x + 1, y));
        if (!canGoLeft && !canGoRight)
            return 0;
        if (!canGoLeft && momenta.has(Momentum.LEFT)) {
            matrix.addMomenta(x, y, NONE);
            return 0;
        }
        if (!canGoRight && momenta.has(Momentum.RIGHT)) {
            matrix.addMomenta(x, y, NONE);
            return 0;
        }

        if (true) {
            boolean canGoLeft2 = canGoLeft && x > 1 && fluid.displaces(matrix.get(x-2, y));
            if (momenta.has(Momentum.LEFT) && canGoLeft) {
                return matrix.swapMove(x, y, canGoLeft2 ? -2 : -1, 0);
            }
            boolean canGoRight2 = canGoRight && x < matrix.width -2 && fluid.displaces(matrix.get(x+2, y));
            if (momenta.has(Momentum.RIGHT) && canGoRight) {
                return matrix.swapMove(x, y, canGoRight2 ? +2 : +1, 0);
            }
        }
        if (true && dx > 0) { // this makes sure we prefer to move with the direction in which the dx is processed horizontally to get a maximal spreading effect
            if (canGoRight)
                return matrix.addMomenta(x,y, JUST_RIGHT).swapMove(x, y, +1, 0);
            if (canGoLeft)
                return matrix.addMomenta(x,y, JUST_LEFT).swapMove(x, y, -1, 0);
        } else if (true) {
            if (canGoLeft)
                return matrix.addMomenta(x,y, JUST_LEFT).swapMove(x, y, -1, 0);
            if (canGoRight)
                return matrix.addMomenta(x,y, JUST_RIGHT).swapMove(x, y, +1, 0);
        }
        //IDEA: use momentum to move left or right and stay there, never move against momentum, remove momentum when left and right neighours are either same fluid or not displaceable
        return 0;
    };
    // if we can go left or right and there is same fluid (or any?) above we move
    // do we have a momentum? => continue with chance
    //KEY: if a fluid moves horizontal it does give momentum to the neighbours its gets moved to if they are also of the fluid, otherwise, it looses momentum after 1 move


}
