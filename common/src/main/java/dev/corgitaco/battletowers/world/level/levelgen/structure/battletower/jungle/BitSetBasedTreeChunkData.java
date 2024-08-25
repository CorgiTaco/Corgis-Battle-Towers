package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;

import java.util.BitSet;

public class BitSetBasedTreeChunkData implements BitSetChunkData {

    private final int widthBits;
    private final int height;
    private final int minY;
    private final BitSet data;
    private boolean locked = false;

    public BitSetBasedTreeChunkData(int width, LevelHeightAccessor heightAccessor) {
        this(Integer.numberOfTrailingZeros(width), heightAccessor.getHeight(), heightAccessor.getMinBuildHeight());

    }

    public BitSetBasedTreeChunkData(int widthBits, int height, int minY) {
        this.widthBits = widthBits;
        this.height = height;
        this.minY = minY;
        int width = 1 << this.widthBits;
        this.data = new BitSet(width * width * height);
    }


    public void forEach(ChunkPos chunkPos, PosGetter getter) {
        for (int idx = this.data.nextSetBit(0); idx >= 0; idx = this.data.nextSetBit(idx + 1)) {
            int width = 1 << this.widthBits;
            int x = (idx >> 20) & (width - 1);
            int y = (idx >> 10) & (Mth.smallestEncompassingPowerOfTwo(this.height) - 1);
            int z = idx & (width - 1);
            getter.get(chunkPos.getBlockX(x), y + this.minY, chunkPos.getBlockZ(z));
        }
    }

    public void add(int x, int y, int z) {
        if (!locked) {
            this.data.set(pack(x, y - minY, z));
        }
    }

    public void remove(int x, int y, int z) {
        if (!locked) {
            this.data.clear(pack(x, y - minY, z));
        }
    }

    @Override
    public boolean occupied(int x, int y, int z) {
        return this.data.get(pack(x, y - minY, z));
    }

    public int pack(int x, int y, int z) {
        int width = 1 << this.widthBits;

        x &= (width - 1);
        y &= (Mth.smallestEncompassingPowerOfTwo(this.height) - 1);
        z &= (width - 1);

        return (x << 20) | (y << 10) | z;
    }

    public int[] unpack(int packedValue) {
        int width = 1 << this.widthBits;
        int x = (packedValue >> 20) & (width - 1);
        int y = (packedValue >> 10) & (Mth.smallestEncompassingPowerOfTwo(this.height) - 1);
        int z = packedValue & (width - 1);
        return new int[]{x, y, z};
    }

    private BitSetBasedTreeChunkData lock() {
        this.locked = true;
        return this;
    }

}