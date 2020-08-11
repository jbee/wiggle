package se.jbee.pixels;

public interface Simulation {

    Rnd LEFT_RIGHT = new Rnd(42L);

    boolean simulate(int x, int y, MaterialMatrix matrix, int run);

    Simulation SAND = (x, y, matrix, run) -> {
        // need to fall?
        if (y < matrix.height - 1) {
            Material below = matrix.get(x, y +1);
            if (below.isNothing()) { // fall
                matrix.move(x, y, 0, 1);
                return true;
            }
            Material belowLeft = matrix.get(x - 1, y+1);
            Material belowRight = matrix.get(x+1, y+1);
            int evenOdd = LEFT_RIGHT.nextInt() % 2;
            if (belowLeft.isNothing() && (!belowRight.isNothing() || evenOdd == 0)) {
                matrix.move(x, y, -1, +1);
                return true;
            }
            if (belowRight.isNothing() && (!belowLeft.isNothing() || evenOdd == 1)) {
                matrix.move(x, y, 1, 1);
                return true;
            }
        }
        return false;
    };

    Simulation WATER = (x, y, matrix, run) -> {
        if (!SAND.simulate(x,y,matrix, run)) {
            Material left = matrix.get(x-1, y);
            Material right = matrix.get(x+1, y);
            int evenOdd = LEFT_RIGHT.nextInt() % 2;
            if (left.isNothing() && (!right.isNothing() || evenOdd == 0)) {
                matrix.move(x, y, -1, 0);
                return true;
            }
            if (right.isNothing() && (!left.isNothing() || evenOdd == 1)) {
                matrix.move(x, y, 1, 0);
                return true;
            }
        }
        return true;
    };
}
