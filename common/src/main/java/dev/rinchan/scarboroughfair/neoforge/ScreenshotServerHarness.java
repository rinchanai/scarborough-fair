package dev.rinchan.scarboroughfair.neoforge;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

final class ScreenshotServerHarness {
    private static boolean prepared;
    private static int ticksAfterPrepare;

    private ScreenshotServerHarness() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(ScreenshotServerHarness::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(ScreenshotServerHarness::onServerTick);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (prepared || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.server;
        ServerLevel level = server.getLevel(ScarboroughFair.LEVEL);
        if (level == null) {
            throw new IllegalStateException("Scarborough Fair dimension did not load");
        }

        ScenicSpot spot = findScenicSpot(level);
        player.setGameMode(GameType.SPECTATOR);
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 180, 0, false, false));
        player.teleportTo(level, spot.camera().getX() + 0.5D, spot.camera().getY() + 0.5D, spot.camera().getZ() + 0.5D, 135.0F, 4.0F);
        level.setDayTime(1500L);
        level.setWeatherParameters(0, 0, false, false);
        prepared = true;
        System.out.println("SCARBOROUGH_FAIR_SCREENSHOT_SCENE target=" + spot.target() + " camera=" + spot.camera() + " score=" + spot.score());
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (!prepared) {
            return;
        }
        ticksAfterPrepare++;
        if (ticksAfterPrepare >= 1800) {
            event.getServer().halt(false);
        }
    }

    private static ScenicSpot findScenicSpot(ServerLevel level) {
        ScenicSpot best = null;
        for (int cx = 52; cx <= 92; cx++) {
            for (int cz = -24; cz <= 24; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
                ChunkScore score = scoreChunk(level, cx, cz);
                if (score.score() <= 0) {
                    continue;
                }
                BlockPos target = score.target();
                BlockPos camera = new BlockPos(target.getX() + 144, Math.min(target.getY() + 8, 130), target.getZ() + 144);
                ScenicSpot spot = new ScenicSpot(target, camera, score.score());
                if (best == null || spot.score() > best.score()) {
                    best = spot;
                }
            }
        }
        if (best != null) {
            return best;
        }
        return new ScenicSpot(new BlockPos(1024, 80, 0), new BlockPos(1080, 165, 56), 0);
    }

    private static ChunkScore scoreChunk(ServerLevel level, int cx, int cz) {
        int minX = cx * 16;
        int minZ = cz * 16;
        int grass = 0;
        int water = 0;
        int logs = 0;
        int stone = 0;
        int purpur = 0;
        BlockPos target = null;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                if (y <= level.getMinBuildHeight() + 2 || y >= 150) {
                    continue;
                }
                pos.set(x, y, z);
                BlockState surface = level.getBlockState(pos);
                if (surface.is(Blocks.WATER)) {
                    water += 8;
                } else if (surface.is(Blocks.GRASS_BLOCK)) {
                    grass += 2;
                    if (target == null && level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above(2)).isAir()) {
                        target = pos.immutable();
                    }
                }
                for (int yy = Math.max(level.getMinBuildHeight(), y - 24); yy <= Math.min(150, y + 32); yy++) {
                    pos.set(x, yy, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(BlockTags.LOGS)) {
                        logs += 7;
                    } else if (state.is(Blocks.WATER)) {
                        water += 2;
                    } else if (state.is(Blocks.STONE)) {
                        stone++;
                    } else if (state.is(Blocks.PURPUR_BLOCK) || state.is(Blocks.PURPUR_PILLAR) || state.is(Blocks.PURPUR_STAIRS) || state.is(Blocks.PURPUR_SLAB) || state.is(Blocks.END_STONE_BRICKS)) {
                        purpur += 20;
                    }
                }
            }
        }
        if (target == null) {
            target = new BlockPos(minX + 8, 96, minZ + 8);
        }
        return new ChunkScore(target, logs * 24 + water * 12 + grass + stone / 120 - purpur * 80, logs, water, grass);
    }

    private record ChunkScore(BlockPos target, int score, int logs, int water, int grass) {
    }

    private record ScenicSpot(BlockPos target, BlockPos camera, int score) {
    }
}
