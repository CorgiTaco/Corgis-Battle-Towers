package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;

public record NoiseSphereGenerator(double minRadius, int maxRadius, CachedNoiseSampler noise) {

    public void sphereAroundForComponent(BlockPos origin, TreeGenerator generator, TreeGenerator.TreeComponent treeComponent) {
        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        for (int x = -maxRadius; x <= maxRadius; x++) {
            for (int y = -maxRadius; y <= maxRadius; y++) {
                for (int z = -maxRadius; z <= maxRadius; z++) {
                    int currentX = originX + x;
                    int currentY = originY + y;
                    int currentZ = originZ + z;

                    double distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared <= Mth.square(maxRadius)) {
                        double noise1 = (noise.getNoise(currentX, currentY, currentZ) + 1) * 0.5;
                        double localRadius = Mth.clampedLerp(minRadius, maxRadius, noise1);

                        if (distanceSquared <= Mth.square(localRadius)) {
                            generator.getPositions(SectionPos.blockToSectionCoord(currentX), SectionPos.blockToSectionCoord(currentZ), treeComponent).add(currentX, currentY, currentZ);
                        }
                    }
                }
            }
        }
    }

}
