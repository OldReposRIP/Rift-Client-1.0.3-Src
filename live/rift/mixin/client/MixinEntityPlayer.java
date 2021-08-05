package live.rift.mixin.client;

import live.rift.RiftMod;
import live.rift.event.events.EventPlayerTravel;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = { EntityPlayer.class},
    priority = Integer.MAX_VALUE
)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {

    @Inject(
        method = { "travel"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void travel(float strafe, float vertical, float forward, CallbackInfo info) {
        EventPlayerTravel l_Event = new EventPlayerTravel();

        RiftMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled()) {
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            info.cancel();
        }

    }
}
