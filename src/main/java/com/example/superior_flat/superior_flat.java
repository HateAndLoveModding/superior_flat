package com.example.superior_flat;

import com.example.superior_flat.BottomlessPit.BottomlessPitNetherGenerator;
import com.example.superior_flat.Classic.ClassicEndGenerator;
import com.example.superior_flat.Classic.ClassicNetherGenerator;
import com.example.superior_flat.Classic.ClassicOverworldGenerator;
import com.example.superior_flat.Default.EndGenerator;
import com.example.superior_flat.Default.NetherGenerator;
import com.example.superior_flat.Default.OverworldGenerator;
import com.example.superior_flat.Desert.SoulSandValleyNetherGenerator;
import com.example.superior_flat.Leaves.LeavesEndGenerator;
import com.example.superior_flat.Leaves.LeavesGenerator;
import com.example.superior_flat.TunnelersDream.TunnelersDreamNetherGenerator;
import com.example.superior_flat.TunnelersDream.TunnelersDreamOverworldGenerator;
import com.example.superior_flat.LavaWorld.LavaWorldNetherGenerator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class superior_flat implements ModInitializer {
    //Cave biomes
    public static final String MOD_ID = "superior_flat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "overworld"), OverworldGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "nether"), NetherGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "end"), EndGenerator.CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "classic_overworld"), ClassicOverworldGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "classic_nether"), ClassicNetherGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "classic_end"), ClassicEndGenerator.CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "end_leaves"), LeavesEndGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "leaves"), LeavesGenerator.CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "tunnelers_dream_overworld"), TunnelersDreamOverworldGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "tunnelers_dream_nether"), TunnelersDreamNetherGenerator.CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "lava_world_nether"), LavaWorldNetherGenerator.CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "bottomless_pit_nether"), BottomlessPitNetherGenerator.CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "soul_sand_valley_nether"), SoulSandValleyNetherGenerator.CODEC);
    }
}
