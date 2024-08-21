package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import com.google.common.cache.RemovalListener;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructureTypes;
import dev.corgitaco.battletowers.world.level.levelgen.structure.UnsafeBoundingBox;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class JungleBattleTowerStructure extends Structure {

    public static final Codec<JungleBattleTowerStructure> CODEC = RecordCodecBuilder.<JungleBattleTowerStructure>mapCodec(builder ->
            builder.group(
                    settingsCodec(builder)
            ).apply(builder, JungleBattleTowerStructure::new)
    ).codec();

    public JungleBattleTowerStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return onTopOfChunkCenter(context, Heightmap.Types.OCEAN_FLOOR_WG, (piecesBuilder) -> {
            ChunkPos chunkPos = context.chunkPos();

            RandomState randomState = context.randomState();
            WorldgenRandom random = context.random();
            ChunkGenerator chunkGenerator = context.chunkGenerator();

            int blockX = chunkPos.getBlockX(random.nextInt(16));
            int blockZ = chunkPos.getBlockZ(random.nextInt(16));
            BlockPos origin = new BlockPos(blockX, chunkGenerator.getBaseHeight(blockX, blockZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), randomState), blockZ);


            XoroshiroRandomSource xoroshiroRandomSource = new XoroshiroRandomSource(origin.asLong() + context.seed());

            Long2ObjectOpenHashMap<UnsafeBoundingBox> map = new Long2ObjectOpenHashMap<>();

            Consumer<BlockPos> boxCreation = pos -> map.computeIfAbsent(ChunkPos.asLong(pos), key -> new UnsafeBoundingBox()).encapsulate(pos);

            forAllPositions(origin, xoroshiroRandomSource, boxCreation, lists -> {},  boxCreation);

            for (UnsafeBoundingBox value : map.values()) {
                piecesBuilder.addPiece(new JungleBattleTowerPiece(0, value.toBoundingBox(), origin));
            }
        });
    }

    @Override
    public StructureType<?> type() {
        return CBTStructureTypes.JUNGLE_BATTLE_TOWER.get();
    }


    public static void forAllPositions(BlockPos origin, RandomSource randomSource, Consumer<BlockPos> trunkPlacer, Consumer<List<BlockPos>> branchPlacer, Consumer<BlockPos> leavePlacer) {
        forAllPositions(8, 16, origin, randomSource, trunkPlacer, branchPlacer, leavePlacer);
    }

    public static void forAllPositions(int range, int width, BlockPos origin, RandomSource randomSource, Consumer<BlockPos> trunkPlacer, Consumer<List<BlockPos>> branchPlacer, Consumer<BlockPos> leavePlacer) {
        LongList treeTrunkPositions = getTreeTrunkPositions(range, origin, randomSource);
        positions(treeTrunkPositions, width, trunkPlacer);
        generateBranches(randomSource, treeTrunkPositions, trunkPlacer, branchPlacer, leavePlacer);
    }


    public static LongList getTreeTrunkPositions(int range, BlockPos origin, RandomSource randomSource) {
        LongList positions = new LongArrayList();


        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos().set(origin);

        int height = randomSource.nextInt(150, 200);

        positions.add(mutableBlockPos.asLong());

        int rawStep = 0;
        int heightOffset = randomSource.nextInt(30, 50);


        double noise = (new ImprovedNoise(randomSource).noise(0, 0, 0) + 1) * 0.5F;
        double angle = Mth.clampedLerp(-180, 180, noise);
        double xOffset = Math.cos(angle) * range;

        double zOffset = Math.sin(angle) * range;

        for (int y = heightOffset; y <= height; y += heightOffset) {
            if (rawStep % 2 == 0) {
                mutableBlockPos.set(origin.getX() + xOffset, origin.getY() + y, origin.getZ() + zOffset);
            } else {
                mutableBlockPos.set(origin.getX() - xOffset, origin.getY() + y, origin.getZ() - zOffset);
            }
            positions.add(mutableBlockPos.asLong());

            rawStep++;

        }
        return positions;
    }


    public static void positions(LongList treeTrunkPositions, int width, Consumer<BlockPos> logPlacer) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < treeTrunkPositions.size() - 1; i++) {
            long current = treeTrunkPositions.getLong(i);
            mutableBlockPos.set(current);
            BlockPos currentTrunkPos = mutableBlockPos.immutable();

            long next = treeTrunkPositions.getLong(i + 1);
            mutableBlockPos.set(next);
            BlockPos nextTrunkPos = mutableBlockPos.immutable();

            BlockPos difference = nextTrunkPos.subtract(currentTrunkPos);
            Vector3d normalize = normalize(difference);

            double distance = length(difference);

            for (int step = 0; step < distance; step++) {
                BlockPos offset = currentTrunkPos.offset((int) (normalize.x * step), (int) (normalize.y * step), (int) (normalize.z * step));
                sphereAround(width, offset, logPlacer);
            }
        }

    }

    private static void generateBranches(RandomSource randomSource, LongList treeTrunkPositions, Consumer<BlockPos> logPlacer, Consumer<List<BlockPos>> branchesGetter, Consumer<BlockPos> leavesPlacer) {
        for (Long treeTrunkPosition : treeTrunkPositions) {
            List<BlockPos> branchPositions = new ArrayList<>();
            BlockPos currentTrunkPos = BlockPos.of(treeTrunkPosition);
            recursivelyGenerateBranches(3, randomSource.nextInt(1, 4), 6, UniformInt.of(20, 30), UniformInt.of(-6, 0), UniformFloat.of(0, 360), randomSource, logPlacer, pos -> {
                branchPositions.add(pos.immutable());
            }, leavesPlacer, currentTrunkPos,  0);

            branchesGetter.accept(branchPositions);
        }


    }

    private static void recursivelyGenerateBranches(int totalSegmentCount, int branchCount, int branchRadius, IntProvider rangeGetter, IntProvider yOffset, FloatProvider angleGetter, RandomSource randomSource, Consumer<BlockPos> logPlacer, Consumer<BlockPos> branchOrigins, Consumer<BlockPos> leavesPlacer, BlockPos currentTrunkPos, int internalCount) {
        for (int count = 0; count < branchCount; count++) {
            float angle = angleGetter.sample(randomSource);
            int range = rangeGetter.sample(randomSource);
            int randomYOffset = yOffset.sample(randomSource);
            double xOffset = Math.cos(Math.toRadians(angle)) * range;
            double zOffset = Math.sin(Math.toRadians(angle)) * range;

            BlockPos endPos = currentTrunkPos.offset((int) (xOffset), randomYOffset, (int) (zOffset));
            BlockPos difference = endPos.subtract(currentTrunkPos);
            Vector3d normalize = normalize(difference);

            double distance = length(difference);

            for (int step = 0; step < distance; step++) {
                BlockPos offset = currentTrunkPos.offset((int) (normalize.x * step), (int) (normalize.y * step), (int) (normalize.z * step));
                branchOrigins.accept(offset);
                sphereAround(branchRadius, offset, logPlacer);
            }

            if (internalCount < totalSegmentCount) {
                float angleRange = angleGetter.getMaxValue() - angleGetter.getMinValue();
                float minInclusive = angle - (angleRange * 0.2F);
                float maxExclusive = angle + (angleRange * 0.2F);
                recursivelyGenerateBranches(totalSegmentCount, branchCount + randomSource.nextIntBetweenInclusive(1, 2), branchRadius - 2, ConstantInt.of((int) (range * 0.9F)), yOffset, UniformFloat.of(minInclusive, maxExclusive), randomSource, logPlacer, branchOrigins, leavesPlacer, endPos, internalCount + 1);
            }
        }
    }


    private static final ImprovedNoise noise = new ImprovedNoise(new LegacyRandomSource(0));

    public static void sphereAround(int width, BlockPos origin, Consumer<BlockPos> consumer) {
        sphereAround(width, 0.1F, 0.2F, origin, consumer);
    }

    public static void sphereAround(int width, double freq, double minRadius, BlockPos origin, Consumer<BlockPos> consumer) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -width; x <= width; x++) {
            for (int y = -width; y <= width; y++) {
                for (int z = -width; z <= width; z++) {
                    mutable.setWithOffset(origin, x, y, z);
                    if (mutable.closerThan(origin, width)) {
                        double noise1 = (noise.noise(mutable.getX() * freq, mutable.getY() * freq, mutable.getZ() * freq) + 1) * 0.5;
                        double localRadius = Mth.clampedLerp(width * minRadius, width, noise1);
                        if (mutable.closerThan(origin, localRadius)) {
                            consumer.accept(mutable);
                        }
                    }
                }
            }
        }
    }


    public static Vector3d normalize(Vec3i vector) {
        float length = (float) Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY() + vector.getZ() * vector.getZ());
        if (length == 0) {
            return new Vector3d(0, 0, 0); // Handle zero-length vector
        }
        return new Vector3d(vector.getX() / length, vector.getY() / length, vector.getZ() / length);
    }

    public static double length(Vec3i vector) {
        return Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY() + vector.getZ() * vector.getZ());
    }
}