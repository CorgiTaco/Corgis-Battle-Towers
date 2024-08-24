package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;

public class RoaringBitMapBasedTreeChunkData implements BitSetChunkData {

    private final int widthBits;
    private final int height;
    private final int minY;
    private final RoaringBitmap data;
    private boolean locked = false;

    public RoaringBitMapBasedTreeChunkData(int width, LevelHeightAccessor heightAccessor) {
        this(Integer.numberOfTrailingZeros(width), heightAccessor.getHeight(), heightAccessor.getMinBuildHeight());

    }

    public RoaringBitMapBasedTreeChunkData(int widthBits, int height, int minY) {
        this.widthBits = widthBits;
        this.height = height;
        this.minY = minY;
        int width = 1 << this.widthBits;
        this.data = RoaringBitmap.bitmapOfRange(0, (long) width * width * height);
    }


    public void forEach(ChunkPos chunkPos, PosGetter getter) {
        int width = 1 << this.widthBits;

        this.data.forEach((IntConsumer) idx -> {
            int x = (idx >> 20) & (width - 1);
            int y = (idx >> 10) & (Mth.smallestEncompassingPowerOfTwo(this.height) - 1);
            int z = idx & (width - 1);
            getter.get(chunkPos.getBlockX(x), y + minY, chunkPos.getBlockZ(z));
        });
    }


    public void add(Vec3i pos) {
        add(pos.getX(), pos.getY(), pos.getZ());
    }

    public void add(int x, int y, int z) {
        if (!locked) {
            this.data.add(pack(x, y - minY, z));
        }
    }

    public void remove(int x, int y, int z) {
        if (!locked) {
            this.data.remove(pack(x, y, z));
        }
    }

    public void remove(Vec3i pos) {
        remove(pos.getX(), pos.getY(), pos.getZ());
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

    private RoaringBitMapBasedTreeChunkData lock() {
        this.locked = true;
        return this;
    }

}