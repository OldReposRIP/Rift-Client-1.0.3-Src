package live.rift.module.modules.render;

import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SkyColor extends Module {

    public Setting cancel = new Setting("Cancel fog", this, true);
    public Setting dense = new Setting("Density of fog", this, 1.0D, 0.0D, 1.0D, false);
    public Setting OWR = new Setting("Overworld Red", this, 1.0D, 0.0D, 1.0D, false);
    public Setting OWG = new Setting("Overworld Green", this, 1.0D, 0.0D, 1.0D, false);
    public Setting OWB = new Setting("Overworld Blue", this, 1.0D, 0.0D, 1.0D, false);
    public Setting NTR = new Setting("Nether Red", this, 1.0D, 0.0D, 1.0D, false);
    public Setting NTG = new Setting("Nether Green", this, 1.0D, 0.0D, 1.0D, false);
    public Setting NTB = new Setting("Nether Blue", this, 1.0D, 0.0D, 1.0D, false);

    public SkyColor() {
        super("SkyColor", 0, Category.RENDER);
    }

    @SubscribeEvent
    public void onFogDensity(FogDensity event) {
        event.setDensity(this.dense.getValFloat());
        event.setCanceled(this.cancel.getValBoolean());
    }

    @SubscribeEvent
    public void onFogColor(FogColors event) {
        if (Minecraft.getMinecraft().player.dimension == 0) {
            event.setRed(this.OWR.getValFloat() / 1.15F);
            event.setGreen(this.OWG.getValFloat() / 1.15F);
            event.setBlue(this.OWB.getValFloat() / 1.15F);
        } else if (Minecraft.getMinecraft().player.dimension == -1) {
            event.setRed(this.NTR.getValFloat() / 1.15F);
            event.setGreen(this.NTG.getValFloat() / 1.15F);
            event.setBlue(this.NTB.getValFloat() / 1.15F);
        }

    }

    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
