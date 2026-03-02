# Line Detector

Simple REST API that stores 2D points and detects all lines passing through at least **N** points.

## Design Choice
Points are modeled using integer coordinates (`x` and `y` as integers / long values).
This avoids floating-point precision issues when computing slopes and ensures exact
mathematical comparisons using GCD normalization.

We calculate the slope and group by these the point to understand how many points are collinear.

---

## Endpoints

### Add a point
POST /point

```json
{
  "x": 1,
  "y": 2
}
```

### Get all points

GET /space

### Get all lines with at least N collinear points

GET /lines/{n}

### Clear all points

DELETE /space

## Algorithm Overview


For each point P:
1.	Compute the direction vector to every other point Q:

```
      dx = x₂ - x₁
      dy = y₂ - y₁
```

(Related concept: Vector direction
https://en.wikipedia.org/wiki/Direction_(geometry))

2. Normalize the direction using the Greatest Common Divisor (GCD)
to avoid floating point precision issues:
•	Divide (dx, dy) by gcd(|dx|, |dy|)
•	Normalize sign to ensure canonical representation
(GCD: https://en.wikipedia.org/wiki/Greatest_common_divisor
Euclidean algorithm: https://en.wikipedia.org/wiki/Euclidean_algorithm)
3.	Group points by normalized direction (slope).

If a group contains at least N-1 points relative to P, then those points are collinear.
(Collinearity: https://en.wikipedia.org/wiki/Collinearity)