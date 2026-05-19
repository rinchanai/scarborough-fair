package dev.rinchan.loamyend.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SoilCoatFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();

    public SoilCoatFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int minY = Math.max(level.getMinBuildHeight(), 0);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, 160);
        int chunkX = Math.floorDiv(context.origin().getX(), 16) * 16;
        int chunkZ = Math.floorDiv(context.origin().getZ(), 16) * 16;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        boolean changed = false;

        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int z = chunkZ; z < chunkZ + 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(BlockTags.BASE_STONE_OVERWORLD)) {
                        continue;
                    }
                    if (isExposed(level, pos, neighbor)) {
                        level.setBlock(pos, DIRT, 2);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private static boolean isExposed(WorldGenLevel level, BlockPos pos, BlockPos.MutableBlockPos neighbor) {
        for (Direction direction : Direction.values()) {
            neighbor.setWithOffset(pos, direction);
            BlockState state = level.getBlockState(neighbor);
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
