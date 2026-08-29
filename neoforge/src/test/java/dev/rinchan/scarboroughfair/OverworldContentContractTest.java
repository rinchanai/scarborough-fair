package dev.rinchan.scarboroughfair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class OverworldContentContractTest {
    private static final Path ROOT = Path.of(System.getProperty("user.dir")).getFileName().toString().equals("neoforge")
        ? Path.of(System.getProperty("user.dir")).getParent()
        : Path.of(System.getProperty("user.dir"));
    private static final Path RESOURCES = ROOT.resolve("common/src/main/resources");

    @Test
    void dimensionCombinesVanillaEndShapeWithVanillaOverworldContent() throws IOException {
        JsonObject dimension = JsonParser.parseString(Files.readString(
            RESOURCES.resolve("data/scarborough_fair/dimension/scarborough_fair.json")
        )).getAsJsonObject();
        JsonObject generator = dimension.getAsJsonObject("generator");
        assertEquals("scarborough_fair:end_shape_overworld_content", generator.get("type").getAsString());
        assertEquals("minecraft:multi_noise", generator.getAsJsonObject("biome_source").get("type").getAsString());
        assertEquals("minecraft:overworld", generator.getAsJsonObject("biome_source").get("preset").getAsString());
        assertEquals("minecraft:end", generator.get("shape_settings").getAsString());
        assertEquals("minecraft:overworld", generator.get("content_settings").getAsString());
    }

    @Test
    void modOwnsNoBiomesFeaturesOrContentInjection() throws IOException {
        for (String path : List.of(
            "data/scarborough_fair/worldgen/biome",
            "data/scarborough_fair/worldgen/configured_feature",
            "data/scarborough_fair/worldgen/placed_feature",
            "data/scarborough_fair/worldgen/noise_settings",
            "data/scarborough_fair/neoforge",
            "data/c/tags/worldgen/biome",
            "data/minecraft/tags/worldgen/biome"
        )) {
            assertFalse(Files.exists(RESOURCES.resolve(path)), "stale owned content path: " + path);
        }
        assertFalse(Files.exists(RESOURCES.resolve("scarborough_fair.mixins.json")));
    }

    @Test
    void productionContainsNoRuntimeOrCaptureHarness() throws IOException {
        Path packageRoot = ROOT.resolve("common/src/main/java/dev/rinchan/scarboroughfair/neoforge");
        for (String retired : List.of(
            "VmVideoServerHarness.java",
            "VmVideoClientHarness.java",
            "ScarboroughFairSmokeHarness.java",
            "ScreenshotServerHarness.java",
            "ScreenshotClientHarness.java"
        )) {
            assertFalse(Files.exists(packageRoot.resolve(retired)), "production harness remains: " + retired);
        }
        String entrypoint = Files.readString(packageRoot.resolve("ScarboroughFairNeoForge.java"));
        assertFalse(entrypoint.contains("vmVideo"));
        assertFalse(entrypoint.contains("scarboroughFair.smoke"));
        assertFalse(entrypoint.contains("scarboroughFair.screenshot"));
        assertFalse(entrypoint.contains("new BlockPos(OUTER_RADIUS_MIN, 96, 0)"));
        assertTrue(entrypoint.contains("if (spawn == null)"));
        assertFalse(Files.readString(ROOT.resolve("neoforge/build.gradle")).contains("scarboroughFair.screenshot"));
    }

    @Test
    void onlyOptionalFirstSpawnRemainsConfigurable() throws IOException {
        String source = Files.readString(ROOT.resolve(
            "common/src/main/java/dev/rinchan/scarboroughfair/ScarboroughFairConfig.java"
        ));
        assertTrue(source.contains("defaultSpawnInDimension"));
        assertEquals(1, source.split("\\.define\\(", -1).length - 1);
        assertFalse(source.contains("useOverworldStructureList"));
        assertFalse(source.contains("generatePassiveMobs"));
        assertFalse(source.contains("generateHostileMobs"));
    }
}
