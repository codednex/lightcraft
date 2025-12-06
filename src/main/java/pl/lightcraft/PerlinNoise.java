package pl.lightcraft;

import java.util.Random;

public class PerlinNoise {
    private final int[] p = new int[512];

    public PerlinNoise(long seed) {
        Random rand = new Random(seed);
        int[] permutation = new int[256];
        for (int i = 0; i < 256; i++) permutation[i] = i;

        for (int i = 0; i < 256; i++) {
            int swapIndex = rand.nextInt(256 - i) + i;
            int temp = permutation[i];
            permutation[i] = permutation[swapIndex];
            permutation[swapIndex] = temp;
        }

        for (int i = 0; i < 256; i++) p[i] = p[i + 256] = permutation[i];
    }

    public double noise(double x, double z) {
        int X = (int) Math.floor(x) & 255;
        int Z = (int) Math.floor(z) & 255;
        x -= Math.floor(x);
        z -= Math.floor(z);

        double u = fade(x);
        double w = fade(z);

        int A = p[X] + Z, AA = p[A], AB = p[A + 1];
        int B = p[X + 1] + Z, BA = p[B], BB = p[B + 1];

        return lerp(w, lerp(u, grad(p[AA], x, 0, z), grad(p[BA], x - 1, 0, z)),
                lerp(u, grad(p[AB], x, 0, z - 1), grad(p[BB], x - 1, 0, z - 1)));
    }

    private double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private double lerp(double t, double a, double b) { return a + t * (b - a); }
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}