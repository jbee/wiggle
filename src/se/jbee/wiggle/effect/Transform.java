package se.jbee.wiggle.effect;

import se.jbee.wiggle.engine.Effect;
import se.jbee.wiggle.engine.Substance;

import java.util.function.Predicate;

import static se.jbee.wiggle.game.WiggleWobble.Poison;
import static se.jbee.wiggle.game.WiggleWobble.Water;

public final class Transform {

    private Transform() {
        throw new UnsupportedOperationException("util");
    }

    public static Effect transforms(Substance to, Predicate<Substance> when) {
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

    public static Effect pollutes(Predicate<Substance> when) {
        return transforms(null, when);
    }

    public static Effect becomes(Substance other, int permille) {
        return (cell, x, y, world, dx) -> {
        if (world.rng.nextPermille(permille))
                world.replaceAt(x,y, other);
            return false;
        };
    }

    public static Effect becomesNeighboring(int permille) {
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
}
