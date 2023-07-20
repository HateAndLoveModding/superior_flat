package com.example.superior_flat.Leaves;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LeavesEndGenerator extends NoiseChunkGenerator {
    public final Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry;
    public final Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;
    public static List<Block> leafEndBlocks = new ArrayList<>();
    public static List<Block> leafEndBlocks1 = new ArrayList<>();

    public static final Codec<LeavesEndGenerator> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            NoiseChunkGenerator.createStructureSetRegistryGetter(instance).and(
                                            instance
                                                    .group(
                                                            RegistryOps.createRegistryCodec(Registry.NOISE_KEY).forGetter(generator -> generator.noiseRegistry),
                                                            (BiomeSource.CODEC.fieldOf("biome_source")).forGetter(LeavesEndGenerator::getBiomeSource),
                                                            (ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings")).forGetter(LeavesEndGenerator::getSettings)))
                                    .apply(instance, instance.stable(LeavesEndGenerator::new)));

    public LeavesEndGenerator(Registry<StructureSet> structureSetRegistry, Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry, BiomeSource populationSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(structureSetRegistry, noiseRegistry, populationSource, settings);

        this.noiseRegistry = noiseRegistry;
        this.indexedFeaturesListSupplier = Suppliers.memoize(() -> PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(), true));
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        for(int i = Math.min(leafEndBlocks.size(), world.getTopY()) - 1; i >= 0; --i) {
            BlockState blockState = leafEndBlocks.get(i).getDefaultState();
            if (blockState != null && heightmap.getBlockPredicate().test(blockState)) {
                return world.getBottomY() + i + 1;
            }
        }

        return world.getBottomY();
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(
            Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor accessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

        Registry<Biome> biomeRegistry = accessor.getRegistryManager().get(Registry.BIOME_KEY);
        for (int i = 0; i < Math.min(chunk.getHeight(), leafEndBlocks.size()); ++i) {
            BlockState blockState;
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    mutable.set(k, i, l);
                    Biome currentBiome = chunk.getBiomeForNoiseGen(k >> 2, i, l >> 2).value();
                    if (currentBiome.equals(biomeRegistry.get(BiomeKeys.THE_END))) {
                        blockState = leafEndBlocks1.get(i).getDefaultState();
                    } else {
                        blockState = leafEndBlocks.get(i).getDefaultState();
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
    public void buildSurface(Chunk chunk, HeightContext heightContext, NoiseConfig noiseConfig, StructureAccessor structureAccessor, BiomeAccess biomeAccess, Registry<Biome> biomeRegistry, Blender blender) {}

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }
    @Override
    protected Codec<? extends LeavesEndGenerator> getCodec() {
        return CODEC;
    }
    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        ChunkPos chunkPos = chunk.getPos();
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());
        BlockPos minChunkPos = chunkSectionPos.getMinPos();

        Registry<Structure> structureRegistry = world.getRegistryManager().get(Registry.STRUCTURE_KEY);
        Map<Integer, List<Structure>> structuresByStep = structureRegistry.stream().collect(Collectors.groupingBy(structureType -> structureType.getFeatureGenerationStep().ordinal()));
        List<PlacedFeatureIndexer.IndexedFeatures> indexedFeatures = this.indexedFeaturesListSupplier.get();

        ChunkRandom chunkRandom = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
        long populationSeed = chunkRandom.setPopulationSeed(world.getSeed(), minChunkPos.getX(), minChunkPos.getZ());

        // Get all surrounding biomes for biome-based structures
        ObjectArraySet<Biome> biomeSet = new ObjectArraySet<>();
        ChunkPos.stream(chunkSectionPos.toChunkPos(), 1).forEach(curChunkPos -> {
            Chunk curChunk = world.getChunk(curChunkPos.x, curChunkPos.z);
            for (ChunkSection chunkSection : curChunk.getSectionArray()) {
                chunkSection.getBiomeContainer().forEachValue(registryEntry -> biomeSet.add(registryEntry.value()));
            }
        });
        biomeSet.retainAll(this.biomeSource.getBiomes().stream().map(RegistryEntry::value).collect(Collectors.toSet()));

        int numIndexedFeatures = indexedFeatures.size();
        try {
            Registry<PlacedFeature> placedFeatures = world.getRegistryManager().get(Registry.PLACED_FEATURE_KEY);
            int numSteps = Math.max(GenerationStep.Feature.values().length, numIndexedFeatures);
            for (int genStep = 0; genStep < numSteps; ++genStep) {
                int m = 0;
                if (structureAccessor.shouldGenerateStructures()) {
                    List<Structure> structuresForStep = structuresByStep.getOrDefault(genStep, Collections.emptyList());
                    for (Structure structure : structuresForStep) {
                        chunkRandom.setDecoratorSeed(populationSeed, m, genStep);
                        Supplier<String> featureNameSupplier = () -> structureRegistry.getKey(structure).map(Object::toString).orElseGet(structure::toString);
                        world.setCurrentlyGeneratingStructureName(featureNameSupplier);
                        structureAccessor.getStructureStarts(chunkSectionPos, structure).forEach((start) -> {
                            start.place(world, structureAccessor, this, chunkRandom, getBlockBoxForChunk(chunk), chunkPos);
                        });
                        ++m;
                    }
                }
                if (genStep >= numIndexedFeatures) continue;
                IntArraySet intSet = new IntArraySet();
                for (Biome biome : biomeSet) {
                    List<RegistryEntryList<PlacedFeature>> biomeFeatureStepList = biome.getGenerationSettings().getFeatures();
                    if (genStep >= biomeFeatureStepList.size()) continue;
                    RegistryEntryList<PlacedFeature> biomeFeaturesForStep = biomeFeatureStepList.get(genStep);
                    PlacedFeatureIndexer.IndexedFeatures indexedFeature = indexedFeatures.get(genStep);
                    biomeFeaturesForStep.stream().map(RegistryEntry::value).forEach(placedFeature -> intSet.add(indexedFeature.indexMapping().applyAsInt(placedFeature)));
                }
                int n = intSet.size();
                int[] is = intSet.toIntArray();
                Arrays.sort(is);
                PlacedFeatureIndexer.IndexedFeatures indexedFeature = indexedFeatures.get(genStep);
                for (int o = 0; o < n; ++o) {
                    int p = is[o];
                    PlacedFeature placedFeature = indexedFeature.features().get(p);
                    Supplier<String> placedFeatureNameSupplier = () -> placedFeatures.getKey(placedFeature).map(Object::toString).orElseGet(placedFeature::toString);
                    chunkRandom.setDecoratorSeed(populationSeed, p, genStep);
                    world.setCurrentlyGeneratingStructureName(placedFeatureNameSupplier);
                    try {
                        placedFeature.generate(world, this, chunkRandom, minChunkPos);
                    } catch (Exception e) {
                        CrashReport crashReport = CrashReport.create(e, "Feature placement");
                        crashReport.addElement("Feature").add("Description", placedFeatureNameSupplier::get);
                        throw new CrashException(crashReport);
                    }
                }
            }
            world.setCurrentlyGeneratingStructureName(null);
        } catch (Exception e) {
            CrashReport crashReport = CrashReport.create(e, "Biome decoration");
            crashReport.addElement("Generation").add("CenterX", chunkPos.x).add("CenterZ", chunkPos.z).add("Seed", populationSeed);
            throw new CrashException(crashReport);
        }
    }

    private static BlockBox getBlockBoxForChunk(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        HeightLimitView heightLimitView = chunk.getHeightLimitView();
        int bottomY = heightLimitView.getBottomY() + 1;
        int topY = heightLimitView.getTopY() - 1;
        return new BlockBox(startX, bottomY, startZ, startX + 15, topY, startZ + 15);
    }

    static {
        for (int i = 0; i < 67; i++) {leafEndBlocks.add(Blocks.AIR);}
        for (int i = 0; i < 3; i++) {leafEndBlocks.add(Blocks.OAK_LEAVES);}

        for (int i = 0; i < 46; i++) {leafEndBlocks1.add(Blocks.AIR);}
        for (int i = 0; i < 3; i++) {leafEndBlocks1.add(Blocks.OAK_LEAVES);}
        for (int i = 0; i < 21; i++) {leafEndBlocks1.add(Blocks.AIR);}
    }
}
