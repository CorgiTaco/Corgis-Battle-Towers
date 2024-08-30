package dev.corgitaco.battletowers;

import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.IntSetChunkData;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.TreeGenerator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelHeightAccessor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TreeGeneratorTest {


    @Test
    public void testCreateTreeGenerator() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        System.out.println("Starting...");

        System.out.println("Warming up...");

        for (int i = 0; i < 5; i++) {
            getTreeGenerator();
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
    }

    @Test
    public void memoryTest() throws InterruptedException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        System.out.println("Starting...");

        System.out.println("Warming up...");

        int countPerThread = 1000000;

        AtomicInteger integer = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();

        ConcurrentHashMap<Integer, TreeGenerator> generatorConcurrentHashMap = new ConcurrentHashMap<>();

        for (int thread = 0; thread < Runtime.getRuntime().availableProcessors() - 1; thread++) {
            int finalThread = thread;
            Thread thread1 = new Thread(() -> {
                for (int i = 0; i < countPerThread; i++) {
                    generatorConcurrentHashMap.put((finalThread * countPerThread)+ 1 + i, getTreeGenerator());
                    System.out.println(integer.getAndIncrement());
                }
            });

            thread1.start();
            threads.add(thread1);
        }


        while (true) {
            boolean closeProgram = true;
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    closeProgram = false;
                    break;
                }
            }

            if (closeProgram) {
                break;
            }
        }
    }


    private static TreeGenerator getTreeGenerator() {
        return new TreeGenerator(BlockPos.ZERO, 0, 128, () -> new IntSetChunkData(16, new LevelHeightAccessor() {
            @Override
            public int getHeight() {
                return 384;
            }

            @Override
            public int getMinBuildHeight() {
                return -64;
            }
        }, IntOpenHashSet::new));
    }
}
