package se.jbee.pixels;

public final class World {

    public final int width;
    public final int height;

    public final Rnd rnd = new Rnd();

    private final int[] matrix;

    // TODO coating material for each pixel
    private final Materials materials;
    private final Material border;
    private boolean nextLeftToRight = false;

    private int loopCount;

    World(int width, int height, Materials materials, Material border) {
        this.width = width;
        this.height = height;
        this.materials = materials;
        this.border = border;
        this.matrix = new int[width * height];
    }

    private byte byteOfInt(int value, int n) {
        return (byte) ((value >> (n * 8)) & 0xFF);
    }

    public void replaceAt(int x, int y, Material material) {
        replaceAt(x,y, material.variant(0));
    }
    public void replaceAt(int x, int y, MaterialVariant material) {
        if (y < 0)
            y = height + y;
        if (x < 0)
            x = width + x;
        matrix[y * width + x] = material.toGameCell();
    }

    public Material materialAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return border;
        return materials.byId(matrix[y * width + x] & 0xFF);
    }

    public MaterialVariant materialVariantAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return border.variant(0);
        int cell = this.matrix[y * width + x];
        return materials.byId(cell & 0xFF).variant(byteOfInt(cell, 1));
    }

    public Momenta momentaAt(int x, int y) {
        return new Momenta((matrix[y * width + x] >> 16) & 0xFF);
    }

    public World clearMomentaAt(int x, int y) {
        int i = y * width + x;
        matrix[i] &= 0xff00ffff;
        return this;
    }

    public boolean swap(int x, int y, int dx, int dy) {
        if (dx == 0 && dy == 0)
            return false;
        int x2 = x+dx;
        int y2 = y+dy;
        int i = y * width + x;
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
        this.matrix[i2] = moved;
        this.matrix[i] = tmp;
        return true;
    }

    public int loopCount() {
        return loopCount;
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
        Material cell = materialAt(x, y);
        return cell.isSimulated() &&  cell.effect.applyTo(cell, x, y, this, dx);
    }

}
