package se.jbee.wiggle.engine;

import java.util.function.Supplier;

/**
 * Each {@link Substance} comes in one or more {@link Variety}s.
 *
 * In the simplest case this is used to have different {@link Animation}s for the same {@link Substance}.
 */
public final class Variety {

    /**
     * As the actual {@link Substance} references changes when further {@link Variety} are added the final {@link Substance} has to be resolved lazily.
     */
    private final Supplier<Substance> substance;
    /**
     * Back reference to this {@link Variety}'s {@link Substance}, uses the 1st byte (from right)
     */
    public final byte substanceId;
    /**
     * Use the 2nd byte (from right) of the game cell data int.
     */
    public final byte variantId;
    public final String name;
    /**
     * How many parts of this {@link Variety} occur in the {@link Substance}. Higher means  more.
     * Default is 1 part. The sum of all occurrences of a substance's varieties defines 100%.
     */
    public final int occurrence;
    public final Animation animation;

    public Variety(Supplier<Substance> substance, int variantId, String name, int occurrence, Animation animation) {
        this.substance = substance;
        this.substanceId = substance.get().substanceId;
        this.variantId = (byte) variantId;
        this.name = name;
        this.occurrence = occurrence;
        this.animation = animation;
    }

    public Substance substance() {
        return substance.get();
    }

    public boolean isAnimated() {
        return animation != null;
    }

    public int asGameCell() {
        return (variantId << 8) + substanceId;
    }
}
