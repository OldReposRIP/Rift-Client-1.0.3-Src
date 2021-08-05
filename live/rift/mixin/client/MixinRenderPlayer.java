package live.rift.mixin.client;

import live.rift.RiftMod;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = { RenderPlayer.class},
    priority = Integer.MAX_VALUE
)
public class MixinRenderPlayer {

    @Inject(
        method = { "renderEntityName"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void renderLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        if (RiftMod.fevents.moduleManager != null && RiftMod.fevents.moduleManager.getModule("NameTags") != null && RiftMod.fevents.moduleManager.getModule("NameTags").isEnabled()) {
            info.cancel();
        }

    }
}
