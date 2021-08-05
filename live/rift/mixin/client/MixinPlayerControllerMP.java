package live.rift.mixin.client;

import live.rift.RiftMod;
import live.rift.event.events.EventPlayerClickBlock;
import live.rift.event.events.EventPlayerDamageBlock;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = { PlayerControllerMP.class},
    priority = Integer.MAX_VALUE
)
public class MixinPlayerControllerMP {

    @Inject(
        method = { "onPlayerDamageBlock"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable callback) {
        EventPlayerDamageBlock l_Event = new EventPlayerDamageBlock(posBlock, directionFacing);

        RiftMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled()) {
            callback.setReturnValue(Boolean.valueOf(false));
            callback.cancel();
        }

    }

    @Inject(
        method = { "clickBlock"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void clickBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable callback) {
        EventPlayerClickBlock l_Event = new EventPlayerClickBlock(loc, face);

        RiftMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled()) {
            callback.setReturnValue(Boolean.valueOf(false));
            callback.cancel();
        }

    }
}
