package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;

public interface ChunkData {

    void forEach(ChunkPos chunkPos, PosGetter getter);

    void add(int x, int y, int z);

    default void add(Vec3i pos) {
        add(pos.getX(), pos.getY(), pos.getZ());
    }

    void remove(int x, int y, int z);

    default void remove(Vec3i pos) {
        remove(pos.getX(), pos.getY(), pos.getZ());
    }

    int pack(int x, int y, int z);

    boolean occupied(int x, int y, int z);

    @FunctionalInterface
    interface PosGetter {
        void get(int x, int y, int z);
    }
}
