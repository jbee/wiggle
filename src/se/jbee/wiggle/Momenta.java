package se.jbee.wiggle;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Momenta implements Iterable<Momentum> {

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
            mask |= m.asGameCell;
        return mask;
    }

    public boolean isNone() {
        return momenta == 0;
    }

    public boolean is(Momentum momentum) {
        return (momenta & momentum.asGameCell) > 0;
    }

    public boolean isLeft() {
        return is(Momentum.LEFT);
    }

    public boolean isRight() {
        return is(Momentum.RIGHT);
    }

    public boolean isUp() {
        return is(Momentum.UP);
    }

    public boolean isDown() {
        return is(Momentum.DOWN);
    }

    public int toGameCell() {
        return momenta;
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
        private int nextIndex = 0;

        @Override
        public boolean hasNext() {
            return left > 0;
        }

        @Override
        public Momentum next() {
            if (left <= 0)
                throw new NoSuchElementException();
            for (int i = nextIndex; i < MOMENTA.length; i++) {
                Momentum m = MOMENTA[i];
                if (is(m)) {
                    nextIndex =i+1;
                    left--;
                    return m;
                }
            }
            throw new NoSuchElementException();
        }
    }
}
