package com.example.superior_flat;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.*;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.*;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
public class OverworldGenerator extends NoiseChunkGenerator {
    public final Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry;
    public final Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;

    private final List<Block> blockList = Collections.unmodifiableList(superior_flat_settings.worldBlocks);
    private final List<Block> riverList = Collections.unmodifiableList(superior_flat_settings.riverBlocks);
    private final List<Block> myceliumList = Collections.unmodifiableList(superior_flat_settings.myceliumBlocks);
    private final List<Block> badlandsList = Collections.unmodifiableList(superior_flat_settings.badlandsBlocks);
    private final List<Block> deepOceanList = Collections.unmodifiableList(superior_flat_settings.deepOceanBlocks);
    private final List<Block> shallowOceanList = Collections.unmodifiableList(superior_flat_settings.shallowOceanBlocks);
    private final List<Block> desertList = Collections.unmodifiableList(superior_flat_settings.desertBlocks);
    private final List<Block> beachList = Collections.unmodifiableList(superior_flat_settings.beachBlocks);
    private final List<Block> stoneList = Collections.unmodifiableList(superior_flat_settings.stoneBlocks);
    private final List<Block> swampList = Collections.unmodifiableList(superior_flat_settings.swampBlocks);
    private final List<Block> mangroveList = Collections.unmodifiableList(superior_flat_settings.mangroveBlocks);
    private final List<Block> voidList = Collections.unmodifiableList(superior_flat_settings.voidBlocks);

    public static final Codec<OverworldGenerator> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            NoiseChunkGenerator.createStructureSetRegistryGetter(instance).and(
                                            instance
                                                    .group(
                                                            RegistryOps.createRegistryCodec(Registry.NOISE_KEY).forGetter(generator -> generator.noiseRegistry),
                                                            (BiomeSource.CODEC.fieldOf("biome_source")).forGetter(OverworldGenerator::getBiomeSource),
                                                            (ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings")).forGetter(OverworldGenerator::getSettings)))
                                    .apply(instance, instance.stable(OverworldGenerator::new)));

    public OverworldGenerator(Registry<StructureSet> structureSetRegistry, Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry, BiomeSource populationSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(structureSetRegistry, noiseRegistry, populationSource, settings);

        this.noiseRegistry = noiseRegistry;
        this.indexedFeaturesListSupplier = Suppliers.memoize(() -> PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(), true));
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        for(int i = Math.min(blockList.size(), world.getTopY()) - 1; i >= 0; --i) {
            BlockState blockState = blockList.get(i).getDefaultState();
            if (blockState != null && heightmap.getBlockPredicate().test(blockState)) {
                return world.getBottomY() + i + 1;
            }
        }

        return world.getBottomY();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(world.getBottomY(), blockList.stream().limit(world.getHeight()).map((state) -> state == null ? Blocks.AIR.getDefaultState() : state.getDefaultState()).toArray(BlockState[]::new));
    }
    @Override
    public CompletableFuture<Chunk> populateNoise(
            Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor accessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

        Registry<Biome> biomeRegistry = accessor.getRegistryManager().get(Registry.BIOME_KEY);
        for (int i = 0; i < Math.min(chunk.getHeight(), blockList.size()); ++i) {
            BlockState blockState = blockList.get(i).getDefaultState();
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    mutable.set(k, i - 63, l);
                    Biome currentBiome = chunk.getBiomeForNoiseGen(k >> 2, i, l >> 2).value();
                    if (currentBiome.equals(biomeRegistry.get(BiomeKeys.RIVER))) {
                        blockState = riverList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.MUSHROOM_FIELDS))) {
                        blockState = myceliumList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DESERT))) {
                        blockState = desertList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.STONY_SHORE)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.JAGGED_PEAKS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.FROZEN_PEAKS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.STONY_PEAKS))) {
                        blockState = stoneList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.BADLANDS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.WOODED_BADLANDS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.ERODED_BADLANDS))) {
                        blockState = badlandsList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.BEACH))) {
                        blockState = beachList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DEEP_FROZEN_OCEAN))) { //I need help
                        blockState = deepOceanList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DEEP_COLD_OCEAN))) {
                        blockState = deepOceanList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DEEP_OCEAN))) {
                        blockState = deepOceanList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DEEP_LUKEWARM_OCEAN))) {
                        blockState = deepOceanList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.OCEAN))) {
                        blockState = shallowOceanList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.COLD_OCEAN))) {
                        blockState = shallowOceanList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.FROZEN_OCEAN))) {
                        blockState = shallowOceanList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.LUKEWARM_OCEAN))) {
                        blockState = shallowOceanList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.WARM_OCEAN))) {
                        blockState = shallowOceanList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.FROZEN_RIVER))) {
                        blockState = riverList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.SWAMP))) {
                        blockState = swampList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.MANGROVE_SWAMP))) {
                        blockState = mangroveList.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DRIPSTONE_CAVES))) {
                        blockState = voidList.get(i).getDefaultState();
                    } else {
                        blockState = blockList.get(i).getDefaultState();
                    }
                    chunk.setBlockState(mutable, blockState, false);
                    heightmap.trackUpdate(k, i - 63, l, blockState);
                    heightmap2.trackUpdate(k, i - 63, l, blockState);
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
    protected Codec<? extends OverworldGenerator> getCodec() {
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

                        if (superior_flat_settings.generateAllStructures) {
                            structureAccessor.getStructureStarts(chunkSectionPos, structure).forEach((start) -> {
                                start.place(world, structureAccessor, this, chunkRandom, getBlockBoxForChunk(chunk), chunkPos);
                            });
                        } else {
                            if (superior_flat_settings.generateVillages && structure instanceof JigsawStructure) {
                                structureAccessor.getStructureStarts(chunkSectionPos, structure).forEach((start) -> {
                                    start.place(world, structureAccessor, this, chunkRandom, getBlockBoxForChunk(chunk), chunkPos);
                                });
                            }

                            if (superior_flat_settings.generateStrongholds && structure.getType() == StructureType.STRONGHOLD) {
                                structureAccessor.getStructureStarts(chunkSectionPos, structure).forEach((start) -> {
                                    start.place(world, structureAccessor, this, chunkRandom, getBlockBoxForChunk(chunk), chunkPos);
                                });
                            }
                        }

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
                        if (superior_flat_settings.generateFeatures) {
                            placedFeature.generate(world, this, chunkRandom, minChunkPos);
                        }
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
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID + "overworld"), OverworldGenerator.CODEC);
    }
}
