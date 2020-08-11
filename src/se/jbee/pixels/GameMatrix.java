package se.jbee.pixels;

public final class GameMatrix {

    public final int width;
    public final int height;

    private final int[] matrix;
    // TODO moment for each pixel
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

    public void set(int x, int y, Material material) {
        set(x,y, material.variant(0));
    }
    public void set(int x, int y, MaterialVariant material) {
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

    public void swapMove(int x, int y, int dx, int dy) {
        int x2 = x+dx;
        int y2 = y+dy;
        int i = y * width + x;
        int i2 = y2 * width + x2;
        int tmp = matrix[i2];
        matrix[i2] = matrix[i];
        matrix[i] = tmp;
    }

    public void simulate(int x, int y, int run) {
        Material material = get(x, y);
        if (material.isSimulated()) {
            material.simulation.simulate(x, y, this, run);
        }
    }
}
