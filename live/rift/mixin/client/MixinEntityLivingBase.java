package live.rift.mixin.client;

import live.rift.RiftMod;
import live.rift.event.events.EventPlayerIsPotionActive;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = { EntityLivingBase.class},
    priority = Integer.MAX_VALUE
)
public abstract class MixinEntityLivingBase extends MixinEntity {

    @Shadow
    public void jump() {}

    @Inject(
        method = { "isPotionActive"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void isPotionActive(Potion potionIn, CallbackInfoReturnable callbackInfoReturnable) {
        EventPlayerIsPotionActive l_Event = new EventPlayerIsPotionActive(potionIn);

        RiftMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled()) {
            callbackInfoReturnable.setReturnValue(Boolean.valueOf(false));
        }

    }
}
