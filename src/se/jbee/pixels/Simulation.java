package se.jbee.pixels;

public interface Simulation {

    void simulate(int x, int y, MaterialMatrix matrix, int run);

    Simulation SAND = (x, y, matrix, run) -> {
        Material type = matrix.get(x,y);
        // need to fall?
        if (y < matrix.height - 1) {
            Material below = matrix.get(x, y +1);
            if (below.isNothing()) { // fall
                matrix.move(x, y, 0, 1);
            } else {
                Material left = matrix.get(x - 1, y+1);
                Material right = matrix.get(x+1, y+1);
                if (left.isNothing() && (!right.isNothing() || run % 2 == 0)) {
                    matrix.move(x, y, -1, +1);
                }
                if (right.isNothing() && (!left.isNothing() || run % 2 == 1)) {
                    matrix.move(x, y, 1, 1);
                }
            }
        }
    };
}
