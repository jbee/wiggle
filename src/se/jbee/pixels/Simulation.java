package se.jbee.pixels;

import java.util.List;

public interface Simulation {

    Rnd LEFT_RIGHT = new Rnd(42L);

    boolean simulate(int x, int y, GameMatrix matrix, int frame);

    Simulation SOLID_GRANULAR = (x, y, matrix, frame) -> {
        // need to fall?
        if (y >= matrix.height - 1) {
            return false;
        }
        Material solid = matrix.get(x,y);
        Material below = matrix.get(x, y +1);
        if (solid.swapsWith(below)) { // fall
            matrix.swapMove(x, y, 0, 1);
            return true;
        }
        Material belowLeft = matrix.get(x - 1, y+1);
        Material belowRight = matrix.get(x+1, y+1);
        boolean preferLeft = LEFT_RIGHT.nextInt() % 2 == 0;
        boolean doesSwapWithRight = solid.swapsWith(belowRight);
        if (solid.swapsWith(belowLeft) && (preferLeft || !doesSwapWithRight)) {
            matrix.swapMove(x, y, -1, +1);
            return true;
        }
        if (doesSwapWithRight) {
            matrix.swapMove(x, y, 1, 1);
            return true;
        }
        return false;
    };

    Simulation FLUID = (x, y, matrix, frame) -> {
        if (y >= matrix.height - 1) {
            return false;
        }
        Material fluid = matrix.get(x,y);
        Momenta momenta = matrix.getMomenta(x,y);
        Material below = matrix.get(x, y +1);
        if (fluid.swapsWith(below)) { // fall
            matrix.swapMove(x, y, 0, 1, new Momenta(Momentum.DOWN));
            return true;
        }
        Material belowLeft = matrix.get(x - 1, y+1);
        Material belowRight = matrix.get(x+1, y+1);
        boolean preferLeft = momenta.has(Momentum.LEFT)
                || (!momenta.has(Momentum.RIGHT) && LEFT_RIGHT.nextInt() % 2 == 0);
        boolean doesSwapWithRight = fluid.swapsWith(belowRight);
        if (fluid.swapsWith(belowLeft) && (preferLeft || !doesSwapWithRight)) {
            matrix.swapMove(x, y, -1, +1, new Momenta(Momentum.LEFT, Momentum.DOWN));
            return true;
        }
        if (doesSwapWithRight) {
            matrix.swapMove(x, y, 1, 1, new Momenta(Momentum.RIGHT, Momentum.DOWN));
            return true;
        }
        Material left = matrix.get(x-1, y);
        Material right = matrix.get(x+1, y);
        doesSwapWithRight = fluid.swapsWith(right);
        if (fluid.swapsWith(left) && (preferLeft || !doesSwapWithRight)) {
            matrix.swapMove(x, y, -1, 0, new Momenta(Momentum.LEFT));
            return true;
        }
        if (doesSwapWithRight) {
            matrix.swapMove(x, y, 1, 0, new Momenta(Momentum.RIGHT));
            return true;
        }
        return false;
    };
}
