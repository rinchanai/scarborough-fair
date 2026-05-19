package dev.rinchan.scarboroughfair.mixin;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    private ClientLevel level;

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 30.0F))
    private float scarboroughFair$doubleSun(float original) {
        return this.level != null && this.level.dimension().equals(ScarboroughFair.LEVEL) ? original * 2.0F : original;
    }

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0F))
    private float scarboroughFair$doubleMoon(float original) {
        return this.level != null && this.level.dimension().equals(ScarboroughFair.LEVEL) ? original * 2.0F : original;
    }
}
