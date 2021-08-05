package live.rift.mixin.client;

import live.rift.module.modules.render.NoRender;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ GuiIngame.class})
public class MixinGuiIngame {

    @Inject(
        method = { "renderPortal"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    protected void renderPortal(float n, ScaledResolution scaledResolution, CallbackInfo info) {
        if (NoRender.i.isEnabled() && NoRender.i.portal.getValBoolean()) {
            info.cancel();
        }

    }

    @Inject(
        method = { "renderPumpkinOverlay"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    protected void renderPumpkinOverlay(ScaledResolution scaledRes, CallbackInfo info) {
        if (NoRender.i.isEnabled() && NoRender.i.pumpkin.getValBoolean()) {
            info.cancel();
        }

    }
}
