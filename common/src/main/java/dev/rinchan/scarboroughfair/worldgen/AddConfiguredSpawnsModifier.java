package dev.rinchan.scarboroughfair.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rinchan.scarboroughfair.ScarboroughFairConfig;
import dev.rinchan.scarboroughfair.registry.ScarboroughFairRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record AddConfiguredSpawnsModifier(HolderSet<Biome> biomes) implements BiomeModifier {
    public static final MapCodec<AddConfiguredSpawnsModifier> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddConfiguredSpawnsModifier::biomes)
        ).apply(instance, AddConfiguredSpawnsModifier::new)
    );

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !this.biomes.contains(biome)) {
            return;
        }
        if (ScarboroughFairConfig.GENERATE_PASSIVE_MOBS.get()) {
            addPassiveSpawns(builder);
        }
        if (ScarboroughFairConfig.GENERATE_HOSTILE_MOBS.get()) {
            addHostileSpawns(builder);
        }
    }

    private static void addPassiveSpawns(ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        builder.getMobSpawnSettings().addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 8, 8));
        builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 12, 4, 4));
        builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 4, 4));
        builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 4, 4));
        builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 4, 2, 4));
        builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 5, 2, 6));
        builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 3));
        builder.getMobSpawnSettings().addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 4, 6));
    }

    private static void addHostileSpawns(ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 4, 4));
        builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 95, 4, 4));
        builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 100, 4, 4));
        builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 100, 4, 4));
        builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ScarboroughFairRegistries.ADD_CONFIGURED_SPAWNS.get();
    }
}
