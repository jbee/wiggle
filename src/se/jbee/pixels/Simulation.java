package se.jbee.pixels;

public interface Simulation {

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
     * Go DOWN
     */
    Simulation SOLID = (x, y, matrix, dx) -> {
        if (y >= matrix.height - 1)
            return 0;
        Material solid = matrix.get(x,y);
        if (solid.displaces(matrix.get(x, y +1)))
            return matrix.swapMove(x, y, 0, 1);
        return 0;
    };


    /**
     * Go DOWN, DOWN LEFT or DOWN RIGHT
     */
    Simulation SOLID_GRANULAR = (x, y, matrix, dx) -> {
        // need to fall?
        if (y >= matrix.height - 1)
            return 0;
        Material solid = matrix.get(x,y);
        if (solid.displaces(matrix.get(x, y +1))) { // fall
            // some random fast falling
            if (solid.displaces(matrix.get(x, y +2)) && matrix.rnd.nextChance(30))
                return matrix.swapMove(x, y, 0, 2);
            return matrix.swapMove(x, y, 0, 1);
        }
        boolean canGoLeft = solid.displaces(matrix.get(x - 1, y + 1));
        boolean canGoRight = solid.displaces(matrix.get(x+1, y+1));
        if (canGoLeft && canGoRight)
            return matrix.swapMove(x, y, matrix.rnd.nextInt() % 2 == 0 ? -1 : 1, 1);
        if (canGoLeft)
            return matrix.swapMove(x, y, -1, +1);
        if (canGoRight)
            return matrix.swapMove(x, y, 1, 1);
        return 0;
    };

    /**
     * Go DOWN, LEFT or RIGHT
     */
    Simulation FLUID = (x, y, matrix, dx) -> {
        if (y >= matrix.height - 1)
            return 0;

        Material fluid = matrix.get(x,y);

        // down (1 or 2)?
        if (fluid.displaces(matrix.get(x, y + 1))) {
            if (fluid.displaces(matrix.get(x, y +2)) && matrix.rnd.nextChance(20))
                matrix.swapMove(x, y, 0, 2);
            return matrix.swapMove(x, y, 0, 1);
        }

        // left or right?
        boolean canGoLeft = x > 0 && fluid.displaces(matrix.get(x - 1, y));
        boolean canGoRight = x < matrix.width - 1 && fluid.displaces(matrix.get(x + 1, y));
        if (!canGoLeft && !canGoRight) {
            matrix.clearMomentum(x,y);
            return 0;
        }
        Momenta momenta = matrix.getMomenta(x, y);
        if (canGoLeft && canGoRight) {
            if (fluid.displaces(matrix.get(x-dx,y+1)))
                return matrix.swapMove(x,y,-dx,1);
            if (momenta.isNone() || (!momenta.isLeft() && !momenta.isRight()))
                return 0;
            // we have a left or right momentum
            if (x >= 2 && fluid == matrix.get(x-2, y) || x < matrix.width - 2 && fluid == matrix.get(x+2, y))
                matrix.clearMomentum(x, y);
            return 0;
        }
        if (!canGoLeft && momenta.isLeft()) {
            // right down?
            if (fluid.displaces(matrix.get(x+1,y+1)))
                return matrix.swapMove(x,y,1,1);
            return 0;
        }
        if (!canGoRight && momenta.isRight()) {
            // left down?
            if (fluid.displaces(matrix.get(x - 1, y + 1)))
                return matrix.swapMove(x, y, -1, 1);
            return 0;
        }
        boolean canGoLeft2 = canGoLeft && x > 1 && fluid.displaces(matrix.get(x-2, y));
        if (momenta.isLeft() && canGoLeft)
            return matrix.swapMove(x, y, canGoLeft2 ? -2 : -1, 0);
        boolean canGoRight2 = canGoRight && x < matrix.width -2 && fluid.displaces(matrix.get(x+2, y));
        if (momenta.isRight() && canGoRight)
            return matrix.swapMove(x, y, canGoRight2 ? +2 : +1, 0);
        return matrix.swapMove(x, y, canGoLeft ? -1 : 1, 0);
    };

    /**
     * Go DOWN, LEFT or RIGHT
     */
    Simulation GOO = (x, y, matrix, dx) -> {
        if (y >= matrix.height - 1)
            return 0;

        Material fluid = matrix.get(x,y);

        // down (1 or 2)?
        if (fluid.displaces(matrix.get(x, y + 1))) {
            if (fluid.displaces(matrix.get(x, y +2)) && matrix.rnd.nextChance(1))
                matrix.swapMove(x, y, 0, 2);
            return matrix.swapMove(x, y, 0, 1);
        }

        // left or right?
        boolean canGoLeft = x > 0 && fluid.displaces(matrix.get(x - 1, y));
        boolean canGoRight = x < matrix.width - 1 && fluid.displaces(matrix.get(x + 1, y));
        if (!canGoLeft && !canGoRight) {
            matrix.clearMomentum(x,y);
            return 0;
        }
        Momenta momenta = matrix.getMomenta(x, y);
        if (canGoLeft && canGoRight) {
            if (fluid.displaces(matrix.get(x-dx,y+1)))
                return matrix.swapMove(x,y,-dx,1);
            if (momenta.isNone() || (!momenta.isLeft() && !momenta.isRight()))
                return 0;
            // we have a left or right momentum
            if (x >= 2 && fluid == matrix.get(x-2, y) || x < matrix.width - 2 && fluid == matrix.get(x+2, y)) {
                matrix.clearMomentum(x, y);
            }
            if (x >= 3 && fluid == matrix.get(x-3, y) || x < matrix.width - 3 && fluid == matrix.get(x+3, y)) {
                if (momenta.isRight())
                    return matrix.swapMove(x,y, -1, 0);
                if (momenta.isLeft())
                    return matrix.swapMove(x,y, +1, 0);
            }
            return 0;
        }
        if (!canGoLeft && momenta.isLeft()) {
            // right down?
            if (fluid.displaces(matrix.get(x+1,y+1)))
                return matrix.swapMove(x,y,1,1);
            return 0;
        }
        if (!canGoRight && momenta.isRight()) {
            // left down?
            if (fluid.displaces(matrix.get(x - 1, y + 1)))
                return matrix.swapMove(x, y, -1, 1);
            return 0;
        }
        boolean canGoLeft2 = canGoLeft && x > 1 && fluid.displaces(matrix.get(x-2, y));
        if (momenta.isLeft() && canGoLeft)
            return matrix.swapMove(x, y, canGoLeft2 ? -2 : -1, 0);
        boolean canGoRight2 = canGoRight && x < matrix.width -2 && fluid.displaces(matrix.get(x+2, y));
        if (momenta.isRight() && canGoRight)
            return matrix.swapMove(x, y, canGoRight2 ? +2 : +1, 0);
        return matrix.swapMove(x, y, canGoLeft ? -1 : 1, 0);
    };
}
