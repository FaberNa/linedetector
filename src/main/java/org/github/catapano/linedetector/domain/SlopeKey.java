package org.github.catapano.linedetector.domain;


public record SlopeKey(long dx, long dy) {

    public static SlopeKey of(long dx, long dy) {
        if (dx == 0 && dy == 0) throw new IllegalArgumentException("duplicate point");
        if (dx == 0) return new SlopeKey(0, 1);      // vertical
        if (dy == 0) return new SlopeKey(1, 0);      // horizontal

        long g = gcd(Math.abs(dx), Math.abs(dy));
        dx /= g;
        dy /= g;

        // normalize sign: make dx positive; if dx negative flip both
        if (dx < 0) {
            dx = -dx;
            dy = -dy;
        }
        return new SlopeKey(dx, dy);
    }


    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
}