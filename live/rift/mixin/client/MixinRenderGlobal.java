package live.rift.mixin.client;

import live.rift.RiftMod;
import live.rift.module.modules.render.SkyColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ RenderGlobal.class})
public class MixinRenderGlobal {

    @Redirect(
        method = { "renderSky(FI)V"},
        at =             @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/multiplayer/WorldClient;getSkyColor(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;"
            )
    )
    public Vec3d getSkyColorRedirect(WorldClient worldClient, Entity entityIn, float partialTicks) {
        SkyColor mod = (SkyColor) RiftMod.fevents.moduleManager.getModule("SkyColor");

        if (mod.isEnabled()) {
            if (Minecraft.getMinecraft().player.dimension == 0) {
                return new Vec3d((double) mod.OWR.getValFloat(), (double) mod.OWG.getValFloat(), (double) mod.OWB.getValFloat());
            }

            if (Minecraft.getMinecraft().player.dimension == -1) {
                return new Vec3d((double) mod.NTR.getValFloat(), (double) mod.NTG.getValFloat(), (double) mod.NTB.getValFloat());
            }
        }

        return Minecraft.getMinecraft().world.getSkyColor(entityIn, partialTicks);
    }
}
