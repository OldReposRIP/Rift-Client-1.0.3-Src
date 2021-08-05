package live.rift.mixin.client;

import live.rift.RiftMod;
import live.rift.event.AlpineEvent;
import live.rift.event.events.EventMotionUpdate;
import live.rift.event.events.PlayerMoveEvent;
import live.rift.event.events.PushOutBlockEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = { EntityPlayerSP.class},
    priority = Integer.MAX_VALUE
)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer {

    private EventMotionUpdate eventUpdate;
    private final Minecraft mc = Minecraft.getMinecraft();

    @Inject(
        method = { "onUpdateWalkingPlayer"},
        at = {             @At("HEAD")}
    )
    private void onUpdateWalkingPlayerHead(CallbackInfo ci) {
        this.eventUpdate = new EventMotionUpdate(AlpineEvent.Era.PRE, (EntityPlayerSP) this, this.mc.player.rotationYaw, this.mc.player.rotationPitch, this.mc.player.posX, this.mc.player.posY, this.mc.player.posZ, this.mc.player.onGround);
        RiftMod.EVENT_BUS.post(this.eventUpdate);
    }

    @Redirect(
        method = { "onUpdateWalkingPlayer"},
        at =             @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/entity/EntityPlayerSP;posX:D"
            )
    )
    private double onUpdateWalkingPlayerPosX(EntityPlayerSP player) {
        return this.eventUpdate.getX();
    }

    @Redirect(
        method = { "onUpdateWalkingPlayer"},
        at =             @At(
                value = "FIELD",
                target = "Lnet/minecraft/util/math/AxisAlignedBB;minY:D"
            )
    )
    private double onUpdateWalkingPlayerMinY(AxisAlignedBB boundingBox) {
        return this.eventUpdate.getY();
    }

    @Redirect(
        method = { "onUpdateWalkingPlayer"},
        at =             @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/entity/EntityPlayerSP;posZ:D"
            )
    )
    private double onUpdateWalkingPlayerPosZ(EntityPlayerSP player) {
        return this.eventUpdate.getZ();
    }

    @Redirect(
        method = { "onUpdateWalkingPlayer"},
        at =             @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/entity/EntityPlayerSP;onGround:Z"
            )
    )
    private boolean onUpdateWalkingPlayerOnGround(EntityPlayerSP player) {
        return this.eventUpdate.isOnGround();
    }

    @Redirect(
        method = { "onUpdateWalkingPlayer"},
        at =             @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotationYaw:F"
            )
    )
    private float onUpdateWalkingPlayerRotationYaw(EntityPlayerSP player) {
        return this.eventUpdate.getYaw();
    }

    @Redirect(
        method = { "onUpdateWalkingPlayer"},
        at =             @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotationPitch:F"
            )
    )
    private float onUpdateWalkingPlayerRotationPitch(EntityPlayerSP player) {
        return this.eventUpdate.getPitch();
    }

    @Inject(
        method = { "move"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void move(MoverType type, double x, double y, double z, CallbackInfo info) {
        PlayerMoveEvent event = new PlayerMoveEvent(x, y, z, Minecraft.getMinecraft().player.onGround);

        RiftMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            super.move(type, event.x, event.y, event.z);
            info.cancel();
        }

    }

    @Inject(
        method = { "pushOutOfBlocks"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    private void onPushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable cir) {
        PushOutBlockEvent eventPushOutOfBlocks = new PushOutBlockEvent();

        RiftMod.EVENT_BUS.post(eventPushOutOfBlocks);
        if (eventPushOutOfBlocks.isCancelled()) {
            cir.setReturnValue(Boolean.valueOf(false));
        }

    }
}
