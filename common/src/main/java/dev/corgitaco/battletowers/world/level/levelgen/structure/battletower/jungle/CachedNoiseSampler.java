package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

import java.util.Arrays;

public class CachedNoiseSampler {

    private final Long2ObjectOpenHashMap<float[]> cachedNoise = new Long2ObjectOpenHashMap<>();
    private final ImprovedNoise improvedNoise;
    private final float freq;

    public CachedNoiseSampler(ImprovedNoise improvedNoise, float freq) {
        this.improvedNoise = improvedNoise;
        this.freq = freq;
    }

    public float getNoise(double x, double y, double z) {
        return getNoise((int) x, (int) y, (int) z);
    }

    public float getNoise(int x, int y, int z) {
        long sectionPos = SectionPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(y), SectionPos.blockToSectionCoord(z));

        float[] cachedNoise = this.cachedNoise.get(sectionPos);
        if (cachedNoise == null) {
            float[] floats = new float[16 * 16 * 16];
            Arrays.fill(floats, Float.NaN);
            cachedNoise = floats;
            this.cachedNoise.put(sectionPos, floats);
        }

        int flatIndex = getFlatIndex(x & 15, y & 15, z & 15, 16, 16);
        float value = cachedNoise[flatIndex];
        if (Float.isNaN(value)) {
            value = (float) improvedNoise.noise(x * freq, y * freq, z * freq);
            cachedNoise[flatIndex] = value;
        }

        return value;
    }

    public static int getFlatIndex(int x, int y, int z, int width, int height) {
        return z * (width * height) + y * width + x;
    }
}
