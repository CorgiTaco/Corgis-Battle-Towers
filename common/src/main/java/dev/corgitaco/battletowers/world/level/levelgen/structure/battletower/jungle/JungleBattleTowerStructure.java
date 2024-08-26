package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructureTypes;
import dev.corgitaco.battletowers.world.level.levelgen.structure.UnsafeBoundingBox;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

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
        ChunkPos chunkPos = context.chunkPos();


        RandomState randomState = context.randomState();
        WorldgenRandom random = context.random();
        ChunkGenerator chunkGenerator = context.chunkGenerator();

        int blockX = chunkPos.getBlockX(random.nextInt(16));
        int blockZ = chunkPos.getBlockZ(random.nextInt(16));
        BlockPos origin = new BlockPos(blockX, chunkGenerator.getBaseHeight(blockX, blockZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), randomState), blockZ);


        int blendRadius = 128;
        int blendStep = 16;

        Pair<BlockPos, Holder<Biome>> biomeHorizontal = context.biomeSource().findBiomeHorizontal(origin.getX(), origin.getY(), origin.getZ(), blendRadius, blendStep, biomeHolder -> !context.validBiome().test(biomeHolder), random, false, context.randomState().sampler());

        if (biomeHorizontal != null) {
            return Optional.empty();
        }

        return onTopOfChunkCenter(context, Heightmap.Types.OCEAN_FLOOR_WG, (piecesBuilder) -> {

            Long2ObjectOpenHashMap<UnsafeBoundingBox> map = new Long2ObjectOpenHashMap<>();

            ChunkData.PosGetter boxCreation = (x, y, z) -> map.computeIfAbsent(ChunkPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)), key -> new UnsafeBoundingBox()).encapsulate(x, y, z);

            TreeGenerator treeGenerator = new TreeGenerator(origin, context.seed(), 128, () -> new IntSetChunkData(16, context.heightAccessor(), IntOpenHashSet::new));

            treeGenerator.forAllAvailableChunkData((chunkX, chunkZ, chunkData) -> {
                if (chunkData != null) {
                    chunkData.forEach(new ChunkPos(chunkX, chunkZ), boxCreation);
                }
            });



            for (UnsafeBoundingBox value : map.values()) {
                piecesBuilder.addPiece(new JungleBattleTowerPiece(0, value.toBoundingBox(), origin, treeGenerator));
            }
        });
    }

    @Override
    public StructureType<?> type() {
        return CBTStructureTypes.JUNGLE_BATTLE_TOWER.get();
    }
}