package se.jbee.wiggle.engine;

/**
 * A set of {@link Substance} that defines all {@link Substance}s used in a {@link World}.
 */
public final class Substances {


    //TODO CRYO => freezes water

    //TODO PLANT => grows in water and soil

    private int nextSubstanceId = 0;
    private final Substance[] materialsById = new Substance[128];

    public int nextSubstanceId() {
        return nextSubstanceId++;
    }

    public void add(Substance m) {
        materialsById[m.substanceId] = m;
    }

    public Substance byId(int substanceId) {
        return materialsById[substanceId];
    }

    public int count() {
        return nextSubstanceId;
    }
}
