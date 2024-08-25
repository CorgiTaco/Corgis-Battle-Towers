package dev.corgitaco.battletowers;

import dev.corgitaco.battletowers.data.collections.ChunkDataLookup;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

public class ChunkDataLookupTest {

    @Test
    public void locationTest() {
        BlockPos origin = new BlockPos(1000, 0, 1000);
        ChunkDataLookup<String> locationTest = new ChunkDataLookup<>(origin, 128, String[]::new, () -> "Yeef");

        int radius = 100;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                String string = locationTest.getForWorld(origin.getX() + x, origin.getZ() + z);
                System.out.println(string);
            }
        }

        locationTest.forEach((chunkX, chunkZ, value) -> {
            if (value != null) {
                System.out.println(value);
            }
        });

    }
}
