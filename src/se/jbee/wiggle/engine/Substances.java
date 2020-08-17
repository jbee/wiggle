package se.jbee.wiggle.engine;

import se.jbee.wiggle.effect.Nature;
import se.jbee.wiggle.effect.Transform;

import java.awt.*;

import static se.jbee.wiggle.engine.Animation.*;
import static se.jbee.wiggle.effect.Transform.becomes;
import static se.jbee.wiggle.effect.Transform.pollutes;
import static se.jbee.wiggle.engine.Phase.*;

/**
 * A set of {@link Substance} that defines all {@link Substance}s used in a {@link World}.
 */
public final class Substances {


    //TODO CRYO => freezes water

    //TODO PLANT => grows in water and soil

    private int nextId = 0;
    private final Substance[] materialsById = new Substance[128];

    public int nextId() {
        return nextId++;
    }

    public void add(Substance m) {
        materialsById[m.substanceId] = m;
    }

    public Substance byId(int id) {
        return materialsById[id];
    }

    public int count() {
        return nextId;
    }
}
