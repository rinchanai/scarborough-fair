package dev.rinchan.scarboroughfair.registry;

import com.mojang.serialization.MapCodec;
import dev.rinchan.scarboroughfair.ScarboroughFair;
import dev.rinchan.scarboroughfair.worldgen.EndShapeOverworldContentChunkGenerator;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ScarboroughFairRegistries {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(
        BuiltInRegistries.CHUNK_GENERATOR,
        ScarboroughFair.MOD_ID
    );

    public static final Supplier<MapCodec<? extends ChunkGenerator>> END_SHAPE_OVERWORLD_CONTENT = CHUNK_GENERATORS.register(
        "end_shape_overworld_content",
        () -> EndShapeOverworldContentChunkGenerator.CODEC
    );

    private ScarboroughFairRegistries() {
    }

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }
}
