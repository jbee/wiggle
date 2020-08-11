package se.jbee.pixels;

import java.awt.*;

public class Material {

    public final String name;
    public final byte id;
    public final boolean isSimulated;
    public final Simulation simulation;
    public Color color;

    public Material(String name, int id, Simulation simulation, Color color) {
        this.name = name;
        this.id = (byte) id;
        this.isSimulated = simulation != null;
        this.simulation = simulation;
        this.color = color;
    }

    public boolean isNothing() {
        return id == 0;
    }

    public boolean isPainted() {
        return color != null;
    }
}
