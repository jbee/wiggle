package se.jbee.pixels;

import java.awt.*;

public final class MaterialVariant {

    public final Material material;
    public final byte variantId;
    public final String name;
    private final Color color;
    //TODO animation: different color dependent on the frame #

    public MaterialVariant(Material material, int variantId, String name, Color color) {
        this.material = material;
        this.variantId = (byte) variantId;
        this.name = name;
        this.color = color;
    }

    public boolean isPainted() {
        return color != null;
    }

    public int getRGB(int frame) {
        return color.getRGB();
    }

    public int toGameCell() {
        return (variantId << 8) + material.id;
    }
}
