package org.github.catapano.linedetector.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


class SlopeKeyTest {

    @Test
    void of_shouldThrow_whenDuplicatePointVector() {
        assertThrows(IllegalArgumentException.class, () -> SlopeKey.of(0, 0));
    }

    @ParameterizedTest
    @CsvSource({
            // dx == 0 => vertical, canonical (0,1)
            "0,  5,  0, 1",
            "0, -5,  0, 1",
            "0,  1,  0, 1",
    })
    void of_shouldNormalizeVertical(long dx, long dy, long expDx, long expDy) {
        assertEquals(new SlopeKey(expDx, expDy), SlopeKey.of(dx, dy));
    }

    @ParameterizedTest
    @CsvSource({
            // dy == 0 => horizontal, canonical (1,0)
            " 5, 0,  1, 0",
            "-5, 0,  1, 0",
            " 1, 0,  1, 0",
    })
    void of_shouldNormalizeHorizontal(long dx, long dy, long expDx, long expDy) {
        assertEquals(new SlopeKey(expDx, expDy), SlopeKey.of(dx, dy));
    }

    @ParameterizedTest
    @CsvSource({
            // gcd reduction
            " 6,  9,  2,  3",
            "10,  5,  2,  1",
            "14, 21,  2,  3",

            // already reduced
            " 2,  3,  2,  3",
            " 3,  2,  3,  2",

            // sign normalization: if dx < 0 flip both
            "-6, -9,  2,  3",
            "-6,  9,  2, -3",
            "-2,  3,  2, -3",
    })
    void of_shouldReduceByGcd_andNormalizeSign(long dx, long dy, long expDx, long expDy) {
        assertEquals(new SlopeKey(expDx, expDy), SlopeKey.of(dx, dy));
    }

    @Test
    void equals_shouldWorkForSameReducedSlope() {
        // (6,9) reduces to (2,3)
        SlopeKey a = SlopeKey.of(6, 9);
        SlopeKey b = SlopeKey.of(2, 3);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode(), "Equal keys must have same hashCode (Map key safety)");
    }

    @Test
    void equals_shouldDistinguishDifferentSlopes() {
        SlopeKey a = SlopeKey.of(2, 3);
        SlopeKey b = SlopeKey.of(2, 1);

        assertNotEquals(a, b);
    }

    @Test
    void of_shouldNotFlipWhenDxPositive() {
        // dx positive -> keep dy sign
        assertEquals(new SlopeKey(2, -3), SlopeKey.of(6, -9));
    }
}