package dev.rinchan.scarboroughfair.feature;

import com.mojang.serialization.Codec;
import dev.rinchan.scarboroughfair.registry.ScarboroughFairRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CenterVoidHoleFeature extends Feature<NoneFeatureConfiguration> {
    public CenterVoidHoleFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int chunkX = Math.floorDiv(context.origin().getX(), 16) * 16;
        int chunkZ = Math.floorDiv(context.origin().getZ(), 16) * 16;
        if (chunkX != 0 || chunkZ != 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        int centerSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 0, 0) - 1;
        int minY = level.getMinBuildHeight();
        if (centerSurfaceY <= minY) {
            return false;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = false;
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                int columnSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (columnSurfaceY <= minY) {
                    continue;
                }
                if (isPortalColumn(x, z)) {
                    pos.set(x, minY, z);
                    if (level.ensureCanWrite(pos)) {
                        level.setBlock(pos, ScarboroughFairRegistries.RETURN_PORTAL.get().defaultBlockState(), 2);
                        changed = true;
                    }
                }
                for (int y = minY + 1; y <= columnSurfaceY + 3; y++) {
                    if (!isPunctureColumn(x, z, y, columnSurfaceY, minY)) {
                        continue;
                    }
                    pos.set(x, y, z);
                    if (level.ensureCanWrite(pos)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        changed = true;
                    }
                }
                if (isScorchedRim(x, z) || isSurfaceCrack(x, z)) {
                    scorchSurface(level, pos, x, columnSurfaceY, z);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static boolean isPortalColumn(int x, int z) {
        return Math.abs(x) <= 1 && Math.abs(z) <= 1;
    }

    private static boolean isPunctureColumn(int x, int z, int y, int surfaceY, int minY) {
        if (isPortalColumn(x, z)) {
            return true;
        }
        int height = Math.max(1, surfaceY - minY);
        double nearSurface = Math.max(0.0D, Math.min(1.0D, (double) (y - minY) / (double) height));
        double shiftX = Math.sin(y * 0.071D) * 0.28D + nearSurface * 0.55D;
        double shiftZ = Math.cos(y * 0.059D) * 0.22D - nearSurface * 0.35D;
        double radiusX = 1.30D + nearSurface * 2.85D;
        double radiusZ = 1.20D + nearSurface * 2.35D;
        double dx = x - shiftX;
        double dz = z - shiftZ;
        double oval = (dx * dx) / (radiusX * radiusX) + (dz * dz) / (radiusZ * radiusZ);
        double raggedEdge = (hash(x * 29 + y, z * 37 - y) % 100) / 100.0D;
        if (oval <= 0.86D + raggedEdge * 0.18D) {
            return true;
        }

        int depthFromSurface = surfaceY - y;
        if (depthFromSurface > 10) {
            return false;
        }
        if (x >= 2 && x <= 7 && Math.abs(z + 1) <= 1 && hash(x + y, z) % 5 != 0) {
            return true;
        }
        return z >= -7 && z <= -2 && Math.abs(x - 1) <= 1 && hash(x, z - y) % 6 == 0;
    }

    private static boolean isScorchedRim(int x, int z) {
        int distanceSq = x * x + z * z;
        return distanceSq >= 12 && distanceSq <= 52 && hash(x, z) % 8 != 0;
    }

    private static boolean isSurfaceCrack(int x, int z) {
        int distanceSq = x * x + z * z;
        if (distanceSq < 22 || distanceSq > 100) {
            return false;
        }
        return nearRay(x, z, 1.0D, -0.20D, 4.0D, 9.5D, 0.65D)
            || nearRay(x, z, -0.65D, -0.75D, 4.5D, 9.0D, 0.55D)
            || nearRay(x, z, -0.15D, 1.0D, 3.5D, 8.0D, 0.50D)
            || nearRay(x, z, 0.88D, 0.55D, 4.0D, 7.5D, 0.50D);
    }

    private static boolean nearRay(int x, int z, double dx, double dz, double min, double max, double width) {
        double length = Math.sqrt(dx * dx + dz * dz);
        dx /= length;
        dz /= length;
        double along = x * dx + z * dz;
        double cross = Math.abs(x * dz - z * dx);
        return along >= min && along <= max && cross <= width && hash(x, z) % 4 != 0;
    }

    private static void scorchSurface(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int surfaceY, int z) {
        boolean crack = isSurfaceCrack(x, z);
        pos.set(x, surfaceY, z);
        if (level.ensureCanWrite(pos) && !level.getBlockState(pos).isAir()) {
            if (crack && hash(x + 11, z - 5) % 5 == 0) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            } else {
                level.setBlock(pos, scorchedBlock(x, z), 2);
            }
        }
        pos.set(x, surfaceY - 1, z);
        if (level.ensureCanWrite(pos) && !level.getBlockState(pos).isAir() && (crack || hash(x, z) % 3 == 0)) {
            level.setBlock(pos, scorchedBlock(z, x), 2);
        }
        if (!crack && hash(x + 17, z + 3) % 9 == 0) {
            pos.set(x, surfaceY + 1, z);
            if (level.ensureCanWrite(pos) && level.getBlockState(pos).isAir()) {
                level.setBlock(pos, scorchedBlock(z, x), 2);
            }
        }
    }

    private static BlockState scorchedBlock(int x, int z) {
        int value = hash(x, z) % 9;
        if (value == 0) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        if (value <= 3) {
            return Blocks.COBBLESTONE.defaultBlockState();
        }
        if (value <= 5) {
            return Blocks.STONE.defaultBlockState();
        }
        return Blocks.TUFF.defaultBlockState();
    }

    private static int hash(int x, int z) {
        int value = x * 73428767 ^ z * 91227153 ^ 0x6C8E9CF5;
        value ^= value >>> 16;
        return value & Integer.MAX_VALUE;
    }
}
