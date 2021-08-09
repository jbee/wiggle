package se.jbee.wiggle;

import java.awt.*;

public class RGBA {

    public static int alpha(int rgba) {
        return rgba >> 24 & 255;
    }

    public static int red(int rgba) {
        return rgba >> 16 & 255;
    }

    public static int green(int rgba) {
        return rgba >> 8 & 255;
    }

    public static int blue(int rgba) {
        return rgba >> 0 & 255;
    }

    public static int blend(int top, int bottom) {
        int a = alpha(top);
        if (a == 255)
            return top;
        if (a == 0)
            return bottom;
        int r = (red(top) * a + red(bottom) * (255 - a)) / 255;
        int g = (green(top) * a + green(bottom) * (255 - a)) / 255;
        int b = (blue(top) * a + blue(bottom) * (255 - a)) / 255;
        return 255 << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0;
    }
}
