package se.jbee.pixels;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public final class Momenta implements Iterable<Momentum> {

    public static final Momenta NONE = new Momenta(0);
    public static final Momenta JUST_LEFT = new Momenta(Momentum.LEFT);
    public static final Momenta JUST_RIGHT = new Momenta(Momentum.RIGHT);
    public static final Momenta JUST_DOWN = new Momenta(Momentum.DOWN);

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
        return (momenta & (1 << index)) > 0;
    }

    public int toGameCell() {
        return momenta << 16;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Momentum m : this)
            str.append(m.name()).append(',');
        return str.toString();
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
                    return MOMENTA[index++];
                }
                index++;
            }
            throw new NoSuchElementException();
        }
    }
}
