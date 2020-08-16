package se.jbee.wiggle;

import java.util.function.Predicate;

import static se.jbee.wiggle.Substances.Poison;
import static se.jbee.wiggle.Substances.Water;

/**
 * Encodes a {@link Substance}'s behaviour in the {@link World} pixel by pixel.
 *
 * Usually each pixel looks at a few if its direct neighbours to determine if and where it wants to move.
 *
 * Some behaviours also morph or transform themselves or neighbouring pixels.
 */
public interface Effect {

    /**
     * Simulates a single pixel.
     *
     * @param x the x coordinate of the pixel to simulate
     * @param y the y coordinate of the pixel to simulate
     * @param world the game world with the pixel data
     * @param dx the direction in which the simulation processes pixels horizontally
     * @return true if the pixel moved, else false
     */
    boolean applyTo(Substance cell, int x, int y, World world, int dx);

    default Effect and(Effect effect) {
        // this is important to use | (single) so that the effect is executed second but also executed in any case
        return (cell, x, y, world, dx) -> this.applyTo(cell, x,y,world,dx) | effect.applyTo(cell, x,y,world,dx);
    }

    static Effect transforms(Substance to, Predicate<Substance> when) {
        return (cell, x,y,world,dx) -> {
            final Substance m = to != null ? to : cell;
            if (world.rng.nextChance(50))
                return false;
            if (x < world.width - 1) {
                Substance target = world.substanceAt(x+1, y);
                if (when.test(target))
                    world.replaceAt(x + 1, y, m);
            }
            if (x > 0) {
                Substance target = world.substanceAt(x-1, y);
                if (when.test(target))
                    world.replaceAt(x - 1, y, m);
            }
            if (y > 0) {
                Substance target = world.substanceAt(x, y - 1);
                if (when.test(target))
                    world.replaceAt(x ,y-1, m);
            }
            if (y < world.height-1) {
                Substance target = world.substanceAt(x, y + 1);
                if (when.test(target))
                    world.replaceAt(x, y+1, m);
            }
            return false;
        };
    }

    static Effect pollutes(Predicate<Substance> when) {
        return transforms(null, when);
    }

    static Effect becomes(Substance other, int permille) {
        return (cell, x, y, world, dx) -> {
        if (world.rng.nextPermille(permille))
                world.replaceAt(x,y, other);
            return false;
        };
    }

    static Effect becomesNeighboring(int permille) {
        return (cell, x, y, world, dx) -> {
            Substance target = null;
            Substance left = x <= 0 ? null : world.substanceAt(x - 1, y);
            Substance right = x >= world.width-1 ? null : world.substanceAt(x+1, y);
            if (left != null && left == right && left.phase == cell.phase)
                target = left;
            if (target != null && world.rng.nextPermille(permille))
                world.replaceAt(x,y, target);
            return false;
        };
    }

    Effect HEALIUM = (healium, x,y,world, dx) -> {
        Substance top = world.substanceAt(x,y-1);
        Substance bottom = world.substanceAt(x,y+1);
        Substance left = world.substanceAt(x-1, y);
        Substance right = world.substanceAt(x+1, y);
        if (top == Poison)
            world.replaceAt(x,y-1, healium);
        if (bottom == Poison)
            world.replaceAt(x,y+1, healium);
        if (left == Poison)
            world.replaceAt(x-1,y, healium);
        if (right == Poison)
            world.replaceAt(x+1,y, healium);
        if (!bottom.phase.isFluid() && bottom != healium)
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

}
