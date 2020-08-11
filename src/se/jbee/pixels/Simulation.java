package se.jbee.pixels;

public interface Simulation {

    Rnd LEFT_RIGHT = new Rnd(42L);

    boolean simulate(int x, int y, GameMatrix matrix, int frame);

    Simulation SOLID_GRANULAR = (x, y, matrix, frame) -> {
        // need to fall?
        if (y < matrix.height - 1) {
            Material sand = matrix.get(x,y);
            Material below = matrix.get(x, y +1);
            if (sand.swapsWith(below)) { // fall
                matrix.swapMove(x, y, 0, 1);
                return true;
            }
            Material belowLeft = matrix.get(x - 1, y+1);
            Material belowRight = matrix.get(x+1, y+1);
            boolean preferLeft = LEFT_RIGHT.nextInt() % 2 == 0;
            boolean doesSwapWithRight = sand.swapsWith(belowRight);
            if (sand.swapsWith(belowLeft) && (preferLeft || !doesSwapWithRight)) {
                matrix.swapMove(x, y, -1, +1);
                return true;
            }
            if (doesSwapWithRight) {
                matrix.swapMove(x, y, 1, 1);
                return true;
            }
        }
        return false;
    };

    Simulation FLUID = (x, y, matrix, frame) -> {
        if (SOLID_GRANULAR.simulate(x,y,matrix, frame)) {
            return true;
        }
        Material fluid = matrix.get(x,y);
        Material left = matrix.get(x-1, y);
        Material right = matrix.get(x+1, y);
        boolean preferLeft = LEFT_RIGHT.nextInt() % 2 == 0;
        boolean doesSwapWithRight = fluid.swapsWith(right);
        if (fluid.swapsWith(left) && (preferLeft || !doesSwapWithRight)) {
            matrix.swapMove(x, y, -1, 0);
            return true;
        }
        if (doesSwapWithRight) {
            matrix.swapMove(x, y, 1, 0);
            return true;
        }
        return false;
    };
}
