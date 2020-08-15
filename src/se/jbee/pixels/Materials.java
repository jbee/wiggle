package se.jbee.pixels;

import java.awt.*;

import static se.jbee.pixels.Behaviour.pollutes;
import static se.jbee.pixels.MaterialGroup.*;

/**
 * A set of {@link Material} that defines all {@link Material}s used in a {@link GameSimulation}.
 */
public final class Materials {

    public static final Materials TEST = new Materials();

    static final Material Air = new Material(TEST,"Air", GASSY, null, 0)
            .addVariant("thin");

    static final Material HardRock = new Material(TEST,"Hard Rock", SOLID_CRYSTALLINE, null, 1000)
            .addVariant("dense", new Color(139, 94, 96));

    static final Material Water = new Material(TEST,"Water", FLUID, Behaviour.FLUID, 100)
            .addVariant("sweet", new Color(9, 125, 156, 174));

    static final Material Steam = new Material(TEST, "Steam", GASSY, Behaviour.GAS, 10)
            .addVariant("water", new Color(184, 227, 232));

    static final Material Poison = new Material(TEST, "Poison", FLUID, Behaviour.FLUID.with(pollutes(other -> other == Water)), 70)
            .addVariant("week", new Color(184, 216, 22)); //, new Color(214, 254, 29)

    static final Material Oil = new Material(TEST,"Oil", FLUID, Behaviour.FLUID, 40)
            .addVariant("sticky", new Color(194, 76, 36));

    static final Material Slime = new Material(TEST,"Soylent Green", SPONGY, Behaviour.GOO, 130)
            .addVariant("itchy", new Color(178, 226, 3))
            .addVariant("bitchy", new Color(129, 229, 94));

    static final Material Dirt = new Material(TEST,"Dirt", SOLID_GRANULAR, Behaviour.SOLID_GRANULAR, 600)
            .addVariant("chocolate", new Color(213, 151, 75))
            .addVariant("khaki", new Color(221, 180, 128))
            .addVariant("light", new Color(255, 237, 206))
            .addVariant("dust", new Color(197, 175, 125));

    static final Material LimeStone = new Material(TEST, "Lime Stone", SOLID_CRYSTALLINE, Behaviour.SOLID, 800)
            .addVariant("bolder", new Color(167, 166, 171, 255))
            .addVariant("bolder", new Color(198, 203, 209, 255));

    private int nextId = 0;
    private final Material[] materialsById = new Material[128];

    private Materials() {
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
