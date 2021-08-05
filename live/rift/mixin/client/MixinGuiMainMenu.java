package live.rift.mixin.client;

import java.awt.Color;
import java.awt.Font;
import live.rift.font.CFontRenderer;
import live.rift.util.RainbowUtil;
import live.rift.util.apiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ GuiMainMenu.class})
public class MixinGuiMainMenu extends GuiScreen {

    public CFontRenderer cfont = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
    String s = "Rift b1.0.3";
    int l_I = 300;
    RainbowUtil rainbowUtil = new RainbowUtil(9);

    @Inject(
        method = { "drawScreen"},
        at = {             @At("TAIL")},
        cancellable = true
    )
    public void drawText(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        this.cfont.drawStringWithShadow(this.s, 8.0D, 2.0D, this.rainbowUtil.GetRainbowColorAt(this.l_I));
        this.cfont.drawStringWithShadow("Priority Queue is: " + apiUtil.getPrioQueueLength(), 8.0D, 14.0D, this.rainbowUtil.GetRainbowColorAt(this.l_I));
        this.cfont.drawStringWithShadow("Priority Queue time is: " + apiUtil.getPrioTime(), 8.0D, 24.0D, this.rainbowUtil.GetRainbowColorAt(this.l_I));
        this.cfont.drawStringWithShadow("Hi, " + Minecraft.getMinecraft().session.getUsername(), 8.0D, 34.0D, this.rainbowUtil.GetRainbowColorAt(this.l_I));
        this.drawVerticalLine(4, 1, 43, this.rainbowUtil.GetRainbowColorAt(this.l_I));
        this.drawVerticalLine(5, 2, 43, Color.black.hashCode());
        this.l_I += 10;
        if (this.l_I >= 355) {
            this.l_I = 0;
        }

    }
}
