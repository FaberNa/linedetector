package org.github.catapano.linedetector.domain;

import org.github.catapano.linedetector.model.Point;

/**
 * Canonical, integer representation of an infinite 2D line.
 */
public record LineKey(long A, long B, long C) {

    public LineKey {
        // 1) Reduce by gcd to remove common factors.
        long gcd = gcd3(Math.abs(A), Math.abs(B), Math.abs(C));
        if (gcd != 0) {
            A /= gcd;
            B /= gcd;
            C /= gcd;
        }

        // 2) Normalize sign so the same geometric line always produces the same triple.
        //    We prefer a positive leading coefficient.
        if (A < 0 || (A == 0 && B < 0) || (A == 0 && B == 0 && C < 0)) {
            A = -A;
            B = -B;
            C = -C;
        }

    }

    /**
     * Builds a canonical line from two distinct points.
     *
     */
    public static LineKey fromPoints(Point p1, Point p2) {
        // Note: If p1 == p2, this produces the degenerate triple (0,0,0). This is not a valid line, but we can ignore this case since the caller only calls fromPoints on distinct points.
        long a = p2.y() - p1.y();
        long b = p1.x() - p2.x();
        long c = -(a * p1.x() + b * p1.y());
        return new LineKey(a, b, c);
    }



    /** GCD (greatest common divisor) via Euclid. Example: gcd(18,12)=6. */
    private static long gcd(long a, long b) {
        while (b != 0) {
            long r = a % b;
            a = b;
            b = r;
        }
        return a;
    }

    private static long gcd3(long a, long b, long c) {
        return gcd(gcd(a, b), c);
    }
}