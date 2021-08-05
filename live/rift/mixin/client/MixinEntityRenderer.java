package live.rift.mixin.client;

import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.List;
import live.rift.event.events.RenderHandEvent;
import live.rift.module.modules.misc.NoEntityBlock;
import live.rift.module.modules.render.NoRender;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = { EntityRenderer.class},
    priority = Integer.MAX_VALUE
)
public class MixinEntityRenderer {

    @Redirect(
        method = { "getMouseOver"},
        at =             @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"
            )
    )
    public List getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate) {
        return (List) (NoEntityBlock.doBlock() ? new ArrayList() : worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate));
    }

    @Inject(
        method = { "renderHand"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void renderLivingLabel(float partialTicks, int pass, CallbackInfo info) {
        RenderHandEvent e = new RenderHandEvent();

        if (e.isCancelled()) {
            info.cancel();
        }

    }

    @Inject(
        method = { "hurtCameraEffect"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        if (NoRender.i.isEnabled() && NoRender.i.hurtCam.getValBoolean()) {
            info.cancel();
        }

    }

    @Redirect(
        method = { "setupCameraTransform"},
        at =             @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/entity/EntityPlayerSP;prevTimeInPortal:F"
            )
    )
    public float prevTimeInPortal(EntityPlayerSP entityPlayerSP) {
        return NoRender.i.isEnabled() && NoRender.i.nausea.getValBoolean() ? -3.4028235E38F : entityPlayerSP.prevTimeInPortal;
    }

    @Inject(
        method = { "updateLightmap"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    private void updateLightmap(float partialTicks, CallbackInfo info) {
        if (NoRender.i.isEnabled() && NoRender.i.skylight.getValBoolean()) {
            info.cancel();
        }

    }

    @Inject(
        method = { "renderItemActivation"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void renderItemActivationHook(CallbackInfo info) {
        if (NoRender.i.isEnabled() && NoRender.i.totem.getValBoolean()) {
            info.cancel();
        }

    }
}
