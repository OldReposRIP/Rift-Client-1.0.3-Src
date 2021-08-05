package live.rift.mixin.client;

import live.rift.module.modules.render.NoRender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ World.class})
public class MixinWorld {

    @Inject(
        method = { "checkLightFor"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    private void updateLightmapHook(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable info) {
        if (NoRender.i.skylight.getValBoolean() && NoRender.i.isEnabled()) {
            info.setReturnValue(Boolean.valueOf(true));
            info.cancel();
        }

    }
}
