package dev.rinchan.scarboroughfair.neoforge;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

final class VmVideoClientHarness {
    private static final int OUTER_SCENIC_TICKS = 140;
    private static final int CRUISE_TIMEOUT_TICKS = 850;
    private static final int APPROACH_TIMEOUT_TICKS = 980;
    private static final int HOLE_VIEW_TIMEOUT_TICKS = 1030;
    private static final double CENTER_X = 0.5D;
    private static final double CENTER_Z = 0.5D;
    private static final double CENTER_SURFACE_Y = 64.0D;

    private static int ticks;
    private static int sceneTicks;
    private static boolean connecting;
    private static boolean preparedIntegratedServer;
    private static boolean drivingScarboroughScene;

    private VmVideoClientHarness() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(VmVideoClientHarness::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ticks++;
        if (Boolean.getBoolean("scarboroughFair.vmVideo.connect") && !connecting && minecraft.player == null && minecraft.level == null && ticks >= 40 && minecraft.screen != null) {
            connecting = true;
            ConnectScreen.startConnecting(
                minecraft.screen,
                minecraft,
                ServerAddress.parseString("localhost"),
                new ServerData("Scarborough Fair Video", "localhost", ServerData.Type.OTHER),
                false,
                null
            );
        }
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (!preparedIntegratedServer && minecraft.getSingleplayerServer() != null) {
            ServerPlayer serverPlayer = minecraft.getSingleplayerServer().getPlayerList().getPlayer(minecraft.player.getUUID());
            if (serverPlayer != null) {
                preparedIntegratedServer = true;
                VmVideoServerHarness.prepareScene(serverPlayer);
            }
        }
        minecraft.options.tutorialStep = TutorialSteps.NONE;
        minecraft.options.hideGui = false;
        minecraft.options.pauseOnLostFocus = false;
        minecraft.options.fov().set(85);
        minecraft.options.renderDistance().set(12);
        minecraft.getToasts().clear();
        driveScarboroughScene(minecraft);
    }

    private static void driveScarboroughScene(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || !player.level().dimension().equals(ScarboroughFair.LEVEL)) {
            releaseVideoKeys(minecraft);
            drivingScarboroughScene = false;
            sceneTicks = 0;
            return;
        }
        if (!drivingScarboroughScene) {
            drivingScarboroughScene = true;
            sceneTicks = 0;
        }
        sceneTicks++;
        releaseVideoKeys(minecraft);

        double dx = player.getX() - CENTER_X;
        double dz = player.getZ() - CENTER_Z;
        double distanceToCenter = Math.sqrt(dx * dx + dz * dz);
        if (sceneTicks <= OUTER_SCENIC_TICKS) {
            lookAt(player, player.getX(), CENTER_SURFACE_Y, player.getZ(), 52.0F, 76.0F, 5.0F, 3.0F);
            return;
        }
        if (distanceToCenter > 260.0D && sceneTicks <= CRUISE_TIMEOUT_TICKS) {
            lookAt(player, CENTER_X, CENTER_SURFACE_Y + 42.0D, CENTER_Z, 1.5F, 10.0F, 5.0F, 1.5F);
            minecraft.options.keyUp.setDown(true);
            minecraft.options.keySprint.setDown(true);
            return;
        }
        if (distanceToCenter > 14.0D && sceneTicks <= APPROACH_TIMEOUT_TICKS) {
            lookAt(player, CENTER_X, CENTER_SURFACE_Y + 7.0D, CENTER_Z, 16.0F, 48.0F, 4.0F, 2.5F);
            minecraft.options.keyUp.setDown(true);
            if (player.getY() > CENTER_SURFACE_Y + 42.0D) {
                minecraft.options.keyShift.setDown(true);
            }
            return;
        }
        if (sceneTicks <= HOLE_VIEW_TIMEOUT_TICKS) {
            lookAt(player, CENTER_X, CENTER_SURFACE_Y - 1.0D, CENTER_Z, 58.0F, 82.0F, 5.0F, 3.0F);
            if (player.getY() > CENTER_SURFACE_Y + 42.0D) {
                minecraft.options.keyShift.setDown(true);
            }
        }
    }

    private static void releaseVideoKeys(Minecraft minecraft) {
        minecraft.options.keyUp.setDown(false);
        minecraft.options.keyDown.setDown(false);
        minecraft.options.keyLeft.setDown(false);
        minecraft.options.keyRight.setDown(false);
        minecraft.options.keyJump.setDown(false);
        minecraft.options.keyShift.setDown(false);
        minecraft.options.keySprint.setDown(false);
    }

    private static void lookAt(LocalPlayer player, double targetX, double targetY, double targetZ, float minPitch, float maxPitch, float maxYawStep, float maxPitchStep) {
        double dx = targetX - player.getX();
        double dy = targetY - player.getEyeY();
        double dz = targetZ - player.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float targetPitch = (float) (-(Mth.atan2(dy, horizontalDistance) * Mth.RAD_TO_DEG));
        targetPitch = Mth.clamp(targetPitch, minPitch, maxPitch);
        float yaw = approachDegrees(player.getYRot(), targetYaw, maxYawStep);
        float pitch = approach(player.getXRot(), targetPitch, maxPitchStep);
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
    }

    private static float approachDegrees(float current, float target, float maxStep) {
        float delta = Mth.wrapDegrees(target - current);
        delta = Mth.clamp(delta, -maxStep, maxStep);
        return current + delta;
    }

    private static float approach(float current, float target, float maxStep) {
        float delta = Mth.clamp(target - current, -maxStep, maxStep);
        return current + delta;
    }
}
