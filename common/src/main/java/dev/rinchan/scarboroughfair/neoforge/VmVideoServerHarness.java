package dev.rinchan.scarboroughfair.neoforge;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

final class VmVideoServerHarness {
    private static final int CENTER_TITLE_TICK = 920;
    private static final int DROP_TICK = 1040;

    private static ServerPlayer player;
    private static int ticks;
    private static boolean prepared;
    private static boolean jumped;
    private static boolean returned;
    private static int returnedTicks;
    private static int landedTicks;
    private static double jumpY;

    private VmVideoServerHarness() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(VmVideoServerHarness::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(VmVideoServerHarness::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(VmVideoServerHarness::onServerTick);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!Boolean.getBoolean("scarboroughFair.vmVideo.connect") || !(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        prepareScene(serverPlayer);
    }

    private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer) || !event.getTo().equals(Level.OVERWORLD)) {
            return;
        }
        returned = true;
        returnedTicks = 0;
        landedTicks = 0;
        player = serverPlayer;
        serverPlayer.setGameMode(GameType.SURVIVAL);
        clearFlight(serverPlayer);
        serverPlayer.setDeltaMovement(0.0D, -2.0D, 0.0D);
        serverPlayer.setOnGround(false);
        serverPlayer.setYRot(0.0F);
        serverPlayer.setXRot(48.0F);
        showTitle(serverPlayer, "已返回主世界", "minecraft:overworld");
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (!prepared || player == null) {
            return;
        }
        ticks++;
        if (!jumped && player.level().dimension().equals(ScarboroughFair.LEVEL)) {
            if (ticks == CENTER_TITLE_TICK) {
                showTitle(player, "中心返回洞", "塌陷裂隙通向主世界");
                System.out.println("SCARBOROUGH_FAIR_VM_VIDEO_CENTER pos=" + player.getX() + "," + player.getY() + "," + player.getZ());
            }
            if (ticks == DROP_TICK) {
                jumped = true;
                player.setGameMode(GameType.SURVIVAL);
                clearFlight(player);
                player.teleportTo(player.server.getLevel(ScarboroughFair.LEVEL), 0.5D, jumpY, 0.5D, 0.0F, 65.0F);
                player.setDeltaMovement(0.0D, -1.2D, 0.0D);
                player.setOnGround(false);
                System.out.println("SCARBOROUGH_FAIR_VM_VIDEO_DROP y=" + jumpY);
            }
        }
        if (returned) {
            returnedTicks++;
            if (returnedTicks > 80 && player.onGround() && player.getY() <= 66.5D) {
                landedTicks++;
                if (landedTicks > 60) {
                    event.getServer().halt(false);
                }
            } else {
                landedTicks = 0;
            }
            if (returnedTicks > 260) {
                event.getServer().halt(false);
            }
        }
        if (ticks > DROP_TICK + 560) {
            event.getServer().halt(false);
        }
    }

    static void prepareScene(ServerPlayer serverPlayer) {
        ServerLevel scarborough = serverPlayer.server.getLevel(ScarboroughFair.LEVEL);
        ServerLevel overworld = serverPlayer.server.getLevel(Level.OVERWORLD);
        if (scarborough == null || overworld == null) {
            throw new IllegalStateException("Required levels are not loaded");
        }
        scarborough.getChunk(0, 0, ChunkStatus.FULL, true);
        int islandSurfaceY = scarborough.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 4, 4);
        BlockPos outerStart = findOuterStart(scarborough);
        jumpY = islandSurfaceY + 55.0D;
        player = serverPlayer;
        ticks = 0;
        prepared = true;
        jumped = false;
        returned = false;
        returnedTicks = 0;
        landedTicks = 0;

        serverPlayer.setGameMode(GameType.CREATIVE);
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60, 0, false, false));
        serverPlayer.setDeltaMovement(0.0D, 0.0D, 0.0D);
        serverPlayer.fallDistance = 0.0F;
        setFlying(serverPlayer, true);
        serverPlayer.getAbilities().setFlyingSpeed(0.70F);
        serverPlayer.onUpdateAbilities();
        serverPlayer.teleportTo(scarborough, outerStart.getX() + 0.5D, outerStart.getY() + 38.0D, outerStart.getZ() + 0.5D, 90.0F, 76.0F);
        scarborough.setDayTime(1500L);
        scarborough.setWeatherParameters(0, 0, false, false);
        overworld.setDayTime(1500L);
        overworld.setWeatherParameters(0, 0, false, false);
        showTitle(serverPlayer, "外岛出发", "飞向 scarborough_fair 主岛");
        System.out.println("SCARBOROUGH_FAIR_VM_VIDEO_READY surfaceY=" + islandSurfaceY + " outer=" + outerStart.getX() + "," + outerStart.getY() + "," + outerStart.getZ());
    }

    private static BlockPos findOuterStart(ServerLevel level) {
        for (int x = 1024; x <= 1536; x += 16) {
            for (int z = -192; z <= 192; z += 16) {
                BlockPos candidate = findSafeSurface(level, x, z);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return new BlockPos(1024, 96, 0);
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
        return feet;
    }

    private static void setFlying(ServerPlayer player, boolean flying) {
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = flying;
        player.onUpdateAbilities();
    }

    private static void clearFlight(ServerPlayer player) {
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    private static void showTitle(ServerPlayer player, String title, String subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 45, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
        player.displayClientMessage(Component.literal(subtitle), true);
        player.sendSystemMessage(Component.literal("[VM Video] " + title + " | " + subtitle));
    }
}
