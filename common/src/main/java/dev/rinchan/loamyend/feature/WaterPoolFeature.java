package dev.rinchan.loamyend.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public class WaterPoolFeature extends Feature<NoneFeatureConfiguration> {
    public WaterPoolFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int centerX = context.origin().getX();
        int centerZ = context.origin().getZ();
        int centerY = surfaceY(level, centerX, centerZ);
        if (centerY < level.getMinBuildHeight() + 4 || centerY > 150 || !isIslandSurface(level, centerX, centerY, centerZ)) {
            return false;
        }

        int radius = 3 + random.nextInt(4);
        int depth = 1 + random.nextInt(2);
        boolean changed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz) + random.nextDouble() * 0.55;
                if (distance > radius) {
                    continue;
                }
                int x = centerX + dx;
                int z = centerZ + dz;
                int y = surfaceY(level, x, z);
                if (Math.abs(y - centerY) > 4 || !isIslandSurface(level, x, y, z)) {
                    continue;
                }
                int localDepth = distance < radius - 1 ? depth : 1;
                for (int yy = y; yy > y - localDepth; yy--) {
                    pos.set(x, yy, z);
                    if (!level.ensureCanWrite(pos)) {
                        continue;
                    }
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                    level.scheduleTick(pos, Fluids.WATER, 1);
                    changed = true;
                }
            }
        }
        return changed;
    }

    static int surfaceY(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
    }

    static boolean isIslandSurface(WorldGenLevel level, int x, int y, int z) {
        BlockPos floor = new BlockPos(x, y, z);
        if (!level.ensureCanWrite(floor)) {
            return false;
        }
        var state = level.getBlockState(floor);
        return !state.isAir() && state.getFluidState().isEmpty() && (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK));
    }
}
