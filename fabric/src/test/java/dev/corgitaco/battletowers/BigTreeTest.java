package dev.corgitaco.battletowers;

import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.BigTreeInfo;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.BitSetBasedTreeChunkData;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.BitSetChunkData;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.RoaringBitMapBasedTreeChunkData;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import org.junit.jupiter.api.Test;

public class BigTreeTest {

    @Test
    public void testBitSet() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        System.out.println("Starting...");
        LevelHeightAccessor levelHeightAccessor = new LevelHeightAccessor() {
            @Override
            public int getHeight() {
                return 384;
            }

            @Override
            public int getMinBuildHeight() {
                return -64;
            }
        };


        for (int i = 0; i < 10; i++) {
            BigTreeInfo.getBigTreeInfo(BlockPos.ZERO, 0, () -> new BitSetBasedTreeChunkData(16, levelHeightAccessor));
        }
        for (int i = 0; i < 10; i++) {
            BigTreeInfo.getBigTreeInfo(BlockPos.ZERO, 0, () -> new RoaringBitMapBasedTreeChunkData(16, levelHeightAccessor));
        }


        {
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                BigTreeInfo.getBigTreeInfo(BlockPos.ZERO, 0, () -> new BitSetBasedTreeChunkData(16, levelHeightAccessor));
            }
            System.out.printf("Bitset Time spent: %d%n", System.currentTimeMillis() - startTime);
        }
        {
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                BigTreeInfo.getBigTreeInfo(BlockPos.ZERO, 0, () -> new RoaringBitMapBasedTreeChunkData(16, levelHeightAccessor));
            }
            System.out.printf("Roaring Time spent: %d%n", System.currentTimeMillis() - startTime);
        }

    }

    @Test
    public void testLoop() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        System.out.println("Starting...");
        LevelHeightAccessor levelHeightAccessor = new LevelHeightAccessor() {
            @Override
            public int getHeight() {
                return 384;
            }

            @Override
            public int getMinBuildHeight() {
                return -64;
            }
        };

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        BigTreeInfo bitSetBigTreeInfo = BigTreeInfo.getBigTreeInfo(BlockPos.ZERO, 0, () -> new BitSetBasedTreeChunkData(16, levelHeightAccessor));
        for (BitSetChunkData value : bitSetBigTreeInfo.trunkInfo().insideTrunkPositions().values()) {
            value.forEach(new ChunkPos(0,0), mutableBlockPos::set);
        }

        BigTreeInfo roaringBitmap = BigTreeInfo.getBigTreeInfo(BlockPos.ZERO, 0, () -> new RoaringBitMapBasedTreeChunkData(16, levelHeightAccessor));
        for (BitSetChunkData value : roaringBitmap.trunkInfo().insideTrunkPositions().values()) {
            value.forEach(new ChunkPos(0,0), mutableBlockPos::set);
        }

        {
            long startTime = System.currentTimeMillis();
            for (BitSetChunkData value : bitSetBigTreeInfo.trunkInfo().insideTrunkPositions().values()) {
                value.forEach(new ChunkPos(0,0), mutableBlockPos::set);
            }
            System.out.printf("Bitset Time spent: %d%n", System.currentTimeMillis() - startTime);
        }
        {
            long startTime = System.currentTimeMillis();
            for (BitSetChunkData value : roaringBitmap.trunkInfo().insideTrunkPositions().values()) {
                value.forEach(new ChunkPos(0,0), mutableBlockPos::set);
            }
            System.out.printf("Roaring Time spent: %d%n", System.currentTimeMillis() - startTime);
        }

    }

}
