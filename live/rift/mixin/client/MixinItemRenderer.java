package live.rift.mixin.client;

import live.rift.module.modules.render.NoRender;
import live.rift.module.modules.render.ViewModelChanger;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ ItemRenderer.class})
public abstract class MixinItemRenderer {

    @Inject(
        method = { "renderFireInFirstPerson"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void renderFireInFirstPersonHook(CallbackInfo info) {
        if (NoRender.i.isEnabled() && NoRender.i.fire.getValBoolean()) {
            info.cancel();
        }

    }

    @Inject(
        method = { "renderItemInFirstPerson"},
        at = {             @At("HEAD")}
    )
    public void renderItemInFirstPersonHead(float partialTicks, CallbackInfo i) {
        if (ViewModelChanger.i.isEnabled()) {
            GlStateManager.translate(0.0D, (ViewModelChanger.i.ly.getValDouble() - 360.0D) / 180.0D, (ViewModelChanger.i.lz.getValDouble() - 360.0D) / 180.0D);
        }

    }

    @Inject(
        method = { "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void renderItemInFirstPersonHook(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo info) {
        if (ViewModelChanger.i.isEnabled()) {
            if (hand == EnumHand.MAIN_HAND) {
                GlStateManager.translate((ViewModelChanger.i.lx.getValDouble() - 360.0D) / 180.0D, 0.0D, 0.0D);
            } else {
                GlStateManager.translate(-((ViewModelChanger.i.lx.getValDouble() - 360.0D) / 90.0D), 0.0D, 0.0D);
            }
        }

    }
}
