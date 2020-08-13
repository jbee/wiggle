package se.jbee.pixels;

public final class GameMatrix {

    public final int width;
    public final int height;

    private final int[] matrix;
    // TODO moment for each pixel: left, right, up, down, disturbed

    // TODO coating material for each pixel
    private final WorldMaterials materials;
    private final Material border;

    GameMatrix(int width, int height, WorldMaterials materials, Material border) {
        this.width = width;
        this.height = height;
        this.materials = materials;
        this.border = border;
        this.matrix = new int[width * height];
    }

    private byte byteOfInt(int value, int n) {
        return (byte) ((value >> (n * 8)) & 0xFF);
    }

    public void insert(int x, int y, Material material) {
        insert(x,y, material.variant(0));
    }
    public void insert(int x, int y, MaterialVariant material) {
        if (y < 0)
            y = height + y;
        if (x < 0)
            x = width + x;
        matrix[y * width + x] = material.toGameCell();
    }

    public Material get(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return border;
        return materials.byId(matrix[y * width + x] & 0xFF);
    }

    public MaterialVariant getVariant(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return border.variant(0);
        int cell = this.matrix[y * width + x];
        return materials.byId(cell & 0xFF).variant(byteOfInt(cell, 1));
    }

    public Momenta getMomenta(int x, int y) {
        return new Momenta((matrix[y * width + x] >> 16) & 0xFF);
    }

    public GameMatrix addMomenta(int x, int y, Momenta change) {
        int i = y * width + x;
        matrix[i] &= 0xff00ffff;
        matrix[i] |= change.toGameCell();
        return this;
    }

    public int swapMove(int x, int y, int dx, int dy) {
        int x2 = x+dx;
        int y2 = y+dy;
        int i = y * width + x;
        int i2 = y2 * width + x2;
        int tmp = matrix[i2];
        this.matrix[i2] = matrix[i];
        this.matrix[i] = tmp;
        return dy == 0 ? dx : 0;
    }

    public void simulate(int frame) {
        for (int y = height - 1; y >= 0; y--) {
            if (frame % 2 == 0) {
                for (int x = 0; x < width; x++) {
                    simulate(x, y, +1);
                }
            } else {
                for (int x = width-1; x >= 0; x--) {
                    simulate(x, y, -1);
                }
            }
        }
    }

    private int simulate(int x, int y, int frame) {
        Material material = get(x, y);
        if (material.isSimulated()) {
            return material.simulation.simulate(x, y, this, frame);
        }
        return 0;
    }

}
