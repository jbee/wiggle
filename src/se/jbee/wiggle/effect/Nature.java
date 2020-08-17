package se.jbee.wiggle.effect;

import se.jbee.wiggle.engine.Effect;
import se.jbee.wiggle.engine.Momenta;
import se.jbee.wiggle.engine.Phase;
import se.jbee.wiggle.engine.Substance;

/**
 * Namespace for {@link Effect}s that simulate different {@link Phase}s.
 * This means how the material behaves generally in the "physical" world.
 *
 * This does not cover special effects that {@link Substance}s might have.
 */
public final class Nature {

    private Nature() {
        throw new UnsupportedOperationException("util");
    }

    /**
     * Go DOWN
     */
    public static final Effect SOLID = (solid, x, y, world, dx) -> {
        if (y >= world.height - 1)
            return false;
        if (solid.displaces(world.substanceAt(x, y +1)))
            return world.swap(x, y, 0, 1);
        return false;
    };
    /**
     * Go DOWN, DOWN LEFT or DOWN RIGHT
     */
    public static final Effect SOLID_GRANULAR = (solid, x, y, world, dx) -> {
        // need to fall?
        if (y >= world.height - 1)
            return false;
        if (solid.displaces(world.substanceAt(x, y +1))) { // fall
            // some random fast falling
            if (solid.displaces(world.substanceAt(x, y +2)) && world.rng.nextChance(30))
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
    public static final Effect POWDER = (powder, x, y, world, dx) -> {
        // need to fall?
        if (y >= world.height - 1)
            return false;
        Momenta momenta = world.momentaAt(x,y);
        if (momenta.isDown()) {
            world.calm(x,y);
            return false;
        }
        if (powder.displaces(world.substanceAt(x,y+1))) {
            if (powder.displaces(world.substanceAt(x, y +2)) && world.rng.nextChance(1))
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
    public static final Effect FLUID = (fluid, x, y, world, dx) -> {
        if (y >= world.height - 1)
            return false;

        // down (1 or 2)?
        if (fluid.displaces(world.substanceAt(x, y + 1))) {
            if (fluid.displaces(world.substanceAt(x, y +2)) && world.rng.nextChance(20))
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
    public static final Effect GAS = (gas, x, y, world, dx) -> {
        if (world.loopCount() % 3 == 0) // OBS: careful with skipping even number of loops as that means a tendency to one side, therefore we use 3
            return false;
        Momenta momenta = world.momentaAt(x,y);
        if (momenta.isUp()) {
            world.calm(x,y);
            return false; // this prevents pixel from to top in a single run as we never move up 2 times in a row
        }
        if (y > 0) {
            if (gas.displaces(world.substanceAt(x, y-1))) {
                if (y > 1 && gas.displaces(world.substanceAt(x, y - 2)) && world.rng.nextChance(20))
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
        if (true && world.rng.nextChance(60)) // This essentially controls how fast the gas moves left, right, lower means faster
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
    public static final Effect GOO = (goo, x, y, world, dx) -> {
        if (y >= world.height - 1)
            return false;

        // down (1 or 2)?
        if (goo.displaces(world.substanceAt(x, y + 1))) {
            if (goo.displaces(world.substanceAt(x, y +2)) && world.rng.nextChance(1))
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
}
