package se.jbee.pixels;

import java.util.function.Predicate;

/**
 * Encodes a {@link Material}'s behaviour in the {@link GameSimulation} pixel by pixel.
 *
 * Usually each pixel looks at a few if its direct neighbours to determine if and where it wants to move.
 *
 * Some behaviours also morph or transform themselves or neighbouring pixels.
 */
public interface Behaviour {

    /**
     * Simulates a single pixel.
     *
     * @param x the x coordinate of the pixel to simulate
     * @param y the y coordinate of the pixel to simulate
     * @param matrix the game matrix with the pixel data
     * @param dx the direction in which the simulation processes pixels horizontally
     * @return true if the pixel moved, else false
     */
    boolean simulate(int x, int y, GameSimulation matrix, int dx);

    default Behaviour with(Behaviour effect) {
        // this is important to use | (single) so that the effect is executed second but also executed in any case
        return (x, y, matrix, dx) -> this.simulate(x,y,matrix,dx) | effect.simulate(x,y,matrix,dx);
    }

    /**
     * Go DOWN
     */
    Behaviour SOLID = (x, y, matrix, dx) -> {
        if (y >= matrix.height - 1)
            return false;
        Material solid = matrix.materialAt(x,y);
        if (solid.displaces(matrix.materialAt(x, y +1)))
            return matrix.swap(x, y, 0, 1);
        return false;
    };

    static Behaviour pollutes(Predicate<Material> when) {
        return (x,y,matrix,dx) -> {
            Material to = matrix.materialAt(x, y);
            if (matrix.loopCount() % 4 != 0)
                return false;
            if (dx != 1 && x < matrix.width - 1) {
                Material target = matrix.materialAt(x+1, y);
                if (when.test(target))
                    matrix.replaceAt(x + 1, y, to);
            }
            if (x > 0 && dx != -1) {
                Material target = matrix.materialAt(x-1, y);
                if (when.test(target))
                    matrix.replaceAt(x - 1, y, to);
            }
            if (y > 0) {
                Material target = matrix.materialAt(x, y - 1);
                if (when.test(target))
                    matrix.replaceAt(x ,y-1, to);
            }
            if (y < matrix.height-1) {
                Material target = matrix.materialAt(x, y + 1);
                if (when.test(target))
                    matrix.replaceAt(x, y+1, to);
            }
            return false;
        };

    }

    /**
     * Go DOWN, DOWN LEFT or DOWN RIGHT
     */
    Behaviour SOLID_GRANULAR = (x, y, matrix, dx) -> {
        // need to fall?
        if (y >= matrix.height - 1)
            return false;
        Material solid = matrix.materialAt(x,y);
        if (solid.displaces(matrix.materialAt(x, y +1))) { // fall
            // some random fast falling
            if (solid.displaces(matrix.materialAt(x, y +2)) && matrix.rnd.nextChance(30))
                return matrix.swap(x, y, 0, 2);
            return matrix.swap(x, y, 0, 1);
        }
        boolean canGoLeft = solid.displaces(matrix.materialAt(x - 1, y + 1));
        boolean canGoRight = solid.displaces(matrix.materialAt(x+1, y+1));
        if (canGoLeft && canGoRight)
            return matrix.swap(x, y, matrix.rnd.nextInt() % 2 == 0 ? -1 : 1, 1);
        if (canGoLeft)
            return matrix.swap(x, y, -1, +1);
        if (canGoRight)
            return matrix.swap(x, y, 1, 1);
        return false;
    };

    /**
     * Go DOWN, LEFT or RIGHT
     */
    Behaviour FLUID = (x, y, matrix, dx) -> {
        if (y >= matrix.height - 1)
            return false;

        Material fluid = matrix.materialAt(x,y);

        // down (1 or 2)?
        if (fluid.displaces(matrix.materialAt(x, y + 1))) {
            if (fluid.displaces(matrix.materialAt(x, y +2)) && matrix.rnd.nextChance(20))
                matrix.swap(x, y, 0, 2);
            return matrix.swap(x, y, 0, 1);
        }

        // left or right?
        boolean canGoLeft = x > 0 && fluid.displaces(matrix.materialAt(x - 1, y));
        boolean canGoRight = x < matrix.width - 1 && fluid.displaces(matrix.materialAt(x + 1, y));
        if (!canGoLeft && !canGoRight) {
            matrix.clearMomentaAt(x,y);
            return false;
        }
        Momenta momenta = matrix.momentaAt(x, y);
        if (canGoLeft && canGoRight) {
            if (fluid.displaces(matrix.materialAt(x-dx,y+1)))
                return matrix.swap(x,y,-dx,1);
            if (momenta.isNone() || (!momenta.isLeft() && !momenta.isRight()))
                return false;
            // we have a left or right momentum
            if (x >= 2 && fluid == matrix.materialAt(x-2, y) || x < matrix.width - 2 && fluid == matrix.materialAt(x+2, y))
                matrix.clearMomentaAt(x, y);
            return false;
        }
        if (!canGoLeft && momenta.isLeft()) {
            // right down?
            if (fluid.displaces(matrix.materialAt(x+1,y+1)))
                return matrix.swap(x,y,1,1);
            return false;
        }
        if (!canGoRight && momenta.isRight()) {
            // left down?
            if (fluid.displaces(matrix.materialAt(x - 1, y + 1)))
                return matrix.swap(x, y, -1, 1);
            return false;
        }
        boolean canGoLeft2 = canGoLeft && x > 1 && fluid.displaces(matrix.materialAt(x-2, y));
        if (momenta.isLeft() && canGoLeft)
            return matrix.swap(x, y, canGoLeft2 ? -2 : -1, 0);
        boolean canGoRight2 = canGoRight && x < matrix.width -2 && fluid.displaces(matrix.materialAt(x+2, y));
        if (momenta.isRight() && canGoRight)
            return matrix.swap(x, y, canGoRight2 ? +2 : +1, 0);
        return matrix.swap(x, y, canGoLeft ? -1 : 1, 0);
    };

