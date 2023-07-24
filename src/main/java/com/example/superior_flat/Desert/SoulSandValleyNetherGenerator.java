package com.example.superior_flat.Desert;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class SoulSandValleyNetherGenerator extends NoiseChunkGenerator {
    public final Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry;
    public final Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;

    public static List<Block> soulSandValleyNetherBlocks = new ArrayList<>();
    public static List<Block> soulSoilValleyNetherBlocks = new ArrayList<>();

    public static final Codec<SoulSandValleyNetherGenerator> CODEC =
            RecordCodecBuilder.create(instance -> NoiseChunkGenerator.createStructureSetRegistryGetter(instance).and(instance.group(
                            RegistryOps.createRegistryCodec(Registry.NOISE_KEY).forGetter(generator -> generator.noiseRegistry),
                            (BiomeSource.CODEC.fieldOf("biome_source")).forGetter(SoulSandValleyNetherGenerator::getBiomeSource),
                            (ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings")).forGetter(SoulSandValleyNetherGenerator::getSettings)))
                    .apply(instance, instance.stable(SoulSandValleyNetherGenerator::new)));

    public SoulSandValleyNetherGenerator(Registry<StructureSet> structureSetRegistry, Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry, BiomeSource populationSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(structureSetRegistry, noiseRegistry, populationSource, settings);

        this.noiseRegistry = noiseRegistry;
        this.indexedFeaturesListSupplier = Suppliers.memoize(() -> PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(), true));
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        for(int i = Math.min(soulSandValleyNetherBlocks.size(), world.getTopY()) - 1; i >= 0; --i) {
            BlockState blockState = soulSandValleyNetherBlocks.get(i).getDefaultState();
            if (blockState != null && heightmap.getBlockPredicate().test(blockState)) {
                return world.getBottomY() + i + 1;
            }
        }

        return world.getBottomY();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(world.getBottomY(), soulSandValleyNetherBlocks.stream().limit(world.getHeight()).map((state) -> state == null ? Blocks.AIR.getDefaultState() : state.getDefaultState()).toArray(BlockState[]::new));
    }
    @Override
    public CompletableFuture<Chunk> populateNoise(
            Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor accessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        Random random = new Random();
        for (int i = 0; i < Math.min(chunk.getHeight(), soulSandValleyNetherBlocks.size()); ++i) {
            BlockState blockState = soulSandValleyNetherBlocks.get(i).getDefaultState();
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    mutable.set(k, i - 63, l);
                    chunk.setBlockState(mutable, blockState, false);
                    if (random.nextBoolean()) {
                        blockState = soulSoilValleyNetherBlocks.get(i).getDefaultState();
                    } else {
                        blockState = soulSandValleyNetherBlocks.get(i).getDefaultState();
                    }
                    heightmap.trackUpdate(k, i - 63, l, blockState);
                    heightmap2.trackUpdate(k, i - 63, l, blockState);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }
    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }
    @Override
    protected Codec<? extends SoulSandValleyNetherGenerator> getCodec() {
        return CODEC;
    }

    static {
        for (int i = 0; i < 63; i++) {soulSandValleyNetherBlocks.add(Blocks.AIR);}
        soulSandValleyNetherBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 3; i++) {soulSandValleyNetherBlocks.add(Blocks.SOUL_SAND);}

        for (int i = 0; i < 63; i++) {soulSoilValleyNetherBlocks.add(Blocks.AIR);}
        soulSoilValleyNetherBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 3; i++) {soulSoilValleyNetherBlocks.add(Blocks.SOUL_SOIL);}
    }
}
