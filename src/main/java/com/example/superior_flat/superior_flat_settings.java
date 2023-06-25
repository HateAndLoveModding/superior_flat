package com.example.superior_flat;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import java.util.ArrayList;
import java.util.List;
public class superior_flat_settings {
    public static boolean generateFeatures = true;
    public static boolean generateAllStructures = true;
    public static boolean generateVillages = true;
    public static boolean generateStrongholds = true;

    public static List<Block> worldBlocks;
    public static List<Block> riverBlocks;
    public static List<Block> myceliumBlocks;
    public static List<Block> badlandsBlocks;
    public static List<Block> deepOceanBlocks;
    public static List<Block> shallowOceanBlocks;
    public static List<Block> desertBlocks;
    public static List<Block> beachBlocks;
    public static List<Block> swampBlocks;
    public static List<Block> stoneBlocks;
    public static List<Block> mangroveBlocks;
    public static List<Block> voidBlocks;

    public static List<Block> netherBlocks;
    public static List<Block> warpedBlocks;
    public static List<Block> crimsonBlocks;
    public static List<Block> soilBlocks;
    public static List<Block> sandBlocks;
    public static List<Block> basaltBlocks;

    public static List<Block> endBlocks;
    public static List<Block> highlandBlocks;
    public static List<Block> midlandBlocks;
    public static List<Block> barrenBlocks;
    public static List<Block> smallBlocks;
    static {
        worldBlocks = new ArrayList<>();
        worldBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {worldBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {worldBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 11; i++) {worldBlocks.add(Blocks.DIRT);}
        worldBlocks.add(Blocks.GRASS_BLOCK);

        riverBlocks = new ArrayList<>();
        riverBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {riverBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {riverBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 6; i++) {riverBlocks.add(Blocks.DIRT);}
        for (int i = 0; i < 7; i++) {riverBlocks.add(Blocks.WATER);}
        riverBlocks.add(Blocks.WATER);

        myceliumBlocks = new ArrayList<>();
        myceliumBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {myceliumBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {myceliumBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 11; i++) {myceliumBlocks.add(Blocks.DIRT);}
        myceliumBlocks.add(Blocks.MYCELIUM);

        badlandsBlocks = new ArrayList<>();
        badlandsBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {badlandsBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {badlandsBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 12; i++) {badlandsBlocks.add(Blocks.TERRACOTTA);}

        deepOceanBlocks = new ArrayList<>();
        deepOceanBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {deepOceanBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {deepOceanBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 12; i++) {deepOceanBlocks.add(Blocks.WATER);}

        shallowOceanBlocks = new ArrayList<>();
        shallowOceanBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {shallowOceanBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {shallowOceanBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 4; i++) {shallowOceanBlocks.add(Blocks.DIRT);}
        for (int i = 0; i < 9; i++) {shallowOceanBlocks.add(Blocks.WATER);}

        desertBlocks = new ArrayList<>();
        desertBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {desertBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {desertBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 12; i++) {desertBlocks.add(Blocks.SAND);}

        beachBlocks = new ArrayList<>();
        beachBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {beachBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {beachBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 7; i++) {beachBlocks.add(Blocks.DIRT);}
        for (int i = 0; i < 6; i++) {beachBlocks.add(Blocks.SAND);}

        stoneBlocks = new ArrayList<>();
        stoneBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {stoneBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 78; i++) {stoneBlocks.add(Blocks.STONE);}

        swampBlocks = new ArrayList<>();
        swampBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {swampBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {swampBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 9; i++) {swampBlocks.add(Blocks.DIRT);}
        for (int i = 0; i < 4; i++) {swampBlocks.add(Blocks.WATER);}

        mangroveBlocks = new ArrayList<>();
        mangroveBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {mangroveBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {mangroveBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 11; i++) {mangroveBlocks.add(Blocks.DIRT);}
        mangroveBlocks.add(Blocks.MUD);

        voidBlocks = new ArrayList<>();
        voidBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 140; i++) {voidBlocks.add(Blocks.AIR);}

        netherBlocks = new ArrayList<>();
        netherBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 64; i++) {netherBlocks.add(Blocks.NETHERRACK);}

        warpedBlocks = new ArrayList<>();
        warpedBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 63; i++) {warpedBlocks.add(Blocks.NETHERRACK);}
        warpedBlocks.add(Blocks.WARPED_NYLIUM);

        crimsonBlocks = new ArrayList<>();
        crimsonBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 63; i++) {crimsonBlocks.add(Blocks.NETHERRACK);}
        crimsonBlocks.add(Blocks.CRIMSON_NYLIUM);

        soilBlocks = new ArrayList<>();
        soilBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 63; i++) {soilBlocks.add(Blocks.NETHERRACK);}
        soilBlocks.add(Blocks.SOUL_SOIL);

        sandBlocks = new ArrayList<>();
        sandBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 63; i++) {sandBlocks.add(Blocks.NETHERRACK);}
        sandBlocks.add(Blocks.SOUL_SAND);

        basaltBlocks = new ArrayList<>();
        basaltBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 64; i++) {basaltBlocks.add(Blocks.NETHERRACK);}

        endBlocks = new ArrayList<>();
        for (int i = 0; i < 9; i++) {endBlocks.add(Blocks.AIR);}
        for (int i = 0; i < 17; i++) {endBlocks.add(Blocks.END_STONE);}

        highlandBlocks = new ArrayList<>();
        for (int i = 0; i < 26; i++) {highlandBlocks.add(Blocks.END_STONE);}

        midlandBlocks = new ArrayList<>();
        for (int i = 0; i < 17; i++) {midlandBlocks.add(Blocks.AIR);}
        for (int i = 0; i < 9; i++) {midlandBlocks.add(Blocks.END_STONE);}

        barrenBlocks = new ArrayList<>();
        for (int i = 0; i < 13; i++) {barrenBlocks.add(Blocks.AIR);}
        for (int i = 0; i < 13; i++) {barrenBlocks.add(Blocks.END_STONE);}

        smallBlocks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {smallBlocks.add(Blocks.AIR);}
        for (int i = 0; i < 6; i++) {smallBlocks.add(Blocks.END_STONE);}
    }
}
