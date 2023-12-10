package com.example.superior_flat.Classic;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ClassicEndGenerator extends NoiseChunkGenerator {
    public final RegistryEntry<ChunkGeneratorSettings> settings;
    public final Supplier<AquiferSampler.FluidLevelSampler> fluidLevelSampler;
    public final Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;
    public static List<Block> classicEndBlocks = new ArrayList<>();
    public static List<Block> classicEndBlocks1 = new ArrayList<>();
    public static List<Block> classicVoidBlocks = new ArrayList<>();
    public ClassicEndGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource, settings);

        this.settings = settings;
        this.fluidLevelSampler = Suppliers.memoize(() -> {
            return createFluidLevelSampler(settings.value());
        });
        this.indexedFeaturesListSupplier = Suppliers.memoize(() -> PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(), true));

    }
    public static final Codec<ClassicEndGenerator> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> {
            return generator.biomeSource;
        }), ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((generator) -> {
            return generator.settings;
        })).apply(instance, instance.stable(ClassicEndGenerator::new));
    });
    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        for(int i = Math.min(classicEndBlocks.size(), world.getTopY()) - 1; i >= 0; --i) {
            BlockState blockState = classicEndBlocks.get(i).getDefaultState();
            if (blockState != null && heightmap.getBlockPredicate().test(blockState)) {
                return world.getBottomY() + i + 1;
            }
        }

        return world.getBottomY();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(world.getBottomY(), classicEndBlocks.stream().limit(world.getHeight()).map((state) -> state == null ? Blocks.AIR.getDefaultState() : state.getDefaultState()).toArray(BlockState[]::new));
    }
    @Override
    public CompletableFuture<Chunk> populateNoise(
            Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor accessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

        Registry<Biome> biomeRegistry = accessor.getRegistryManager().get(RegistryKeys.BIOME);
        for (int i = 0; i < Math.min(chunk.getHeight(), classicEndBlocks.size()); ++i) {
            BlockState blockState;
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    mutable.set(k, i, l);
                    Biome currentBiome = chunk.getBiomeForNoiseGen(k >> 2, i, l >> 2).value();
                    if (currentBiome.equals(biomeRegistry.get(BiomeKeys.END_HIGHLANDS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.SMALL_END_ISLANDS))) {
                        blockState = classicEndBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.THE_END))) {
                        blockState = classicEndBlocks1.get(i).getDefaultState();
                    } else {
                        blockState = classicVoidBlocks.get(i).getDefaultState();
                    }
                    chunk.setBlockState(mutable, blockState, false);
                    heightmap.trackUpdate(k, i, l, blockState);
                    heightmap2.trackUpdate(k, i, l, blockState);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    protected Codec<? extends ClassicEndGenerator> getCodec() {
        return CODEC;
    }

    static {
        for (int i = 0; i < 46; i++) {classicEndBlocks.add(Blocks.AIR);}
        for (int i = 0; i < 24; i++) {classicEndBlocks.add(Blocks.END_STONE);}

        for (int i = 0; i < 46; i++) {classicEndBlocks1.add(Blocks.AIR);}
        for (int i = 0; i < 3; i++) {classicEndBlocks1.add(Blocks.END_STONE);}
        for (int i = 0; i < 21; i++) {classicEndBlocks1.add(Blocks.AIR);}

        for (int i = 0; i < 70; i++) {classicVoidBlocks.add(Blocks.AIR);}
        }
}
