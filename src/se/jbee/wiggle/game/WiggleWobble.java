package se.jbee.wiggle.game;

import se.jbee.wiggle.effect.Nature;
import se.jbee.wiggle.engine.Animation;
import se.jbee.wiggle.engine.Effect;
import se.jbee.wiggle.engine.Substance;
import se.jbee.wiggle.engine.Substances;

import java.awt.*;

import static se.jbee.wiggle.effect.Transform.becomes;
import static se.jbee.wiggle.effect.Transform.pollutes;
import static se.jbee.wiggle.engine.Animation.fill;
import static se.jbee.wiggle.engine.Animation.pattern;
import static se.jbee.wiggle.engine.Phase.*;

/**
 * Defines the {@link Substance}s used in the on-going experiment of a wiggly wobbly world.
 */
public interface WiggleWobble {

    Substances SUBSTANCES = new Substances();

    Substance Air = new Substance(SUBSTANCES,"Air", GASSY, null, 0)
            .addVariety("thin", null);

    Substance Steam = new Substance(SUBSTANCES, "Steam", GASSY, Nature.GAS.and(becomes(Air, 1)), 10)
            .addVariety("water", fill(new Color(184, 227, 232)));

    Substance Water = new Substance(SUBSTANCES,"Water", FLUID, Nature.FLUID, 100)
            .addVariety("sweet", fill(new Color(9, 125, 156, 174)));

    Substance Poison = new Substance(SUBSTANCES, "Poison", FLUID, Nature.FLUID.and(pollutes(other -> other == Water)), 100)
            .addVariety("week", fill(new Color(184, 216, 22))); //, new Color(214, 254, 29)

    Effect Cleanse = (cleansium, x, y, world, dx) -> {
        Substance top = world.substanceAt(x,y-1);
        Substance bottom = world.substanceAt(x,y+1);
        Substance left = world.substanceAt(x-1, y);
        Substance right = world.substanceAt(x+1, y);
        if (top == Poison)
            world.replaceAt(x,y-1, cleansium);
        if (bottom == Poison)
            world.replaceAt(x,y+1, cleansium);
        if (left == Poison)
            world.replaceAt(x-1,y, cleansium);
        if (right == Poison)
            world.replaceAt(x+1,y, cleansium);
        if (!bottom.phase.isFluid() && bottom != cleansium)
            return false;
        int c = 0;
        if (left == Poison || left == Water)
            c++;
        if (right == Poison || right == Water)
            c++;
        if (top == Poison || top == Water)
            c++;
        if (bottom == Poison || bottom == Water)
            c++;
        if (c == 0)
            return false;
        if (world.rng.nextChance(c*25))
            world.replaceAt(x,y, Water);
        return false;
    };

    Substance Cleansium = new Substance(SUBSTANCES, "Cleansium", FLAKY, Nature.POWDER.and(Cleanse), 101)
            .addVariety("bright", Animation.shuffle(10,
                    new Color(231, 131, 241),
                    new Color(224, 117, 234),
                    new Color(231, 131, 241),
                    new Color(238, 182, 243),
                    new Color(246, 194, 252)));

    // a pixel that caught fire never goes back
    Substance Fire = new Substance(SUBSTANCES, "Fire", SOLID_CRYSTALLINE, Nature.SOLID_GRANULAR, 200)
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

    Substance Coal = new Substance(SUBSTANCES, "Coal", SOLID_GRANULAR, Nature.SOLID_GRANULAR, 400)
            .addVariety("a", fill(new Color(121, 96, 74)))
            .addVariety("c", fill(new Color(134, 111, 85)))
            .addVariety("b", fill(new Color(87, 68, 54)));

    Substance Nitre = new Substance(SUBSTANCES, "Nitre", SOLID_GRANULAR, Nature.SOLID_GRANULAR, 200)
            .addVariety("a", 2, fill(new Color(245, 120, 82)))
            .addVariety("b", fill(new Color(154, 76, 82)))
            .addVariety("c", fill(new Color(253, 160, 113)));

    Substance LimeStone = new Substance(SUBSTANCES, "Lime Stone", SOLID_CRYSTALLINE, Nature.SOLID, 800)
            .addVariety("a", pattern(new Color(167, 166, 171, 255), new Color(198, 203, 209, 255)));

    Substance Dirt = new Substance(SUBSTANCES,"Dirt", SOLID_GRANULAR, Nature.SOLID_GRANULAR, 600)
            .addVariety("chocolate", fill(new Color(213, 151, 75)))
            .addVariety("khaki", fill(new Color(221, 180, 128)))
            .addVariety("light", fill(new Color(255, 237, 206)))
            .addVariety("dust", fill(new Color(197, 175, 125)));

    Substance Slime = new Substance(SUBSTANCES,"Soylent Green", GOOEY, Nature.GOO, 130)
            .addVariety("itchy", fill(new Color(178, 226, 3)))
            .addVariety("bitchy", fill(new Color(129, 229, 94)));

    Substance Oil = new Substance(SUBSTANCES,"Oil", FLUID, Nature.FLUID, 40)
            .addVariety("sticky", fill(new Color(194, 76, 36)));

    Substance HardRock = new Substance(SUBSTANCES,"Hard Rock", SOLID_CRYSTALLINE, null, 1000)
            .addVariety("dense",fill(new Color(139, 94, 96)));

}
