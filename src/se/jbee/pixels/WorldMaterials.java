package se.jbee.pixels;

import java.awt.*;

public final class WorldMaterials {

    public static final WorldMaterials TEST = new WorldMaterials();

    static final Material air = new Material(TEST,"Air", null, 0)
        .addVariant("thin");

    static final Material eterium = new Material(TEST,"Eterium", null, 1000)
        .addVariant("dense", new Color(243, 3, 13));

    static final Material water = new Material(TEST,"Water", Simulation.FLUID, 100)
        .addVariant("sweet", new Color(9, 125, 156, 174));

    static final Material Poison = new Material(TEST, "Poison", Simulation.FLUID.with(Simulation.morphs(other -> other == water)), 70)
            .addVariant("week", 20, new Color(54, 180, 26), new Color(57, 194, 27));

    static final Material oil = new Material(TEST,"Oil", Simulation.FLUID, 40)
            .addVariant("sticky", new Color(194, 76, 36));

    static final Material slime = new Material(TEST,"Soylent Green", Simulation.GOO, 130)
            .addVariant("itchy", new Color(178, 226, 3))
            .addVariant("bitchy", new Color(129, 229, 94));

    static final Material sand = new Material(TEST,"Dirt", Simulation.SOLID_GRANULAR, 600)
        .addVariant("chocolate", new Color(213, 151, 75))
        .addVariant("khaki", new Color(221, 180, 128))
        .addVariant("light", new Color(255, 237, 206))
        .addVariant("dust", new Color(197, 175, 125));

    static final Material stone = new Material(TEST, "Stone", Simulation.SOLID, 800)
        .addVariant("bolder", new Color(135, 135, 135, 255))
        .addVariant("bolder", new Color(156, 156, 156, 255));

    private int nextId = 0;
    private final Material[] materialsById = new Material[128];

    private WorldMaterials() {
    }

    public int nextId() {
        return nextId++;
    }

    public void add(Material m) {
        materialsById[m.id] = m;
    }

    public Material byId(int id) {
        return materialsById[id];
    }

    public int count() {
        return nextId;
    }
}
