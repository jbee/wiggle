package se.jbee.pixels;

import java.awt.*;

public final class MaterialVariant {

    public final Material material;
    public final byte variantId;
    public final String name;
    private final Color[] colors;
    private final int animationSpeed;

    // TODO occurrence, how big is the chance for a variant to occur naturally

    public MaterialVariant(Material material, int variantId, String name, int animationSpeed, Color... colors) {
        this.material = material;
        this.variantId = (byte) variantId;
        this.name = name;
        this.animationSpeed = animationSpeed;
        this.colors = colors;
    }

    public boolean isPainted() {
        return colors.length > 0;
    }

    //TODO textures can be done with colour array, modulo and if this method also has x,y

    public int getRGB(int frame) {
        return colors[(frame / animationSpeed) % colors.length].getRGB();
    }

    public int toGameCell() {
        return (variantId << 8) + material.id;
    }
}
