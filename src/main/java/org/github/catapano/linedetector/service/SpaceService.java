package org.github.catapano.linedetector.service;

import org.github.catapano.linedetector.domain.LineKey;
import org.github.catapano.linedetector.dto.LineDto;
import org.github.catapano.linedetector.dto.PointDto;
import org.github.catapano.linedetector.model.Point;
import org.springframework.stereotype.Service;

import org.github.catapano.linedetector.domain.SlopeKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SpaceService {
    private final AtomicLong seq = new AtomicLong(1);
    private final Map<Long, Point> points = new ConcurrentHashMap<>();
    private final Object mutex = new Object();

    public Point add(long x, long y) {
        // need for syncrhonization to ensure consistent state of points map
        synchronized (mutex) {
            long id = seq.getAndIncrement();
            Point p = new Point(id, x, y);
            points.put(id, p);
            return p;
        }
    }

    public List<Point> allPoints() {
        return snapshotPoints().stream()
                .sorted(Comparator.comparingLong(Point::id))
                .toList();
    }

    public void clear() {
        synchronized (mutex) {
            points.clear();
        }
    }

    private List<Point> snapshotPoints() {
        synchronized (mutex) {
            return new ArrayList<>(points.values());
        }
    }

    public List<LineDto> linesAtLeast(int n) {
        if (n <= 1) throw new IllegalArgumentException("n must be >= 2");
        List<Point> pts = snapshotPoints().stream()
                .sorted(Comparator.comparingLong(Point::id))
                .toList();
        int numberOfPoints = pts.size();
        // if there are fewer points than n, we can return early with empty list
        if (numberOfPoints < n) return List.of();

        Map<LineKey, Set<Point>> lines = new HashMap<>();

        /** need to group points by slope
         *
         */
        for (int i = 0; i < numberOfPoints; i++) {
            // for every point i, we will compute slopes to all points j > i and group by slope
            Point pi = pts.get(i);

            // slope groups for anchor i
            Map<SlopeKey, List<Point>> groups = new HashMap<>();

            for (int j = i + 1; j < numberOfPoints; j++) {
                Point pj = pts.get(j);
                long dx = pj.x() - pi.x();
                long dy = pj.y() - pi.y();

                SlopeKey sk = SlopeKey.of(dx, dy);
                groups.computeIfAbsent(sk, _ -> new ArrayList<>()).add(pj);
            }

            for (var e : groups.entrySet()) {
                List<Point> group = e.getValue();
                if (group.size() + 1 >= n) {
                    // build canonical line key using anchor and first point in group
                    LineKey lk = LineKey.fromPoints(pi, group.getFirst());
                    Set<Point> set = lines.computeIfAbsent(lk, _ -> new LinkedHashSet<>());
                    set.add(pi);
                    set.addAll(group);
                }
            }
        }

        // Filter final lines by size >= n and map to DTO
        return lines.entrySet().stream()
                .filter(en -> en.getValue().size() >= n)
                .map(en -> new LineDto(
                        en.getKey().toString(),
                        en.getValue().stream()
                                .sorted(Comparator.comparingLong(Point::id))
                                .map(p -> new PointDto(p.id(), p.x(), p.y()))
                                .toList()
                ))
                .toList();
    }

}