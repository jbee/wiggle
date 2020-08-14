package se.jbee.pixels;

import java.awt.*;
import java.util.Arrays;

public final class Material {

    public final WorldMaterials materials;
    public final String name;
    public final byte id;
    public final Simulation simulation;
    public final int density;
    private final MaterialVariant[] variants;

    // flammable
    // combustible

    // TODO "life-time" or change of behaviour after certain time can be done as part of the simulation based on chance.
    //  This means no state has to be kept to track if it is time for the change. it just happens on time on average.
    // for example a fire that burns out

    //TODO the simulation effect might be slower (like diffuse body in water vs. dense body in water) then every turn
    //     also some fluids might mix very slow due to similar density

    public Material(WorldMaterials materials, String name, Simulation simulation, int density) {
        this(materials, materials.nextId(), name, simulation, density);
    }

    private Material(WorldMaterials materials, int id, String name, Simulation simulation, int density, MaterialVariant... variants) {
        this.materials = materials;
        this.name = name;
        this.id = (byte) id;
        this.simulation = simulation;
        this.density = density;
        this.variants = variants;
        materials.add(this);
    }

    public Material addVariant(String name, Color... colors) {
        return addVariant(name, 1, colors);
    }

    public Material addVariant(String name, int annimationSpeed, Color... colors) {
        MaterialVariant[] variants = Arrays.copyOf(this.variants, this.variants.length + 1);
        variants[this.variants.length] = new MaterialVariant(() -> materials.byId(id), this.variants.length, name, annimationSpeed, colors);
        return new Material(materials, id, this.name, simulation, density, variants);
    }

    public boolean isSimulated() {
        return simulation != null;
    }

    public boolean displaces(Material other) {
        return density > other.density;
    }

    public MaterialVariant variant(int n) {
        return variants[n];
    }

    public MaterialVariant variant(Rnd rnd) {
        //TODO also consider occurances of variants
        return variants.length == 1 ? variants[0] : variant(rnd.nextInt(0, variants.length-1));
    }
}
