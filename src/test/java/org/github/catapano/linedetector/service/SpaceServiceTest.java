package org.github.catapano.linedetector.service;

import static org.junit.jupiter.api.Assertions.*;

import org.github.catapano.linedetector.dto.LineDto;
import org.github.catapano.linedetector.dto.PointDto;
import org.github.catapano.linedetector.model.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class SpaceServiceTest {

    private SpaceService sut;

    @BeforeEach
    void setUp() {
        sut = new SpaceService();
    }

    @Test
    void add_shouldAssignIncrementalIds_andAllPointsShouldBeSortedById() {
        sut.add(10, 10);
        sut.add(20, 20);
        sut.add(30, 30);

        List<Point> pts = sut.allPoints();

        assertEquals(3, pts.size());
        assertEquals(1L, pts.get(0).id());
        assertEquals(2L, pts.get(1).id());
        assertEquals(3L, pts.get(2).id());
    }

    @Test
    void clear_shouldRemoveAllPoints() {
        sut.add(1, 1);
        sut.add(2, 2);

        sut.clear();

        assertTrue(sut.allPoints().isEmpty());
        assertTrue(sut.linesAtLeast(2).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1})
    void linesAtLeast_shouldRejectInvalidN(int n) {
        sut.add(0, 0);
        assertThrows(IllegalArgumentException.class, () -> sut.linesAtLeast(n));
    }

    @ParameterizedTest
    @CsvSource({
            "2,1",
            "3,2",
            "4,3"
    })
    void linesAtLeast_shouldReturnEmpty_whenPointsLessThanN(int n, int pointsToAdd) {
        for (int i = 0; i < pointsToAdd; i++) {
            sut.add(i, i);
        }

        List<LineDto> lines = sut.linesAtLeast(n);

        assertNotNull(lines);
        assertTrue(lines.isEmpty());
    }

    @Test
    void linesAtLeast_shouldReturnEmpty_whenNoLineHasAtLeastNPoints() {
        // 4 points, but no 3 collinear
        sut.add(0, 0);
        sut.add(1, 2);
        sut.add(2, 1);
        sut.add(3, 3);

        List<LineDto> lines = sut.linesAtLeast(3);

        assertTrue(lines.isEmpty());
    }

    @Test
    void linesAtLeast_shouldFindHorizontalLine() {
        // y = 5 has 3 points
        var p1 = sut.add(0, 5);
        var p2 = sut.add(1, 5);
        var p3 = sut.add(2, 5);

        // noise
        sut.add(10, 10);

        List<LineDto> lines = sut.linesAtLeast(3);

        assertEquals(1, lines.size());
        assertHasExactlyPoints(lines.getFirst(), Set.of(p1.id(), p2.id(), p3.id()));
    }

    @Test
    void linesAtLeast_shouldFindVerticalLine() {
        // x = 7 has 4 points
        var a = sut.add(7, 0);
        var b = sut.add(7, 1);
        var c = sut.add(7, 2);
        var d = sut.add(7, 3);

        // noise
        sut.add(0, 7);

        List<LineDto> lines3 = sut.linesAtLeast(3);
        assertEquals(1, lines3.size());
        assertHasExactlyPoints(lines3.getFirst(), Set.of(a.id(), b.id(), c.id(), d.id()));

        List<LineDto> lines4 = sut.linesAtLeast(4);
        assertEquals(1, lines4.size());
        assertHasExactlyPoints(lines4.getFirst(), Set.of(a.id(), b.id(), c.id(), d.id()));

        List<LineDto> lines5 = sut.linesAtLeast(5);
        assertTrue(lines5.isEmpty());
    }

    @Test
    void linesAtLeast_shouldFindDiagonalLine() {
        // y = x has 3 points
        var p1 = sut.add(0, 0);
        var p2 = sut.add(1, 1);
        var p3 = sut.add(2, 2);

        // noise
        sut.add(2, 3);
        sut.add(5, 1);

        List<LineDto> lines = sut.linesAtLeast(3);

        assertEquals(1, lines.size());
        assertHasExactlyPoints(lines.getFirst(), Set.of(p1.id(), p2.id(), p3.id()));
    }

    @Test
    void linesAtLeast_shouldReturnMultipleLines_whenDifferentLinesMeetThreshold() {
        // Line 1: y = 0 (3 points)
        var l1a = sut.add(0, 0);
        var l1b = sut.add(1, 0);
        var l1c = sut.add(2, 0);

        // Line 2: x = 9 (3 points)
        var l2a = sut.add(9, 1);
        var l2b = sut.add(9, 2);
        var l2c = sut.add(9, 3);

        // noise
        sut.add(100, 100);

        List<LineDto> lines = sut.linesAtLeast(3);

        assertEquals(2, lines.size());

        // Since ordering of lines isn't guaranteed, we check as sets
        var linePointSets = lines.stream()
                .map(dto -> dto.points().stream().map(PointDto::id).collect(Collectors.toSet()))
                .collect(Collectors.toSet());

        assertTrue(linePointSets.contains(Set.of(l1a.id(), l1b.id(), l1c.id())));
        assertTrue(linePointSets.contains(Set.of(l2a.id(), l2b.id(), l2c.id())));
    }

    @Test
    void linesAtLeast_shouldDeduplicateSameGeometricLineFoundFromDifferentAnchors() {
        // 4 collinear points on y = x
        var p1 = sut.add(0, 0);
        var p2 = sut.add(1, 1);
        var p3 = sut.add(2, 2);
        var p4 = sut.add(3, 3);

        List<LineDto> lines = sut.linesAtLeast(3);

        // Must be 1 line, not multiple duplicates
        assertEquals(1, lines.size());
        assertHasExactlyPoints(lines.getFirst(), Set.of(p1.id(), p2.id(), p3.id(), p4.id()));
    }

    private static void assertHasExactlyPoints(LineDto line, Set<Long> expectedIds) {
        assertNotNull(line);
        assertNotNull(line.points());
        Set<Long> ids = line.points().stream().map(PointDto::id).collect(Collectors.toSet());
        assertEquals(expectedIds, ids);
    }
}