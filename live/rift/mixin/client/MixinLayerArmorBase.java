package live.rift.mixin.client;

import live.rift.RiftMod;
import live.rift.module.modules.render.NoRender;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ LayerArmorBase.class})
public class MixinLayerArmorBase {

    @Inject(
        method = { "renderArmorLayer"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void renderArmorLayer(EntityLivingBase p_Entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo p_Info) {
        if (RiftMod.fevents.moduleManager != null && NoRender.i.isEnabled() && NoRender.i.armor.getValBoolean()) {
            p_Info.cancel();
        }

    }
}
