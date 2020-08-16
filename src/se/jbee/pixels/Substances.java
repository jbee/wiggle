package se.jbee.pixels;

import java.awt.*;

import static se.jbee.pixels.Animation.*;
import static se.jbee.pixels.Effect.becomes;
import static se.jbee.pixels.Effect.pollutes;
import static se.jbee.pixels.MaterialGroup.*;

/**
 * A set of {@link Substance} that defines all {@link Substance}s used in a {@link World}.
 */
public final class Substances {

    public static final Substances TEST = new Substances();

    static final Substance Air = new Substance(TEST,"Air", GASSY, null, 0)
            .addVariety("thin", null);

    static final Substance HardRock = new Substance(TEST,"Hard Rock", SOLID_CRYSTALLINE, null, 1000)
            .addVariety("dense", fill(new Color(139, 94, 96)));

    static final Substance Water = new Substance(TEST,"Water", FLUID, Effect.FLUID, 100)
            .addVariety("sweet", fill(new Color(9, 125, 156, 174)));

    static final Substance Steam = new Substance(TEST, "Steam", GASSY, Effect.GAS.and(becomes(Air, 1)), 10)
            .addVariety("water", fill(new Color(184, 227, 232)));

    static final Substance Poison = new Substance(TEST, "Poison", FLUID, Effect.FLUID.and(pollutes(other -> other == Water)), 100)
            .addVariety("week", fill(new Color(184, 216, 22))); //, new Color(214, 254, 29)

    static final Substance Healium = new Substance(TEST, "Healium", SPONGY, Effect.GOO.and(Effect.HEALIUM), 101)
            .addVariety("bright", Animation.shuffle(10,
                    new Color(231, 131, 241),
                    new Color(224, 117, 234),
                    new Color(231, 131, 241),
                    new Color(238, 182, 243),
                     new Color(246, 194, 252)));

    static final Substance Oil = new Substance(TEST,"Oil", FLUID, Effect.FLUID, 40)
            .addVariety("sticky", fill(new Color(194, 76, 36)));

    static final Substance Slime = new Substance(TEST,"Soylent Green", SPONGY, Effect.GOO, 130)
            .addVariety("itchy", fill(new Color(178, 226, 3)))
            .addVariety("bitchy", fill(new Color(129, 229, 94)));

    static final Substance Dirt = new Substance(TEST,"Dirt", SOLID_GRANULAR, Effect.SOLID_GRANULAR, 600)
            .addVariety("chocolate", fill(new Color(213, 151, 75)))
            .addVariety("khaki", fill(new Color(221, 180, 128)))
            .addVariety("light", fill(new Color(255, 237, 206)))
            .addVariety("dust", fill(new Color(197, 175, 125)));

    static final Substance LimeStone = new Substance(TEST, "Lime Stone", SOLID_CRYSTALLINE, Effect.SOLID, 800)
            .addVariety("a", pattern(new Color(167, 166, 171, 255), new Color(198, 203, 209, 255)));

    static final Substance Nitre = new Substance(TEST, "Nitre", SOLID_GRANULAR, Effect.SOLID_GRANULAR, 200)
            .addVariety("a", 2, fill(new Color(245, 120, 82)))
            .addVariety("b", fill(new Color(154, 76, 82)))
            .addVariety("c", fill(new Color(253, 160, 113)));

    static final Substance Coal = new Substance(TEST, "Coal", SOLID_GRANULAR, Effect.SOLID_GRANULAR, 400)
            .addVariety("a", fill(new Color(121, 96, 74)))
            .addVariety("c", fill(new Color(134, 111, 85)))
            .addVariety("b", fill(new Color(87, 68, 54)));

    // a pixel that caught fire never goes back
    static final Substance Fire = new Substance(TEST, "Fire", SOLID_CRYSTALLINE, Effect.SOLID_GRANULAR, 200)
            .addVariety("a", Animation.sequence(19,
                    new Color(253, 249, 207),
                    new Color(254, 246, 96),
                    new Color(250, 177, 90),
                    new Color(253, 130, 36)
            ))
            .addVariety("a", Animation.sequence(41,
                    new Color(254, 246, 96),
                    new Color(253, 130, 36),
                    new Color(250, 177, 90),
                    new Color(253, 249, 207)
            ))
            .addVariety("a", Animation.sequence(29,
                    new Color(253, 130, 36),
                    new Color(250, 177, 90),
                    new Color(253, 249, 207),
                    new Color(254, 246, 96)
            ))
            .addVariety("a", Animation.sequence(11,
                    new Color(253, 249, 207),
                    new Color(253, 130, 36),
                    new Color(254, 246, 96),
                    new Color(250, 177, 90)
            ))
            ;

    private int nextId = 0;
    private final Substance[] materialsById = new Substance[128];

    private Substances() {
    }

    public int nextId() {
        return nextId++;
    }

    public void add(Substance m) {
        materialsById[m.id] = m;
    }

    public Substance byId(int id) {
        return materialsById[id];
    }

    public int count() {
        return nextId;
    }
}
