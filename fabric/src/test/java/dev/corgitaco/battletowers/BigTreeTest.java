package dev.corgitaco.battletowers;

import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.BigTreeInfo;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.BitSetBasedTreeChunkData;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.BitSetChunkData;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import org.junit.jupiter.api.Test;

public class BigTreeTest {

    @Test
    public void testBitSet() {

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


        {
            long startTime = System.currentTimeMillis();
            for (BitSetChunkData value : bitSetBigTreeInfo.trunkInfo().insideTrunkPositions().values()) {
                value.forEach(new ChunkPos(0,0), mutableBlockPos::set);
            }
            System.out.printf("Bitset Time spent: %d%n", System.currentTimeMillis() - startTime);
        }
    }

}
