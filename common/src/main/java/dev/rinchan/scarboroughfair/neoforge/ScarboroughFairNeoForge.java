package dev.rinchan.scarboroughfair.neoforge;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import dev.rinchan.scarboroughfair.ScarboroughFairConfig;
import dev.rinchan.scarboroughfair.registry.ScarboroughFairRegistries;
import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(ScarboroughFair.MOD_ID)
public final class ScarboroughFairNeoForge {
    private static final int OUTER_RADIUS_MIN = 1024;
    private static final int OUTER_RADIUS_RANGE = 2304;
    private static final Set<UUID> NEW_PLAYER_SPAWNS = ConcurrentHashMap.newKeySet();

    public ScarboroughFairNeoForge(IEventBus modBus) {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, ScarboroughFairConfig.SPEC);
        ScarboroughFairRegistries.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::onPlayerDataLoad);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        if (Boolean.getBoolean("scarboroughFair.smoke")) {
            ScarboroughFairSmokeHarness.register();
        }
        if (Boolean.getBoolean("scarboroughFair.screenshot")) {
            ScreenshotServerHarness.register();
            if (FMLEnvironment.dist.isClient()) {
                ScreenshotClientHarness.register();
            }
        }
        if (Boolean.getBoolean("scarboroughFair.vmVideo")) {
            VmVideoServerHarness.register();
            if (FMLEnvironment.dist.isClient()) {
                VmVideoClientHarness.register();
            }
        }
    }

    private void onPlayerDataLoad(PlayerEvent.LoadFromFile event) {
        File vanillaData = new File(event.getPlayerDirectory(), event.getPlayerUUID() + ".dat");
        if (!vanillaData.exists()) {
            NEW_PLAYER_SPAWNS.add(UUID.fromString(event.getPlayerUUID()));
        }
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !NEW_PLAYER_SPAWNS.remove(player.getUUID()) || !ScarboroughFairConfig.DEFAULT_SPAWN_IN_DIMENSION.get()) {
            return;
        }
        ServerLevel level = player.server.getLevel(ScarboroughFair.LEVEL);
        if (level == null || player.level().dimension() != Level.OVERWORLD) {
            return;
        }
        BlockPos spawn = findOuterIslandSpawn(level, player.getUUID());
        Vec3 position = new Vec3(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
        DimensionTransition transition = new DimensionTransition(level, position, Vec3.ZERO, player.getYRot(), player.getXRot(), false, DimensionTransition.DO_NOTHING);
        if (player.changeDimension(transition) != null) {
            player.setRespawnPosition(ScarboroughFair.LEVEL, spawn, player.getYRot(), true, false);
        }
    }

    private static BlockPos findOuterIslandSpawn(ServerLevel level, UUID playerId) {
        long seed = level.getSeed() ^ playerId.getMostSignificantBits() ^ Long.rotateLeft(playerId.getLeastSignificantBits(), 17);
        RandomSource random = RandomSource.create(seed);
        for (int attempt = 0; attempt < 2048; attempt++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            int radius = OUTER_RADIUS_MIN + random.nextInt(OUTER_RADIUS_RANGE);
            int x = Mth.floor(Math.cos(angle) * radius) + random.nextInt(33) - 16;
            int z = Mth.floor(Math.sin(angle) * radius) + random.nextInt(33) - 16;
            BlockPos candidate = findSafeSurface(level, x, z);
            if (candidate != null) {
                return candidate;
            }
        }
        BlockPos fallback = findSafeSurface(level, OUTER_RADIUS_MIN, 0);
        return fallback != null ? fallback : new BlockPos(OUTER_RADIUS_MIN, 96, 0);
    }

    private static BlockPos findSafeSurface(ServerLevel level, int x, int z) {
        level.getChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16), ChunkStatus.FULL, true);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinBuildHeight() + 2 || y >= level.getMaxBuildHeight() - 2) {
            return null;
        }
        BlockPos feet = new BlockPos(x, y, z);
        BlockState floor = level.getBlockState(feet.below());
        if (floor.isAir() || !floor.getFluidState().isEmpty()) {
            return null;
        }
        if (!level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()) {
            return null;
        }
        if (Math.sqrt((double) x * x + (double) z * z) < OUTER_RADIUS_MIN) {
            return null;
        }
        return feet;
    }
}
