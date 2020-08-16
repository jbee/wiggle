package se.jbee.wiggle;

public final class World {

    public final int width;
    public final int height;

    public final Rnd rnd = new Rnd();

    private final int[] matrix;

    // TODO coating material for each pixel
    private final Substances substances;
    private final Substance edge;
    private boolean nextLeftToRight = false;

    private int loopCount;

    World(int width, int height, Substances substances, Substance edge) {
        this.width = width;
        this.height = height;
        this.substances = substances;
        this.edge = edge;
        this.matrix = new int[width * height];
    }

    private byte byteOfInt(int value, int n) {
        return (byte) ((value >> (n * 8)) & 0xFF);
    }

    public void replaceAt(int x, int y, Substance substance) {
        replaceAt(x,y, substance.variety(0));
    }
    public void replaceAt(int x, int y, SubstanceVariety material) {
        if (y < 0)
            y = height + y;
        if (x < 0)
            x = width + x;
        matrix[y * width + x] = material.toGameCell();
    }

    public Substance substanceAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return edge;
        return substances.byId(matrix[y * width + x] & 0xFF);
    }

    public SubstanceVariety varietyAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return edge.variety(0);
        int cell = this.matrix[y * width + x];
        return substances.byId(cell & 0xFF).variety(byteOfInt(cell, 1));
    }

    public Momenta momentaAt(int x, int y) {
        return new Momenta(matrix[y * width + x] & 0x00FF0000);
    }

    /**
     * Clears any existing {@link Momentum}
     */
    public World calm(int x, int y) {
        int i = y * width + x;
        matrix[i] &= 0xff00ffff;
        return this;
    }

    public boolean swap(int x, int y, int dx, int dy) {
        int i = y * width + x;
        if (dx == 0 && dy == 0) {
            int moved = matrix[i];
            matrix[i] = moved |= Momentum.SPIN.toGameCell;
            return false;
        }
        int x2 = x+dx;
        int y2 = y+dy;
        int i2 = y2 * width + x2;
        int tmp = matrix[i2];
        int moved = matrix[i];
        if (dy != 0) { // clears
            moved &= 0xff00ffff;
            moved |= dy < 0 ? Momentum.UP.toGameCell : Momentum.DOWN.toGameCell;
        }
        if (dx != 0 && dy == 0) {
            moved &= 0xff00ffff;
            moved |= dx < 0 ? Momentum.LEFT.toGameCell : Momentum.RIGHT.toGameCell;
        }
        matrix[i2] = moved;
        matrix[i] = tmp;
        return true;
    }

    public int loopCount() {
        return loopCount;
    }

    public int randomDx() {
        return rnd.nextChance(50) ? -1: 1;
    }

    public void tick() {
        loopCount++;
        boolean leftToRight = nextLeftToRight;
        for (int y = height - 1; y >= 0; y--) {
            if (leftToRight) {
                for (int x = 0; x < width; x++) {
                    tick(x, y, +1);
                }
            } else {
                for (int x = width-1; x >= 0; x--) {
                    tick(x, y, -1);
                }
            }
        }
        nextLeftToRight = !nextLeftToRight;
    }

    private boolean tick(int x, int y, int dx) {
        Substance cell = substanceAt(x, y);
        return cell.isSimulated() &&  cell.effect.applyTo(cell, x, y, this, dx);
    }

}
