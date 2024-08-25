package dev.corgitaco.battletowers;

import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.BigTreeInfo;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.BitSetBasedTreeChunkData;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.TreeGenerator;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelHeightAccessor;
import org.junit.jupiter.api.Test;

public class TreeGeneratorTest {



    @Test
    public void testCreateTreeGenerator() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        System.out.println("Starting...");

        System.out.println("Warming up...");

        for (int i = 0; i < 100; i++) {
            getTreeGenerator();
        }
        for (int i = 0; i < 100; i++) {
            getLegacyTreeGenerator();
        }

        {
            System.out.println("Beginning timer for Tree Generator...");
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                getTreeGenerator();
            }
            long endTime = System.currentTimeMillis();
            System.out.printf("Took %dms to create 100 tree generators!%n", endTime - startTime);
        }
        {
            System.out.println("Beginning timer for legacy Tree Generator...");
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                getLegacyTreeGenerator();
            }
            long endTime = System.currentTimeMillis();
            System.out.printf("Took %dms to create 100 legacy tree generators!%n", endTime - startTime);
        }
    }

    private static void getLegacyTreeGenerator() {
        BigTreeInfo.getBigTreeInfo(BlockPos.ZERO, 0, () -> new BitSetBasedTreeChunkData(16, new LevelHeightAccessor() {
            @Override
            public int getHeight() {
                return 384;
            }

            @Override
            public int getMinBuildHeight() {
                return -64;
            }
        }));
    }

    private static void getTreeGenerator() {
        new TreeGenerator(BlockPos.ZERO, 0, 128, () -> new BitSetBasedTreeChunkData(16, new LevelHeightAccessor() {
            @Override
            public int getHeight() {
                return 384;
            }

            @Override
            public int getMinBuildHeight() {
                return -64;
            }
        }));
    }
}
