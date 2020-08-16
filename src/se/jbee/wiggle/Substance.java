package se.jbee.wiggle;

import java.util.Arrays;

import static java.util.Arrays.asList;

public final class Substance {

    public final Substances substances;
    public final String name;
    public final byte id;
    public final MaterialGroup group;
    public final Effect effect;
    public final int density;
    private final SubstanceVariety[] variants;
    private final int totalOccurrence;
    // flammable
    // combustible

    // TODO "life-time" or change of behaviour after certain time can be done as part of the simulation based on chance.
    //  This means no state has to be kept to track if it is time for the change. it just happens on time on average.
    // for example a fire that burns out

    //TODO the simulation effect might be slower (like diffuse body in water vs. dense body in water) then every turn
    //     also some fluids might mix very slow due to similar density

    public Substance(Substances substances, String name, MaterialGroup group, Effect effect, int density) {
        this(substances, substances.nextId(), name, group, effect, density);
    }

    private Substance(Substances substances, int id, String name, MaterialGroup group, Effect effect, int density, SubstanceVariety... variants) {
        this.substances = substances;
        this.name = name;
        this.id = (byte) id;
        this.group = group;
        this.effect = effect;
        this.density = density;
        this.variants = variants;
        this.totalOccurrence = asList(variants).stream().mapToInt(e -> e.occurrence).sum();
        substances.add(this);
    }

    public Substance addVariety(String name, Animation animation) {
        return addVariety(name, 1, animation);
    }

    public Substance addVariety(String name, int occurrence, Animation animation) {
        SubstanceVariety[] variants = Arrays.copyOf(this.variants, this.variants.length + 1);
        variants[this.variants.length] = new SubstanceVariety(() -> substances.byId(id), this.variants.length, name, occurrence, animation);
        return new Substance(substances, id, this.name, group, effect, density, variants);
    }

    public boolean isSimulated() {
        return effect != null;
    }

    public boolean displaces(Substance other) {
        return density > other.density;
    }

    public SubstanceVariety variety(int n) {
        return variants[n];
    }

    public SubstanceVariety variety(Rnd rnd) {
        if (variants.length == 1)
            return variants[0];
        int index = 0;
        if (totalOccurrence == variants.length) {
            return variants[rnd.nextInt(0, totalOccurrence -1)];
        }
        int occur = rnd.nextInt(0, totalOccurrence);
        for (int i = 0; i < variants.length; i++) {
            if (occur <= variants[i].occurrence)
                return variants[i];
            occur -= variants[i].occurrence;
        }
        return variants[0]; // error but...
    }

    @Override
    public String toString() {
        return "Material{" +
                "name='" + name + '\'' +
                '}';
    }

    public boolean isFluid() {
        return group == MaterialGroup.FLUID;
    }

    public boolean isSolid() {
        return group == MaterialGroup.SOLID_CRYSTALLINE || group == MaterialGroup.SOLID_GRANULAR;
    }
}
