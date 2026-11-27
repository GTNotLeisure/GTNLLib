package com.gtnllib.utils;

/**
 * Flight curves for things that launch, cruise along an arc, brake hard and settle into place. Pure math, safe on
 * both sides. Times are fractions 0..1 unless a parameter says ticks.
 */
public class FlightPath {

    public static final double BRAKE_AT = 0.76;
    private static final double[] DISTANCE = new double[257];

    static {
        double sum = 0;
        for (int i = 1; i <= 256; i++) {
            sum += speed((i - 0.5) / 256);
            DISTANCE[i] = sum;
        }
        for (int i = 1; i <= 256; i++) DISTANCE[i] /= sum;
    }

    private FlightPath() {}

    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    /** Smoothstep of t clamped to 0..1. */
    public static double ease(double t) {
        t = clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    /** Relative speed at time t: a quick cubic launch, a slowly rising cruise, then a hard brake after BRAKE_AT. */
    public static double speed(double t) {
        if (t < 0.10) {
            double u = t / 0.10;
            return 1 - (1 - u) * (1 - u) * (1 - u);
        }
        if (t < BRAKE_AT) return 1 + 0.08 * (t - 0.10);
        double u = (t - BRAKE_AT) / (1 - BRAKE_AT);
        return (1 + 0.08 * (BRAKE_AT - 0.10)) * Math.pow(1 - u, 3);
    }

    /** Fraction of the path covered at time t when moving with {@link #speed}. */
    public static double profile(double t) {
        double x = clamp(t, 0, 1) * 256;
        int i = Math.min(255, (int) x);
        return DISTANCE[i] + (DISTANCE[i + 1] - DISTANCE[i]) * (x - i);
    }

    public static Vec3d cubic(Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, double t) {
        double u = 1 - t;
        double a = u * u * u, b = 3 * u * u * t, c = 3 * u * t * t, d = t * t * t;
        return new Vec3d(
            a * p0.x() + b * p1.x() + c * p2.x() + d * p3.x(),
            a * p0.y() + b * p1.y() + c * p2.y() + d * p3.y(),
            a * p0.z() + b * p1.z() + c * p2.z() + d * p3.z());
    }

    /** Length of a cubic curve, measured over the given number of straight steps. */
    public static double cubicLength(Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, int steps) {
        double length = 0;
        Vec3d previous = p0;
        for (int i = 1; i <= steps; i++) {
            Vec3d point = cubic(p0, p1, p2, p3, i / (double) steps);
            length += Math.sqrt(point.distanceToSqr(previous));
            previous = point;
        }
        return length;
    }

    /**
     * Time at which a part that closes over the window [0, window] ticks locks: parts lock one after another, the
     * last at the window's end.
     */
    public static double lockTick(int part, int parts, double window) {
        return window * (part + 1) / parts;
    }

    /** How far a part has closed (0 open, 1 shut) at the given ticks, accelerating over the last span ticks. */
    public static double close(double ticks, int part, int parts, double window, double span) {
        double end = lockTick(part, parts, window), begin = Math.max(0, end - span);
        double t = clamp((ticks - begin) / Math.max(1.0E-6, end - begin), 0, 1);
        return t * t * t;
    }

    /** Damped overshoot after a lock: 0 at rest, swinging around 0 with the given amplitude. */
    public static double recoil(double ticks, double amplitude) {
        if (ticks <= 0) return 0;
        return amplitude * Math.exp(-ticks / 2.2) * Math.cos(ticks * 1.9);
    }
}
