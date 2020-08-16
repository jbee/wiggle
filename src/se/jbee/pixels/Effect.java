package se.jbee.pixels;

import java.util.function.Predicate;

import static se.jbee.pixels.Substances.Poison;
import static se.jbee.pixels.Substances.Water;

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

    /**
     * Go DOWN
     */
    Effect SOLID = (solid, x, y, world, dx) -> {
        if (y >= world.height - 1)
            return false;
        if (solid.displaces(world.substanceAt(x, y +1)))
            return world.swap(x, y, 0, 1);
        return false;
    };

    static Effect transforms(Substance to, Predicate<Substance> when) {
        return (cell, x,y,world,dx) -> {
            final Substance m = to != null ? to : cell;
            if (world.rnd.nextChance(50))
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
        if (world.rnd.nextPermille(permille))
                world.replaceAt(x,y, other);
            return false;
        };
    }

    static Effect becomesNeighboring(int permille) {
        return (cell, x, y, world, dx) -> {
            Substance target = null;
            Substance left = x <= 0 ? null : world.substanceAt(x - 1, y);
            Substance right = x >= world.width-1 ? null : world.substanceAt(x+1, y);
            if (left != null && left == right && left.group == cell.group)
                target = left;
            if (target != null && world.rnd.nextPermille(permille))
                world.replaceAt(x,y, target);
            return false;
        };
    }

    /**
     * Go DOWN, DOWN LEFT or DOWN RIGHT
     */
    Effect SOLID_GRANULAR = (solid, x, y, world, dx) -> {
        // need to fall?
        if (y >= world.height - 1)
            return false;
        if (solid.displaces(world.substanceAt(x, y +1))) { // fall
            // some random fast falling
            if (solid.displaces(world.substanceAt(x, y +2)) && world.rnd.nextChance(30))
                return world.swap(x, y, 0, 2);
            return world.swap(x, y, 0, 1);
        }
        boolean canGoLeft = solid.displaces(world.substanceAt(x - 1, y + 1));
        boolean canGoRight = solid.displaces(world.substanceAt(x+1, y+1));
        if (canGoLeft && canGoRight)
            return world.swap(x, y, world.randomDx(), 1);
        if (canGoLeft)
            return world.swap(x, y, -1, +1);
        if (canGoRight)
            return world.swap(x, y, 1, 1);
        return false;
    };

    Effect POWDER = (powder, x,y, world, dx) -> {
        // need to fall?
        if (y >= world.height - 1)
            return false;
        Momenta momenta = world.momentaAt(x,y);
        if (momenta.isDown()) {
            world.calm(x,y);
            return false;
        }
        if (powder.displaces(world.substanceAt(x,y+1))) {
            if (powder.displaces(world.substanceAt(x, y +2)) && world.rnd.nextChance(1))
                world.swap(x, y, 0, 2);
            return world.swap(x, y, 0, 1);
        }
        boolean canGoLeft = powder.displaces(world.substanceAt(x - 1, y + 1));
        boolean canGoRight = powder.displaces(world.substanceAt(x+1, y+1));
        if (canGoLeft && canGoRight)
            return world.swap(x, y, world.randomDx(), 1);
        if (canGoLeft)
            return world.swap(x, y, -1, +1);
        if (canGoRight)
            return world.swap(x, y, 1, 1);
        return false;
    };

    /**
     * Go DOWN, LEFT or RIGHT
     */
    Effect FLUID = (fluid, x, y, world, dx) -> {
        if (y >= world.height - 1)
            return false;

        // down (1 or 2)?
        if (fluid.displaces(world.substanceAt(x, y + 1))) {
            if (fluid.displaces(world.substanceAt(x, y +2)) && world.rnd.nextChance(20))
                world.swap(x, y, 0, 2);
            return world.swap(x, y, 0, 1);
        }

        // left or right?
        boolean canGoLeft = x > 0 && fluid.displaces(world.substanceAt(x - 1, y));
        boolean canGoRight = x < world.width - 1 && fluid.displaces(world.substanceAt(x + 1, y));
        if (!canGoLeft && !canGoRight) {
            world.calm(x,y);
            return false;
        }
        Momenta momenta = world.momentaAt(x, y);
        if (canGoLeft && canGoRight) {
            if (fluid.displaces(world.substanceAt(x-dx,y+1)))
                return world.swap(x,y,-dx,1);
            if (momenta.isNone() || (!momenta.isLeft() && !momenta.isRight()))
                return false;
            // we have a left or right momentum
            if (x >= 2 && fluid == world.substanceAt(x-2, y) || x < world.width - 2 && fluid == world.substanceAt(x+2, y))
                world.calm(x, y);
            return false;
        }
        if (!canGoLeft && momenta.isLeft()) {
            // right down?
            if (fluid.displaces(world.substanceAt(x+1,y+1)))
                return world.swap(x,y,1,1);
            return false;
        }
        if (!canGoRight && momenta.isRight()) {
            // left down?
            if (fluid.displaces(world.substanceAt(x - 1, y + 1)))
                return world.swap(x, y, -1, 1);
            return false;
        }
        boolean canGoLeft2 = canGoLeft && x > 1 && fluid.displaces(world.substanceAt(x-2, y));
        if (momenta.isLeft() && canGoLeft)
            return world.swap(x, y, canGoLeft2 ? -2 : -1, 0);
        boolean canGoRight2 = canGoRight && x < world.width -2 && fluid.displaces(world.substanceAt(x+2, y));
        if (momenta.isRight() && canGoRight)
            return world.swap(x, y, canGoRight2 ? +2 : +1, 0);
        return world.swap(x, y, canGoLeft ? -1 : 1, 0);
    };

    Effect GAS = (gas, x, y, world, dx) -> {
        if (world.loopCount() % 3 == 0) // OBS: careful with skipping even number of loops as that means a tendency to one side, therefore we use 3
            return false;
        Momenta momenta = world.momentaAt(x,y);
        if (momenta.isUp()) {
            world.calm(x,y);
            return false; // this prevents pixel from to top in a single run as we never move up 2 times in a row
        }
        if (y > 0) {
            if (gas.displaces(world.substanceAt(x, y-1))) {
                if (y > 1 && gas.displaces(world.substanceAt(x, y - 2)) && world.rnd.nextChance(20))
                    world.swap(x, y, 0, - 2);
                return world.swap(x, y, 0, -1);
            }
        }
        boolean canGoLeft = x > 0 && gas.displaces(world.substanceAt(x - 1, y));
        boolean canGoRight = x < world.width - 1 && gas.displaces(world.substanceAt(x + 1, y));
        if (!canGoLeft && !canGoRight) {
            if (!momenta.isNone())
                world.calm(x,y);
            return false;
        }
        if (true && world.rnd.nextChance(60)) // This essentially controls how fast the gas moves left, right, lower means faster
            return false;
        if (canGoLeft && canGoRight)
            return world.swap(x,y, world.randomDx(), 0);
        if (canGoLeft)
            return world.swap(x,y, -1, 0);
        if (canGoRight)
            return world.swap(x,y, +1, 0);
        return false;
    };

    /**
     * Go DOWN, LEFT or RIGHT
     */
    Effect GOO = (goo, x, y, world, dx) -> {
        if (y >= world.height - 1)
            return false;

        // down (1 or 2)?
        if (goo.displaces(world.substanceAt(x, y + 1))) {
            if (goo.displaces(world.substanceAt(x, y +2)) && world.rnd.nextChance(1))
                world.swap(x, y, 0, 2);
            return world.swap(x, y, 0, 1);
        }

        // left or right?
        boolean canGoLeft = x > 0 && goo.displaces(world.substanceAt(x - 1, y));
        boolean canGoRight = x < world.width - 1 && goo.displaces(world.substanceAt(x + 1, y));
        if (!canGoLeft && !canGoRight) {
            world.calm(x,y);
            return false;
        }
        Momenta momenta = world.momentaAt(x, y);
        if (canGoLeft && canGoRight) {
            if (goo.displaces(world.substanceAt(x-dx,y+1)))
                return world.swap(x,y,-dx,1);
            if (momenta.isNone() || (!momenta.isLeft() && !momenta.isRight()))
                return false;
            // we have a left or right momentum
            if (x >= 2 && goo == world.substanceAt(x-2, y) || x < world.width - 2 && goo == world.substanceAt(x+2, y)) {
                world.calm(x, y);
            }
            if (x >= 3 && goo == world.substanceAt(x-3, y) || x < world.width - 3 && goo == world.substanceAt(x+3, y)) {
                if (momenta.isRight())
                    return world.swap(x,y, -1, 0);
                if (momenta.isLeft())
                    return world.swap(x,y, +1, 0);
            }
            return false;
        }
        if (!canGoLeft && momenta.isLeft()) {
            // right down?
            if (goo.displaces(world.substanceAt(x+1,y+1)))
                return world.swap(x,y,1,1);
            return false;
        }
        if (!canGoRight && momenta.isRight()) {
            // left down?
            if (goo.displaces(world.substanceAt(x - 1, y + 1)))
                return world.swap(x, y, -1, 1);
            return false;
        }
        boolean canGoLeft2 = canGoLeft && x > 1 && goo.displaces(world.substanceAt(x-2, y));
        if (momenta.isLeft() && canGoLeft)
            return world.swap(x, y, canGoLeft2 ? -2 : -1, 0);
        boolean canGoRight2 = canGoRight && x < world.width -2 && goo.displaces(world.substanceAt(x+2, y));
        if (momenta.isRight() && canGoRight)
            return world.swap(x, y, canGoRight2 ? +2 : +1, 0);
        return world.swap(x, y, canGoLeft ? -1 : 1, 0);
    };

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
        if (!bottom.isFluid() && bottom != healium)
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
        if (world.rnd.nextChance(c*25))
            world.replaceAt(x,y, Water);
        return false;
    };

}
