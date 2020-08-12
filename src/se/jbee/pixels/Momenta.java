package se.jbee.pixels;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Momenta implements Iterable<Momentum> {

    public static Momenta NONE = new Momenta(0);

    private final int momenta;

    public Momenta(int momenta) {
        this.momenta = momenta;
    }

    public Momenta(Momentum... momenta) {
        this(bitmask(momenta));
    }

    private static int bitmask(Momentum[] momenta) {
        int mask = 0;
        for (Momentum m : momenta)
            mask |= (1 << m.ordinal());
        return mask;
    }

    public boolean hasMomentum() {
        return momenta != 0;
    }

    public boolean has(Momentum momentum) {
        return has(momentum.ordinal());
    }

    private boolean has(int index) {
        return (momenta & (1 << index)) == 1;
    }

    public int toGameCell() {
        return momenta << 16;
    }

    @Override
    public Iterator<Momentum> iterator() {
        return new MomentumIterator();
    }

    private static final Momentum[] MOMENTA = Momentum.values();
    private final class MomentumIterator implements Iterator<Momentum> {

        private int left = Integer.bitCount(momenta);
        private int index = 0;

        @Override
        public boolean hasNext() {
            return left > 0;
        }

        @Override
        public Momentum next() {
            if (left <= 0)
                throw new NoSuchElementException();
            while (index < MOMENTA.length) {
                if (has(index)) {
                    left--;
                    return MOMENTA[index];
                }
                index++;
            }
            throw new NoSuchElementException();
        }
    }
}
