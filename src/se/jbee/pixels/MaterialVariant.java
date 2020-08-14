package se.jbee.pixels;

import java.awt.*;
import java.util.function.Supplier;

public final class MaterialVariant {

    private final Supplier<Material> material;
    public final byte materialId;
    public final byte variantId;
    public final String name;
    private final Color[] colors;
    private final int animationSpeed;

    // TODO occurrence, how big is the chance for a variant to occur naturally

    public MaterialVariant(Supplier<Material> material, int variantId, String name, int animationSpeed, Color... colors) {
        this.material = material;
        this.materialId = material.get().id;
        this.variantId = (byte) variantId;
        this.name = name;
        this.animationSpeed = animationSpeed;
        this.colors = colors;
    }

    public Material material() {
        return material.get();
    }

    public boolean isPainted() {
        return colors.length > 0;
    }

    //TODO textures can be done with colour array, modulo and if this method also has x,y

    public int getRGB(int frame) {
        return colors[(frame / animationSpeed) % colors.length].getRGB();
    }

    public int toGameCell() {
        return (variantId << 8) + materialId;
    }
}
