package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;


import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;

import java.util.function.Supplier;

public class IntSetChunkData implements ChunkData {


    private IntSet data;
    private final int widthBits;
    private final int height;
    private final int minY;
    private boolean locked = false;

    public IntSetChunkData(int width, LevelHeightAccessor heightAccessor, Supplier<IntSet> factory) {
        this(Integer.numberOfTrailingZeros(width), heightAccessor.getHeight(), heightAccessor.getMinBuildHeight(), factory);

    }

    public IntSetChunkData(int widthBits, int height, int minY, Supplier<IntSet> factory) {
        this.widthBits = widthBits;
        this.height = height;
        this.minY = minY;
        int width = 1 << this.widthBits;
        this.data = factory.get();
    }


    public void forEach(ChunkPos chunkPos, PosGetter getter) {
        this.data.forEach(idx -> {
            int width = 1 << this.widthBits;
            int x = (idx >> 20) & (width - 1);
            int y = (idx >> 10) & (Mth.smallestEncompassingPowerOfTwo(this.height) - 1);
            int z = idx & (width - 1);
            getter.get(chunkPos.getBlockX(x), y + this.minY, chunkPos.getBlockZ(z));
        });
    }

    public void add(int x, int y, int z) {
        if (!locked) {
            this.data.add(pack(x, y - minY, z));
        }
    }

    public void remove(int x, int y, int z) {
        if (!locked) {
            this.data.remove(pack(x, y - minY, z));
        }
    }

    public int pack(int x, int y, int z) {
        int width = 1 << this.widthBits;

        x &= (width - 1);
        y &= (Mth.smallestEncompassingPowerOfTwo(this.height) - 1);
        z &= (width - 1);

        return (x << 20) | (y << 10) | z;
    }

    @Override
    public boolean occupied(int x, int y, int z) {
        return this.data.contains(pack(x, y - minY, z));
    }
}
