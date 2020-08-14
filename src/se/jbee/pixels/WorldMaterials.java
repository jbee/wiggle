package se.jbee.pixels;

import java.awt.*;

public final class WorldMaterials {

    public static final WorldMaterials TEST = new WorldMaterials();

    static final Material air = new Material(TEST,"Air", null, 0)
        .addVariant("thin");

    static final Material rock = new Material(TEST,"Rock", null, 100)
        .addVariant("dense", new Color(243, 3, 13));

    static final Material water = new Material(TEST,"Water", Simulation.FLUID, 5)
        .addVariant("sweet", 10, new Color(9, 125, 156, 174))
        .addVariant("sweet", 10, new Color(9, 125, 156, 221));

    static final Material oil = new Material(TEST,"Oil", Simulation.FLUID, 4)
            .addVariant("sticky", new Color(194, 76, 36));

    static final Material slime = new Material(TEST,"Soylent Green", Simulation.FLUID, 6)
            .addVariant("itchy", new Color(178, 226, 3))
            .addVariant("bitchy", new Color(129, 229, 94));

    static final Material sand = new Material(TEST,"Sand", Simulation.SOLID_GRANULAR, 10)
        .addVariant("chocolate", new Color(213, 151, 75))
        .addVariant("khaki", new Color(221, 180, 128))
        .addVariant("light", new Color(255, 237, 206))
        .addVariant("dust", new Color(197, 175, 125));

    private int nextId = 0;
    private final Material[] materialsById = new Material[128];

    private WorldMaterials() {
    }

    public int nextId() {
        return nextId++;
    }

    public void add(Material m) {
        materialsById[m.id] = m;
        System.out.println("added "+m.name +" as "+m.id);
    }

    public Material byId(int id) {
        return materialsById[id];
    }

    public int count() {
        return nextId;
    }
}
