package dev.rinchan.scarboroughfair.neoforge;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import dev.rinchan.scarboroughfair.registry.ScarboroughFairRegistries;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(ScarboroughFair.MOD_ID)
public final class ScarboroughFairNeoForge {
    private static final int OUTER_RADIUS_MIN = 1024;
    private static final int OUTER_RADIUS_RANGE = 2304;

    public ScarboroughFairNeoForge(IEventBus modBus) {
        ScarboroughFairRegistries.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::onPlayerChangedDimension);
        if (Boolean.getBoolean("scarboroughFair.smoke")) {
            ScarboroughFairSmokeHarness.register();
        }
        if (Boolean.getBoolean("scarboroughFair.screenshot")) {
            ScreenshotServerHarness.register();
            if (FMLEnvironment.dist.isClient()) {
                ScreenshotClientHarness.register();
            }
        }
    }

    private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (Boolean.getBoolean("scarboroughFair.screenshot") || !event.getTo().equals(ScarboroughFair.LEVEL) || !(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos spawn = findOuterIslandSpawn(level, player.getUUID());
        player.teleportTo(level, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, player.getYRot(), player.getXRot());
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
