package dev.rinchan.scarboroughfair.feature;

import com.mojang.serialization.Codec;
import dev.rinchan.scarboroughfair.registry.ScarboroughFairRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
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
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 0, 0) - 1;
        int minY = level.getMinBuildHeight();
        if (surfaceY <= minY) {
            return false;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = false;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                pos.set(x, minY, z);
                if (level.ensureCanWrite(pos)) {
                    level.setBlock(pos, ScarboroughFairRegistries.RETURN_PORTAL.get().defaultBlockState(), 2);
                    changed = true;
                }
                for (int y = minY + 1; y <= surfaceY + 1; y++) {
                    pos.set(x, y, z);
                    if (!level.ensureCanWrite(pos)) {
                        continue;
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    changed = true;
                }
            }
        }
        return changed;
    }
}
