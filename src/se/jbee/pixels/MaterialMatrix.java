package se.jbee.pixels;

public final class MaterialMatrix {

    public final int width;
    public final int height;

    private final byte[] matrix;
    private final Material[] materials;
    private final Material border;

    MaterialMatrix(int width, int height, Material nothing, Material border) {
        this.width = width;
        this.height = height;
        this.materials = new Material[128];
        this.materials[nothing.id] = nothing;
        this.materials[border.id] = border;
        this.border = border;
        this.matrix = new byte[width * height];
    }

    public void set(int x, int y, Material material) {
        if (y < 0)
            y = height + y;
        if (x < 0)
            x = width + x;
        matrix[y * width + x] = material.id;
        materials[material.id] = material;
    }
    public Material get(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return border;
        return materials[matrix[y * width + x]];
    }

    public void move(int x, int y, int dx, int dy) {
        move(x, y, dx, dy, null);
    }

    public void move(int x, int y, int dx, int dy, Material fill) {
        set(x+dx, y+dy, get(x, y));
        set(x,y, fill == null ? materials[0] : fill);
    }

    public void simulate(int x, int y, int run) {
        Material material = get(x, y);
        if (material.isSimulated) {
            material.simulation.simulate(x, y, this, run);
        }
    }
}
