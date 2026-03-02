package org.github.catapano.linedetector.dto;

/**
 * Request body for adding a point to the space.
 * @param x x-axis coordinate
 * @param y y-axis coordinate
 */
public record PointRequest( long x, long y) {}
