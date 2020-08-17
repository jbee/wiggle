package se.jbee.wiggle.engine;

import java.awt.*;

public interface Animation {

    int rgba(int x, int y, World world, long frame);

    public static Animation fill(Color c) {
        int rgb = c.getRGB();
        return (x,y, world, frame) -> rgb;
    }

    public static Animation pattern(Color...cs) {
        if (cs.length == 1)
            return fill(cs[0]);
        final int[] rgbs = rgbs(cs);
        return (x, y, world, frame) -> rgbs[x % rgbs.length];
    }

    public static Animation sequence(int speed, Color...cs) {
        if (cs.length == 1)
            return fill(cs[0]);
        final int[] rgbs = rgbs(cs);
        return (x,y, world, frame) -> rgbs[(int) ((frame / speed) % rgbs.length)];
    }

    public static Animation blink(int speed, int baseOccurrence, Color base, int highlightOccurrence, Color highlight) {
        return (x, y, world, frame) -> {
            int test = (int) (x+y+frame % 100);
            return world.rng.nextInt(0, baseOccurrence+highlightOccurrence) < highlightOccurrence
                    ? highlight.getRGB()
                    : base.getRGB();
        };
    }

    public static Animation shuffle(int speed, Color...cs) {
        if (cs.length == 1)
            return fill(cs[0]);
        final int[] rgbs = rgbs(cs);
        final long[] frameGroup = new long[1];
        final int[][] rowRGB = new int[40][40];
        return (x,y, world, frame) -> {
            long g = frame / speed;
            if (g != frameGroup[0]) {
                frameGroup[0] = g;
                for (int yi = 0; yi < rowRGB.length; yi++)
                    for (int xi = 0; xi < rowRGB[yi].length; xi++)
                        rowRGB[yi][xi] = rgbs[world.rng.nextInt() % rgbs.length];
            }
            return rowRGB[y % 40][x % 40];
        };
    }

    private static int[] rgbs(Color[] cs) {
        int[] rgbs = new int[cs.length];
        for (int i = 0; i < cs.length; i++)
            rgbs[i] = cs[i].getRGB();
        return rgbs;
    }
}