    Behaviour GAS = (x, y, matrix, dx) -> {
        if (matrix.loopCount() % 3 == 0) // OBS: careful with skipping even number of loops as that means a tendency to one side, therefore we use 3
            return false;
        Material gas = matrix.materialAt(x,y);
        Momenta momenta = matrix.momentaAt(x,y);
        if (momenta.isUp()) {
            matrix.clearMomentaAt(x,y);
            return false; // this prevents pixel from to top in a single run as we never move up 2 times in a row
        }
        if (y > 0) {
            if (gas.displaces(matrix.materialAt(x, y-1))) {
                if (y > 1 && gas.displaces(matrix.materialAt(x, y - 2)) && matrix.rnd.nextChance(20))
                    matrix.swap(x, y, 0, - 2);
                return matrix.swap(x, y, 0, -1);
            }
        }
        boolean canGoLeft = x > 0 && gas.displaces(matrix.materialAt(x - 1, y));
        boolean canGoRight = x < matrix.width - 1 && gas.displaces(matrix.materialAt(x + 1, y));
        if (!canGoLeft && !canGoRight) {
            if (!momenta.isNone())
                matrix.clearMomentaAt(x,y);
            return false;
        }
        if (true && matrix.rnd.nextChance(15)) // This essentially controls how fast the gas moves left, right, lower means faster
            return false;
        if (canGoLeft)
            return matrix.swap(x,y, -1, 0);
        if (canGoRight)
            return matrix.swap(x,y, +1, 0);
        return false;
    };

    /**
     * Go DOWN, LEFT or RIGHT
     */
    Behaviour GOO = (x, y, matrix, dx) -> {
        if (y >= matrix.height - 1)
            return false;

        Material fluid = matrix.materialAt(x,y);

        // down (1 or 2)?
        if (fluid.displaces(matrix.materialAt(x, y + 1))) {
            if (fluid.displaces(matrix.materialAt(x, y +2)) && matrix.rnd.nextChance(1))
                matrix.swap(x, y, 0, 2);
            return matrix.swap(x, y, 0, 1);
        }

        // left or right?
        boolean canGoLeft = x > 0 && fluid.displaces(matrix.materialAt(x - 1, y));
        boolean canGoRight = x < matrix.width - 1 && fluid.displaces(matrix.materialAt(x + 1, y));
        if (!canGoLeft && !canGoRight) {
            matrix.clearMomentaAt(x,y);
            return false;
        }
        Momenta momenta = matrix.momentaAt(x, y);
        if (canGoLeft && canGoRight) {
            if (fluid.displaces(matrix.materialAt(x-dx,y+1)))
                return matrix.swap(x,y,-dx,1);
            if (momenta.isNone() || (!momenta.isLeft() && !momenta.isRight()))
                return false;
            // we have a left or right momentum
            if (x >= 2 && fluid == matrix.materialAt(x-2, y) || x < matrix.width - 2 && fluid == matrix.materialAt(x+2, y)) {
                matrix.clearMomentaAt(x, y);
            }
            if (x >= 3 && fluid == matrix.materialAt(x-3, y) || x < matrix.width - 3 && fluid == matrix.materialAt(x+3, y)) {
                if (momenta.isRight())
                    return matrix.swap(x,y, -1, 0);
                if (momenta.isLeft())
                    return matrix.swap(x,y, +1, 0);
            }
            return false;
        }
        if (!canGoLeft && momenta.isLeft()) {
            // right down?
            if (fluid.displaces(matrix.materialAt(x+1,y+1)))
                return matrix.swap(x,y,1,1);
            return false;
        }
        if (!canGoRight && momenta.isRight()) {
            // left down?
            if (fluid.displaces(matrix.materialAt(x - 1, y + 1)))
                return matrix.swap(x, y, -1, 1);
            return false;
        }
        boolean canGoLeft2 = canGoLeft && x > 1 && fluid.displaces(matrix.materialAt(x-2, y));
        if (momenta.isLeft() && canGoLeft)
            return matrix.swap(x, y, canGoLeft2 ? -2 : -1, 0);
        boolean canGoRight2 = canGoRight && x < matrix.width -2 && fluid.displaces(matrix.materialAt(x+2, y));
        if (momenta.isRight() && canGoRight)
            return matrix.swap(x, y, canGoRight2 ? +2 : +1, 0);
        return matrix.swap(x, y, canGoLeft ? -1 : 1, 0);
    };
}
