package se.jbee.wiggle;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;

public final class Substance {

    public final Substances substances;
    public final String name;
    public final byte substanceId;
    public final Phase phase;
    public final Effect effect;
    public final int density;
    private final Variety[] varieties;
    private final int totalOccurrence;
    // whenIgnited => Effect for what happens when ignited, having an Effect also means it is combustible
    // whenExploding => Effect for what happens when exploding

    // TODO "life-time" or change of behaviour after certain time can be done as part of the simulation based on chance.
    //  This means no state has to be kept to track if it is time for the change. it just happens on time on average.
    // for example a fire that burns out

    //TODO the simulation effect might be slower (like diffuse body in water vs. dense body in water) then every turn
    //     also some fluids might mix very slow due to similar density

    public Substance(Substances substances, String name, Phase phase, Effect effect, int density) {
        this(substances, substances.nextId(), name, phase, effect, density);
    }

    private Substance(Substances substances, int substanceId, String name, Phase phase, Effect effect, int density, Variety... varieties) {
        this.substances = substances;
        this.name = name;
        this.substanceId = (byte) substanceId;
        this.phase = phase;
        this.effect = effect;
        this.density = density;
        this.varieties = varieties;
        this.totalOccurrence = asList(varieties).stream().mapToInt(e -> e.occurrence).sum();
        substances.add(this);
    }

    public Substance addVariety(String name, Animation animation) {
        return addVariety(name, 1, animation);
    }

    public Substance addVariety(String name, int occurrence, Animation animation) {
        Variety[] newVarieties = copyOf(varieties, varieties.length + 1);
        newVarieties[varieties.length] = new Variety(
                () -> substances.byId(substanceId), varieties.length, name, occurrence, animation);
        return new Substance(substances, substanceId, this.name, phase, effect, density, newVarieties);
    }

    public boolean isEffectless() {
        return effect == null;
    }

    public boolean displaces(Substance other) {
        return density > other.density;
    }

    public Variety variety(int n) {
        return varieties[n];
    }

    public Variety variety(RNG rng) {
        if (varieties.length == 1)
            return varieties[0];
        int index = 0;
        if (totalOccurrence == varieties.length) {
            return varieties[rng.nextInt(0, totalOccurrence -1)];
        }
        int occur = rng.nextInt(0, totalOccurrence);
        for (int i = 0; i < varieties.length; i++) {
            if (occur <= varieties[i].occurrence)
                return varieties[i];
            occur -= varieties[i].occurrence;
        }
        return varieties[0]; // error but...
    }

    @Override
    public String toString() {
        return "Material{" +
                "name='" + name + '\'' +
                '}';
    }

}
