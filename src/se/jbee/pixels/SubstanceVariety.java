package se.jbee.pixels;

import java.awt.*;
import java.util.function.Supplier;

public final class SubstanceVariety {

    private final Supplier<Substance> material;
    public final byte materialId;
    public final byte variantId;
    public final String name;
    public final int occurrence;
    public final Animation animation;

    public SubstanceVariety(Supplier<Substance> material, int variantId, String name, int occurrence, Animation animation) {
        this.material = material;
        this.materialId = material.get().id;
        this.variantId = (byte) variantId;
        this.name = name;
        this.occurrence = occurrence;
        this.animation = animation;
    }

    public Substance material() {
        return material.get();
    }

    public boolean isPainted() {
        return animation != null;
    }

    public int toGameCell() {
        return (variantId << 8) + materialId;
    }
}
