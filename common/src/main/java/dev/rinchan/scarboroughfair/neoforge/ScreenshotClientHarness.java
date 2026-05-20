package dev.rinchan.scarboroughfair.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import dev.rinchan.scarboroughfair.ScarboroughFair;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.tutorial.TutorialSteps;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

final class ScreenshotClientHarness {
    private static int clientTicks;
    private static int inWorldTicks;
    private static boolean connecting;
    private static boolean captured;

    private ScreenshotClientHarness() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(ScreenshotClientHarness::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        clientTicks++;
        if (!connecting && minecraft.player == null && minecraft.level == null && clientTicks >= 40 && minecraft.screen != null) {
            connecting = true;
            ConnectScreen.startConnecting(
                minecraft.screen,
                minecraft,
                ServerAddress.parseString("localhost"),
                new ServerData("Scarborough Fair Screenshot", "localhost", ServerData.Type.OTHER),
                false,
                null
            );
        }
        if (minecraft.player == null || minecraft.level == null || minecraft.getMainRenderTarget() == null) {
            return;
        }
        if (!minecraft.level.dimension().equals(ScarboroughFair.LEVEL)) {
            inWorldTicks = 0;
            return;
        }
        inWorldTicks++;
        minecraft.options.tutorialStep = TutorialSteps.NONE;
        minecraft.options.hideGui = true;
        minecraft.options.pauseOnLostFocus = false;
        minecraft.options.fov().set(88);
        minecraft.options.renderDistance().set(32);
        minecraft.options.cloudStatus().set(CloudStatus.OFF);
        minecraft.gui.getChat().clearMessages(false);
        minecraft.getToasts().clear();
        minecraft.player.setYRot(135.0F);
        minecraft.player.setXRot(30.0F);

        if (!captured && inWorldTicks >= 1200) {
            save(minecraft, "scarborough-fair-dimension-shader.png");
            captured = true;
        }
        if ((captured && inWorldTicks >= 1260) || inWorldTicks >= 1600) {
            minecraft.stop();
        }
    }

    private static void save(Minecraft minecraft, String fileName) {
        try {
            Path outputDir = Path.of(System.getProperty("scarboroughFair.screenshot.dir", minecraft.gameDirectory.getAbsolutePath()));
            Files.createDirectories(outputDir);
            try (NativeImage image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                image.writeToFile(outputDir.resolve(fileName));
            }
            System.out.println("SCARBOROUGH_FAIR_SCREENSHOT_SAVED " + outputDir.resolve(fileName));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save screenshot " + fileName, e);
        }
    }
}
