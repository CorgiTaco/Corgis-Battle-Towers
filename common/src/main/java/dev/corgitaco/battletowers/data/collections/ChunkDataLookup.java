package dev.corgitaco.battletowers.data.collections;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nullable;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ChunkDataLookup<T> {

    private final BlockPos origin;
    private final int chunkRadius;

    private final Short2ObjectOpenHashMap<T> data;
    private final Supplier<T> dataFactory;
    private final BoundingBox bounds;


    public ChunkDataLookup(BlockPos origin, int radius, IntFunction<T[]> storageFactory, Supplier<T> dataFactory) {
        this.origin = origin;
        this.chunkRadius = SectionPos.blockToSectionCoord(radius) + 1;


        int diameter = chunkRadius + chunkRadius;
        this.data = new Short2ObjectOpenHashMap<>();
        this.dataFactory = dataFactory;


        int minX = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getX()) - chunkRadius);
        int minZ = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getZ()) - chunkRadius);

        int maxX = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getX()) + chunkRadius + 1) - 1;
        int maxZ = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getZ()) + chunkRadius + 1) - 1;

        this.bounds = new BoundingBox(minX, Integer.MIN_VALUE, minZ, maxX, Integer.MAX_VALUE, maxZ);
    }


    public T getForWorld(Vec3i pos) {
        return getForWorld(pos.getX(), pos.getZ());
    }

    public T getForWorld(int x, int z) {
        if (!this.bounds.isInside(x, 0, z)) {
            throw new IllegalArgumentException("Requested position %s which is out of bounds. Chunk Bounds: %s | Block Bounds: %s".formatted(String.format("[x=%d,z=%d]".formatted(x, z)), printChunkBounds(this.bounds), this.bounds.toString()));
        }

        return getUnchecked(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
    }

    public T getForChunk(int chunkX, int chunkZ) {
        if (!this.bounds.isInside(SectionPos.sectionToBlockCoord(chunkX), 0, SectionPos.sectionToBlockCoord(chunkZ))) {
            throw new IllegalArgumentException("Requested chunk %s out of bounds. Chunk Bounds: %s | Block Bounds: %s".formatted(String.format("[chunkx=%d,chunkz=%d]".formatted(chunkX, chunkZ)), printChunkBounds(this.bounds), this.bounds.toString()));
        }

        return getUnchecked(chunkX, chunkZ);
    }

    private T getUnchecked(int chunkX, int chunkZ) {
        int localX = getLocalXFromChunkX(chunkX);
        int localZ = getLocalZFromChunkZ(chunkZ);

        int index = getIndex(localX, localZ);
        T datum = this.data.get((short) index);

        if (datum == null) {
            datum = this.dataFactory.get();
            this.data.put((short) index, datum);
        }

        return datum;
    }

    private int getLocalXFromChunkX(int chunkX) {
        return (chunkX - SectionPos.blockToSectionCoord(this.origin.getX()) + chunkRadius);
    }

    private int getLocalZFromChunkZ(int chunkZ) {
        return (chunkZ - SectionPos.blockToSectionCoord(this.origin.getZ()) + chunkRadius);
    }

    private int getIndex(int localX, int localZ) {
        return (localX + localZ * (this.chunkRadius + this.chunkRadius));
    }

    public void forEach(Consumer<T> consumer) {
        int minChunkX = SectionPos.blockToSectionCoord(this.bounds.minX());
        int minChunkZ = SectionPos.blockToSectionCoord(this.bounds.minZ());

        int maxChunkX = SectionPos.blockToSectionCoord(this.bounds.maxX());
        int maxChunkZ = SectionPos.blockToSectionCoord(this.bounds.maxZ());


        for (int chunkX = minChunkX; chunkX < maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ++) {
                consumer.forEach(chunkX, chunkZ, this.data.get((short) getIndex(getLocalXFromChunkX(chunkX), getLocalZFromChunkZ(chunkZ))));
            }
        }
    }

    private static String printChunkBounds(BoundingBox boundingBox) {
        return String.format("[minchunkx=%d,minchunkz=%d,maxchunkx=%d,maxchunkz=%d]", SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()), SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
    }

    @FunctionalInterface
    public interface Consumer<T> {

        void forEach(int chunkX, int chunkZ, @Nullable T value);
    }
}
