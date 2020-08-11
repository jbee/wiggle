package se.jbee.pixels;

import java.awt.*;

public final class WorldMaterials {

    public static final WorldMaterials TEST = new WorldMaterials();

    static final Material air = new Material(TEST,"Air", null, 0)
        .addVariant("thin", null);

    static final Material wall = new Material(TEST,"Rock", null, 100)
        .addVariant("dense", Color.RED);

    static final Material water = new Material(TEST,"Water", Simulation.FLUID, 5)
        .addVariant("sweet", new Color(30, 144, 255));

    static final Material sand = new Material(TEST,"Sand", Simulation.SOLID_GRANULAR, 10)
        .addVariant("1", new Color(213, 151, 75))
        .addVariant("2", new Color(221, 180, 128))
        .addVariant("3", new Color(255, 237, 206))
        .addVariant("4", new Color(197, 175, 125));

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
}
